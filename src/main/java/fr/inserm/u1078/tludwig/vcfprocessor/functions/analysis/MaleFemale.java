package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.ArrayList;
import java.util.HashMap;

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

  @Override
  public boolean needVEP() {
    return true;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

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
      HashMap<String, VEPAnnotation> geneCsqs = variant.getInfo().getWorstVEPAnnotationsByGene(a);
      String geneCsq = "";
      for(String gene : geneCsqs.keySet()){
        geneCsq += "," + gene + ":" + VEPConsequence.getWorstConsequence(geneCsqs.get(gene).getConsequence());
      }

      if(geneCsq.length() > 0)
        geneCsq = geneCsq.substring(1);

      StringBuilder sb = new StringBuilder(variant.getChrom());
      sb.append(T).append(variant.getPos());
      sb.append(T).append(variant.getId());
      sb.append(T).append(variant.getRef());
      sb.append(T).append(variant.getAllele(a));
      sb.append(T).append(variant.getFilter());
      sb.append(T).append(geneCsq);
      sb.append(T).append(afT[a]);
      sb.append(T).append(af[Ped.SEX_MALE][a]);
      sb.append(T).append(af[Ped.SEX_FEMALE][a]);

      ret[a - 1] = sb.toString();
    }

    return ret;
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty();
  }
}
