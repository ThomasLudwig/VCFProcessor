package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.VCFFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Kappa Comparision between to vcf files.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-06-14
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-09-23
 */
public class Kappa extends VCFFunction { //TODO parallelize like in IQS

  private final VCFFileParameter vcffile2 = new VCFFileParameter(OPT_VCF + 2, "File2.vcf", "the second input VCF File (can be gzipped)");
  private final TSVFileParameter project = new TSVFileParameter(OPT_TSV, "output.tsv", "the result TSV File");
  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();
  private static final String[] HEADER = {"CHROM","POS","ID","MAF_FILE1","MAF_FILE2","KAPPA_With_Missing","KAPPA_Ignore_Missing"};

  private final NumberSeries missing = new NumberSeries("Missing", SortedList.Strategy.SORT_AFTERWARDS);
  private final NumberSeries ignore = new NumberSeries("Ignore", SortedList.Strategy.SORT_AFTERWARDS);

  private ArrayList<String> samples;
  
  @Override
  public String getSummary() {
    return "Kappa Comparision between to vcf files.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("See : https://journals.sagepub.com/doi/abs/10.1177/001316446002000104?journalCode=epma and https://en.wikipedia.org/wiki/Cohen%27s_kappa")
            .addLine("Output format is :")
            .addColumns(HEADER);
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return "Results are given for the first alternate allele, why is expected to be the same in both files."; //TODO change implementation
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
  public void executeFunction() throws Exception {
    VCF vcf1 = this.vcffile.getVCF(VCF.MODE_QUICK_GENOTYPING, VCF.STEP10000);//VCF vcf1 = this.vcffile.getVCF(ped, VCF.MODE_QUICK_GENOTYPING, VCF.STEP10000);
    VCF vcf2 = this.vcffile2.getVCF(VCF.MODE_QUICK_GENOTYPING, VCF.STEP_OFF);//VCF vcf2 = this.vcffile2.getVCF(ped, VCF.MODE_QUICK_GENOTYPING, VCF.STEP_OFF);
    this.samples = VCF.commonSamples(vcf1, vcf2);

    vcf1.getReaderAndStart();
    vcf2.getReaderAndStart();
    
    Variant v1 = vcf1.getNextVariant();
    Variant v2 = vcf2.getNextVariant();
    int nbReadLeft = 1;
    int nbReadRight = 1;
    int nbProcessed = 0;

    PrintWriter out = getPrintWriter(dir.getDirectory() + this.project + ".tsv");

    out.println("CHROM" + T + "POS" + T + "ID" + T + "MAF_" + this.vcffile.getBasename() + T + "MAF_" + this.vcffile2.getBasename() + T + "KAPPA_With_Missing" + T + "KAPPA_Ignore_Missing");

    while (v1 != null && v2 != null) {
      if (nbReadLeft % 10000 == 0)
        Message.progressInfo(nbProcessed + "/[" + nbReadLeft + "|" + nbReadRight + "] variants processed");

      int comparePosition = v1.compareTo(v2);

      if (comparePosition == 0) {
        nbProcessed++;
        this.evaluateVariant(v1, v2, out);
      }
      if (comparePosition < 0) {
        //in case of equality, we only forward the right file.
        //the left file has multiallelic snps on the same line
        //the right file has multiallelic snps on SUCCESSIVE UNSORTED lines
        //++ this is the only way to make sure we do all the comparison in case the right variant is multiallelic
        //-- the drawback is that in the vast majority of equality of position, we do 1 superfluous position comparison
        nbReadLeft++;
        v1 = vcf1.getNextVariant();
      }
      if (comparePosition >= 0) {
        nbReadRight++;
        v2 = vcf2.getNextVariant();
      }
    }
    vcf1.close();
    vcf2.close();
    Message.info(nbProcessed + "/[" + nbReadLeft + "|" + nbReadRight + "] variants processed");

    out.close();

    // Print Statistics
    out = getPrintWriter(dir + this.project.getFilename() + ".stats", false);
    out.println(T + "Kappa_With_Missing" + T + "Kappa_Ignore_Missing");
    out.println("Mean" + T + missing.getMean() + T + ignore.getMean());
    out.println("Standard Deviation" + T + missing.getStandardDeviation() + T + ignore.getStandardDeviation());
    out.println("Min" + T + missing.getMin() + T + ignore.getMin());
    out.println("D1" + T + missing.getPercentile(.1) + T + ignore.getPercentile(.1));
    out.println("D2" + T + missing.getPercentile(.2) + T + ignore.getPercentile(.2));
    out.println("Q1" + T + missing.getPercentile(.25) + T + ignore.getPercentile(.25));
    out.println("D3" + T + missing.getPercentile(.3) + T + ignore.getPercentile(.3));
    out.println("D4" + T + missing.getPercentile(.4) + T + ignore.getPercentile(.4));
    out.println("Median" + T + missing.getMedian() + T + ignore.getMedian());
    out.println("D6" + T + missing.getPercentile(.6) + T + ignore.getPercentile(.6));
    out.println("D7" + T + missing.getPercentile(.7) + T + ignore.getPercentile(.7));
    out.println("Q3" + T + missing.getPercentile(.75) + T + ignore.getPercentile(.75));
    out.println("D8" + T + missing.getPercentile(.8) + T + ignore.getPercentile(.8));
    out.println("D9" + T + missing.getPercentile(.9) + T + ignore.getPercentile(.9));
    out.println("Max" + T + missing.getMax() + T + ignore.getMax());
    out.close();
  }

  private void evaluateVariant(Variant v1, Variant v2, PrintWriter out) {
    String chrom = v1.getChrom();
    int pos = v2.getPos();
    String id = v1.getId();
    if (id.equals("."))
      id = v2.getId();
    String maf1 = StringTools.formatDouble(v1.getAlleleFrequencyTotal(1), 3);
    String maf2 = StringTools.formatDouble(v2.getAlleleFrequencyTotal(1), 3);
    double kappawith = this.kappa(v1, v2);
    double kappaignore = this.kappaIgnoreMissing(v1, v2);
    String kappaW = StringTools.formatDouble(kappawith, 3);
    String kappaI = StringTools.formatDouble(kappaignore, 3);

    this.missing.add(kappawith);

    if (!Double.isNaN(kappaignore))
      this.ignore.add(kappaignore);

    out.println(chrom + T + pos + T + id + T + maf1 + T + maf2 + T + kappaW + T + kappaI);
  }

  private static final int AA = 0;
  private static final int AT = 1;
  private static final int TT = 2;
  private static final int TX = 3;
  private static final int MS = 4;
  private static final int TOT = 5;
  
  private static int getIndex(Genotype g){
    if(g.isMissing() || g.getNbChrom() != 2)
      return MS;
    int ref = g.getCount(0);
    int alt = g.getCount(1);        
    if (ref == 2)
      return AA;
    else if (ref == 1 && alt == 1)
      return AT;
    else if (alt == 2)
      return TT;
    return TX;
  }
  
  private double kappa(Variant a, Variant b) {
    if (a == null || b == null)
      return 0;    

    int[][] t = new int[6][6];

    for (String sample : samples) {
      int abs = getIndex(a.getGenotype(sample));
      int ord = getIndex(b.getGenotype(sample));

      t[abs][ord]++;
      t[abs][TOT]++;
      t[TOT][ord]++;
      t[TOT][TOT]++;
    }
    
    /*LineBuilder msg = new LineBuilder();
    for(int i = 0 ; i <= TOT; i++){
      msg.append("[");
      msg.append(Arrays.toString(t[i]));
      msg.append("]");
    }*/

    
    double pa = 0;
    double pe = 0;

    for (int i = 0; i < TOT; i++) {
      pa += t[i][i];
      pe += t[i][TOT] * t[TOT][i];
    }
    pa = pa / t[TOT][TOT];
    pe = pe / (t[TOT][TOT] * t[TOT][TOT]);
    
    /*msg.addSpace("pa=").append(pa);
    msg.addSpace("pe=").append(pe);
    Message.debug(msg.toString());*/
    
    if (pe == 1) //pe == 1, when all observation, for both files are in the same unique category
      return 1;
    
    if (pa == 1) //pa == 1, when all observation match in both files
      return 1; //avoid pointless division x/x

    double kappa = (pa - pe) / (1 - pe);
    return kappa;
  }

  private double kappaIgnoreMissing(Variant a, Variant b) {
    if (a == null || b == null)
      return 0;

    int[][] t = new int[6][6];

    for (String sample : samples) {
      int abs = getIndex(a.getGenotype(sample));
      int ord = getIndex(b.getGenotype(sample));

      if (abs != MS && ord != MS) {
        t[abs][ord]++;
        t[abs][TOT]++;
        t[TOT][ord]++;
        t[TOT][TOT]++;
      }
    }

    double pa = 0;
    double pe = 0;

    for (int i = 0; i < TOT; i++) {
      pa += t[i][i];
      pe += t[i][TOT] * t[TOT][i];
    }
    pa = pa / t[TOT][TOT];
    pe = pe / (t[TOT][TOT] * t[TOT][TOT]);
    
    if (pe == 1) //pe == 1, when all observation, for both files are in the same unique category
      return 1;
    
    if (pa == 1) //pa == 1, when all observation match in both files
      return 1; //avoid pointless division x/x

    double kappa = (pa - pe) / (1 - pe);
    return kappa;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript ts = TestingScript.newDirectoryAnalysis();
    ts.addAnonymousFilename("vcf", "vcf");
    ts.addAnonymousFilename("vcf2", "vcf2");
    ts.addAnonymousValue("tsv", "output");
    return new TestingScript[]{ts};
  }
}
