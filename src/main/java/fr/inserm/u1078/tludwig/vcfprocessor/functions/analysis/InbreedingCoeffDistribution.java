package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Outputs a sorted list of all Inbreeding Coeff from a VCF File.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2017-09-08
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-07
 */
public class InbreedingCoeffDistribution extends ParallelVCFVariantFunction<Double> {
  NumberSeries coeffs;
  
  @Override
  public String getSummary() {
    return "Outputs a sorted list of all Inbreeding Coeff from a VCF File.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("The output file has no header, the values are sorted ascendingly");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return new VCFPolicies(VCFPolicies.MultiAllelicPolicy.NA, false, "Input file must contains Inbreeding Coeff. annotation");}

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    coeffs = new NumberSeries("Coeffs", SortedList.Strategy.SORT_AFTERWARDS);
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return null;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    if(coeffs.isEmpty())
      return super.getFooters();
    ArrayList<String> out = new ArrayList<>();
    for (Double coeff : coeffs.getAllValues())
      out.add(""+coeff);
    return out.toArray(new String[0]);
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    Double coeff = variant.getInfo().getInbreedingCoeff();
    if (coeff != null)
      pushAnalysis(coeff);
    return NO_OUTPUT;
  }  

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Double analysis) {
    coeffs.add(analysis);
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
