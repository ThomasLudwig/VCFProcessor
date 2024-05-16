package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Returns a VCF containing only the position homozygous to alt for the given SAMPLES
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-10-14
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class KeepHomoAlt extends ParallelVCFVariantFilterFunction {

  private final StringParameter sampleID = new StringParameter(OPT_SAMPLE, "s1,s2,...sN", "list (comma separated) of samples to test");
  private String[] samples;

  @Override
  public String getSummary() {
    return "Returns a VCF containing only the position homozygous to alt for the given SAMPLES";
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return "Various is kept if different samples are homozygous to different alternative alleles";//TODO maybe change that
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
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    this.samples = this.sampleID.getStringValue().split(",");
  }
  
  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    for(String sample : samples)
      if (!variant.getGenotype(sample).isHomozygousOrHaploidToAlt())
        return NO_OUTPUT;
    return asOutput(variant);
  }

  @SuppressWarnings("SpellCheckingInspection")
  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addAnonymousValue("sample", "B00FWWD,B00FWWO");
    return new TestingScript[]{scr};
  }
}