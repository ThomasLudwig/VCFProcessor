package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * For every variants, exports the variant allele count for each sample
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-10-05
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-14
 */
public class ExtractAlleleCounts extends ParallelVCFVariantFunction {
  String[] HEADERS = {"#CHROM","POS","ID","REF","ALT"};
  
  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public String getSummary() {
    return "For every variants, exports the variant allele count for each sample";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output has the following format")
            .addColumns(HEADERS)
            .addLine("Followed by the allele count for each sample")
            .addLine("Allele Counts can be 0, 1 or 2 for diploides")
            .addLine("Missing genotypes have \".\" as an allele count");    
  }

  @Override
  public boolean needVEP() {
    return false;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String[] getHeaders() {
    String header = String.join(T, HEADERS);
    for (Sample sample : getVCF().getSamples())
      header += T + sample.getId();
    return new String[]{header};
  }

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
    String[] outs = new String[variant.getAlleleCount() -1];
    for (int a = 1; a < variant.getAlleleCount(); a++) {
      LineBuilder out = new LineBuilder(common);
      out.addColumn(variant.getAllele(a));
      for (Sample sample : getVCF().getSamples()) {
        Genotype g = variant.getGenotype(sample);
        if (g.isMissing())
          out.addColumn(".");
        else {
          out.addColumn(g.getCount(a));
        }
      }
      outs[a-1] = out.toString();
    }
    return outs;
  }
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}