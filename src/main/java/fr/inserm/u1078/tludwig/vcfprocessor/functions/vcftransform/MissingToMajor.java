package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.maok.tools.MathTools;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Replaces every missing genotype by the most frequent allele present
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-11-12
 * Checked for release on 2020-05-26
 * Unit Test defined on   2020-08-04
 */
public class MissingToMajor extends ParallelVCFVariantFunction {

  @Override
  public String getSummary() {
    return "Replaces every missing genotype by the most frequent allele present";
  }

  @Override
  public Description getDesc() {
    return new Description("Replaces every missing genotype by the most frequent allele present")
            .addLine("updatse AC,AF,AN annotations")
            .addLine("The genotype is homozygous to the most frequent allele A in the form  "+Description.code("A/A:0:0:0,0,0..."));
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return "The major allele is the most frequent allele from ref and each alternate.";
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
  public String[] processInputVariant(Variant variant) {
    int majorAllele = variant.getMajorAllele();
    String dflt = majorAllele+"";
    for(int i = 1; i < variant.getMostFrequentPloidy(); i++)
      dflt += "/"+majorAllele;
    String format = variant.getFormat().toString();
    String[] keys = format.split(":");
    for (int i = 1; i < keys.length; i++)
      switch (keys[i]) {
        case "AD":
          dflt += ":0,0";
          break;
        case "PL":
          dflt += ":0";
          for(int a = 0 ; a < MathTools.triangularNumber(variant.getAlleleCount()+1) - 1; a++)
            dflt += ",0";
          break;
        default:
          dflt += ":0";
          break;
      }

    for(Genotype g : variant.getGenotypes())
      if(g.isMissing())
        g.setTo(dflt);

    variant.recomputeACAN();
    return asOutput(variant);
  }
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
