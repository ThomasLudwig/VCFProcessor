package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.FisherExactTest;
import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Info;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.QC.*;

/**
 * Reports the distributions of each parameter used by the class QC1078
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2019-10-15
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 * Last Tested on         2020-08-14
 */
public class QCParametersDistribution extends ParallelVCFVariantPedFunction<QCParametersDistribution.Analysis> {

  HashMap<String, ArrayList<String>> samples;
  private FisherExactTest fisherET;

  private NumberSeries callrateSB;
  private NumberSeries fisherCallrateSB;
  private NumberSeries qualByDepthSB;
  private NumberSeries inbreedingCoefSB;
  private NumberSeries mq_rsSB;
  private NumberSeries fs_snpSB;
  private NumberSeries sor_snpSB;
  private NumberSeries mq_snpSB;
  private NumberSeries rprs_snpSB;
  private NumberSeries fs_indelSB;
  private NumberSeries sor_indelSB;
  private NumberSeries mq_indelSB;
  private NumberSeries rprs_indelSB;
  private NumberSeries hqPercentSB;
  private NumberSeries gqSB;
  private NumberSeries sumADSB;
  private NumberSeries abHetDistSB;

  @Override
  public String getSummary() {
    return "Reports the distributions of each parameter used by " + QC.class.getSimpleName();
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("One parameter per line, sorted values");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return "The VCF File must contain the following INFO : " + String.join(",", KEYS);
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Analysis a) {
    for(double callrate : a.callrates)
      callrateSB.add(callrate);
    for(double fisher : a.fishers)
      fisherCallrateSB.add(fisher);
    if(a.qd != null)
      qualByDepthSB.add(a.qd);
    if(a.inbreeding != null)
      inbreedingCoefSB.add(a.inbreeding);
    if(a.mqranksum != null)
      mq_rsSB.add(a.mqranksum);
    if(a.fs_snp != null)
      fs_snpSB.add(a.fs_snp);
    if(a.sor_snp != null)
      sor_snpSB.add(a.sor_snp);
    if(a.mq_snp != null)
      mq_snpSB.add(a.mq_snp);
    if(a.rprs_snp != null)
      rprs_snpSB.add(a.rprs_snp);
    if(a.fs_indel != null)
      fs_indelSB.add(a.fs_indel);
    if(a.sor_indel != null)
      sor_indelSB.add(a.sor_indel);
    if(a.mq_indel != null)
      mq_indelSB.add(a.mq_indel);
    if(a.rprs_indel != null)
      rprs_indelSB.add(a.rprs_indel);
    if(a.hqPercent != null)
      hqPercentSB.add(a.hqPercent);
    for(int gq : a.gqs)
      gqSB.add(gq);
    for(int sumAD : a.sumADs)
      sumADSB.add(sumAD);
    for(double distABHet : a.distABHets)
      abHetDistSB.add(distABHet);
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    out.add(callrateSB.getAllValuesAsString());
    out.add(fisherCallrateSB.getAllValuesAsString());
    out.add(qualByDepthSB.getAllValuesAsString());
    out.add(inbreedingCoefSB.getAllValuesAsString());
    out.add(mq_rsSB.getAllValuesAsString());
    out.add(fs_snpSB.getAllValuesAsString());
    out.add(sor_snpSB.getAllValuesAsString());
    out.add(mq_snpSB.getAllValuesAsString());
    out.add(rprs_snpSB.getAllValuesAsString());
    out.add(fs_indelSB.getAllValuesAsString());
    out.add(sor_indelSB.getAllValuesAsString());
    out.add(mq_indelSB.getAllValuesAsString());
    out.add(rprs_indelSB.getAllValuesAsString());
    out.add(hqPercentSB.getAllValuesAsString());
    out.add(gqSB.getAllValuesAsString());
    out.add(sumADSB.getAllValuesAsString());
    out.add(abHetDistSB.getAllValuesAsString());
    return out.toArray(new String[0]);
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return null;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    callrateSB = new NumberSeries("CallRate", SortedList.Strategy.SORT_AFTERWARDS);
    fisherCallrateSB = new NumberSeries("Fisher(CallRate)", SortedList.Strategy.SORT_AFTERWARDS);
    qualByDepthSB = new NumberSeries("QualByDepth", SortedList.Strategy.SORT_AFTERWARDS);
    inbreedingCoefSB = new NumberSeries("InbreedingCoef", SortedList.Strategy.SORT_AFTERWARDS);
    mq_rsSB = new NumberSeries("MQRankSum", SortedList.Strategy.SORT_AFTERWARDS);
    fs_snpSB = new NumberSeries("FS-snp", SortedList.Strategy.SORT_AFTERWARDS);
    sor_snpSB = new NumberSeries("SOR-snp", SortedList.Strategy.SORT_AFTERWARDS);
    mq_snpSB = new NumberSeries("MQ-snp", SortedList.Strategy.SORT_AFTERWARDS);
    rprs_snpSB = new NumberSeries("RPRS-snp", SortedList.Strategy.SORT_AFTERWARDS);
    fs_indelSB = new NumberSeries("FS-indel", SortedList.Strategy.SORT_AFTERWARDS);
    sor_indelSB = new NumberSeries("SOR-indel", SortedList.Strategy.SORT_AFTERWARDS);
    mq_indelSB = new NumberSeries("MQ-indel", SortedList.Strategy.SORT_AFTERWARDS);
    rprs_indelSB = new NumberSeries("RPRS-indel", SortedList.Strategy.SORT_AFTERWARDS);
    hqPercentSB = new NumberSeries("HQ%", SortedList.Strategy.SORT_AFTERWARDS);
    gqSB = new NumberSeries("GQ", SortedList.Strategy.SORT_AFTERWARDS);
    sumADSB = new NumberSeries("SUM(AD)", SortedList.Strategy.SORT_AFTERWARDS);
    abHetDistSB = new NumberSeries("ABHetDistanceFrom0.5", SortedList.Strategy.SORT_AFTERWARDS);

    this.samples = new HashMap<>();
    if ("null".equals(this.pedFile.getFilename())) {
      String group = "NO_GROUP";
      ArrayList<String> ss = new ArrayList<>();
      for (Sample sample : getVCF().getSortedSamples())
        ss.add(sample.getId());
      this.samples.put(group, ss);
    } else {
      try {
        Ped ped = this.pedFile.getPed();
        for (Sample s : getVCF().getSortedSamples()) {
          String id = s.getId();
          Sample sample = Objects.requireNonNull(ped.getSample(id), "Sample not found [" + id + "]");
          String group = sample.getGroup() + sample.getPhenotype();
          if (!samples.containsKey(group))
            samples.put(group, new ArrayList<>());
          samples.get(group).add(id);
        }
      } catch (PedException ex) {
        Message.fatal("Could not read Ped file", ex, true);
      } catch (NullPointerException npe) {
        Message.fatal(npe.getMessage(), npe, true);
      }
    }

    fisherET = new FisherExactTest(getVCF().getNumberOfSamples());

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
      a.qd = Double.parseDouble(info.getAnnot(KEY_QD));
    } catch (Exception ignore) { }
    try {
      a.inbreeding = Double.parseDouble(info.getAnnot(KEY_INBREEDING));
    } catch (Exception ignore) { }
    try {
      a.mqranksum = Double.parseDouble(info.getAnnot(KEY_MQRANKSUM));
    } catch (Exception ignore) { }

