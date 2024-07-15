package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BooleanParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.EnumParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.ArrayList;

/**
 * Keeps only variants that respect the Dominant pattern of inheritance.
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-09-09
 * Checked for release on 2020-08-07
 * Unit Test defined on 2020-08-10
 */
public class Dominant extends ParallelVCFVariantFilterPedFunction {
  private final BooleanParameter missing = new BooleanParameter(OPT_MISSING, "Missing genotypes allowed ?");//TODO Missing should always be allowed, but rejected from the commandline filters
  private final BooleanParameter noHomo = new BooleanParameter(OPT_NO_HOMO, "Reject if a case is homozygous to alternate allele ?");
  private final EnumParameter strict = new EnumParameter(OPT_MODE, "strict,loose", "Mode", "strict : true for all cases | loose : true for at least one case");

  private boolean isStrict = false;

  @Override
  public String getSummary() {
    return "Keeps only variants that respect the Dominant pattern of inheritance";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("In the Dominant pattern of inheritance")
            .addItemize("Cases should have the causal variant",
                "Controls cannot have the causal variant")
            .addLine("Thus, a variant is rejected if")
            .addItemize("one case doesn't possess the alternate allele (strict mode)",
                "one control possesses the alternate allele")
            .addLine("If " + Description.code(missing.getKey() + " true")+", missing genotypes are considered compatible with the transmission pattern.")
            .addLine("The " + Description.code(noHomo.getKey()) + " options allows to reject alternate alleles if at least one case is homozygous. (If you expect the resulting phenotype would not be consistent for example.)")
            .addLine("In the strict mode, all cases must have the alternate allele. In the loose mode, only one case has to have the allele (More permissive for larger panels).")
            ;
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FILTER_ONE;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    this.isStrict = strict.getStringValue().equalsIgnoreCase("strict");
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {

    if (this.isStrict) {
      for (int a = 1; a < variant.getAlleles().length; a++) {
        boolean keep = true;

        for (Genotype g : variant.getGenotypes()) {
          if (g.isMissing()) {
            if (!this.missing.getBooleanValue())
              keep = false;
          } else {
            if (g.getSample().isCase()) {
              if (!g.hasAllele(a)) //reject case without allele
                keep = false;
              if (noHomo.getBooleanValue() && g.getCount(a) == 2) //reject case homozygous
                keep = false;
            } else
              if (g.hasAllele(a)) //reject control with allele
                keep = false;
          }
          if (!keep)
            break;
        }
        if (keep)
          return asOutput(variant);
      }
      return NO_OUTPUT;
    } else {
      for (int a = 1; a < variant.getAlleles().length; a++) {
        boolean keep = true;
        int validCases = 0;
        for (Genotype g : variant.getGenotypes()) {
          if (g.isMissing()) {
            if (!this.missing.getBooleanValue())
              keep = false;
          } else {
            if (g.getSample().isCase()) {
              if (g.hasAllele(a)) //count case with allele
                validCases++;
              if (noHomo.getBooleanValue() && g.getCount(a) == 2) //reject case homozygous
                keep = false;
            } else
              if (g.hasAllele(a)) //reject control with allele
                keep = false;
          }
          if (!keep)
            break;
        }
        if (keep && validCases > 0)
          return asOutput(variant);
      }
      return NO_OUTPUT;
    }
  }

  @SuppressWarnings("SpellCheckingInspection")
  @Override
  public TestingScript[] getScripts() {
    ArrayList<TestingScript> scripts = new ArrayList<>();
    for(String setName : new String[]{"2trios", "trio"})
      for(String missingP : new String[]{"false", "true"})
        for(String noHomoP : new String[]{"false", "true"})
          for(String mode : new String[]{"loose", "strict"}){
            TestingScript scr = TestingScript.newFileTransform();
            scr.addNamingFilename("vcf", setName+".vcf");
            scr.addAnonymousFilename("ped", setName+".ped");
            scr.addNamingValue("missing", missingP);
            scr.addNamingValue("nohomo", noHomoP);
            scr.addNamingValue("mode", mode);
            scripts.add(scr);
          }
    
    return scripts.toArray(new TestingScript[0]);
  }
}
