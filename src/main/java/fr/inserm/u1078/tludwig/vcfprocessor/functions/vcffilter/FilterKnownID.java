package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
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

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("The field must be empty or equals to \".\"");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public String[] processInputRecordForFilter(VariantRecord record) {
    String id = record.getID();
    return id == null || id.isEmpty() || id.equals(".") ? new String[]{record.toString()} : NO_OUTPUT;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
