package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.InfoColumn;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.FileOutputer;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gets all the Metrics used by the QC function
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2023-07-04
 * Checked for release on XXXX-XX-XX
 * Unit Test defined on   XXXX-XX-XX
 */
public class GetQCMetrics extends ParallelVCFVariantFunction<GetQCMetrics.Values> {
  private final StringParameter filename = new StringParameter(OPT_FILE, "metrics.my.project", "output filename prefix");

  AtomicInteger gt0;
  AtomicInteger gt1;
  AtomicInteger gt2;
  AtomicInteger gtM;
  FileOutputer gtProportion;
  FileOutputer ad0;
  FileOutputer ad1;
  FileOutputer ad2;
  FileOutputer pl0;
  FileOutputer pl1;
  FileOutputer pl2;

  FileOutputer qual;
  FileOutputer inbreedingCoef;
  FileOutputer fs;
  FileOutputer sor;
  FileOutputer mq;
  FileOutputer readPosRankSum;

  @Override
  public String getSummary() {
    return "Gets all the Metrics used by the QC function";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(getSummary());
  }

  @Override
  public String getOutputExtension() {
    return OUT_TXT;
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return new VCFPolicies(VCFPolicies.MultiAllelicPolicy.DROP, false, "VCF Requires the following annotations QUAL_BY_DEPTH,INBREEDING_COEF,FS,SOR,MQ,READPOSRANKSUM,AD,PL,GT"); }

  @Override
  public Println[] processInputVariant(Variant variant) {
    if(variant.isBiallelic())
      this.pushAnalysis(new Values(variant));

    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Values v) {
    gt0.addAndGet(v.getGT()[0]);
    gt1.addAndGet(v.getGT()[1]);
    gt2.addAndGet(v.getGT()[2]);
    gtM.addAndGet(v.getGT()[3]);

    gtProportion.println(Println.join(T, v.getGT()[0], v.getGT()[1], v.getGT()[2]));

    for(int[] ad_0 : v.getAd()[0])
      ad0.println(new Println(ad_0[0], T, ad_0[1]));
    for(int[] ad_1 : v.getAd()[1])
      ad1.println(new Println(ad_1[0], T, ad_1[1]));
    for(int[] ad_2 : v.getAd()[2])
      ad2.println(new Println(ad_2[0], T, ad_2[1]));

    for(int[] pl_0 : v.getPl()[0])
      pl0.println(Println.join(T, pl_0[0], pl_0[1], pl_0[2]));
    for(int[] pl_1 : v.getPl()[1])
      pl1.println(Println.join(T, pl_1[0], pl_1[1], pl_1[2]));
    for(int[] pl_2 : v.getPl()[2])
      pl2.println(Println.join(T, pl_2[0], pl_2[1], pl_2[2]));
    qual.println(new Println(v.getQual()));

    if(v.getInbreedingCoef() != null)
      inbreedingCoef.println(new Println(v.getInbreedingCoef()));
    if(v.getFs() != null)
      fs.println(new Println(v.getFs()));
    if(v.getSor() != null)
      sor.println(new Println(v.getSor()));
    if(v.getMq() != null)
      mq.println(new Println(v.getMq()));
    if(v.getReadPosRankSum() != null)
      readPosRankSum.println(new Println(v.getReadPosRankSum()));
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    gt0 = new AtomicInteger(0);
    gt1 = new AtomicInteger(0);
    gt2 = new AtomicInteger(0);
    gtM = new AtomicInteger(0);

    try {
      gtProportion = getFileOutputer(filename.getStringValue() + "gtProportion" + ".txt");
      ad0 = getFileOutputer(filename.getStringValue() + "AD0" + ".txt");
      ad1 = getFileOutputer(filename.getStringValue() + "AD1" + ".txt");
      ad2 = getFileOutputer(filename.getStringValue() + "AD2" + ".txt");
      pl0 = getFileOutputer(filename.getStringValue() + "PL0" + ".txt");
      pl1 = getFileOutputer(filename.getStringValue() + "PL1" + ".txt");
      pl2 = getFileOutputer(filename.getStringValue() + "PL2" + ".txt");
      qual = getFileOutputer(filename.getStringValue() + "QUAL" + ".txt");
      inbreedingCoef = getFileOutputer(filename.getStringValue() + InfoColumn.INBREEDING_COEFF + ".txt");
      fs = getFileOutputer(filename.getStringValue() + "FS" + ".txt");
      sor = getFileOutputer(filename.getStringValue() + "SOR" + ".txt");
      mq = getFileOutputer(filename.getStringValue() + "MQ" + ".txt");
      readPosRankSum = getFileOutputer(filename.getStringValue() + "ReadPosRankSum" + ".txt");
    } catch(IOException e){
      Message.fatal("Error in init", e, true);
    }
  }

  @SuppressWarnings("unused")
  @Override
  public void end() {
    super.end();
    for(FileOutputer fo : new FileOutputer[]{gtProportion, ad0, ad1, ad2, pl0, pl1, pl2, qual, inbreedingCoef, fs, sor, mq, readPosRankSum})
    try { fo.close(); }
    catch(Exception e){ Message.error("Error while closing ["+fo.getFilename()+"] "+e.getMessage()); }
  }

  @SuppressWarnings("unused")
  @Override
  public Println[] getHeaders() {
    return NO_OUTPUT;
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  public static class Values {
    // Genotype values
    private final Variant variant;
    private final int[] gt;
    private final ArrayList<int[]>[] ad;
    private final ArrayList<int[]>[] pl;

    //Variants values
    private final Double qual;
    private final Double inbreedingCoef;
    private final Double fs;
    private final Double sor;
    private final Double mq;
    private final Double readPosRankSum;

    @SuppressWarnings("unchecked")
    Values(Variant variant){
      this.variant = variant;
      gt = new int[4];

      ad = new ArrayList[3];
      pl = new ArrayList[3];
      for(int i = 0 ; i < 3; i++){
        ad[i] = new ArrayList<>();
        pl[i] = new ArrayList<>();
      }

      for(Genotype geno : variant.getGenotypes()){
        if(geno.isMissing())
          gt[3]++;
        else {
          int g = geno.getCount(1);
          gt[g]++;
          ad[g].add(geno.getAD());
          pl[g].add(geno.getPL());
        }
      }

      this.qual = variant.getQual();
      this.inbreedingCoef = variant.getInfo().getInbreedingCoeff();
      this.fs = variant.getInfo().getFS();
      this.sor = variant.getInfo().getSOR();
      this.mq = variant.getInfo().getMQ();
      this.readPosRankSum = variant.getInfo().getReadPosRankSum();
    }

    public int[] getGT() { return gt; }
    public ArrayList<int[]>[] getAd() { return ad; }
    public ArrayList<int[]>[] getPl() { return pl; }
    public Double getQual() { return qual; }
    public Double getInbreedingCoef() { return inbreedingCoef; }
    public Double getFs() { return fs; }
    public Double getSor() { return sor; }
    public Double getMq() { return mq; }
    public Double getReadPosRankSum() { return readPosRankSum; }
  }
}
