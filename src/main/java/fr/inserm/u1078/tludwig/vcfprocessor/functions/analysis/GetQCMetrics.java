package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Info;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
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
public class GetQCMetrics extends ParallelVCFVariantFunction {
  StringParameter filename = new StringParameter(OPT_FILE, "metrics.my.project", "output filename prefix");

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

  @Override
  public Description getDesc() {
    return new Description(getSummary());
  }

  @Override
  public String getOutputExtension() {
    return OUT_TXT;
  }

  @Override
  public boolean needVEP() {
    return false;
  }

  @Override
  public String getCustomRequirement() {
    return "VCF Requires the following annotations QUAL_BY_DEPTH,INBREEDING_COEF,FS,SOR,MQ,READPOSRANKSUM,AD,PL,GT";
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_DROP;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    if(variant.isBiallelic())
      this.pushAnalysis(new Values(variant));

    return NO_OUTPUT;
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    Values v = (Values)analysis;
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

    if(v.getInbreedingCoef() != Double.NEGATIVE_INFINITY)
      inbreedingCoef.println(v.getInbreedingCoef());
    if(v.getFs() != Double.NEGATIVE_INFINITY)
      fs.println(v.getFs());
    if(v.getSor() != Double.NEGATIVE_INFINITY)
      sor.println(v.getSor());
    if(v.getMq() != Double.NEGATIVE_INFINITY)
      mq.println(v.getMq());
    if(v.getReadPosRankSum() != Double.NEGATIVE_INFINITY)
      readPosRankSum.println(v.getReadPosRankSum());
    return true;
  }

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
      inbreedingCoef = new PrintWriter(new FileWriter(filename.getStringValue() + Info.INBREEDING_COEFF + ".txt"));
      fs = new PrintWriter(new FileWriter(filename.getStringValue() + "FS" + ".txt"));
      sor = new PrintWriter(new FileWriter(filename.getStringValue() + "SOR" + ".txt"));
      mq = new PrintWriter(new FileWriter(filename.getStringValue() + "MQ" + ".txt"));
      readPosRankSum = new PrintWriter(new FileWriter(filename.getStringValue() + "ReadPosRankSum" + ".txt"));
    } catch(IOException e){
      Message.die("Error in init", e);
    }
  }

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

  @Override
  public String[] getHeaders() {
    return new String[]{};
  }

  /*@Override
  public String[] getFooters() {
    return new String[]{
            gtProportion.toString(),
            "GT012M\t"+gt0+","+gt1+","+gt2+","+gtM,
            ad0.toString(),
            ad1.toString(),
            ad2.toString(),
            pl0.toString(),
            pl1.toString(),
            pl2.toString(),
            qual.toString(),
            inbreedingCoef.toString(),
            fs.toString(),
            sor.toString(),
            mq.toString(),
            readPosRankSum.toString()
    };
  }*/

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  private static class Values {
    // Genotype values
    private final Variant variant;
    private final int[] gt;
    private final ArrayList<int[]>[] ad;
    private final ArrayList<int[]>[] pl;

    //Variants values
    private final double qual;
    private final double inbreedingCoef;
    private final double fs;
    private final double sor;
    private final double mq;
    private final double readPosRankSum;

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

      this.qual = parseValue(variant.getQual(), "QUAL");
      this.inbreedingCoef = parseValue(Info.INBREEDING_COEFF);
      this.fs = parseValue("FS");
      this.sor = parseValue("SOR");
      this.mq = parseValue("MQ");
      this.readPosRankSum = parseValue("ReadPosRankSum");
    }

    private double parseValue(String key){
      return parseValue(variant.getInfo().getValue(key), key);
    }

    private double parseValue(String val, String type){
      double v = Double.NEGATIVE_INFINITY;
      try{
        v = Double.parseDouble(val);
      } catch(NumberFormatException | NullPointerException e) {
        Message.warning("Unable to parse "+type+" ["+val+"] for variant ["+variant.shortString()+"]");
      }
      return v;
    }

    public int[] getGT() {
      return gt;
    }

    public ArrayList<int[]>[] getAd() {
      return ad;
    }

    public ArrayList<int[]>[] getPl() {
      return pl;
    }

    public double getQual() {
      return qual;
    }

    public double getInbreedingCoef() {
      return inbreedingCoef;
    }

    public double getFs() {
      return fs;
    }

    public double getSor() {
      return sor;
    }

    public double getMq() {
      return mq;
    }

    public double getReadPosRankSum() {
      return readPosRankSum;
    }
  }
}
