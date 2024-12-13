package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Computes F2 data.
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-05-11
 * Checked for release on 2020-05-06
 * Unit Test defined on 2020-05-15
 */
public class F2 extends ParallelVCFVariantPedFunction<F2.F2Analysis> {

  private final StringParameter prefix = new StringParameter(OPT_PREFIX, "prefix", "prefix of the output files");
  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();

  private ArrayList<String> groups;
  private int[][] f2all;
  private int[][] f2old;
  private int[][] f2new;
  private int[][] f2snpAll;
  private int[][] f2snpOld;
  private int[][] f2snpNew;
  private int total;

  @Override
  public String getSummary() {
    return "Computes F2 data.";
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
  public String getCustomRequirement() {
    return null;
  }

  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_IGNORE_STAR_ALLELE_AS_LINE;
  }
  
  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    this.groups = getPed().getGroups();
    total = this.groups.size();
    this.f2all = new int[total][total + 1];
    this.f2old = new int[total][total + 1];
    this.f2new = new int[total][total + 1];
    this.f2snpAll = new int[total][total + 1];
    this.f2snpOld = new int[total][total + 1];
    this.f2snpNew = new int[total][total + 1];
  }

  private void process(Variant variant, int a) {
    int found = 0;
    int first = -1;
    int second = -1;
    for (Genotype geno : variant.getGenotypes()) {
      int c = geno.getCount(a);
      if (c > 1)
        return; //two allele in the same person -> not f2
      if (c == 1) {
        found++;
        if (found == 1)
          first = this.groups.indexOf(geno.getSample().getGroup());
        else
          second = this.groups.indexOf(geno.getSample().getGroup());
      }

      if (found > 2) //more than two allele -> not f2
        return;
    }

    if (found != 2) //0 or 1 allele, not f2
      return;

    //Ok so we have exactly 2 allele
    this.pushAnalysis(new F2Analysis(variant, a, first, second));
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(F2Analysis an) {
    increment(this.f2all, an);
    if (an.isOld)
      increment(this.f2old, an);
    else
      increment(this.f2new, an);

    if (an.isSnp) {
      increment(this.f2snpAll, an);
      if (an.isOld)
        increment(this.f2snpOld, an);
      else
        increment(this.f2snpNew, an);
    }
  }
  
  private void increment(int[][] f2, F2Analysis analysis){
    //now, either they are in the same group, or they aren't, if they are, we must only add them once
    f2[analysis.firstGroup][analysis.secondGroup]++;
    f2[analysis.firstGroup][total]++;
    if (analysis.firstGroup != analysis.secondGroup) {
      f2[analysis.secondGroup][analysis.firstGroup]++;
      f2[analysis.secondGroup][total]++;
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
      StringBuilder line = new StringBuilder("X" + T + String.join(T, groups) + T + "TOTAL");
      out.println(line);

      for (int f = 0; f < total; f++) {
        line = new StringBuilder(this.groups.get(f));
        for (int s = 0; s <= total; s++)
          line.append(T).append(f2[f][s]);
        out.println(line);
      }
      out.close();
    } catch (IOException e) {
      Message.error("Unable to write to result file " + filename);
    }
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    for (int a : variant.getNonStarAltAllelesAsArray())
      process(variant, a);
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return null;
  }

  public static class F2Analysis {
    private final boolean isOld;
    private final boolean isSnp;
    private final int firstGroup;
    private final int secondGroup;
    //private final String comment;

     F2Analysis(Variant variant, int a, int firstGroup, int secondGroup) {
      this.isOld = variant.getInfo().isInDBSNPVEP(a);
      this.isSnp = variant.isSNP(a);
      this.firstGroup = firstGroup;
      this.secondGroup = secondGroup;
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
