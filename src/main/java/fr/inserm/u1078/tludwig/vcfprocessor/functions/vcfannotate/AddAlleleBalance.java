package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcfannotate;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.MathTools;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
  * Adds the annotations : AB, ABHET, ABHOM, OND to a VCF file
  * 
  * @author Thomas E. Ludwig (INSERM - U1078) 
  * Started on             2018-03-13
  * Checked for release on 2020-05-25
  * Unit Test defined on   2020-08-05
 */
public class AddAlleleBalance extends ParallelVCFVariantFunction<Object> {

  public static final String ABHET = "ABHet";
  public static final String ABHOM = "ABHom";
  public static final String ABHET_PA = "ABHetPA";
  public static final String ABHOM_PA = "ABHomPA";
  public static final String OND = "OND";
  public static final String AB = "AB";

  public static final String[] HEADERS = {
    "##FORMAT=<ID="+AB+",Number=1,Type=Float,Description=\"Allele balance for each het genotype\">",
    "##INFO=<ID="+ABHET+",Number=1,Type=Float,Description=\"Allele Balance for heterozygous calls (ref/(ref+alt))\">",
    "##INFO=<ID="+ABHOM+",Number=1,Type=Float,Description=\"Allele Balance for homozygous calls (A/(A+O)) where A is the allele (ref or alt) and O is anything other\">",
    "##INFO=<ID="+ ABHET_PA +",Number=1,Type=Float,Description=\""+ABHET+" Per Allele (ref, alt1, alt2,...,altN)\">",
    "##INFO=<ID="+ ABHOM_PA +",Number=1,Type=Float,Description=\""+ABHOM+" Per Allele (ref, alt1, alt2,...,altN)\">",
    "##INFO=<ID="+OND+",Number=1,Type=Float,Description=\"Overall non-diploid ratio (non-alleles/(alleles+non-alleles))\">"};

  @SuppressWarnings("unused")
  @Override
  public String[] getExtraHeaders() {
    return HEADERS;
  }

  @Override
  public String getSummary() {
    return "Adds the annotations : AB, ABhet, ABhom, OND to a VCF file";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Adds the following annotations :")
            .addItemize(Description.bold("AB")+" : Allele balance for each het genotype (alleleDepth(gt1) / alleleDepth(gt1) + alleleDepth(gt2))",
              Description.bold("ABhet")+" : Allele Balance for heterozygous calls (ref/(ref+alt)), for each variant",
              Description.bold("ABhom")+" : Allele Balance for homozygous calls (A/(A+O)) where A is the allele (ref or alt) and O is anything other, for each variant",
              Description.bold("OND")+" : Overall non-diploid ratio (alleles/(alleles+non-alleles)), for each variant"
            )
            .addLine("Algorithms is taken from GATK, with the following changes (Results are available for INDELs and multiallelic variants, use with caution)");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    double numHom = 0;
    double denomHom = 0;
    double numHet = 0;
    double denomHet = 0;
    double numOND = 0;
    double denomOND = 0;
    
    double[] numHoms = new double[variant.getAlleleCount()];
    double[] denomHoms = new double[variant.getAlleleCount()];
    double[] numHets = new double[variant.getAlleleCount()];
    double[] denomHets = new double[variant.getAlleleCount()];

    variant.getFormat().addField(AB);
    boolean hasHom = false;
    boolean hasHet = false;
    for (Genotype genotype : variant.getGenotypes()) {
      String ab = ".";
      final int[] ad = genotype.getAD();
      if(genotype.getNbChrom() == 2){
        int gt1 = genotype.getAlleles()[0];
        int gt2 = genotype.getAlleles()[1];
        if (ad != null) {
          final long totalReads = MathTools.sum(ad);
          if (genotype.isHeterozygousDiploid()) {
            hasHet = true;
            ab = StringTools.formatDouble((double) ad[gt1] / (ad[gt1] + ad[gt2]), 3);

            numHet += ad[Math.min(gt1,gt2)]; //min is to use the same allele for each samples in case of phased data
            denomHet += ad[gt1] + ad[gt2];

            numHets[gt1] += ad[gt1];
            denomHets[gt1] += ad[gt1] + ad[gt2];          
            numHets[gt2] += ad[gt2];
            denomHets[gt2] += ad[gt1] + ad[gt2];

            numOND += totalReads - (ad[gt1] + ad[gt2]);
            denomOND += totalReads;
          } else if (genotype.isHomozygousOrHaploid()) {
            hasHom = true;

            numHom += ad[gt1];
            denomHom += totalReads;

            numHoms[gt1] += ad[gt1];
            denomHoms[gt1] += totalReads;

            numOND += totalReads - ad[gt1];
            denomOND += totalReads;
          }
        }
      }
      genotype.addField(ab);
    }

    if (hasHet)
      variant.addInfo(ABHET + "=" + StringTools.formatDouble(numHet / denomHet, 3));
    if (hasHom)
      variant.addInfo(ABHOM + "=" + StringTools.formatDouble(numHom / denomHom, 3));
    if (hasHom || hasHet)
      variant.addInfo(OND + "=" + StringTools.formatDouble(numOND / denomOND, 4));
    
    if (hasHet)
      variant.addInfo(ABHET_PA + "=" + getABString(variant, denomHets, numHets));
    if (hasHom)
      variant.addInfo(ABHOM_PA + "=" + getABString(variant, denomHoms, numHoms));

    return asOutput(variant);
  }

  private static String getABString(Variant variant, double[] denom, double[] num){
    StringBuilder ret = new StringBuilder();
    for(int i = 0 ; i < variant.getAlleleCount(); i++)
      ret.append(",").append((denom[i] == 0) ? "." : StringTools.formatDouble(num[i] / denom[i], 3));
    return ret.substring(1);
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}