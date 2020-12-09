package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Keep only the lines containing monoallelic SNVs
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-08-30
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class MonoAllelicSNV extends ParallelVCFFilterFunction {

  @Override
  public String getSummary() {
    return "Keep only the lines containing monoallelic SNVs";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary());
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
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
  public String[] processInputLineForFilter(String line) {
    String[] f = line.split(T);
    if (f[3].length() != 1 || f[3].charAt(0) == '.' || f[4].length() != 1 || f[4].charAt(0) == '.')
      return NO_OUTPUT;    
    return new String[]{line};
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
