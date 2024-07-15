package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Extracts All Variants that are private to a Group.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-01-04
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-15
 * Last Tested on         2020-08-14
 */
public class ExtractPrivateToGroup extends ParallelVCFVariantPedFunction {
  private static final String[] HEADER = {"#CHR","POS","GROUP","SAMPLES"};

  @Override
  public String getSummary() {
    return "Extracts All Variants that are private to a Group.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Only the variants found in a single group of samples (as defined in the ped file) are extracted")
            .addLine("The list of the N samples in the group that have the variant is given")
            .addLine("Output Format :")
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
  public String[] getHeaders() {
    return new String[]{String.join(T,HEADER)};
  }

  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }  

  @Override
  public String[] processInputVariant(Variant variant) {
    ArrayList<String> outs = new ArrayList<>();
    for (int a = 1; a < variant.getAlleleCount(); a++) {
      ArrayList<String> groups = variant.getGroupsWithAllele(a);
      if (groups.size() == 1) {
        String out = variant.getChrom() + T + variant.getPos() + T + groups.get(0);
        StringBuilder samples = new StringBuilder();
        for (Genotype g : variant.getGenotypes())
          if (g.hasAllele(a))
            samples.append(",").append(g.getSample().getId());
        out += T + samples.substring(1);
        outs.add(out);
      }
    }
    return outs.toArray(new String[0]);
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }
}
