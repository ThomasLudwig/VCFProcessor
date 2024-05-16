package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Counts the number of variants for each Sample and print a summary for each group
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-12-02
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-14
 */
public class CountVariants extends ParallelVCFVariantPedFunction {

  private ArrayList<String> groups;
  private ArrayList<Sample>[] samples;
  private ArrayList<int[]> counts; //TODO, a single tab for the counts int[] and in HashMap<,Integer> to get the index of the samples ?

  final String[] HEADER = {"FamilyID","ID","MotherID","FatherID","Sex","Phenotype","Group","NbVariants"};
  
  @Override
  public String getSummary() {
    return "Counts the number of variants for each Samples and print a summary for each group";
  }
  
  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Results Format :")
            .addColumns(HEADER);
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
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
    groups = getPed().getGroups();
    counts = new ArrayList<>();
    samples = getPed().getSamplesByGroup();
    for (String group : groups)
      counts.add(new int[getPed().getGroupSize(group)]);
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    NumberSeries total = new NumberSeries("Total", SortedList.Strategy.SORT_AFTERWARDS);
    ArrayList<NumberSeries> series = new ArrayList<>();
    for (int ig = 0; ig < groups.size(); ig++) {
      NumberSeries cGroup = new NumberSeries(this.groups.get(ig), SortedList.Strategy.SORT_AFTERWARDS);
      series.add(cGroup);
      for (int is = 0; is < this.samples[ig].size(); is++) {
        int value = this.counts.get(ig)[is];
        out.add(this.samples[ig].get(is) + T + value);
        total.add(value);
        cGroup.add(value);
      }
    }

    for (NumberSeries serie : series)
      out.add(serie.getQuartileStats());
    out.add(total.getQuartileStats());
    return out.toArray(new String[0]);
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    for (Genotype g : variant.getGenotypes()) {
      Sample s = g.getSample();
      String group = s.getGroup();
      int ig = groups.indexOf(group);
      int is = samples[ig].indexOf(s);
      for (int a = 1; a < variant.getAlleles().length; a++)
        if (g.hasAllele(a))
          this.pushAnalysis(new int[]{ig, is});
          //this.counts.get(ig)[is]++;
    }
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    try {
      int[] idx = (int[])analysis;
      this.counts.get(idx[0])[idx[1]]++;
      return true;
    } catch (Exception ignore) { }
    return false;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T, HEADER)};
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }
}
