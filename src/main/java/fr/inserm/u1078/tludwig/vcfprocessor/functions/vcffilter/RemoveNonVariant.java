package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Remove variants where only 0/0 and ./. genotypes are present
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-10-16
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class RemoveNonVariant extends ParallelVCFVariantFilterFunction {

  @Override
  public String getSummary() {
    return "Remove variants where only 0/0 and ./. genotypes are present";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary());
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    return variant.hasNoVariants() ?  NO_OUTPUT : asOutput(variant);
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
