package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Print the worst consequence/gene for each variant allele.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-04-26
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-07
 */
public class GetWorstConsequence extends ParallelVCFVariantFunction {

  private static final String[] HEADER = {"#CHR","POS","ID","REF","ALT","WORST_CSQ","AFFECTED_GENE"};

  @Override
  public String getSummary() {
    return "Print the worst consequence/gene for each variant allele.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("For each allele of each variant, the output is in the format:")
            .addColumns(HEADER);
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return true;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_IGNORE_STAR_ALLELE_AS_LINE;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T,HEADER)};
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    int[] nonStar = variant.getNonStarAltAllelesAsArray();
    String[] outs = new String[nonStar.length];
    for (int i = 0 ; i < nonStar.length; i++) {
      int a = nonStar[i];
      VEPAnnotation csqGene = variant.getInfo().getWorstVEPAnnotation(a);
      outs[i] = variant.getChrom() + T + variant.getPos() + T + variant.getId() + T + variant.getRef() + T + variant.getAllele(a) + T + csqGene.getConsequence() + T + csqGene.getSYMBOL();
    }
    return outs;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
