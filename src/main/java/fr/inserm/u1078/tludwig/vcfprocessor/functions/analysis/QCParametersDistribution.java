package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.FisherExactTest;
import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078.KEYS;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078.KEY_FS;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078.KEY_INBREEDING;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078.KEY_MQ;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078.KEY_MQRANKSUM;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078.KEY_QD;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078.KEY_READPOSRANKSUM;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078.KEY_SOR;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078.MIN_DP;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC1078.MIN_GQ;

import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Info;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reports the distributions of each parameter used by the class QC1078
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2019-10-15
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 * Last Tested on         2020-08-14
 */
public class QCParametersDistribution extends ParallelVCFVariantFunction {
  public final PedFileParameter pedfile = new PedFileParameter();

  HashMap<String, ArrayList<String>> samples;
  private FisherExactTest fisherET;

  private NumberSeries callrateSB;
  private NumberSeries fisherCallrateSB;
  private NumberSeries qualByDepthSB;
  private NumberSeries inbreedingCoefSB;
  private NumberSeries mqrsSB;
  private NumberSeries fssnpSB;
  private NumberSeries sorsnpSB;
  private NumberSeries mqsnpSB;
  private NumberSeries rprssnpSB;
  private NumberSeries fsindelSB;
  private NumberSeries sorindelSB;
  private NumberSeries mqindelSB;
  private NumberSeries rprsindelSB;
  private NumberSeries hqPercentSB;
  private NumberSeries gqSB;
  private NumberSeries sumADSB;
  private NumberSeries abHetdistSB;

  @Override
  public String getSummary() {
    return "Reports the distributions of each parameter used by " + QC1078.class.getSimpleName();
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("One parameter per line, sorted values");
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
  }

