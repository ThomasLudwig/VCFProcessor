package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
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
  private final FileParameter variantList = new FileParameter(OPT_FILE, "RAVAQ/run.step4.RARE.genefile.txt", "List of variants and their genes");
  private final HashMap<Canonical, String> variantNames = new HashMap<>();
  private final HashMap<Canonical, String> genesByVariants = new HashMap<>();
  private final SortedList<String> geneList = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_INSERT_SORT);
  private ArrayList<String> groups = new ArrayList<>();
  private HashMap<String, ArrayList<String>> results = new HashMap<>();
  private final AtomicInteger counter = new AtomicInteger(0);

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
    this.groups = this.getPed().getGroups();
    this.results = new HashMap<>();
    try {
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
    } catch(IOException e){
      Message.fatal("Unable to read file ["+variantList.getFilename()+"]", true);
    }
    Message.info("Loaded " + geneList.size() + " genes from " + variantNames.size() + " variants");
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
    return new String[] {String.join(T, new String[]{"Gene","Variant","Genotypes"})};
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
          HashMap<String, StringBuilder> samples = new HashMap<>();
          HashMap<String, Integer> counts = new HashMap<>();
          for(String group : this.groups) {
            for(String type : new String[]{"HET", "HOM"}) {
              String geno = type + "." + group;
              samples.put(geno, new StringBuilder());
              counts.put(geno, 0);
            }
          }

          //compute

          for(Genotype genotype : variant.getGenotypes()) {
            if(genotype.hasAllele(a)) {
              Sample sample = genotype.getSample();
              String geno = (genotype.isHomozygousOrHaploid() ? "HOM" : "HET")+"." + sample.getGroup();
              samples.get(geno).append(",").append(sample.getId());
              counts.put(geno, counts.get(geno) + 1);
            }
          }

          //concat
          StringBuilder genoString = new StringBuilder(variantName);
          for(String group : this.groups) {
            for(String type : new String[]{"HET", "HOM"}) {
              String geno = type+"."+group;
              StringBuilder sb = samples.get(geno);
              genoString.append(T).append(geno).append("(").append(counts.get(geno)).append(")").append(T);
              if (sb.length() > 0)
                genoString.append(sb.substring(1));
            }
          }
          this.pushAnalysis(new VariantAnalysis(gene, genoString.toString()));
        }
      }
    }
    return NO_OUTPUT;
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
