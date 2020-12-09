package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BooleanParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Keeps only variants that are compatible with a De Novo pattern of inheritance.
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-09-06
 * Checked for release on 2020-08-07
 * Unit Test defined on 2020-08-07
 */
public class DeNovo extends ParallelVCFVariantFilterFunction {

  public final PedFileParameter pedfile = new PedFileParameter();
  private final BooleanParameter missing = new BooleanParameter(OPT_MISSING, "Missing genotypes allowed ?");

  @Override
  public String getSummary() {
    return "Keeps only variants that are compatible with a De Novo pattern of inheritance.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addItemize(new String[]{
              "present in every Case",
              "absent in every Controle"
            })
            .addWarning("Father/Mother/Child(ren) Trios are expected");
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
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public void begin() {
    super.begin();
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    for (int a = 1; a < variant.getAlleleCount(); a++) {
      boolean keep = true;
      for (Genotype g : variant.getGenotypes()) {
        //Drop if missing and missing not allowed
        if (g.isMissing() && !this.missing.getBooleanValue())
          keep = false;
        else {
          //Drop if at least one case don't have the variant (or is missing)
          if (g.getSample().isCase() && !g.hasAllele(a))
            keep = false;
          //Drop if at least one control has the variant (or is missing)
          if ((!g.getSample().isCase()) && g.hasAllele(a))
            keep = false;
        }
        if (!keep)
          break;
      }
      if (keep)
        return asOutput(variant);
    }
    return NO_OUTPUT;
  }

  @Override
  public TestingScript[] getScripts() {
    ArrayList<TestingScript> scripts = new ArrayList<>();
    for (String setname : new String[]{"2trios", "trio"})
      for (String missingP : new String[]{"false", "true"}) {
        TestingScript scr = TestingScript.newFileTransform();
        scr.addNamingFilename("vcf", setname + ".vcf");
        scr.addAnonymousFilename("ped", setname + ".ped");
        scr.addNamingValue("missing", missingP);
        scripts.add(scr);
      }

    return scripts.toArray(new TestingScript[scripts.size()]);
  }
}
