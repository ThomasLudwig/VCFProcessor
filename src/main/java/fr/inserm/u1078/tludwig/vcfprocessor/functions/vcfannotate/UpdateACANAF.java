package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcfannotate;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Resets the AC; AN; and AF value for the given VCF file
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-03-13
 * Checked for release on 2020-05-25
 * Unit Test defined on   2020-05-08
 */
public class UpdateACANAF extends ParallelVCFVariantFunction { 
  @Override
  public String getSummary() {
    return "Resets the "+Description.code("AC")+" "+Description.code("AN")+" and "+Description.code("AF")+" values for the given VCF file";
  }

  @Override
  public Description getDesc() {
    return new Description("Adds/Updates the "+Description.code("AC")+" "+Description.code("AN")+" and "+Description.code("AF")+" values for the VCF.");
  }

  @Override
  public boolean needVEP() {
    return false;
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ANNOTATION_FOR_ALL;
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
  public String[] processInputVariant(Variant variant) {
    variant.recomputeACAN();
    return asOutput(variant);
  }
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
