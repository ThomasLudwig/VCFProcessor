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
import java.util.HashMap;

/**
 * Given a VCF file and a list of genes, prints the number of variants per gene for each consequence
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-06-28
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-09
 */
public class NumberOfCsqPerGene extends ParallelVCFVariantFunction {
  
  
  private HashMap<String, Integer> table;
  private ArrayList<String> genes;
  

  private final FileParameter geneFile = new FileParameter(OPT_GENES, "genes.txt", "File listing genes");

  @Override
  public String getSummary() {
    return "Given a VCF file and a list of genes, prints the number of variants per gene for each consequence";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Multiallelic sites are concidered for each alternate allele");
  }

  @Override
  public boolean needVEP() {
    return true;
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
  
  private static String key(String gene, VEPConsequence v){
    return gene+"+"+v.getName();
  }

  @Override
  public void begin() {
    table = new HashMap<>();
    genes = new ArrayList<>();
    
    String gene;
    try {
      UniversalReader in = this.geneFile.getReader();
      while ((gene = in.readLine()) != null) {
        genes.add(gene);
        for(VEPConsequence v : VEPConsequence.values())
          table.put(key(gene,v), 0);
      }
      in.close();
    } catch (IOException e) {
      this.fatalAndDie("Could not read from gene list "+this.geneFile.getFilename(), e);
    }
  }

  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    for (String gene : genes){
      LineBuilder line = new LineBuilder(gene);
      for (VEPConsequence v : VEPConsequence.values()) {
        line.addColumn(table.get(key(gene,v)));
      }
      out.add(line.toString());
    }
    return out.toArray(new String[out.size()]);
  }

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
      HashMap<String, VEPAnnotation>  worsts = variant.getInfo().getWorstVEPAnnotationsByGene(a);
      for (String gene : worsts.keySet()) {
        if(genes.contains(gene)){
          VEPConsequence csq = VEPConsequence.getWorstConsequence(worsts.get(gene));
          this.pushAnalysis(new Object[]{gene, csq});
        }
      }
    }
    return NO_OUTPUT;
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    try {
      String gene = (String)((Object[])analysis)[0];
      VEPConsequence v = (VEPConsequence)((Object[])analysis)[1];
      if(v.getLevel() > VEPConsequence.EMPTY.getLevel()){
        String key = key(gene,v);
        table.put(key, 1+table.get(key));
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
