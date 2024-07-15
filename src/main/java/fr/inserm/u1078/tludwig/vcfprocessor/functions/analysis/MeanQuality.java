package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Prints information and quality statistics for each variant.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-09-21
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-07
 */
public class MeanQuality extends ParallelVCFVariantFunction {

  int samples;

  public static final String[] HEADER = {"#CHROM","POS","IN_dbSBP","IN_GnomAD","meanDP_with_missing","meanGQ_with_missing","meanDP_without_missing","meanGQ_without_missing"};

  @Override
  public String getSummary() {
    return "Prints information and quality statistics for each variant.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("For each variant in the given vcf files. Prints :")
            .addColumns(HEADER);
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return true;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
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
    samples = getVCF().getSamples().size();
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T, HEADER)};
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    String[] outs = new String[variant.getAlleles().length-1];
    for (int a = 1; a < variant.getAlleles().length; a++) {
      int totalDP = 0;
      int totalGQ = 0;
      int known = 0;
      boolean inDbSNP = variant.getInfo().isInDBSNPVEP(a);
      boolean inGnomAD = variant.getInfo().isInGnomADVEP(a);

      for (Genotype genotype : variant.getGenotypes())
        if (!genotype.isMissing()) {          
          int dp = genotype.getDP();
          int gq = genotype.getGQ();          
          if(dp > -1 && gq > -1){
            known++;
            totalGQ += gq;
            totalDP += dp;
          }
        }

      String meanDPWith = StringTools.formatRatio(totalDP, known, 4);
      String meanGQWith = StringTools.formatRatio(totalGQ, known, 4);
      String meanDPWithout = StringTools.formatRatio(totalDP, samples, 4);
      String meanGQWithout = StringTools.formatRatio(totalGQ, samples, 4);
      outs[a-1] = variant.getChrom() + T + variant.getPos() + T + inDbSNP + T + inGnomAD + T + meanDPWith + T + meanGQWith + T + meanDPWithout + T + meanGQWithout;
    }
    return outs;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
