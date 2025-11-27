package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Reads all lines in a VCF Files
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2020-01-28
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class Recode extends ParallelVCFFunction<Object> {

  private int read = 0;

  @Override
  public String getSummary() {
    return "Reads all lines in a VCF Files";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary()).
            addLine("Outputs the input VCF file after applying the various command line filters");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @SuppressWarnings("unused")
  @Override
  public void end() {
    Message.info("Lines processed : " + read);
  }

  @Override
  public String[] processInputRecord(VariantRecord record) {
    pushAnalysis(null);
    return new String[]{record.toString()};
  }
  
  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Object o){
    read++;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}