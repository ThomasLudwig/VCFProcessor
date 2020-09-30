package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * For the given VCF, gives the number of variants private to each group and shared amoung all groups.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             xxxx-xx-xx
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 */
public class PrivateAndShared extends ParallelVCFVariantPedFunction {

  private ArrayList<String> groups;
  private int total = 0;
  private int shared = 0;
  private int[] priv;

  @Override
  public String getSummary() {
    return "For the given VCF, gives the number of variants private to each group and shared amoung all groups.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output :")
            .addItemize(new String[]{
              "The total number of variants in the file",
              "The number of variants present in ALL the groups defined in the the Ped file",
              "The number of variants private to each group"});
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
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public void begin() {
    groups = getPed().getGroups();
    priv = new int[groups.size()];
  }

  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    out.add("Total Number of Variants " + total);
    out.add("Variants shared amoung all groups " + shared);
    for (int g = 0; g < groups.size(); g++)
      out.add("Private to " + groups.get(g) + " " + priv[g]);
    
    return out.toArray(new String[out.size()]);
  }

  @Override
  public String[] getHeaders() {
    return null;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    for (int a = 1; a < variant.getAlleles().length; a++)
      if (!variant.getInfo().isInDBSNPVEP(a)) {
        ArrayList<Integer> gps = new ArrayList<>();
        boolean isShared = false;
        boolean[] found = new boolean[groups.size()];
        for (int i = 0; i < found.length; i++)
          found[i] = false;
        int nbG = 0;

        for (Genotype g : variant.getGenotypes())
          if (g.hasAllele(a)) {
            String group = g.getSample().getGroup();
            found[groups.indexOf(group)] = true;
          }

        for (boolean f : found)
          if (f)
            nbG++;

        if (nbG == groups.size())
          isShared = true;
        if (nbG == 1)
          for (int i = 0; i < groups.size(); i++)
            if (found[i])
              gps.add(i);
        this.pushAnalysis(new Object[]{isShared, gps});
      }
    return NO_OUTPUT;
  }
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    try {
      boolean isShared = (Boolean)((Object[])analysis)[0];
      ArrayList<Integer> gps  = (ArrayList)((Object[])analysis)[1];
      total++;
      if(isShared)
        shared++;
      for(int i : gps)
        priv[i]++;
      return true;
    } catch (Exception e) {
    }
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }
}
