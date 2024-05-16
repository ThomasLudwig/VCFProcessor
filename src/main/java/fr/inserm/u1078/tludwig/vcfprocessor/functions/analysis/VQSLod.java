package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
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
public class VQSLod extends ParallelVCFFunction {

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
  public String[] processInputLine(String line) {
    String[] f = line.split(T);
    String tr = f[VCF.IDX_FILTER];
    String info = f[VCF.IDX_INFO];
    int idx = info.indexOf(VQSLOD);
    if (idx != -1) {
      String v = info.substring(idx + VQSLOD.length()).split(";")[0];
      this.pushAnalysis(new Object[]{tr, Double.parseDouble(v)});
    }
    return new String[]{};
  }

  @SuppressWarnings("unused")
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    if (analysis instanceof Object[]) {
      String tranche = (String) ((Object[]) analysis)[0];
      Double vqslod = (Double) ((Object[]) analysis)[1];
      NumberSeries ns = tranches.get(tranche);
      if (ns == null) {
        ns = new NumberSeries(tranche, SortedList.Strategy.ADD_INSERT_SORT);
        tranches.put(tranche, ns);
      }
      ns.add(vqslod);
      return true;
    } 
    return false;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
