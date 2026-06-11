package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ListEnumParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.RatioParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPFrequencyFacade;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

import java.util.ArrayList;

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
  //private final ListParameter pops = new ListParameter(OPT_POP, "pop1,pop2,...,popN","List example of Populations to test (from "+String.join(", ", VEPFrequencyFacade.getAllKeys())+")");
  private final ListEnumParameter pops = new ListEnumParameter(OPT_POP, VEPFrequencyFacade.ALL_FREQUENCIES.keySet().toArray(new String[0]), "pop1,pop2,...,popN","List of Populations to test (from "+String.join(", ", VEPFrequencyFacade.ALL_FREQUENCIES.keySet())+")");

  @Override
  public String getSummary() { return "Keeps only variants with frequencies below the threshold in all of the selected populations."; }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("If the variant's frequency exceeds the threshold for any of the selected populations, the variant is filtered out.");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.KEEP_IF_ONE_SATISFY); }

  @Override
  public void begin() {
    super.begin();
    //TODO check pops
  }

  private boolean kept(Variant v, int a){
    for(String pop : pops.getList())
      if(v.getInfo().getVEPInfo().getFrequency(pop, a) > this.threshold.getFloatValue())
        return false;
    return true;
  }

  @Override
  public Println[] processInputVariantForFilter(Variant variant) {
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
