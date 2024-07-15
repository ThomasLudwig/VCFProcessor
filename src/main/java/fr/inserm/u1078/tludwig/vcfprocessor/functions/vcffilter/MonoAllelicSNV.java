package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VariantRecord;
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
  public String[] processInputRecordForFilter(VariantRecord record) {
    String ref = record.getRef();
    String alt = record.getAltString();
    if (ref.length() != 1 || ref.charAt(0) == '.' || alt.length() != 1 || alt.charAt(0) == '.')
      return NO_OUTPUT;    
    return new String[]{record.toString()};
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
