package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.EnumParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.concurrent.atomic.AtomicInteger;

public class CommonVariantsInSamplePairs extends ParallelVCFVariantFunction<CommonVariantsInSamplePairs.Count> {
  private EnumParameter processRef = new EnumParameter(OPT_REF, new String[]{"false", "true", "minor"}, "minor", "Also Process REF allele [0] ?");

  private static final int HETHETP = 0;
  private static final int HETHETS = 1;
  private static final int HETHOMP = 2;
  private static final int HETHOMS = 3;
  private static final int HOMHETP = 4;
  private static final int HOMHETS = 5;
  private static final int HOMHOMP = 6;
  private static final int HOMHOMS = 7;
  private static final int TOTAL = 8;

  private int S;
  private int P;
  private String[] SAMPLES;
  private AtomicInteger[][] counts;
  private int startAllele = 1;
  private boolean isMinor = false;

  @Override
  public String getSummary() {
    return "Count variants that are common to each sample pairs";
  }

  @Override
  public Description getDesc() {
    return new Description(getSummary()).addLine("For each pair of samples in the file (A vs B, but not B vs A), output :").addItemize(
        "number of variants A_het & B_het absent in other samples" ,
        "number of variants A_het & B_het present in other samples" ,
        "number of variants A_het & B_hom absent in other samples" ,
        "number of variants A_het & B_hom present in other samples" ,
        "number of variants A_hom & B_het absent in other samples" ,
        "number of variants A_hom & B_het present in other samples" ,
        "number of variants A_hom & B_hom absent in other samples" ,
        "number of variants A_hom & B_hom present in other samples" ,
        "number of variants common to A and B in any form");
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public boolean needVEP() {
    return false;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @Override
  public void begin() {
    super.begin();
    if(this.processRef.getStringValue().equals("true"))
      this.startAllele = 0;
    isMinor = this.processRef.getStringValue().equals("minor");

    this.S = this.getVCF().getNumberOfSamples();
    this.P = (S * (S - 1)) / 2;

    this.SAMPLES = new String[S];
    int s = 0;
    for(Sample sample : this.getVCF().getSortedSamples())
      SAMPLES[s++] = sample.getId();
    this.counts = new AtomicInteger[P][TOTAL + 1];
    for(int i = 0 ; i < this.counts.length; i++)
      for(int j = 0; j < this.counts[i].length; j++)
        this.counts[i][j] = new AtomicInteger(0);
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    //initialize analysis (count)
    Count count = new Count(S);
    int A = variant.getAlleleCount();
    int[] ACs = new int[A];

    //get genotypes for all samples
    int[][] genotypes = new int[S][2];
    for(int s = 0 ; s < S ; s++) {
      genotypes[s][0] = -1;
      genotypes[s][1] = -1;
      Genotype geno = variant.getGenotypes()[s];
      if(geno != null) {
        int[] als = geno.getAlleles();
        if(als != null) {
          genotypes[s][0] = geno.getAlleles()[0];
          genotypes[s][1] = geno.getAlleles()[1];
          if(genotypes[s][0] != -1)
            ACs[genotypes[s][0]]++;
          if(genotypes[s][1] != -1)
            ACs[genotypes[s][1]]++;
        }
      }
    }

    //Counts number of samples barring the variants
    int[] nbSamples = new int[A];
    for(int a = 0 ; a < A; a++)
      for(int s = 0; s < S; s++)
        if(genotypes[s][0] == a || genotypes[s][1] == a)
          nbSamples[a]++;

    int i = 0;
    //analyse genotype pairs
    for(int sa = 0; sa < S - 1; sa++)
      for (int sb = sa + 1; sb < S; sb++, i++)
        //for each pair, // if genotype is not missing
        if (genotypes[sa][0] != -1 && genotypes[sb][0] != -1) {
          if(isMinor){
           int minor = -1;
           int minorC = -1;
           for(int a = 0; a < A; a++)
             if(minorC < ACs[a]){
               minorC = ACs[a];
               minor = a;
             }

           for(int a = 0 ; a < A; a++)
             if(a != minor)
               count.push(i, nbSamples[a] == 2, genotypes[sa], genotypes[sb], a);
            else
               Message.debug("Minor = "+a);
          } else {
            for (int a = startAllele; a < A; a++)
              count.push(i, nbSamples[a] == 2, genotypes[sa], genotypes[sb], a);
          }
        }

    this.pushAnalysis(count);
    return NO_OUTPUT;
  }

  private static boolean homo(int[] g, int a){
    return g[0] == a && g[1] == a;
  }

  private static boolean het(int[] g, int a){
    return (g[0] == a && g[1] != a) || (g[0] != a && g[1] == a);
  }

  @Override
  public void processAnalysis(Count analysis) {
    for(int p = 0; p < P; p++)
      for(int t = 0; t <= TOTAL; t++)
        this.counts[p][t].addAndGet(analysis.cnt[p][t]);
  }

  @Override
  public String[] getHeaders() {
    return new String[]{String.join(
        T,
        "sampleA",
        "sampleB",
        "hethetPrivate",
        "hethetShared",
        "hethomPrivate",
        "hethomShared",
        "homhetPrivate",
        "homhetShared",
        "homhomPrivate",
        "homhomShared",
        "total"
    )};
  }

  @Override
  public String[] getFooters() {
    String[] footers = new String[P];
    int i = 0;
    for(int a = 0; a < S - 1; a++)
      for (int b = a + 1; b < S; b++, i++)
        footers[i] =
            SAMPLES[a]
                +T+SAMPLES[b]
                +T+counts[i][HETHETP]
                +T+counts[i][HETHETS]
                +T+counts[i][HETHOMP]
                +T+counts[i][HETHOMS]
                +T+counts[i][HOMHETP]
                +T+counts[i][HOMHETS]
                +T+counts[i][HOMHOMP]
                +T+counts[i][HOMHOMS]
                +T+counts[i][TOTAL];
    return footers;
  }


  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  public static class Count {
    private final int[][] cnt;

    public Count(int S){
      cnt = new int[S * (S - 1) / 2][TOTAL + 1];
    }

    public void push(int index, int type){
      cnt[index][type]++;
      cnt[index][TOTAL]++;
    }

    public void push(int index, boolean priv, int[] ga, int[] gb, int a){
      if (het(ga, a)) {
        if (het(gb, a))
          push(index, priv ? HETHETP : HETHETS);
        else if (homo(gb, a))
          push(index, priv ? HETHOMP : HETHOMS);
      } else if (homo(ga, a)) {
        if (het(gb, a))
          push(index, priv ? HOMHETP : HOMHETS);
        else if (homo(gb, a))
          push(index, priv ? HOMHOMP : HOMHOMS);
      }
    }
  }
}
