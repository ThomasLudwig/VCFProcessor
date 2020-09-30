package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Keeps Variants found in every "Case" samples.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-08-30
 * Checked for release on 2020-08-03
 * Unit Test defined on   2020-08-03
 */
public class FoundInAllCases extends ParallelVCFVariantFilterFunction {
  public final PedFileParameter pedfile = new PedFileParameter();
  private ArrayList<Sample> cases;

  @Override
  public String getSummary() {
    return "Keeps Variants found in every \"Case\" samples";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Case samples are defined by a \"1\" in the 6th field of the " + Description.code(this.pedfile.getKey()) + " file.")
            .addLine("In case of a multiallelic variant, if any variant allele is found or missing, the whole variant is kept.");
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
    this.cases = this.getVCF().getPed().getCases();
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    for (int a = 1; a < variant.getAlleles().length; a++) {
      boolean good = true;

      for (Sample cas : cases) {
        Genotype g = variant.getGenotype(cas);
        if (!g.isMissing() && g.getCount(a) == 0) {
          good = false;
          break;
        }
      }

      if (good) 
        return asOutput(variant);
    }

    return NO_OUTPUT;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedTransformScript();
  }
}