    if (variant.hasSNP()) {
      try {
        a.fs_snp = Double.parseDouble(info.getAnnot(KEY_FS));
      } catch (Exception ignore) { }
      try {
        a.sor_snp = Double.parseDouble(info.getAnnot(KEY_SOR));
      } catch (Exception ignore) { }
      try {
        a.mq_snp = Double.parseDouble(info.getAnnot(KEY_MQ));
      } catch (Exception ignore) { }
      try {
        a.rprs_snp = Double.parseDouble(info.getAnnot(KEY_READPOSRANKSUM));
      } catch (Exception ignore) { }
    } else {
      try {
        a.fs_indel = Double.parseDouble(info.getAnnot(KEY_FS));
      } catch (Exception ignore) { }
      try {
        a.sor_indel = Double.parseDouble(info.getAnnot(KEY_SOR));
      } catch (Exception ignore) { }
      try {
        a.mq_indel = Double.parseDouble(info.getAnnot(KEY_MQ));
      } catch (Exception ignore) { }
      try {
        a.rprs_indel = Double.parseDouble(info.getAnnot(KEY_READPOSRANKSUM));
      } catch (Exception ignore) { }
    }

    double nbHQ = 0;
    double[] called = new double[this.samples.keySet().size()];
    double[] total = new double[this.samples.keySet().size()];
    int i = 0;
    for (String group : this.samples.keySet()) {
      total[i] = this.samples.get(group).size();
      double[] numHets = new double[variant.getAlleleCount()];
      double[] denomHets = new double[variant.getAlleleCount()];
      for (String sample : this.samples.get(group)) {
        Genotype g = variant.getGenotype(sample);
        
        if (!g.isMissing()){
          a.sumADs.add(g.getSumADOrElseDP());
          a.gqs.add(g.getGQ());
          if (g.getSumADOrElseDP() >= MIN_DP && g.getGQ() >= MIN_GQ) {
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

  public static class Analysis {

    Double qd = null;
    Double inbreeding = null;
    Double mqranksum = null;
    Double fs_snp = null;
    Double sor_snp = null;
    Double mq_snp = null;
    Double rprs_snp = null;
    Double fs_indel = null;
    Double sor_indel = null;
    Double mq_indel = null;
    Double rprs_indel = null;
    int altHQ = 0;
    final ArrayList<Double> distABHets = new ArrayList<>();
    final ArrayList<Double> callrates = new ArrayList<>();
    final ArrayList<Double> fishers = new ArrayList<>();
    final ArrayList<Integer> sumADs = new ArrayList<>();
    final ArrayList<Integer> gqs = new ArrayList<>();
    Double hqPercent = 0d;
  }
}
