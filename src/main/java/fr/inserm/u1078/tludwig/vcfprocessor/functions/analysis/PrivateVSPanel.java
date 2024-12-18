package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.VCFFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Check how many of the variants from the input file are filtered as Already_existing when adding samples from the reference file
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-10-20
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-09-23
 */
public class PrivateVSPanel extends VCFFunction { //TODO parallelize the reading of the largest file (probably the panel)
  //TODO develop a Graph for these results
  private final VCFFileParameter refFile = new VCFFileParameter(OPT_REF, "reference.vcf", "the panel VCF File (can be gzipped)");

  private static final int C = VEPConsequence.values().length;
  private static final int CHROM_NB = 26;
  @SuppressWarnings("unchecked")
  private final ArrayList<MiniVariant>[] variants = new ArrayList[CHROM_NB];
  private final ArrayList<Integer> sampleIndices = new ArrayList<>();
  private int[][] count;
  private int N;
  
  private final int[] total = new int[C];
  @Override
  public String getSummary() {
    return "Check how many of the variants from the input file are filtered as Already_existing when adding samples from the reference file.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Takes all the variants in the given vcfFile")
            .addLine("Compares to each samples from the refFile")
            .addLine("Gives a count of remaining (new) variants (by consequence) each time we add a sample.");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return true;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_IGNORE_STAR_ALLELE_AS_LINE;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    for (int i = 0; i < CHROM_NB; i++)
      this.variants[i] = new ArrayList<>();
    loadVariants();
    browseReference();
    printResults();
  }

  private void loadVariants() throws Exception {    
    VCF vcf = this.vcfFile.getVCF(VCF.MODE_QUICK_GENOTYPING, 10000);
    vcf.getReaderAndStart();
    Message.info("Loading VCF");
    Variant variant;
    while ((variant = vcf.getUnparallelizedNextVariant()) != null) {
      int chrom = variant.getChromNumber();
      for (int a : variant.getNonStarAltAllelesAsArray()) {
        this.variants[chrom].add(new MiniVariant(variant, a));
        for (int level : variant.getInfo().getConsequenceLevels(a))
          total[level]++;
      }
    }
    vcf.close();
  }

  private void printResults() {
    Message.info("Exporting results");
    for (int c = 0; c < C; c++) {
      StringBuilder line = new StringBuilder(Objects.requireNonNull(VEPConsequence.getConsequence(c)).getName());
      int remaining = total[c];
      line.append(T).append(remaining);
      for (int n = 0; n < N; n++) {
        remaining -= count[c][n];
        line.append(T).append(remaining);
      }
      println(line.toString());
    }
  }

  private void browseReference() throws Exception {    
    VCF ref = this.refFile.getVCF(VCF.MODE_QUICK_GENOTYPING, 10000);
    N = ref.getPed().getSampleSize();
    this.count = new int[C][N];
    
    int n = N / 2; //TODO ???
    for (int i = 0; i <= n; i++) {
      this.sampleIndices.add(i);
      int o = N - (i + 1);
      if (o > i)
        this.sampleIndices.add(o);
    }
    
    ref.getReaderAndStart();
    ArrayList<Sample> samples = new ArrayList<>(ref.getSortedSamples());
    
    Message.info("Processing  Ref VCF for " + N + " samples");

    Variant variant;
    while ((variant = ref.getUnparallelizedNextVariant()) != null) {
      int chrom = variant.getChromNumber();

      for (int a : variant.getNonStarAltAllelesAsArray()) {
        int index = this.variants[chrom].indexOf(new MiniVariant(variant, a));
        if (index != -1) {
          MiniVariant found = this.variants[chrom].get(index);

          for (int s = 0; s < N; s++) {
            Sample sample = samples.get(this.sampleIndices.get(s));
            Genotype g = variant.getGenotype(sample);
            if (g.hasAllele(a)) {
              for (int level : found.consequences)
                count[level][s]++;
              break;
            }
          }
        }
      }
    }

    ref.close();
  }

  private static class MiniVariant {

    private final int chrom;
    private final int position;
    private final String ref;
    private final String alt;
    private final ArrayList<Integer> consequences;

    MiniVariant(Variant variant, int allele) {
      this.chrom = variant.getChromNumber();
      this.position = variant.getPos();
      this.ref = variant.getRef();
      this.alt = variant.getAlleles()[allele];
      this.consequences = variant.getInfo().getConsequenceLevels(allele);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.chrom, this.position, this.ref, this.alt, this.consequences);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof MiniVariant) {
        MiniVariant var = (MiniVariant) obj;
        return this.chrom == var.chrom
                && this.position == var.position
                && this.ref.equals(var.ref)
                && this.alt.equals(var.alt);
      }
      return false;
    }

    @Override
    public String toString() {
      return this.chrom + T + this.position + T + this.ref + this.alt;
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript ts = TestingScript.newFileAnalysis();
    ts.addAnonymousFilename("vcf", "vcf");
    ts.addAnonymousFilename("ref", "ref");    
    return new TestingScript[]{ts};
  }
}