  @Override
  public String getCustomRequirement() {
    return "The VCF File must contain the following INFO : " + String.join(",", KEYS);
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    try {
      Analysis a = (Analysis)analysis;
      if(a.callrates != null)
        for(double callrate : a.callrates)
          callrateSB.add(callrate);
      if(a.fishers != null)
        for(double fisher : a.fishers)
          fisherCallrateSB.add(fisher);
      if(a.qd != null)
        qualByDepthSB.add(a.qd);
      if(a.inbreeding != null)
        inbreedingCoefSB.add(a.inbreeding);
      if(a.mqranksum != null)
        mqrsSB.add(a.mqranksum);
      if(a.fssnp != null)
        fssnpSB.add(a.fssnp);
      if(a.sorsnp != null)
        sorsnpSB.add(a.sorsnp);
      if(a.mqsnp != null)
        mqsnpSB.add(a.mqsnp);
      if(a.rprssnp != null)
        rprssnpSB.add(a.rprssnp);
      if(a.fsindel != null)
        fsindelSB.add(a.fsindel);
      if(a.sorindel != null)
        sorindelSB.add(a.sorindel);
      if(a.mqindel != null)
        mqindelSB.add(a.mqindel);
      if(a.rprsindel != null)
        rprsindelSB.add(a.rprsindel);
      if(a.hqPercent != null)
        hqPercentSB.add(a.hqPercent);
      if(a.gqs != null)
        for(int gq : a.gqs)
          gqSB.add(gq);
      if(a.sumADs != null)
        for(int sumAD : a.sumADs)
          sumADSB.add(sumAD);
      if(a.distABHets != null)
        for(double distABHet : a.distABHets)
          abHetdistSB.add(distABHet);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public void end() {
    super.end();
      println(callrateSB.getAllValuesAsString());
      println(fisherCallrateSB.getAllValuesAsString());
      println(qualByDepthSB.getAllValuesAsString());
      println(inbreedingCoefSB.getAllValuesAsString());
      println(mqrsSB.getAllValuesAsString());
      println(fssnpSB.getAllValuesAsString());
      println(sorsnpSB.getAllValuesAsString());
      println(mqsnpSB.getAllValuesAsString());
      println(rprssnpSB.getAllValuesAsString());
      println(fsindelSB.getAllValuesAsString());
      println(sorindelSB.getAllValuesAsString());
      println(mqindelSB.getAllValuesAsString());
      println(rprsindelSB.getAllValuesAsString());
      println(hqPercentSB.getAllValuesAsString());
      println(gqSB.getAllValuesAsString());
      println(sumADSB.getAllValuesAsString());
      println(abHetdistSB.getAllValuesAsString());
  }

  @Override
  public String[] getHeaders() {
    return null;
  }

  @Override
  public void begin() {
    super.begin();
    callrateSB = new NumberSeries("CallRate", SortedList.Strategy.SORT_AFTERWARDS);
    fisherCallrateSB = new NumberSeries("Fisher(CallRate)", SortedList.Strategy.SORT_AFTERWARDS);
    qualByDepthSB = new NumberSeries("QualByDepth", SortedList.Strategy.SORT_AFTERWARDS);
    inbreedingCoefSB = new NumberSeries("InbreedingCoef", SortedList.Strategy.SORT_AFTERWARDS);
    mqrsSB = new NumberSeries("MQRankSum", SortedList.Strategy.SORT_AFTERWARDS);
    fssnpSB = new NumberSeries("FS-snp", SortedList.Strategy.SORT_AFTERWARDS);
    sorsnpSB = new NumberSeries("SOR-snp", SortedList.Strategy.SORT_AFTERWARDS);
    mqsnpSB = new NumberSeries("MQ-snp", SortedList.Strategy.SORT_AFTERWARDS);
    rprssnpSB = new NumberSeries("RPRS-snp", SortedList.Strategy.SORT_AFTERWARDS);
    fsindelSB = new NumberSeries("FS-indel", SortedList.Strategy.SORT_AFTERWARDS);
    sorindelSB = new NumberSeries("SOR-indel", SortedList.Strategy.SORT_AFTERWARDS);
    mqindelSB = new NumberSeries("MQ-indel", SortedList.Strategy.SORT_AFTERWARDS);
    rprsindelSB = new NumberSeries("RPRS-indel", SortedList.Strategy.SORT_AFTERWARDS);
    hqPercentSB = new NumberSeries("HQ%", SortedList.Strategy.SORT_AFTERWARDS);
    gqSB = new NumberSeries("GQ", SortedList.Strategy.SORT_AFTERWARDS);
    sumADSB = new NumberSeries("SUM(AD)", SortedList.Strategy.SORT_AFTERWARDS);
    abHetdistSB = new NumberSeries("ABHetDistanceFrom0.5", SortedList.Strategy.SORT_AFTERWARDS);

    this.samples = new HashMap<>();
    if ("null".equals(this.pedfile.getFilename())) {
      String group = "NOGROUP";
      ArrayList<String> ss = new ArrayList<>();
      for (Sample sample : getVCF().getSamples())
        ss.add(sample.getId());
      this.samples.put(group, ss);
    } else {
      try {
        Ped ped = this.pedfile.getPed();
        for (Sample s : getVCF().getSamples()) {
          String id = s.getId();
          Sample sample = ped.getSample(id);
          if (sample == null)
            this.fatalAndDie("Sample not found [" + id + "]");
          
          String group = "" + sample.getGroup() + sample.getPhenotype();
          if (!samples.containsKey(group))
            samples.put(group, new ArrayList<>());
          samples.get(group).add(id);
        }
      } catch (PedException ex) {
        this.fatalAndDie("Could not read Ped file", ex);
      }
    }

    fisherET = new FisherExactTest(getVCF().getSamples().size());

    for (String key : KEYS) {
      boolean found = false;
      for (String header : getVCF().getHeadersWithoutSamples())
        if (header.startsWith("##INFO=<ID=" + key + ",")) {
          found = true;
          break;
        }
      if (!found)
        Message.warning("Input VCF seems to be missing the following annotation [" + key + "]");
    }
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    Info info = variant.getInfo();
    Analysis a = new Analysis();

    try {
      a.qd = new Double(info.getAnnot(KEY_QD));
    } catch (Exception e) {
    }
    try {
      a.inbreeding = new Double(info.getAnnot(KEY_INBREEDING));
    } catch (Exception e) {
      //Nothing
    }
    try {
      a.mqranksum = new Double(info.getAnnot(KEY_MQRANKSUM));
    } catch (Exception e) {
      //Nothing
    }

    if (variant.hasSNP()) {
      try {
        a.fssnp = new Double(info.getAnnot(KEY_FS));
      } catch (Exception e) {

      }
      try {
        a.sorsnp = new Double(info.getAnnot(KEY_SOR));
      } catch (Exception e) {
      }
      try {
        a.mqsnp = new Double(info.getAnnot(KEY_MQ));
      } catch (Exception e) {
      }
      try {
        a.rprssnp = new Double(info.getAnnot(KEY_READPOSRANKSUM));
      } catch (Exception e) {
        //Nothing
      }
    } else {
      try {
        a.fsindel = new Double(info.getAnnot(KEY_FS));
      } catch (Exception e) {

      }
      try {
        a.sorindel = new Double(info.getAnnot(KEY_SOR));
      } catch (Exception e) {
      }
      try {
        a.mqindel = new Double(info.getAnnot(KEY_MQ));
      } catch (Exception e) {
      }
      try {
        a.rprsindel = new Double(info.getAnnot(KEY_READPOSRANKSUM));
      } catch (Exception e) {
        //Nothing
      }
    }

    double nbHQ = 0;
    double called[] = new double[this.samples.keySet().size()];
    double total[] = new double[this.samples.keySet().size()];
    int i = 0;
    for (String group : this.samples.keySet()) {
      total[i] = this.samples.get(group).size();
      double[] numHets = new double[variant.getAlleleCount()];
      double[] denomHets = new double[variant.getAlleleCount()];
      for (String sample : this.samples.get(group)) {
        Genotype g = variant.getGenotype(sample);
        
        if (!g.isMissing()){
          a.sumADs.add(g.getSumAD());
          a.gqs.add(g.getGQ());
          if (g.getSumAD() >= MIN_DP && g.getGQ() >= MIN_GQ) {
            nbHQ++;
            if (g.hasAlternate())
              a.altHQ++;
          } else
            g.setMissing();
        }

        if (!g.isMissing())
          called[i]++;

        if (g.isHeterozygousDiploid()) {
          final int[] ad = g.getAD();
          if (ad != null) {
            int gt1 = g.getAlleles()[0];
            int gt2 = g.getAlleles()[1];
            numHets[gt1] += ad[gt1];
            numHets[gt2] += ad[gt2];
            denomHets[gt1] += ad[gt1] + ad[gt2];
            denomHets[gt2] += ad[gt1] + ad[gt2];
          }
        }
      }
      for (int h = 0; h < variant.getAlleleCount(); h++)
        if (denomHets[h] != 0)
          a.distABHets.add(Math.abs(0.5 - (numHets[h] / denomHets[h])));
      i++;
    }

    for (i = 0; i < total.length; i++)
      if (total[i] != 0)
        a.callrates.add(called[i] / total[i]);
      else
        a.callrates.add(0d);

    for (i = 0; i < total.length - 1; i++)
      for (int j = i + 1; j < total.length; j++) {
        int fa = (int) called[i];
        int fb = (int) called[j];
        int fc = (int) (total[i] - called[i]);
        int fd = (int) (total[j] - called[j]);
        if (fa + fc != 0 && fb + fd != 0)
          a.fishers.add(fisherET.twoTailed(fa, fb, fc, fd));
      }

    a.hqPercent = (nbHQ / variant.getGenotypes().length);

    this.pushAnalysis(a);
    return NO_OUTPUT;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }

  private class Analysis {

    Double qd = null;
    Double inbreeding = null;
    Double mqranksum = null;
    Double fssnp = null;
    Double sorsnp = null;
    Double mqsnp = null;
    Double rprssnp = null;
    Double fsindel = null;
    Double sorindel = null;
    Double mqindel = null;
    Double rprsindel = null;
    int altHQ = 0;
    ArrayList<Double> distABHets = new ArrayList<>();
    ArrayList<Double> callrates = new ArrayList<>();
    ArrayList<Double> fishers = new ArrayList<>();
    ArrayList<Integer> sumADs = new ArrayList<>();
    ArrayList<Integer> gqs = new ArrayList<>();
    Double hqPercent = 0d;
  }
}
