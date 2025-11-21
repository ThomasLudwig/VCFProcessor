package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.format.PrepareGnomADFile;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RAVAQOutput extends ParallelVCFVariantPedFunction<RAVAQOutput.VariantAnalysis> {
  public static final int IDX_HET = 0;
  public static final int IDX_HOM = 1;
  private final FileParameter variantList = new FileParameter(OPT_FILE, "RAVAQ/run.step4.RARE.genefile.txt", "List of variants and their genes");
  private final TSVFileParameter gnomadFiles = new TSVFileParameter(OPT_FILES, "gnomad.list", "A list of gnomAD file, created with "+ PrepareGnomADFile.class.getSimpleName()+" the 1st column is exome/genome, the 2nd is the chrom(as CHR17) and the 3rd is the filename");
  private final StringParameter gnomadSubpop = new StringParameter(OPT_POP, "NFE", "A gnomAD Population");
  private final HashMap<Canonical, String> variantNames = new HashMap<>();
  private final HashMap<Canonical, String> genesByVariants = new HashMap<>();
  private final SortedList<String> geneList = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_INSERT_SORT);
  private final ArrayList<String> groups = new ArrayList<>();
  private final ArrayList<Double> ANS = new ArrayList<>();
  private HashMap<String, ArrayList<String>> results = new HashMap<>();
  private final AtomicInteger counter = new AtomicInteger(0);

  private final HashMap<Canonical, PrepareGnomADFile.GnomAD> gnomadExome = new HashMap<>();
  private final HashMap<Canonical, PrepareGnomADFile.GnomAD> gnomadGenome = new HashMap<>();

  @Override
  public String getSummary() {
    return "List contributing variants/samples for each gene";
  }

  @Override
  public Description getDesc() {
    return new Description(getSummary());
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
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_IGNORE_STAR_ALLELE_AS_LINE;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public void begin() {
    super.begin();

    //init group names and size
    for (String group : this.getPed().getGroups()) {
      this.groups.add(group);
      this.ANS.add(2.0 * this.getPed().getGroupSize(group));
    }

    //load rare results
    try { loadResults(); }
    catch(IOException e){ Message.fatal("Unable to read file ["+variantList.getFilename()+"]", true); }

    //load gnomAD
    try { loadGnomAD(); }
    catch(IOException e){ Message.fatal("Unable to read gnomAD files", e, true); }
  }

  private void loadResults() throws IOException {
    this.results = new HashMap<>();
    final UniversalReader in = variantList.getReader();
    in.readLine();
    String line;
    while ((line = in.readLine()) != null) {
      final String[] f = line.split("\t");
      final String variant = f[0];
      final String[] genes = f[1].split(",");
      final String[] tokens = variant.split(":");
      final Canonical canonical = new Canonical(tokens[0], Integer.parseInt(tokens[1]), tokens[2], tokens[3]);
      this.variantNames.put(canonical, variant);
      this.genesByVariants.put(canonical, f[1]);
      for(String gene : genes)
        if(!this.geneList.contains(gene))
          this.geneList.add(gene);
      if(variantNames.size()%10000 == 0)
        Message.progressInfo("Loaded " + geneList.size() + " genes from " + variantNames.size() + " variants");
    }
    in.close();
    Message.info("Loaded " + geneList.size() + " genes from " + variantNames.size() + " variants");
  }

  private void loadGnomAD() throws IOException{
    UniversalReader in = gnomadFiles.getReader();
    String line;
    while ((line = in.readLine()) != null) {
      final String[] f = line.split(T, -1);
      String type = f[0].toLowerCase();
      //String chr = f[1];
      String filename = f[2];
      switch (type) {
        case "exomes":
        case "exome" : loadGnomAD(filename, gnomadExome); break;
        case "genomes":
        case "genome" : loadGnomAD(filename, gnomadGenome); break;
        default: System.err.println("Unknown GnomAD type ["+type+"]");
      }
    }
    in.close();
  }

  private void loadGnomAD(String filename, HashMap<Canonical, PrepareGnomADFile.GnomAD> gnomad) throws IOException {
    UniversalReader in = new UniversalReader(filename);
    String line;
    int count = 0;
    int kept = 0;
    while((line = in.readLine()) != null){
      count++;
      if(count%10000 == 0)
        Message.info("Loaded " + kept+"/"+count + " GnomAD variants from " + filename);
      PrepareGnomADFile.GnomAD gnomAD = new PrepareGnomADFile.GnomAD(line);
      if(variantNames.containsKey(gnomAD.getCanonical())) {
        gnomad.put(gnomAD.getCanonical(), gnomAD);
        kept++;
      }
    }
    Message.info("Loaded " + kept+"/"+count + " GnomAD variants from " + filename);
    in.close();
  }

  @Override
  public void end() {
    super.end();
    Message.info("VCF done");

    Message.info("Genes existing ["+geneList.size()+"] genes with variants ["+results.size()+"] processed variants ["+counter+"]");

    for(String gene : geneList) {
      ArrayList<String> outVariant = this.results.get(gene);
      if(outVariant != null)
        for (String var : outVariant)
          System.out.println(gene + T + var);
    }
  }

  @Override
  public String[] getHeaders() {
    StringBuilder h = new StringBuilder("Gene");
    h.append(T).append("Variant");
    for(String group : this.groups) {
      h.append(T).append("HET_").append(group);
      h.append(T).append("HET_SAMPLES_").append(group);
      h.append(T).append("HOM_").append(group);
      h.append(T).append("HOM_SAMPLES_").append(group);
      h.append(T).append(group).append("_AF");
    }
    h.append(T).append("gnomAD_GENOME_AF");
    h.append(T).append("gnomAD_").append(gnomadSubpop.getStringValue().toUpperCase()).append("_GENOME_AF");
    h.append(T).append("gnomAD_GENOME_FILTERS");
    h.append(T).append("gnomAD_EXOME_AF");
    h.append(T).append("gnomAD_").append(gnomadSubpop.getStringValue().toUpperCase()).append("_GENOME_AF");
    h.append(T).append("gnomAD_EXOME_FILTERS");
    h.append(T).append("gnomAD_MAX_AF");
    h.append(T).append("gnomAD_FILTERS");
    return new String[] {h.toString()};
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    for(int a = 1 ; a < variant.getAlleleCount(); a++) {
      final Canonical canonical = variant.getCanonical(a);
      final String genes = this.genesByVariants.get(canonical);
      if(genes != null) {
        this.counter.incrementAndGet();
        final String variantName = this.variantNames.get(canonical);
        for(String gene : genes.split(",")) {
          //init
          ArrayList<String>[][] samples = new ArrayList[groups.size()][2]; // [group][HET|HOM]
          for(int i = 0; i < groups.size(); i++)
            for(int j = 0; j < 2; j++)
              samples[i][j] = new ArrayList<>();

          //compute
          for (Genotype genotype : variant.getGenotypes())
            if (genotype.hasAllele(a)) {
              Sample sample = genotype.getSample();
              int group = groups.indexOf(sample.getGroup());
              int geno = genotype.isHomozygousOrHaploid() ? IDX_HOM : IDX_HET;
              samples[group][geno].add(sample.getId());
            }

          //compupte af = (HET + 2 * HOM)/AN
          double[] afs = new double[groups.size()];
          for(int i = 0; i < groups.size(); i++)
            afs[i] = (samples[i][IDX_HET].size() + 2 * samples[i][IDX_HOM].size()) / ANS.get(i);

          //concat

          StringBuilder genoString = new StringBuilder(variantName);
          for (int i = 0; i < groups.size(); i++) {
            for (int j : new int[]{IDX_HET, IDX_HOM}) {
              //add number of samples
              genoString.append(T).append(samples[i][j].size());
              //add sample list
              genoString.append(T).append(String.join(",", samples[i][j]));
            }
            //add frequencies
            genoString.append(T).append(afs[i]);
          }

          //add gnomAD info
          for(String gnomAD : getGnomADInfo(canonical))
            genoString.append(T).append(gnomAD);

          this.pushAnalysis(new VariantAnalysis(gene, genoString.toString()));
        }
      }
    }
    return NO_OUTPUT;
  }

  private String[] getGnomADInfo(Canonical canonical) {
    String[] ret = {"", "", "", "", "", "", "0", ""};
    PrepareGnomADFile.GnomAD genome = gnomadGenome.get(canonical);
    PrepareGnomADFile.GnomAD exome = gnomadExome.get(canonical);
    if(genome != null) {
      ret[0] = genome.getAF();
      ret[1] = genome.getAF(gnomadSubpop.getStringValue());
      ret[2] = genome.getFilter();
    }
    if(exome != null) {
      ret[3] = exome.getAF();
      ret[4] = exome.getAF(gnomadSubpop.getStringValue());
      ret[5] = exome.getFilter();
    }

    ret[6] = maxAF(ret[0],ret[1],ret[3],ret[4]);
    ret[7] = combine(ret[2], ret[5]);

    return ret;
  }

  private String maxAF(String... s){
    double max = 0;
    for(String ss : s)
      try {
        double f = Double.parseDouble(ss);
        if(f > max) max = f;
      } catch (NumberFormatException ignore) {}
    return ""+max;
  }

  public String combine(String...s){
    ArrayList<String> ret = new ArrayList<>();
    for(String ss : s)
      for(String f : ss.split(";"))
        if(!ret.contains(f) && !"PASS".equals(f))
          ret.add(f);
    return String.join(";", ret);
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  private synchronized void processSyncAnalysis(VariantAnalysis analysis){
    ArrayList<String> variants = this.results.computeIfAbsent(analysis.getGene(), k -> new ArrayList<>());
    variants.add(analysis.getGenoString());
  }

  @Override
  public void processAnalysis(VariantAnalysis analysis) {
    this.processSyncAnalysis(analysis);
  }

  public static class VariantAnalysis {
    private final String gene;
    private final String genoString;

    public VariantAnalysis(String gene, String genoString) {
      this.gene = gene;
      this.genoString = genoString;
    }

    public String getGene() { return gene; }

    public String getGenoString() { return genoString; }
  }
}
