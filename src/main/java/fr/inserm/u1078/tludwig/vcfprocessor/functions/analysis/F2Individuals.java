package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Computes F2 data by samples and not by groups (Each sample is its own group).
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-05-11
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-15
 */
public class F2Individuals extends ParallelVCFVariantPedFunction {

  private final StringParameter prefix = new StringParameter(OPT_PREFIX, "prefix", "prefix of the output files");
  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();
  private int[][] f2all;
  private int[][] f2old;
  private int[][] f2new;
  private int[][] f2snpAll;
  private int[][] f2snpOld;
  private int[][] f2snpNew;
  private int total;
  private ArrayList<Sample> samples;

  @Override
  public String getSummary() {
    return "Computes F2 data by samples and not by groups (Each sample is its own group).";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {    
    return new Description(this.getSummary())
            .addLine("F2 data are described in PubMedId: 23128226, figure 3a")
            .addLine("Six sets of results are given, one for:")
            .addEnumerate("All variants",
                "All SNVs",
                "variants without rs (new)",
                "SNVs without rs (new)",
                "variants with rs (known)",
                "SNVs with rs (known)")
            .addWarning("The difference between known and new is done by looking a the vep annotation, not the ID column.");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return true;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return null;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    samples = new ArrayList<>(getVCF().getSamples());
    total = samples.size();
    this.f2all = new int[total][total + 1];
    this.f2old = new int[total][total + 1];
    this.f2new = new int[total][total + 1];
    this.f2snpAll = new int[total][total + 1];
    this.f2snpOld = new int[total][total + 1];
    this.f2snpNew = new int[total][total + 1];
  }
  
  private void process(Variant variant, int a) {//use of a separate method, so we don't have to manage multiple layers of for-break
    int found = 0;
    int first = -1;
    int second = -1;
    for (Genotype geno : variant.getGenotypes()) {
      int c = geno.getCount(a);
      if (c == 2)
        return; //two allele in the same person -> not f2
      if (c == 1) {
        found++;
        if (found == 1)
          first = this.samples.indexOf(geno.getSample());
        else
          second = this.samples.indexOf(geno.getSample());
      }

      if (found > 2) //more than two allele -> not f2
        return;
    }

    if (found != 2) //0 or 1 allele, not f2
      return;

    //Ok so we have exactly 2 allele
    this.pushAnalysis(new Object[]{variant.getInfo().isInDBSNPVEP(a), variant.isSNP(a), first, second});
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    for (int a = 1; a < variant.getAlleles().length; a++)
        process(variant, a);
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    try {
      boolean old = (boolean) ((Object[]) analysis)[0];
      boolean snp = (boolean) ((Object[]) analysis)[1];
      int first = (int) ((Object[]) analysis)[2];
      int second = (int) ((Object[]) analysis)[3];

      increment(this.f2all, first, second);
      if (old)
        increment(this.f2old, first, second);
      else
        increment(this.f2new, first, second);

      if (snp) {
        increment(this.f2snpAll, first, second);
        if (old)
          increment(this.f2snpOld, first, second);
        else
          increment(this.f2snpNew, first, second);
      }

      
      return true;
    } catch (Exception ignore) { }
    return false;
  }
  
  private void increment(int[][] f2, int f, int s){
    //now, either they are in the same group, or they aren't, if they are, we must only add them once
    f2[f][s]++;
    f2[f][total]++;
    if (f != s) {
      f2[s][f]++;
      f2[s][total]++;
    }
  }  

  @SuppressWarnings("unused")
  @Override
  public void end() {
    String filename = this.dir.getDirectory() + this.prefix.getStringValue();
    printResults(filename + ".all.tsv", this.f2all);
    printResults(filename + ".known.tsv", this.f2old);
    printResults(filename + ".new.tsv", this.f2new);
    printResults(filename + ".snp.all.tsv", this.f2snpAll);
    printResults(filename + ".snp.known.tsv", this.f2snpOld);
    printResults(filename + ".snp.new.tsv", this.f2snpNew);
  }

  private void printResults(String filename, int[][] f2) {
    try {
      PrintWriter out = getPrintWriter(filename);
      LineBuilder line = new LineBuilder("X");
      for (Sample sample : samples)
        line.addColumn(sample.getId());
      line.addColumn("TOTAL");
      out.println(line);

      for (int f = 0; f < total; f++) {
        line = new LineBuilder(this.samples.get(f).getId());
        for (int s = 0; s <= total; s++)
          line.addColumn(f2[f][s]);
        out.println(line);
      }

      out.close();
    } catch (IOException e) {
      Message.error("Unable to write to output file " + filename);
    }
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newDirectoryAnalysis();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addAnonymousFilename("ped", "ped");
    scr.addAnonymousValue("prefix", this.getClass().getSimpleName());
    return new TestingScript[]{scr};
  }
}
