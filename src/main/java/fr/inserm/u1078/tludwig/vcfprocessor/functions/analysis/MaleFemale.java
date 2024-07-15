package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.Map;

/**
 * Shows Male/Female Allele Frequencies
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2020-12-15
 * Checked for release on 2020-12-15 (1.0.4)
 * Unit Test defined on   XXXX-XX-XX
 */

public class MaleFemale extends ParallelVCFVariantPedFunction {

  private static final String[] HEADERS = {
          "CHROM",
          "POS",
          "ID",
          "REF",
          "ALT",
          "FILTER",
          "GENE/CSQ",
          "AF",
          "MALE_AF",
          "FEMALE_AF"
  };

  @Override
  public String getSummary() {
    return "Show Male/Female Allele Frequencies";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output format:")
            .addColumns(HEADERS);
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return true;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T, HEADERS)};
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    String[] ret = new String[variant.getAlleleCount() - 1];

    double[] an = {0,0,0};
    int[][] ac = new int[3][variant.getAlleleCount()];
    double[][] af = new double[3][variant.getAlleleCount()];

    for(Genotype geno : variant.getGenotypes()){
      int sex = geno.getSample().getSex();
      if(!geno.isMissing()){
        for(int al : geno.getAlleles()){
          an[sex]++;
          ac[sex][al]++;
        }
      }
    }

    double anT =  an[0] + an[1] + an[2];
    int[] acT = new int[variant.getAlleleCount()];
    double[] afT = new double[variant.getAlleleCount()];
    for(int a = 0 ; a < variant.getAlleleCount(); a++) {
      acT[a] = ac[0][a] + ac[1][a] + ac[2][a];
      afT[a] = acT[a] / anT;
      for(int s = 0; s < 3; s++)
        af[s][a] = ac[s][a] / an[s];
    }

    for(int a = 1 ; a < variant.getAlleleCount(); a++){
      Map<String, VEPAnnotation> geneCsqs = variant.getInfo().getWorstVEPAnnotationsByGene(a);
      StringBuilder geneCsq = new StringBuilder();
      for(String gene : geneCsqs.keySet()){
        geneCsq.append(",").append(gene).append(":").append(VEPConsequence.getWorstConsequence(geneCsqs.get(gene).getConsequence()));
      }

      if(geneCsq.length() > 0)
        geneCsq = new StringBuilder(geneCsq.substring(1));

      ret[a - 1] = String.join(T
          , variant.getChrom()
          , variant.getPos()+""
          , variant.getId()
          , variant.getRef()
          , variant.getAllele(a)
          , variant.getFilter()
          , geneCsq
          , afT[a]+""
          , af[Ped.SEX_MALE][a]+""
          , af[Ped.SEX_FEMALE][a]+""
      );
    }

    return ret;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty();
  }
}
