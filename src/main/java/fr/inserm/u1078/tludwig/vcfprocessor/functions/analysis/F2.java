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
public class F2 extends ParallelVCFVariantPedFunction {

  private final StringParameter prefix = new StringParameter(OPT_PREFIX, "prefix", "prefix of the output files");
  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();

  private ArrayList<String> groups;
  private int[][] f2all;
  private int[][] f2old;
  private int[][] f2new;
  private int[][] f2snpall;
  private int[][] f2snpold;
  private int[][] f2snpnew;
  private int total;

  @Override
  public String getSummary() {
    return "Computes F2 data.";
  }

  @Override
  public Description getDesc() {

    return new Description(this.getSummary())
            .addLine("F2 data are described in PubMedId: 23128226, figure 3a")
            .addLine("Six sets of results are given, one for:")
            .addEnumerate(new String[]{
      "All variants",
      "All SNVs",
      "variants without rs (new)",
      "SNVs without rs (new)",
      "variants with rs (known)",
      "SNVs with rs (known)"
    })
            .addWarning("The difference between known and new is done by looking a the vep annotation, not the ID column.");
  }

  @Override
  public boolean needVEP() {
    return true;
  }

  @Override
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }
  
  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @Override
  public void begin() {
    this.groups = getPed().getGroups();
    total = this.groups.size();
    this.f2all = new int[total][total + 1];
    this.f2old = new int[total][total + 1];
    this.f2new = new int[total][total + 1];
    this.f2snpall = new int[total][total + 1];
    this.f2snpold = new int[total][total + 1];
    this.f2snpnew = new int[total][total + 1];
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
    this.pushAnalysis(new Analysis(variant, a, first, second));
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    try {
      Analysis an = (Analysis)analysis;

      increment(this.f2all, an);
      if (an.isOld)
        increment(this.f2old, an);
      else
        increment(this.f2new, an);

      if (an.isSnp) {
        increment(this.f2snpall, an);
        if (an.isOld)
          increment(this.f2snpold, an);
        else
          increment(this.f2snpnew, an);
      }

      
      return true;
    } catch (Exception e) {
    }
    return false;
  }
  
  private void increment(int[][] f2, Analysis analysis){
    //now, either they are in the same group, or they aren't, if they are, we must only add them once
    f2[analysis.firstGroup][analysis.secondGroup]++;
    f2[analysis.firstGroup][total]++;
    if (analysis.firstGroup != analysis.secondGroup) {
      f2[analysis.secondGroup][analysis.firstGroup]++;
      f2[analysis.secondGroup][total]++;
    }
  }

  @Override
  public void end() {
    String filename = this.dir.getDirectory() + this.prefix.getStringValue();
    printResults(filename + ".all.tsv", this.f2all);
    printResults(filename + ".known.tsv", this.f2old);
    printResults(filename + ".new.tsv", this.f2new);
    printResults(filename + ".snp.all.tsv", this.f2snpall);
    printResults(filename + ".snp.known.tsv", this.f2snpold);
    printResults(filename + ".snp.new.tsv", this.f2snpnew);
  }

  private void printResults(String filename, int[][] f2) {
    try {
      PrintWriter out = getPrintWriter(filename);
      String line = "X" + T + String.join(T, groups) + T + "TOTAL";
      out.println(line);

      for (int f = 0; f < total; f++) {
        line = this.groups.get(f);
        for (int s = 0; s <= total; s++)
          line += T + f2[f][s];
        out.println(line);
      }
      out.close();
    } catch (IOException e) {
      Message.error("Unable to write to result file " + filename);
    }
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    for (int a = 1; a < variant.getAlleles().length; a++)
      process(variant, a);
    return NO_OUTPUT;
  }

  @Override
  public String[] getHeaders() {
    return null;
  }

  private class Analysis{
    private final boolean isOld;
    private final boolean isSnp;
    private final int firstGroup;
    private final int secondGroup;
    //private final String comment;

     Analysis(Variant variant, int a, int firstGroup, int secondGroup) {
      this.isOld = variant.getInfo().isInDBSNPVEP(a);
      this.isSnp = variant.isSNP(a);
      this.firstGroup = firstGroup;
      this.secondGroup = secondGroup;
      /*String rs = variant.getInfo().getRSs(a);
      if(rs == null || rs.isEmpty())
        rs = ".";
      this.comment = variant.shortString()+" "+a+" ["+rs+"]";
      
      Message.debug(!isOld, firstGroup+" "+secondGroup+" "+comment);*/
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
