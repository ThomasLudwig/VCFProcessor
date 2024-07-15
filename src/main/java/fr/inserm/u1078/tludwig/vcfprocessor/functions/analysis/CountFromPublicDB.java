package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Returns the number of Variants, SNVs, INDEL, in dbSNP, 1kG, GnomAD.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-01-22
 * Checked for release on 2020-05-27
 * Unit Test defined on   2020-07-07
 * Last Tested on         2020-08-14
 */
public class CountFromPublicDB extends ParallelVCFVariantFunction<CountFromPublicDB.CountAnalysis> {

  private int snp = 0;
  private int indel = 0;
  private int snp_1kg = 0;
  private int indel_1kg = 0;
  private int snp_db = 0;
  private int indel_db = 0;
  private int snp_gnomad = 0;
  private int indel_gnomad = 0;

  public static final String[] HEADER = {"Total","dbSNP","1kG","GnomAD","Not dbSNP","Not 1kG","Not GnomAD"};

  @Override
  public String getSummary() {
    return "Returns the number of Variants, SNVs, INDEL, in dbSNP, 1kG, GnomAD.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output format is (For all/SNVs/Indels):")
            .addColumns(HEADER);
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

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    int total = snp + indel;
    int total_db = snp_db + indel_db;
    int total_1kg = snp_1kg + indel_1kg;
    int total_exac = snp_gnomad + indel_gnomad;
    int snp_not_db = snp - snp_db;
    int indel_not_db = indel - indel_db;
    int snp_not_1kg = snp - snp_1kg;
    int indel_not_1kg = indel - indel_1kg;
    int snp_not_exac = snp - snp_gnomad;
    int indel_not_exac = indel - indel_gnomad;
    int total_not_db = snp_not_db + indel_not_db;
    int total_not_1kg = snp_not_1kg + indel_not_1kg;
    int total_not_exac = snp_not_exac + indel_not_exac;

    String ratio_total_db = StringTools.formatRatio(total_not_db, total, 3);
    String ratio_total_1kg = StringTools.formatRatio(total_not_1kg, total, 3);
    String ratio_total_exac = StringTools.formatRatio(total_not_exac, total, 3);

    String ratio_snp_db = StringTools.formatRatio(snp_not_db, snp, 3);
    String ratio_snp_1kg = StringTools.formatRatio(snp_not_1kg, snp, 3);
    String ratio_snp_exac = StringTools.formatRatio(snp_not_exac, snp, 3);

    String ratio_indel_db = StringTools.formatRatio(indel_not_db, indel, 3);
    String ratio_indel_1kg = StringTools.formatRatio(indel_not_1kg, indel, 3);
    String ratio_indel_exac = StringTools.formatRatio(indel_not_exac, indel, 3);
    
    return new String[]{
      "Total" + T + total + T + total_db + T + total_1kg + T + total_exac + T + total_not_db + "(" + ratio_total_db + ")" + T + total_not_1kg + "(" + ratio_total_1kg + ")" + T + total_not_exac + "(" + ratio_total_exac + ")",
      "SNP" + T + snp + T + snp_db + T + snp_1kg + T + snp_gnomad + T + snp_not_db + "(" + ratio_snp_db + ")" + T + snp_not_1kg + "(" + ratio_snp_1kg + ")" + T + snp_not_exac + "(" + ratio_snp_exac + ")",
      "INDEL" + T + indel + T + indel_db + T + indel_1kg + T + indel_gnomad + T + indel_not_db + "(" + ratio_indel_db + ")" + T + indel_not_1kg + "(" + ratio_indel_1kg + ")" + T + indel_not_exac + "(" + ratio_indel_exac + ")"
    };
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{T + String.join(T, HEADER)};
  }

  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }
    
  @Override
  public String[] processInputVariant(Variant variant) {
    for (int a = 1; a < variant.getAlleleCount(); a++)
      this.pushAnalysis(new CountAnalysis(variant.isSNP(a), variant.getInfo().isInDBSNPVEP(a), variant.getInfo().isIn1KgVEP(a), variant.getInfo().isInGnomADVEP(a)));
    return NO_OUTPUT;
  }
  
  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(CountFromPublicDB.CountAnalysis a) {
    if (a.isSNP) {
      snp++;
      if (a.isInDBSNP)
        snp_db++;
      if (a.isIn1KG)
        snp_1kg++;
      if (a.isInGnomAD)
        snp_gnomad++;
    } else {
      indel++;
      if (a.isInDBSNP)
        indel_db++;
      if (a.isIn1KG)
        indel_1kg++;
      if (a.isInGnomAD)
        indel_gnomad++;
    }
  }
  
  public static class CountAnalysis{
    private final boolean isSNP;
    private final boolean isInDBSNP;
    private final boolean isIn1KG;
    private final boolean isInGnomAD;

    CountAnalysis(boolean isSNP, boolean isInDBSNP, boolean isIn1KG, boolean isInGnomAD) {
      this.isSNP = isSNP;
      this.isInDBSNP = isInDBSNP;
      this.isIn1KG = isIn1KG;
      this.isInGnomAD = isInGnomAD;
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
