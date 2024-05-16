package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * "Replaces the Info column by "."
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2017-05-10
 * Checked for release on 2020-05-26
 * Unit Test defined on   2020-08-04
 */
public class ClearInfoField extends ParallelVCFFunction {

  @Override
  public String getSummary() {
    return "Replaces the Info column by \".\"";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Removes all annotation from the VCF file by replacing the content of the Info column by \".\"");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
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
  public String[] processInputLine(String line) {
    String[] f = line.split(T);
    f[7] = ".";
    return new String[]{String.join(T, f)};
  }
  
  @SuppressWarnings("unused")
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
