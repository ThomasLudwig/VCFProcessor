package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Extract HQ variants.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2017-08-10
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class HQ extends ParallelVCFVariantFilterFunction {
  
  @Override
  public String getSummary() {
    return "Extract HQ variants.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Extract HQ variants. Defined in 1.12 of the supplementary information of PubMedID=27535533 as")
            .addEnumerate("VQSR PASS",
                "At least 80% of the genotypes have DP above 10 and GQ above 20",
                "at least one variant genotype has DP above 10 and GQ above 20"
                /*+ "<li>Not in the 10 1kb regions with the highest levels of multi-allelic variation</li>"*/ //will be managed later //TODO get definition of these regions
            );
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
  public String[] processInputVariantForFilter(Variant variant) {
    return variant.isHQ(true, 10, 20, .8, "PASS") ? asOutput(variant) : NO_OUTPUT;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
