package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * For every variant, exports the variant allele count for each sample
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-10-05
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-14
 */
public class ExtractAlleleCounts extends ParallelVCFVariantFunction {
  final String[] HEADERS = {"#CHROM","POS","ID","REF","ALT"};
  
  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public String getSummary() {
    return "For every variants, exports the variant allele count for each sample";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output has the following format")
            .addColumns(HEADERS)
            .addLine("Followed by the allele count for each sample")
            .addLine("Allele Counts can be 0, 1 or 2 for diploids")
            .addLine("Missing genotypes have \".\" as an allele count");    
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

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    StringBuilder header = new StringBuilder(String.join(T, HEADERS));
    for (Sample sample : getVCF().getSamples())
      header.append(T).append(sample.getId());
    return new String[]{header.toString()};
  }

  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }
  
  @Override
  public String[] processInputVariant(Variant variant) {
    String chr = variant.getChrom();
    int pos = variant.getPos();
    String id = variant.getId();
    String ref = variant.getRef();
    String common = chr + T + pos + T + id + T + ref;
    int[] nonStar = variant.getNonStarAltAllelesAsArray();
    String[] outs = new String[nonStar.length];
    for (int i = 0 ; i < nonStar.length; i++) {
      int a = nonStar[i];
      LineBuilder out = new LineBuilder(common);
      out.addColumn(variant.getAllele(a));
      for (Sample sample : getVCF().getSamples()) {
        Genotype g = variant.getGenotype(sample);
        if (g.isMissing())
          out.addColumn(".");
        else
          out.addColumn(g.getCount(a));
      }
      outs[i] = out.toString();
    }
    return outs;
  }
  

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}