package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ConsequenceParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Filters the variants according to their consequences
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2019-10-11
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class FilterConsequenceLevel  extends ParallelVCFVariantFilterFunction {

  private final ConsequenceParameter leastCsq = new ConsequenceParameter();

  @Override
  public String getSummary() {
    return "Filters the variants according to their consequences";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("The consequence of the variant must be at least as severe as the one given");
  }

  @Override
  public boolean needVEP() {
    return true;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FILTER_ONE;
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
  public String[] processInputVariantForFilter(Variant variant) {
    return VEPConsequence.getWorstConsequence(variant.getInfo().getWorstVEPAnnotation()).getLevel() >= this.leastCsq.getConsequenceLevel() ? asOutput(variant) : NO_OUTPUT;
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addNamingValue("csq", "missense_variant");
    return new TestingScript[]{scr};
  }
}
