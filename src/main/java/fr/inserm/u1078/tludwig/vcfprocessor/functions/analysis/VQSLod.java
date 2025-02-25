package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.HashMap;

/**
 * Print VQSLod statistics for each tranche.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-07-11
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 */
public class VQSLod extends ParallelVCFFunction<VQSLod.Analysis> {

  private static final String VQSLOD = "VQSLOD=";
  public static final String[] HEADER = {"Tranche","Mean","Min","D1","D2","D3","D4","Median","D6","D7","D8","D9","Max"};
  private HashMap<String, NumberSeries> tranches;

  @Override
  public String getSummary() {
    return "Print VQSLod statistics for each tranche.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output format is :")
            .addColumns(HEADER);
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return "File must contain VQSLOD annotations";
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    tranches = new HashMap<>();
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T, HEADER)};
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    String[] ret = new String[tranches.size()];
    int i = 0;
    for (NumberSeries tranche : tranches.values()) {
      ret[i] = getStats(tranche);
      i++;
    }
    return ret;
  }

  private String getStats(NumberSeries tranche) {
    StringBuilder out = new StringBuilder(tranche.getName());
    out.append(T).append(tranche.getMean());
    for (double d = 0; d <= 1; d += .1)
      out.append(T).append(tranche.getPercentile(d));
    return out.toString();
  }

  @Override
  public String[] processInputRecord(VariantRecord record) {
    String tr = record.getFiltersString();
    String[][] info = record.getInfo();
    for(String[] kv : info)
      if(kv[0].equals(VQSLOD))
        this.pushAnalysis(new Analysis(tr, Double.parseDouble(kv[1])));
    return new String[]{};
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Analysis analysis) {
      String tranche = analysis.getTranche();
      double vqslod = analysis.getVqslod();
      NumberSeries ns = tranches.get(tranche);
      if (ns == null) {
        ns = new NumberSeries(tranche, SortedList.Strategy.ADD_INSERT_SORT);
        tranches.put(tranche, ns);
      }
      ns.add(vqslod);
  }

  public static class Analysis {
    private final String tranche;
    private final double vqslod;

    public Analysis(String tranche, double vqslod) {
      this.tranche = tranche;
      this.vqslod = vqslod;
    }

    public String getTranche() {
      return tranche;
    }

    public double getVqslod() {
      return vqslod;
    }
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
