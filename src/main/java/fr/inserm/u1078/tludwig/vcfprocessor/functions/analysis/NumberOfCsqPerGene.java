package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
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
public class NumberOfCsqPerGene extends ParallelVCFVariantFunction {

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
  public boolean needVEP() {
    return true;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
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
  public void begin() {
    table = new TreeMap<>();

    String gene;
    try {
      UniversalReader in = this.geneFile.getReader();
      while ((gene = in.readLine()) != null) {
        TreeMap<VEPConsequence, Integer> columns = new TreeMap<>();
        for(VEPConsequence v : VEPConsequence.values())
          columns.put(v, 0);
        table.put(gene, columns);
      }
      in.close();
    } catch (IOException e) {
      this.fatalAndQuit("Could not read from gene list "+this.geneFile.getFilename(), e);
    }
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    for (String gene : table.navigableKeySet()){
      LineBuilder line = new LineBuilder(gene);
      for (VEPConsequence v : VEPConsequence.values())
        line.addColumn(table.get(gene).get(v));
      out.add(line.toString());
    }
    return out.toArray(new String[0]);
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    LineBuilder sb = new LineBuilder("Gene");
    for(VEPConsequence v: VEPConsequence.values())
      sb.addColumn()
        .append(v.getLevel())
        .append(".")
        .append(v.getName())
        .append("(")
        .append(v.getImpact().getName())
        .append(")");
    return new String[]{sb.toString()};
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    for(int a = 1 ; a < variant.getAlleleCount(); a++){
      Map<String, VEPAnnotation> worsts = variant.getInfo().getWorstVEPAnnotationsByGene(a);
      for (String gene : worsts.keySet()) {
        if(table.containsKey(gene)){
          VEPConsequence csq = VEPConsequence.getWorstConsequence(worsts.get(gene));
          this.pushAnalysis(new Object[]{gene, csq});
        }
      }
    }
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    try {
      String gene = (String)((Object[])analysis)[0];
      VEPConsequence v = (VEPConsequence)((Object[])analysis)[1];
      if(v.getLevel() > VEPConsequence.EMPTY.getLevel()){
        TreeMap<VEPConsequence, Integer> columns = table.get(gene);
        columns.put(v, 1 + columns.get(v));
      }
      return true;
    } catch (Exception e) {
      //Ignore
    }
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("genes", "genes");
    return new TestingScript[]{def};
  }
}
