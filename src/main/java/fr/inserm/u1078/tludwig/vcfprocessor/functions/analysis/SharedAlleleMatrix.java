package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * returns a series of matrices [individuals/individuals] with the number of shared alleles.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2017-01-06
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 */
public class SharedAlleleMatrix extends ParallelVCFVariantFunction {

  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();

  private int[][] snpNew;
  private int[][] snp05;
  private int[][] snp1;
  private int[][] snp5;
  private ArrayList<Sample> samples;

  @Override
  public String getSummary() {
    return "returns a series of matrices [individuals/individuals] with the number of shared alleles.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Matrices are newSNP, SNP.f&lt;0.005, SNP.f&lt;0.01, SNP.f&lt;0.05"); //TODO remove and let user apply his own filters inline
  }

  @Override
  public boolean needVEP() {
    return true;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @Override
  public void begin() {
    samples = getVCF().getSamples();
    int N = samples.size();

    this.snpNew = new int[N][N];
    this.snp05 = new int[N][N];
    this.snp1 = new int[N][N];
    this.snp5 = new int[N][N];
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    Genotype[] genos = variant.getGenotypes();

    double[] af = variant.getAF();
    
    for (int a = 1; a < variant.getAlleleCount(); a++) {
      double f = af[a];
      if (variant.isSNP(a) && (f <= 0.05 || !variant.getInfo().isInDBSNPVEP(a)))
        for (int l = 0; l < genos.length; l++)
          for (int c = l + 1; c < genos.length; c++)
            if (genos[l].hasAllele(a) && genos[c].hasAllele(a)) 
              this.pushAnalysis(new Analysis(l, c, !variant.getInfo().isInDBSNPVEP(a), f <= 0.05, f <= 0.01, f <= 0.005));            
    }
    return NO_OUTPUT;
  }
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    if(analysis instanceof Analysis){
      Analysis a = (Analysis)analysis;
      if (a.isNew)
        this.snpNew[a.s1][a.s2]++;
      if (a.is5)
        this.snp5[a.s1][a.s2]++;
      if (a.is1)
        this.snp1[a.s1][a.s2]++;
      if (a.is05)
        this.snp05[a.s1][a.s2]++;
      return true;
    }
    return false;
  }
  
  private class Analysis {
    private final int s1;
    private final int s2;
    private final boolean isNew;
    private final boolean is5;
    private final boolean is1;
    private final boolean is05;

    Analysis(int s1, int s2, boolean isNew, boolean is5, boolean is1, boolean is05) {
      this.s1 = s1;
      this.s2 = s2;
      this.isNew = isNew;
      this.is5 = is5;
      this.is1 = is1;
      this.is05 = is05;
    }        
  }
         

  @Override
  public String[] getHeaders() {
    return null;
  }

  @Override
  public void end() {
    try {
      String directory = this.dir.getDirectory();

      String header = "X";
      for (Sample s : samples)
        header += T + s.getId();

      String prefix = this.vcffile.getBasename();

      PrintWriter outNew = getPrintWriter(directory + prefix + ".snp.new.tsv");
      PrintWriter out05 = getPrintWriter(directory + prefix + ".snp.0.005.tsv");
      PrintWriter out1 = getPrintWriter(directory + prefix + ".snp.0.01.tsv");
      PrintWriter out5 = getPrintWriter(directory + prefix + ".snp.0.05.tsv");

      outNew.println(header);
      out05.println(header);
      out1.println(header);
      out5.println(header);

      for (int l = 0; l < samples.size(); l++) {
        String lNew = samples.get(l).getId();
        String l05 = lNew;
        String l1 = lNew;
        String l5 = lNew;

        for (int c = 0; c < samples.size(); c++) {
          lNew += T + snpNew[l][c];
          l05 += T + snp05[l][c];
          l1 += T + snp1[l][c];
          l5 += T + snp5[l][c];
        }
        outNew.println(lNew);
        out05.println(l05);
        out1.println(l1);
        out5.println(l5);
      }

      outNew.close();
      out05.close();
      out1.close();
      out5.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newDirectoryAnalysis();
    scr.addAnonymousFilename("vcf", "vcf");
    return new TestingScript[]{scr};
  }
}
