package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

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
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    samples = getVCF().getNumberOfSamples();
  }

  @SuppressWarnings("unused")
  @Override
  public Println[] getHeaders() {
    return new Println[]{Println.join(T, HEADER)};
  }

  @Override
  public Println[] processInputVariant(Variant variant) {
    Println[] outs = new Println[variant.getAlleles().length-1];
    for (int a = 1; a < variant.getAlleles().length; a++) {
      int totalDP = 0;
      int totalGQ = 0;
      int known = 0;
      boolean inDbSNP = variant.getInfo().getVEPInfo().hasExistingVariants(a);
      boolean inGnomAD = variant.getInfo().getVEPInfo().isInGnomAD(a);

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

      double meanDPWith = known == 0 ? 0 : totalDP * 1d / known;
      double meanGQWith = known == 0 ? 0 : totalGQ * 1d / known;
      double meanDPWithout = samples  == 0 ? 0 : totalDP * 1d / samples;
      double meanGQWithout = samples  == 0 ? 0 : totalGQ * 1d / samples;
      outs[a-1] = Println.join(T, variant.getChrom(), variant.getPos(), inDbSNP, inGnomAD, meanDPWith, meanGQWith, meanDPWithout, meanGQWithout);
    }
    return outs;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
