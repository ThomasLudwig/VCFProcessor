package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Keeps only variant with and empty 3rd field
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-08-04
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class FilterKnownID extends ParallelVCFFilterFunction {

  @Override
  public String getSummary() {
    return "Keeps only variant with and empty 3rd field";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("The field must be empty or equals to \".\"");
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
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public String[] processInputLineForFilter(String line) {
    String[] f = line.split(T);
    return f[VCF.IDX_ID] == null || f[VCF.IDX_ID].isEmpty() || f[VCF.IDX_ID].equals(".") ? new String[]{line} : NO_OUTPUT;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
