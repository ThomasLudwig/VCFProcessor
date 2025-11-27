package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

/**
 * Prints the list of all gene covered by the VCF file
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-08-03
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-07
 */
public class GeneList extends ParallelVCFVariantFunction<String[]> {

  TreeSet<String> genes;

  @Override
  public String getSummary() {
    return "Prints the list of all gene covered by the VCF file";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary()).addLine("The genes are extracted from the SYMBOL annotation from VEP.");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.ALLELE_AS_LINE); }

  @Override
  public String getOutputExtension() {
    return OUT_TXT;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    genes = new TreeSet<>();
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    out.add(genes.size() + " genes found");
    out.addAll(genes);
    return out.toArray(new String[0]);
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return null;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    pushAnalysis(variant.getGeneList());
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(String[] analysis) {
    Collections.addAll(genes, analysis);
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
