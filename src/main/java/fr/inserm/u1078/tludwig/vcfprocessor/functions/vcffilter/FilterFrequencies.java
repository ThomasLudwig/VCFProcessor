package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ListParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.RatioParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPFormat;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Keeps only variants with frequencies below the threshold in all the selected populations.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-10-12
 * Checked for release on 2020-05-27
 * Unit Test defined on   2020-08-06
 */
public class FilterFrequencies extends ParallelVCFVariantFilterFunction { 

  private final RatioParameter threshold = new RatioParameter(OPT_THRESHOLD, "maximum frequency in any population");
  private final ListParameter pops = new ListParameter(OPT_POP, "pop1,pop2,...,popN","List example of Populations to test (from "+String.join(", ", VEPFormat.FREQUENCY_KEYS)+")");
  //private final ListEnumParameter pops = new ListEnumParameter(OPT_POP, VEPFormat.FREQUENCY_KEYS, "pop1,pop2,...,popN","List of Populations to test (from "+String.join(", ", VEPFormat.FREQUENCY_KEYS)+")");

  @Override
  public String getSummary() {
    return "Keeps only variants with frequencies below the threshold in all of the selected populations.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("If the variant's frequency exceeds the threshold for any of the selected populations, the variant is filtered out.");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return true;
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

  private boolean kept(Variant v, int a){
    for(String pop : pops.getList())
      if(v.getInfo().getFrequency(pop, a) > this.threshold.getFloatValue())
        return false;
    return true;
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    boolean filter = true;
    for(int a = 1 ; a < variant.getAlleleCount(); a++){
      if(kept(variant, a)){
        filter = false;
        break;
      }
    }
    return filter ? NO_OUTPUT : asOutput(variant);
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addNamingValue("threshold", "0.01");
    scr.addNamingValue("pop", "EUR_AF,gnomAD_NFE_AF");
    return new TestingScript[]{scr};
  }
}
