package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.RatioParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ListEnumParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPFormat;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Keeps only variants with frequencies below the threshold in all of the selected populations.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-10-12
 * Checked for release on 2020-05-27
 * Unit Test defined on   2020-08-06
 */
public class FilterFrequencies extends ParallelVCFVariantFilterFunction { 

  private final RatioParameter threshold = new RatioParameter(OPT_THRESHOLD, "maximum frequency in any population");
  private final ListEnumParameter pops = new ListEnumParameter(OPT_POP, VEPFormat.FREQUENCY_KEYS, "pop1,pop2,...,popN","List of Populations to test (from "+String.join(", ", VEPFormat.FREQUENCY_KEYS)+")");

  @Override
  public String getSummary() {
    return "Keeps only variants with frequencies below the threshold in all of the selected populations.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("If the variant's frequency exceeds the threshold for any of the selected populations, the variant is filtered out.");
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

  private boolean kept(Variant v, int a){
    ArrayList<VEPAnnotation> annots = v.getInfo().getVEPAnnotations(a);
    for(String pop : pops.getList())
      if(getFrequency(pop, annots) > this.threshold.getFloatValue())
        return false;
    return true;
  }
  
  public static double getFrequency(String pop, ArrayList<VEPAnnotation> annots){
    try {
      double d = new Double(annots.get(0).getValue(pop));
      return d;
    } catch (Exception e) {
    }
    return 0;
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
