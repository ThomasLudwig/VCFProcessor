package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Keeps only the variants not found in either dbSNP, 1KG or GnomAD
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-10-14
 * Checked for release on 2020-08-05
 * Unit Test defined on   2020-08-05
 */
public class FilterNew extends ParallelVCFVariantFilterFunction {

  @Override
  public String getSummary() {
    return "Keeps only the variants not found in either dbSNP, 1KG or GnomAD";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary());
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
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    for (int a = 1; a < variant.getAlleles().length; a++)
      if (variant.getInfo().isInDBSNPVEP(a) || variant.getInfo().isIn1KgVEP(a) || variant.getInfo().isInGnomADVEP(a))
        return NO_OUTPUT;

    return asOutput(variant);
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
