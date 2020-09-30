package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.Function.OPT_MISSING;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.Function.OPT_MODE;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BooleanParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.EnumParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Keeps only variants that respect thes recessive pattern of inheritance.
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-09-09
 * Checked for release on 2020-08-07
 * Unit Test defined on 2020-08-10
 */
public class Recessive extends ParallelVCFVariantFilterFunction {

  public final PedFileParameter pedfile = new PedFileParameter();
  private final BooleanParameter missing = new BooleanParameter(OPT_MISSING, "Missing genotypes allowed ?"); //TODO Missing should always be allowed, but rejected from the commandline filters
  private final BooleanParameter nohomo = new BooleanParameter(OPT_NO_HOMO, "Reject if a control is homozygous to reference allele ?");
  private final EnumParameter strict = new EnumParameter(OPT_MODE, new String[]{"strict", "loose"}, "Mode", "strict : true for all cases | loose : true for at least one case");

  private boolean isStrict = false;

  @Override
  public String getSummary() {
    return "Keeps only variants that respect the Recessive pattern of inheritance.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("In the Recessive pattern of inheritance")
            .addItemize(new String[]{
      "Cases should be homozygous to the causal allele",
      "Controls should not be homozygous to the causal allele"
    })
            .addLine("Thus, a variant is rejected if")
            .addItemize(new String[]{
      "one case isn't homozygous to the alternate allele (strict mode)",
      "one control is homozygous to the the alternate allele",})
            .addLine("If " + Description.code(missing.getKey() + " true")+", missing genotypes are concidered compatible with the transmission pattern.")
            .addLine("The " + Description.code(nohomo.getKey()) + " options allows to reject alternate alleles if at least one control is not heterozygous to the alternate allele. (If all the controls are supposed to be parents of cases)")
            .addLine("In the strict mode, all cases must be homozygous to the alternate allele. In the loose mode, only one case has to be homozygous to the allele (More permissive for larger panels).")
            ;
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FILTER_ONE;
  }

  @Override
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public void begin() {
    super.begin();
    this.isStrict = strict.getStringValue().equalsIgnoreCase("strict");
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    if (isStrict) {
      for (int a = 1; a < variant.getAlleles().length; a++) {
        boolean keep = true;

        for (Genotype g : variant.getGenotypes()) {
          if (g.isMissing()) {
            if (!this.missing.getBooleanValue())
              keep = false;
          } else
            if (g.getSample().isCase()) {
              if (g.getCount(a) != 2) //reject case not homozygous
                keep = false;
            } else
              if (this.nohomo.getBooleanValue()) {
                if (g.getCount(a) != 1)//reject control without variant : no 1/1 and no 0/0
                  keep = false;
              } else
                if (g.getCount(a) == 2)//reject control homozygous : no 1/1
                  keep = false;
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
          } else
            if (g.getSample().isCase()) {
              if (g.getCount(a) == 2) //count case not homozygous
                validCases++;
            } else
              if (this.nohomo.getBooleanValue()) {
                if (g.getCount(a) != 1)//reject control without variant : no 1/1 and no 0/0
                  keep = false;
              } else
                if (g.getCount(a) == 2)//reject control homozygous : no 1/1
                  keep = false;
          if (!keep)
            break;
        }

        if (keep && validCases > 0)
          return asOutput(variant);
      }

      return NO_OUTPUT;
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    ArrayList<TestingScript> scripts = new ArrayList<>();
    for(String setname : new String[]{"2trios", "trio"})
      for(String missingP : new String[]{"false", "true"})
        for(String nohomoP : new String[]{"false", "true"})
          for(String mode : new String[]{"loose", "strict"}){
            TestingScript scr = TestingScript.newFileTransform();
            scr.addNamingFilename("vcf", setname+".vcf");
            scr.addAnonymousFilename("ped", setname+".ped");
            scr.addNamingValue("missing", missingP);
            scr.addNamingValue("nohomo", nohomoP);
            scr.addNamingValue("mode", mode);
            scripts.add(scr);
          }
    
    return scripts.toArray(new TestingScript[scripts.size()]);
  }
}
