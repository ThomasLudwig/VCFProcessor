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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
  PrintWriter gtProportion;
  PrintWriter ad0;
  PrintWriter ad1;
  PrintWriter ad2;
  PrintWriter pl0;
  PrintWriter pl1;
  PrintWriter pl2;

  PrintWriter qual;
  PrintWriter inbreedingCoef;
  PrintWriter fs;
  PrintWriter sor;
  PrintWriter mq;
  PrintWriter readPosRankSum;

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
  public String[] processInputVariant(Variant variant) {
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

    gtProportion.println(v.getGT()[0]+T+v.getGT()[1]+T+v.getGT()[2]);

    for(int[] ad : v.getAd()[0])
      ad0.println(ad[0]+T+ad[1]);
    for(int[] ad : v.getAd()[1])
      ad1.println(ad[0]+T+ad[1]);
    for(int[] ad : v.getAd()[2])
      ad2.println(ad[0]+T+ad[1]);

    for(int[] pl : v.getPl()[0])
      pl0.println(pl[0]+T+pl[1]+T+pl[2]);
    for(int[] pl : v.getPl()[1])
      pl1.println(pl[0]+T+pl[1]+T+pl[2]);
    for(int[] pl : v.getPl()[2])
      pl2.println(pl[0]+T+pl[1]+T+pl[2]);
    qual.println(v.getQual());

    if(v.getInbreedingCoef() != null)
      inbreedingCoef.println(v.getInbreedingCoef());
    if(v.getFs() != null)
      fs.println(v.getFs());
    if(v.getSor() != null)
      sor.println(v.getSor());
    if(v.getMq() != null)
      mq.println(v.getMq());
    if(v.getReadPosRankSum() != null)
      readPosRankSum.println(v.getReadPosRankSum());
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    gt0 = new AtomicInteger(0);
    gt1 = new AtomicInteger(0);
    gt2 = new AtomicInteger(0);
    gtM = new AtomicInteger(0);

    try {
      gtProportion = new PrintWriter(new FileWriter(filename.getStringValue() + "gtProportion" + ".txt"));
      ad0 = new PrintWriter(new FileWriter(filename.getStringValue() + "AD0" + ".txt"));
      ad1 = new PrintWriter(new FileWriter(filename.getStringValue() + "AD1" + ".txt"));
      ad2 = new PrintWriter(new FileWriter(filename.getStringValue() + "AD2" + ".txt"));
      pl0 = new PrintWriter(new FileWriter(filename.getStringValue() + "PL0" + ".txt"));
      pl1 = new PrintWriter(new FileWriter(filename.getStringValue() + "PL1" + ".txt"));
      pl2 = new PrintWriter(new FileWriter(filename.getStringValue() + "PL2" + ".txt"));
      qual = new PrintWriter(new FileWriter(filename.getStringValue() + "QUAL" + ".txt"));
      inbreedingCoef = new PrintWriter(new FileWriter(filename.getStringValue() + InfoColumn.INBREEDING_COEFF + ".txt"));
      fs = new PrintWriter(new FileWriter(filename.getStringValue() + "FS" + ".txt"));
      sor = new PrintWriter(new FileWriter(filename.getStringValue() + "SOR" + ".txt"));
      mq = new PrintWriter(new FileWriter(filename.getStringValue() + "MQ" + ".txt"));
      readPosRankSum = new PrintWriter(new FileWriter(filename.getStringValue() + "ReadPosRankSum" + ".txt"));
    } catch(IOException e){
      Message.fatal("Error in init", e, true);
    }
  }

  @SuppressWarnings("unused")
  @Override
  public void end() {
    super.end();
    gtProportion.close();
    ad0.close();
    ad1.close();
    ad2.close();
    pl0.close();
    pl1.close();
    pl2.close();
    qual.close();
    inbreedingCoef.close();
    fs.close();
    sor.close();
    mq.close();
    readPosRankSum.close();
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{};
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

      Double q = null;
      try{
        q = Double.parseDouble(variant.getQual());
      } catch(NumberFormatException ignore){}
      this.qual = q;
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
