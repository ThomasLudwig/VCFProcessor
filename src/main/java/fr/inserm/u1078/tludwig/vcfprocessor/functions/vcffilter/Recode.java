package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Reads all lines in a VCF Files
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2020-01-28
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class Recode extends ParallelVCFFunction {

  private int read=0;

  @Override
  public String getSummary() {
    return "Reads all lines in a VCF Files";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary()).
            addLine("Ouputs the input VCF file after applying the various command line filters");
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
  public String[] getHeaders() {
    return super.getHeaders();
  }

  @Override
  public void end() {
    Message.info("Lines processes : " + read);
  }

  @Override
  public void begin() {
    super.begin();
  }
  
  @Override
  public String[] processInputLine(String line) {
    pushAnalysis(Boolean.TRUE);
    return new String[]{line};
  }
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis){
    if(Boolean.TRUE.equals(analysis)){
      read++;
      return true;
    }
    return false;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}