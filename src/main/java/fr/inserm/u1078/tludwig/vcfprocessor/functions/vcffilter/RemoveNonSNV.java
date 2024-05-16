package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Remove variants lines where there have no SNVs
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-06-27
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class RemoveNonSNV extends ParallelVCFVariantFilterFunction {
  
  @Override
  public String getSummary() {
    return "Remove variants lines where there have no SNVs";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary());
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
  
  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    return variant.hasSNP() ? asOutput(variant) : NO_OUTPUT;    
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
