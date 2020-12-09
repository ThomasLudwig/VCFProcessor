package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Prints the Allele frequency in the file and each group, for variants not found in dbSNP, 1kG or GnomAD.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-11-16
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-07
 */
public class FrequencyForPrivate extends ParallelVCFVariantPedFunction {

  private int G;

  @Override
  public String getSummary() {
    return "Prints the Allele frequency in the file and each group, for variants not found in dbSNP, 1kG or GnomAD.";
  }

  @Override
  public Description getDesc() {
    return new Description("For each variant in the file, if the variant is not found in dbSNP, 1KG or GnomAD :")
            .addLine("Prints the frequency in the file, and in each group, as well as its consequence");
  }

  @Override
  public boolean needVEP() {
    return true;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public void begin() {
    G = getPed().getGroups().size();
  }

  @Override
  public String[] getHeaders() {
    String out = "CHROM" + T + "POS" + T + "REF" + T + "ALT" + T + "Frq_Total";
    for (int i = 0; i < G; i++)
      out += T + "Frq_" + getPed().getGroups().get(i);
    out += T + "Consequences";
    return new String[]{out};
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    ArrayList<String> outs = new ArrayList<>();
    for (int a = 1; a < variant.getAlleles().length; a++)
      if (!variant.getInfo().isIn1KgVEP(a) && !variant.getInfo().isInDBSNPVEP(a) && !variant.getInfo().isInGnomADVEP(a)) {
        int[] countByGroup = new int[G];
        int[] totalInGroup = new int[G];
        int count = 0;
        int total = 0;

        for (Genotype genotype : variant.getGenotypes()) {
          if(!genotype.isMissing()){
            int idx = getPed().getGroups().indexOf(genotype.getSample().getGroup());
            total += genotype.getNbChrom();
            totalInGroup[idx] += genotype.getNbChrom();
            int t = genotype.getCount(a);
            if (t > 0) {
              count += t;              
              countByGroup[idx] += t;
            }
          }
        }

        LineBuilder out = new LineBuilder(variant.getChrom());
        out.addColumn(variant.getPos());
        out.addColumn(variant.getRef());
        out.addColumn(variant.getAlleles()[a]);
        out.addColumn(count / (1d * total));
        for (int g = 0; g < G; g++)
          out.addColumn(countByGroup[g] / (1d * totalInGroup[g]));
        out.addColumn(String.join(",", variant.getInfo().getConsequencesSplitted(a)));
        outs.add(out.toString());
      }
    return outs.toArray(new String[outs.size()]);
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }
}
