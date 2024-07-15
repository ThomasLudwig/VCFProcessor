package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.tools.MathTools;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Performs a chi² Association Tests on an input VCF file
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-03-13
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-14
 */
public class Chi2 extends ParallelVCFVariantPedFunction<Chi2.Chi2Analysis> {
  
  private int[] allelesCases;
  private int[] allelesControls;

  @Override
  public String getSummary() {
    return "Performs a chi² Association Tests on an input VCF file";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Does a simple association test on the data present in the input vcf file.")
            .addLine("Computes the number of case samples with and without variants, and the number of control samples with and without variants.")
            .addLine("then does a chi² on those values");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
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
    allelesCases = new int[getPed().getCases().size()];
    allelesControls = new int[getPed().getControls().size()];
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return null;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    int maxCase = MathTools.min(allelesCases);
    int maxControls = MathTools.max(allelesControls);
    int max = Math.max(maxCase, maxControls);

    if (max > 0) {
      int[][] input = new int[2][max + 1];
      for (int cas : allelesCases)
        input[0][cas]++;
      for (int ctrl : allelesControls)
        input[1][ctrl]++;

      out.add("#" + T + "Cases" + T + "Controls");
      for (int i = 0; i <= max; i++)
        out.add(i + T + input[0][i] + T + input[1][i]);

      ArrayList<Integer> nonEmpty = new ArrayList<>();
      for (int i = 0; i <= max; i++)
        if (input[0][i] + input[1][i] > 0)
          nonEmpty.add(i);

      if (nonEmpty.size() > 1) {
        long[][] merged = new long[2][nonEmpty.size()];

        out.add("\nNon Empty Categories");
        out.add("#" + T + "Cases" + T + "Controls");
        for (int i = 0; i < nonEmpty.size(); i++) {
          int idx = nonEmpty.get(i);
          merged[0][i] = input[0][idx];
          merged[1][i] = input[1][idx];
          out.add(idx + T + merged[0][i] + T + merged[1][i]);
        }

        ChiSquareTest chi2 = new ChiSquareTest();
        double chi = chi2.chiSquare(merged);
        double pvalue = chi2.chiSquareTest(merged);

        out.add("Chi2 = [" + chi + "] pvalue=[" + pvalue + "]");

        long[][] simple = new long[2][2];
        simple[0][0] = input[0][0];
        simple[1][0] = input[1][0];
        simple[0][1] = input[0][1];
        simple[1][1] = input[1][1];
        for (int i = 2; i <= max; i++) {
          simple[0][1] += input[0][i];
          simple[1][1] += input[1][i];
        }

        out.add("Variants" + T + "Cases" + T + "Controls");
        out.add("Absent" + T + simple[0][0] + T + simple[1][0]);
        out.add("Present" + T + simple[0][1] + T + simple[1][1]);

        if (simple[0][0] == 0 && simple[1][0] == 0)
          out.add("Neither case nor control have samples without variants");
        else if (simple[0][1] == 0 && simple[1][1] == 0)
          out.add("Neither case nor control have samples with variants");
        else {
          ChiSquareTest chi2PA = new ChiSquareTest();
          double chiPA = chi2PA.chiSquare(simple);
          double pvaluePA = chi2PA.chiSquareTest(simple);
          out.add("Chi2 = [" + chiPA + "] pvalue=[" + pvaluePA + "]");
        }
      } else
        out.add("Not enough categories with non empty data");
    }
    return out.toArray(new String[0]);
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    int i = 0;
    for (Sample sample : getPed().getCases()) {
      int[] alleles = variant.getGenotype(sample).getAlleles();
      if(alleles != null)
        for(int a : alleles)
          if(a > 0)
            this.pushAnalysis(new Chi2Analysis(i, true));
      i++;
    }
    i = 0;
    for (Sample sample : getPed().getControls()) {
      int[] alleles = variant.getGenotype(sample).getAlleles();
      if(alleles != null)
        for(int a : alleles)
          if(a > 0)
            this.pushAnalysis(new Chi2Analysis(i, false));
      i++;
    }
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Chi2Analysis analysis) {
    int i = analysis.getI();
    boolean cas = analysis.isCas();
    if(cas)
      allelesCases[i]++;
    else
      allelesControls[i]++;
  }

  public static class Chi2Analysis {
    private final int i;
    private final boolean cas;

    public Chi2Analysis(int i, boolean cas) {
      this.i = i;
      this.cas = cas;
    }

    public int getI() {
      return i;
    }

    public boolean isCas() {
      return cas;
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }
}
