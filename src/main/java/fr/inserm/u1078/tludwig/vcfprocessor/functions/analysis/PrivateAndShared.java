package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * For the given VCF, gives the number of variants private to each group and shared among all groups.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             xxxx-xx-xx
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 */
public class PrivateAndShared extends ParallelVCFVariantPedFunction<PrivateAndShared.Analysis> {

  private ArrayList<String> groups;
  private int total = 0;
  private int shared = 0;
  private int[] priv;

  @Override
  public String getSummary() {
    return "For the given VCF, gives the number of variants private to each group and shared among all groups.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output :")
            .addItemize("The total number of variants in the file",
                "The number of variants present in ALL the groups defined in the the Ped file",
                "The number of variants private to each group");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return true;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_IGNORE_STAR_ALLELE_AS_LINE;
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
    priv = new int[groups.size()];
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    out.add("Total Number of Variants " + total);
    out.add("Variants shared among all groups " + shared);
    for (int g = 0; g < groups.size(); g++)
      out.add("Private to " + groups.get(g) + " " + priv[g]);
    
    return out.toArray(new String[0]);
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return null;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    for (int a : variant.getNonStarAltAllelesAsArray())
      if (!variant.getInfo().isInDBSNPVEP(a)) {
        ArrayList<Integer> gps = new ArrayList<>();
        boolean isShared = false;
        boolean[] found = new boolean[groups.size()]; //initialized to false
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
        this.pushAnalysis(new Analysis(isShared, gps));
      }
    return NO_OUTPUT;
  }
  
  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Analysis analysis) {
    boolean isShared = analysis.isShared();
    ArrayList<Integer> gps  = analysis.getGps();
    total++;
    if(isShared)
      shared++;
    for(int i : gps)
      priv[i]++;
  }

  public static class Analysis {
    private final boolean shared;
    private final ArrayList<Integer> gps;

    public Analysis(boolean shared, ArrayList<Integer> gps) {
      this.shared = shared;
      this.gps = gps;
    }

    public boolean isShared() {
      return shared;
    }

    public ArrayList<Integer> getGps() {
      return gps;
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }
}
