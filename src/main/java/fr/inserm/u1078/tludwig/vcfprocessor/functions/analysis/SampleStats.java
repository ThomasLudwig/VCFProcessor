package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Print Stats about each samples (Mean Depths, TS/TV Het/HomAlt).
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-11-23
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 */
public class SampleStats extends ParallelVCFVariantPedFunction {

  private int S;

  private int nbSites = 0;
  private int[] depths;
  private int[] depthPresent;
  private int[] missings;
  private int[] singleton;
  private int[] tss;
  private int[] tvs;
  private int[] hets;
  private int[] homAlts;
  private int[] variants;

  private ArrayList<Sample> samples;

  public static final String[] HEADERS = {"Sample","Group","Sites","Genotyped","Missing","%Missing","MeanDepths","Variants", "Singletons","TS","TV","TS/TV","Het","HetRatio","HomAlt"};

  @Override
  public String getSummary() {
    return "Print Stats about each samples (Mean Depths, TS/TV Het/HomAlt).";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output format is :")
            .addColumns(HEADERS);
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
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
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    for (int s = 0; s < S; s++) {
      int genotyped = nbSites - this.missings[s];
      String[] values = {this.samples.get(s).getId(),
      this.samples.get(s).getGroup(),
      nbSites+"",
      genotyped+"",
      this.missings[s]+"",
      StringTools.formatRatio(100*this.missings[s], nbSites, 4)+"%",
      StringTools.formatRatio(this.depths[s],this.depthPresent[s],4),
      this.variants[s]+"",
      this.singleton[s]+"",
      this.tss[s]+"",
      this.tvs[s]+"",
      this.tvs[s] != 0 ? StringTools.formatRatio(this.tss[s], tvs[s], 4) : "0",
      this.hets[s]+"",
      genotyped != 0 ? StringTools.formatRatio(this.hets[s], genotyped, 4) : "0",
      this.homAlts[s]+""};
      
      out.add(String.join(T, values));
    }
    return out.toArray(new String[out.size()]);
  }

  @Override
  public String[] getHeaders() {
    return new String[]{"#"+ String.join(T, HEADERS)};
  }

  @Override
  public void begin() {
    this.S = getPed().getSampleSize();
    depths = new int[S];
    depthPresent = new int[S];
    missings = new int[S];
    singleton = new int[S];
    tss = new int[S];
    tvs = new int[S];
    hets = new int[S];
    homAlts = new int[S];
    variants = new int[S];
    samples = getPed().getSamples();
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    int[] ltss = new int[S];
    int[] ltvs = new int[S];
    int[] lhomAlts = new int[S];
    int[] lvariants = new int[S];
    int[] lhets = new int[S];
    int[] ldepths = new int[S];
    int[] lsingleton = new int[S];
    boolean[] lmissings = new boolean[S];

    boolean[] ts = new boolean[variant.getAlleles().length];
    boolean[] tv = new boolean[variant.getAlleles().length];
    
    variant.recomputeACAN();
    int[] acs = variant.getAC();

    for (int a = 1; a < variant.getAlleles().length; a++) {
      ts[a] = variant.isTransition(a);
      tv[a] = variant.isTransversion(a);
    }

    for (int s = 0; s < S; s++) {
      Genotype geno = variant.getGenotype(samples.get(s));
      ldepths[s] = geno.getDP(); //changed to get the same results as vcftools and bcftools (take dp of missing genotype, but if dp itself is missing does not fall back on sumAD)
      if (geno.isMissing())
        lmissings[s] = true;
      else {
        //ldepths[s] = Math.max(geno.getSumAD(), geno.getDP()); //changed to get the same results as vcftools and bcftools
        if(geno.isHeterozygousDiploid())
          lhets[s]++;
        for (int a = 1; a < variant.getAlleles().length; a++)
          if (geno.hasAllele(a)) {
            lvariants[s]++;
            int ac = geno.getCount(a);
            if(ac == acs[a])
              lsingleton[s] ++;
            
            if (ts[a])
              ltss[s]++;
            else if (tv[a])
              ltvs[s]++;
            if (geno.isHomozygousOrHaploid())
              lhomAlts[s]++;
          }
      }
    }
    this.pushAnalysis(new Analysis(ltss, ltvs, lhomAlts, lvariants, lhets, ldepths, lsingleton, lmissings));
    return NO_OUTPUT;
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    if (analysis instanceof Analysis) {
      Analysis a = (Analysis) analysis;
      nbSites++;
      for (int s = 0; s < S; s++) {
        tss[s] += a.ltss[s];
        tvs[s] += a.ltvs[s];
        homAlts[s] += a.lhomAlts[s];
        variants[s] += a.lvariants[s];
        hets[s] += a.lhets[s];
        singleton[s] += a.lsingleton[s];
        if(a.ldepths[s] > -1){
          depths[s] += a.ldepths[s];
          depthPresent[s]++;
        }
        if(a.lmissings[s])
          missings[s]++;
      }
      return true;
    }
    return false;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }

  private class Analysis {

    private final int[] ltss, ltvs, lhomAlts, lvariants, lhets, ldepths, lsingleton;
    private final boolean[] lmissings;

    Analysis(int[] ltss, int[] ltvs, int[] lhomAlts, int[] lvariants, int[] lhets, int[] ldepths, int[] lsingleton, boolean[] lmissings) {
      this.ltss = ltss;
      this.ltvs = ltvs;
      this.lhomAlts = lhomAlts;
      this.lvariants = lvariants;
      this.lhets = lhets;
      this.ldepths = ldepths;
      this.lsingleton = lsingleton;
      this.lmissings = lmissings;
    }
  }
}
