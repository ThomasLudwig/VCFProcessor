package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Print Stats about each sample (Mean Depths, TS/TV Het/HomAlt).
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-11-23
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 */
public class SampleStats extends ParallelVCFVariantPedFunction<SampleStats.Analysis> {

  private int S;
  private int nbSites = 0;
  private int[] depths;
  private int[] depthPresent;
  private int[] missings;
  private int[] singletons;
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
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }
  @Override
  public String[] getHeaders() {
    return new String[]{"#"+ String.join(T, HEADERS)};
  }

  @Override
  public void begin() {
    S = getVCF().getSamples().size();
    depths = new int[S];
    depthPresent = new int[S];
    missings = new int[S];
    singletons = new int[S];
    tss = new int[S];
    tvs = new int[S];
    hets = new int[S];
    homAlts = new int[S];
    variants = new int[S];
    samples = getPed().getSamples();
  }

  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    for (int s = 0; s < S; s++) {
      Sample sample = this.samples.get(s);
      int genotyped = nbSites - this.missings[s];
      String[] values = {sample.getId(),
          sample.getGroup(),
          nbSites+"",
          genotyped+"",
          this.missings[s]+"",
          StringTools.formatRatio(100*this.missings[s], nbSites, 4)+"%",
          StringTools.formatRatio(this.depths[s],this.depthPresent[s],4),
          this.variants[s]+"",
          this.singletons[s]+"",
          this.tss[s]+"",
          this.tvs[s]+"",
          this.tvs[s] != 0 ? StringTools.formatRatio(this.tss[s], tvs[s], 4) : "0",
          this.hets[s]+"",
          genotyped != 0 ? StringTools.formatRatio(this.hets[s], genotyped, 4) : "0",
          this.homAlts[s]+""};

      out.add(String.join(T, values));
    }
    return out.toArray(new String[0]);
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    int[] lTransitions = new int[S];
    int[] lTransversions = new int[S];
    int[] lHomAlts = new int[S];
    int[] lVariants = new int[S];
    int[] lHets = new int[S];
    int[] lDepths = new int[S];
    int[] lSingletons = new int[S];
    boolean[] lMissings = new boolean[S];

    int[] nonStar = variant.getNonStarAltAllelesAsArray();
    boolean[] ts = new boolean[1+nonStar.length];
    boolean[] tv = new boolean[1+nonStar.length];
    
    variant.recomputeACAN();
    int[] acs = variant.getAC();

    for (int a : nonStar) {
      ts[a] = variant.isTransition(a);
      tv[a] = variant.isTransversion(a);
    }

    for (int s = 0; s < S; s++) {
      Sample sample = samples.get(s);
      Genotype geno = variant.getGenotype(sample);
      if(geno == null) {
        Message.fatal("No genotype found for [" + samples.get(s).getId() + "] " + variant.shortString(), true);
        return new String[0];
      }
      lDepths[s] = geno.getDP(); //changed to get the same results as vcftools and bcftools (take dp of missing genotype, but if dp itself is missing does not fall back on sumAD)
      if (geno.isMissing())
        lMissings[s] = true;
      else {
        //lDepths[s] = Math.max(geno.getSumAD(), geno.getDP()); //changed to get the same results as vcftools and bcftools
        if(geno.isHeterozygousDiploid())
          lHets[s]++;
        for (int a : nonStar)
          if (geno.hasAllele(a)) {
            lVariants[s]++;
            int ac = geno.getCount(a);
            if(ac == acs[a])
              lSingletons[s] ++;
            
            if (ts[a])
              lTransitions[s]++;
            else if (tv[a])
              lTransversions[s]++;
            if (geno.isHomozygousOrHaploid())
              lHomAlts[s]++;
          }
      }
    }
    this.pushAnalysis(new Analysis(lTransitions, lTransversions, lHomAlts, lVariants, lHets, lDepths, lSingletons, lMissings));
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Analysis a) {
    nbSites++;
    for (int s = 0; s < S; s++) {
      tss[s] += a.lTransitions[s];
      tvs[s] += a.lTransversions[s];
      homAlts[s] += a.lHomAlts[s];
      variants[s] += a.lVariants[s];
      hets[s] += a.lHets[s];
      singletons[s] += a.lSingletons[s];
      if(a.lDepths[s] > -1){
        depths[s] += a.lDepths[s];
        depthPresent[s]++;
      }
      if(a.lMissings[s])
        missings[s]++;
    }
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }

  public static class Analysis {

    private final int[] lTransitions, lTransversions, lHomAlts, lVariants, lHets, lDepths, lSingletons;
    private final boolean[] lMissings;

    Analysis(int[] lTransitions, int[] lTransversions, int[] lHomAlts, int[] lVariants, int[] lHets, int[] lDepths, int[] lSingletons, boolean[] lMissings) {
      this.lTransitions = lTransitions;
      this.lTransversions = lTransversions;
      this.lHomAlts = lHomAlts;
      this.lVariants = lVariants;
      this.lHets = lHets;
      this.lDepths = lDepths;
      this.lSingletons = lSingletons;
      this.lMissings = lMissings;
    }
  }
}
