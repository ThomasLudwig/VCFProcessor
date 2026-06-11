package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Given a VCF file and a list of genes, prints the number of variants per gene for each consequence
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-06-28
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-09
 */
public class NumberOfCsqPerGene extends ParallelVCFVariantFunction<NumberOfCsqPerGene.Analysis> {

  /**
   * Index Genes (lines), Key (columns)
   * Inner Index VEPConsequence, Key (values : number of variants)
   */
  private TreeMap<String, TreeMap<VEPConsequence, Integer>> table;
  //private ArrayList<String> genes;
  

  private final FileParameter geneFile = new FileParameter(OPT_GENES, "genes.txt", "File listing genes");

  @Override
  public String getSummary() {
    return "Given a VCF file and a list of genes, prints the number of variants per gene for each consequence";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Multiallelic sites are considered for each alternate allele");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.IGNORE_STAR_ALLELE_AS_LINE); }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }
  
  @SuppressWarnings("unused")
  @Override
  public void begin() {
    table = new TreeMap<>();

    String gene;
    try (UniversalReader in = this.geneFile.getReader()){
      while ((gene = in.readLine()) != null) {
        TreeMap<VEPConsequence, Integer> columns = new TreeMap<>();
        for(VEPConsequence v : VEPConsequence.values())
          columns.put(v, 0);
        table.put(gene, columns);
      }
    } catch (IOException e) {
      Message.fatal("Could not read from gene list "+this.geneFile.getFilename(), e, true);
    }
  }

  @SuppressWarnings("unused")
  @Override
  public Println[] getFooters() {
    ArrayList<Println> out = new ArrayList<>();
    for (String gene : table.navigableKeySet()){
      Println line = new Println(gene);
      for (VEPConsequence v : VEPConsequence.values())
        line.append(T, table.get(gene).get(v));
      out.add(line);
    }
    return out.toArray(new Println[0]);
  }

  @SuppressWarnings("unused")
  @Override
  public Println[] getHeaders() {
    Println sb = new Println("Gene");
    for(VEPConsequence v: VEPConsequence.values())
      sb.append(T, v.getLevel(), ".",v.getName(),"(",v.getImpact().getName(),")");
    return new Println[]{sb};
  }

  @Override
  public Println[] processInputVariant(Variant variant) {
    for(int a : variant.getNonStarAltAllelesAsArray()){
      Map<String, VEPAnnotation> worsts = VEPConsequence.getWorstVEPAnnotationsByGene(variant.getInfo().getVEPInfo().getVEPAnnotations(a));
      for (String gene : worsts.keySet()) {
        if(table.containsKey(gene)){
          VEPConsequence csq = VEPConsequence.getWorstConsequence(worsts.get(gene));
          this.pushAnalysis(new Analysis(gene, csq));
        }
      }
    }
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(NumberOfCsqPerGene.Analysis analysis) {
    String gene = analysis.getGene();
    VEPConsequence v = analysis.getVep();
    if(v.getLevel() > VEPConsequence.EMPTY.getLevel()){
      TreeMap<VEPConsequence, Integer> columns = table.get(gene);
      columns.put(v, 1 + columns.get(v));
    }
  }

  public static class Analysis {
    private final String gene;
    private final VEPConsequence vep;

    public Analysis(String gene, VEPConsequence vep) {
      this.gene = gene;
      this.vep = vep;
    }

    public String getGene() {
      return gene;
    }

    public VEPConsequence getVep() {
      return vep;
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("genes", "genes");
    return new TestingScript[]{def};
  }
}
