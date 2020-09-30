package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.VCFFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Compares the genotypes of the samples in the first and second VCF file.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-04-06
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-07-07
 * Last Tested on         2020-08-14
 */
public class CompareGenotype extends VCFPedFunction {//TODO parallelize like in IQS

  private final VCFFileParameter vcffile2 = new VCFFileParameter(OPT_VCF + 2, "File2.vcf(.gz)", "the second input VCF file (can be bgzipped)");
  private ArrayList<Sample> samples;
  private int[] totals;
  private int[] matches;
  private int[] mismatches;
  private int[] missinglefts;
  private int[] missingrights;
  private NumberSeries[] stats;
  NumberSeries global;
  private static final String[] HEADERS = {"Sample", "Group", "Total", "Concord", "Discord", "LeftMissing", "RightMissing", "%Concord"};

  @Override
  public String getSummary() {
    return "Compares the genotypes of the samples in the first and second VCF file.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Both VCF are suppose to contain the same samples. This function compares the genotypes of each sample for each variant accross the files.")
            .addLine("This can be useful, for example, to compare 2 calling algorithm.")
            .addLine("Output for is :")
            .addColumns(HEADERS);
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return "Alternate alleles are expected to be the same and in the same order in both files"; //TODO change implementation
  }

  @Override
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public void executeFunction() throws Exception {
    Ped ped = this.pedfile.getPed();
    this.stats = new NumberSeries[ped.getGroups().size()];
    for (int i = 0; i < stats.length; i++)
      stats[i] = new NumberSeries(ped.getGroups().get(i), SortedList.Strategy.SORT_AFTERWARDS);
    global = new NumberSeries("Global", SortedList.Strategy.SORT_AFTERWARDS);
    this.samples = ped.getSamples();
    this.totals = new int[this.samples.size()];
    this.matches = new int[this.samples.size()];
    this.mismatches = new int[this.samples.size()];
    this.missinglefts = new int[this.samples.size()];
    this.missingrights = new int[this.samples.size()];
    
    Message.debug("Opening VCFs");

    VCF vcf1 = this.vcffile.getVCF(VCF.MODE_QUICK_GENOTYPING, VCF.STEP10000);//VCF vcf1 = this.vcffile.getVCF(ped, VCF.MODE_QUICK_GENOTYPING, VCF.STEP10000);
    VCF vcf2 = this.vcffile2.getVCF(VCF.MODE_QUICK_GENOTYPING, VCF.STEP_OFF);//VCF vcf2 = this.vcffile2.getVCF(ped, VCF.MODE_QUICK_GENOTYPING, VCF.STEP_OFF);
    
    Message.debug("Opened");
    
    vcf1.getReaderAndStart();
    vcf2.getReaderAndStart();
    
    Message.debug("Reader started");

    Variant v1 = vcf1.getNextVariant();
    Variant v2 = vcf2.getNextVariant();    
    
    Message.debug("Ready");
    
    int nbReadLeft = 1;
    int nbReadRight = 1;
    int nbProcessed = 0;

    while (v1 != null && v2 != null) { //TODO test seems stuck
      //Message.debug("V1["+v1.getPos()+"] vs V2["+v2.getPos()+"]");
      if (nbReadLeft % 10000 == 0)
        Message.progressInfo(nbProcessed + "/[" + nbReadLeft + "|" + nbReadRight + "] variants processed");

      int comparePosition = v1.compareTo(v2);

      if (comparePosition == 0) {
        nbProcessed++;
        this.evaluateVariant(v1, v2);
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
    Message.info(nbProcessed + "/[" + nbReadLeft + "|" + nbReadRight + "] variants processed");

    println(String.join(T, HEADERS));
    for (int s = 0; s < this.samples.size(); s++) {
      String name = this.samples.get(s).getId();
      String group = this.samples.get(s).getGroup();
      int idx = ped.getGroupIndex(group);
      int tot = this.totals[s];
      int match = this.matches[s];
      int mismatch = this.mismatches[s];
      int left = this.missinglefts[s];
      int right = this.missingrights[s];
      double ratio = (100.0 * match) / tot;
      stats[idx].add(ratio);
      global.add(ratio);
      println(String.join(T, new String[]{name,group,tot+"",match+"",mismatch+"",left+"",right+"",ratio+""}));
    }

    println("");
    println(T + "Mean" + T + "Min" + T + "Q1" + T + "Median" + T + "Q3" + T + "Max");
    println(global.getName() + T + StringTools.formatDouble(global.getMean(), 3) + T + StringTools.formatDouble(global.getMin(), 3) + T + StringTools.formatDouble(global.getFirstQuartile(), 3) + T + StringTools.formatDouble(global.getMedian(), 3) + T + StringTools.formatDouble(global.getLastQuartile(), 3) + T + StringTools.formatDouble(global.getMax(), 3));
    for (NumberSeries stat : stats)
      println(stat.getName() + T + StringTools.formatDouble(stat.getMean(), 3) + T + StringTools.formatDouble(stat.getMin(), 3) + T + StringTools.formatDouble(stat.getFirstQuartile(), 3) + T + StringTools.formatDouble(stat.getMedian(), 3) + T + StringTools.formatDouble(stat.getLastQuartile(), 3) + T + StringTools.formatDouble(stat.getMax(), 3));

    vcf1.close();
    vcf2.close();

  }

  private void evaluateVariant(Variant v1, Variant v2) {
    for (int s = 0; s < this.samples.size(); s++) {
      Sample sample = this.samples.get(s);
      Genotype g1 = v1.getGenotype(sample);
      Genotype g2 = v2.getGenotype(sample);

      totals[s]++;
      if (g1.isMissing())
        this.missinglefts[s]++;
      else if (g2.isMissing())
        this.missingrights[s]++;
      else
        if (g1.isSame(g2))
          this.matches[s]++;
        else
          this.mismatches[s]++;
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("ped", "ped");
    def.addAnonymousFilename("vcf2", "vcf2");
    return new TestingScript[]{def};
  }
}
