package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.RatioParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Filters out variants with frequencies above threshold in GnomAD
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-03-12
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class FilterGnomADFrequency extends ParallelVCFVariantFilterFunction {

  private final RatioParameter frq = new RatioParameter(OPT_THRESHOLD, "Maximum GnomAD Frequency");

  @Override
  public String getSummary() {
    return "Filters out variants with frequencies above threshold in GnomAD";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("In case of multiallelic variant, if any alternate allele passes the filter, the variant is kept");
  }

  @Override
  public boolean needVEP() {
    return true;
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
  public String[] processInputVariantForFilter(Variant variant) {
    for(int a = 1 ; a < variant.getAlleleCount(); a++)
      if(variant.getInfo().getFreqGnomadVEP(a) <= this.frq.getFloatValue())
        return asOutput(variant);
    return NO_OUTPUT;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addNamingValue("threshold", "0.005");
    return new TestingScript[]{scr};
  }
}
