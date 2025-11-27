package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcfannotate;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPFormat;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Puts all observed RS numbers in the ID column
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-03-13
 * Checked for release on 2020-05-25
 * Unit Test defined on   2020-08-05
 */
public class ReaffectdbSNP extends ParallelVCFVariantFunction<Object> {

  @Override
  public String getSummary() {
    return "Puts all observed RS numbers in the ID column";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Takes the rs numbers from the "+Description.italic(VEPFormat.KEY_EXISTING_VARIATION)+" annotation (from vep) and adds them to the ID column of the VCF. Puts \".\" if no RS has been found ");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.ANNOTATION_FOR_ALL); }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    String[] f = variant.getFields();
    String rs = variant.getInfo().getRSs();
    if(rs == null)
      rs = ".";
    
    f[VCF.IDX_ID] = rs;
    return new String[]{String.join(T, f)};
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
