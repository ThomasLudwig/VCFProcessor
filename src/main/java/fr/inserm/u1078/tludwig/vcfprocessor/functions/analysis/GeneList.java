package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Prints the list of all gene covered by the VCF file
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-08-03
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-07
 */
public class GeneList extends ParallelVCFVariantFunction {

  TreeSet<String> genes;

  @Override
  public String getSummary() {
    return "Prints the list of all gene covered by the VCF file";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary()).addLine("The genes are extracted from the SYMBOL annotation from VEP.");
  }

  @Override
  public boolean needVEP() {
    return true;
  }

  @Override
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TXT;
  }

  @Override
  public void begin() {
    genes = new TreeSet<>();
  }

  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    out.add(genes.size() + " genes found");
    for (String gene : genes)
      out.add(gene);
    return out.toArray(new String[out.size()]);
  }

  @Override
  public String[] getHeaders() {
    return null;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    pushAnalysis(variant.getGeneList());
    return NO_OUTPUT;
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    if(analysis instanceof String[]){
      for(String gene : (String[])analysis)
        genes.add(gene);
      return true;
    }
    return false;
  }  
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
