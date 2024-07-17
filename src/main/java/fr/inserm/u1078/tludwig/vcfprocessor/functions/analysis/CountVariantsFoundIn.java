package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CountVariantsFoundIn extends ParallelVCFVariantFunction<CountVariantsFoundIn.Analysis> {
  final FileParameter referenceFiles = new FileParameter(OPT_REF, "gnomad.2.1.canonical", "File containing list of files with List of variants found in the reference file (in canonical format)");
  public static final int IS_SNP = 1;
  public static final int IS_SINGLETON = 2;
  public static final int IS_IN_REFERENCE = 4;

  /**
   * Variants found in the reference
   */
  final HashMap<Integer, HashSet<String>> referenceVariants = new HashMap<>();
  /**
   * The counts for each chrom/sample/type
   */
  private int[][][] counts;
  /**
   * The samples found in the VCF
   */
  private String[] samples;

  @Override
  public String getSummary() {
    return "Count the variants of each type for each samples, and globally";
  }

  @Override
  public Description getDesc() {
    return new Description("Variants are filtered than, the count is made by category");
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
    return MULTIALLELIC_IGNORE_STAR_ALLELE_AS_LINE;
  }

  private void loadReferenceVariants() {
    Message.info("Listing reference files from "+referenceFiles.getFilename());
    try (UniversalReader in = this.referenceFiles.getReader()) {
      String line;
      while((line = in.readLine()) != null)
        if(!line.isEmpty())
          this.loadReferenceVariants(line);
    } catch(IOException e) {
      Message.fatal("Could not read reference file ["+referenceFiles+"]", e, true);
    }
  }

  private void loadReferenceVariants(String referenceFile) {

    Message.info("Loading known variants from "+referenceFile);
    int read = 0;
    try (UniversalReader in = new UniversalReader(referenceFile)) {
      String line = in.readLine();
      while((line = in.readLine()) != null) {
        read++;
        String canon = line.split(T)[0];
        int chrom = Integer.parseInt(canon.split(":")[0]);
        HashSet<String> variants = referenceVariants.computeIfAbsent(chrom, ignored -> new HashSet<>());

        variants.add(canon);
        if(read % 100000 == 0)
          Message.progressInfo("Known variants in ["+referenceFile+"] loaded : "+read);
      }
    } catch(IOException e) {
      Message.fatal("Could not read reference file ["+referenceFile+"]", e, true);
    }
    Message.info("Known variants in ["+referenceFile+"] loaded : "+read);
  }

  @Override
  public String[] getHeaders() {
    final String T0 = "INDEL_NOTSINGLETON_ABSENT";
    final String T1 = "SNP_NOTSINGLETON_ABSENT";
    final String T2 = "INDEL_SINGLETON_ABSENT";
    final String T3 = "SNP_SINGLETON_ABSENT";
    final String T4 = "INDEL_NOTSINGLETON_PRESENT";
    final String T5 = "SNP_NOTSINGLETON_PRESENT";
    final String T6 = "INDEL_SINGLETON_PRESENT";
    final String T7 = "SNP_SINGLETON_PRESENT";
    return new String[]{String.join(T, "#CHROM", "SAMPLE", T0, T1, T2, T3, T4, T5, T6, T7)};
  }

  @Override
  public void begin() {
    try {
      super.begin();
      this.loadReferenceVariants();
      int nbSamples = this.getVCF().getSamples().size();
      samples = new String[nbSamples];
      int i = 0;
      for(Sample sample : this.getVCF().getSamples())
        samples[i++] = sample.getId();
      this.counts = new int[24][nbSamples + 1][1 + IS_SNP + IS_SINGLETON + IS_IN_REFERENCE]; //chrom, samples, type
    } catch(Exception e) {
      Message.fatal("Could not start analysis", e, true);
    }
  }

  @Override
  public void end() {
    super.end();

    for(int chr = 0; chr < 24; chr++){
      String chrName = (chr+1)+"";
      if(chr == 22) chrName = "X";
      if(chr == 23) chrName = "Y";
      for(int s = 0 ; s <= samples.length; s++) {
        String sample = s < samples.length ? samples[s] : "GLOBAL";
        StringBuilder out = new StringBuilder(chrName).append(T).append(sample);
        for(int t = 0 ; t <= IS_SNP + IS_SINGLETON + IS_IN_REFERENCE; t++)
          out.append(T).append(counts[chr][s][t]);
        System.out.println(out);
      }
    }
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    if(isKept(variant)) {
      for(int a : variant.getNonStarAltAllelesAsArray()) {
        Canonical canonical = variant.getCanonical(a);
        int chrom = canonical.getChr();
        int type = 0;
        if (canonical.isSNP())
          type += IS_SNP;
        HashSet<String> variants = referenceVariants.get(chrom);
        if (variants != null && variants.contains(canonical.toString()))
          type += IS_IN_REFERENCE;
        Genotype[] genotypes = variant.getGenotypes();
        boolean[] present = new boolean[genotypes.length];

        int count = 0;
        for (int i = 0; i < genotypes.length; i++) {
          present[i] = genotypes[i].hasAllele(a);
          if (canonical.toString().equals("3:101586571:1:TGTGTGTGTGAGAGAGAGAGAGAGAGAGAGTTTCTTGTTTCA"))
            Message.debug(i + " " + present[i] + " " + genotypes[i].getAlleles()[0] + "/" + genotypes[i].getAlleles()[1]);
          if (present[i])
            count++;
        }

        if (count == 1) {
          type += IS_SINGLETON;
        }

        if (canonical.toString().equals("3:101586571:1:TGTGTGTGTGAGAGAGAGAGAGAGAGAGAGTTTCTTGTTTCA"))
          Message.debug(canonical + " (allele[" + a + "]) " + new Analysis(chrom, type, present));

        if (count > 0)
          pushAnalysis(new Analysis(chrom, type, present));
      }
    }

    return new String[0];
  }

  private boolean isKept(Variant variant){
    return "PASS".equals(variant.getFilter());
  }

  @Override
  public void processAnalysis(Analysis analysis) {
    super.processAnalysis(analysis);
    this.counts[analysis.getChrom()][this.samples.length][analysis.getType()]++;
    for(int i = 0 ; i < analysis.getPresent().length; i++)
      if(analysis.getPresent()[i])
        this.counts[analysis.getChrom()][i][analysis.getType()]++;
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  public static class Analysis {
    private final int chrom;
    private final int type;
    private final boolean[] present;

    public Analysis(int chrom, int type, boolean[] present) {
      this.chrom = chrom - 1;
      this.type = type;
      this.present = present;
    }

    public int getChrom() {
      return chrom;
    }

    public int getType() {
      return type;
    }

    public boolean[] getPresent() {
      return present;
    }

    @Override
    public String toString() {
      ArrayList<String> samples = new ArrayList<>();
      for(int i = 1 ; i < present.length; i++)
        if(present[i-1])
          samples.add(i+"");
      return "chr"+(chrom+1)+":"+type+"{"+String.join(",", samples)+"}";
    }
  }
}
