package fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype;

import fr.inserm.u1078.tludwig.vcfprocessor.filters.GenotypeFilter;

/**
 * Filters Heterozygous Genotypes with V_AD/DP above of below a given threshold
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2022-04-25
 * Checked for release on XXXX-XX-XX
 * Unit Test defined on   XXXX-XX-XX
 */
public class GenotypeISKSVAFFilter extends GenotypeFilter {
  public static final String AD = "AD";
  public static final String DP = "DP";
  final double min;
  final double max;
  String[] format;
  int adPos = -1;
  int dpPos = -1;

  public GenotypeISKSVAFFilter(double min, double max) {
    super(true);
    this.min = min;
    this.max = max;
  }

  @Override
  public void setFormat(String[] format) {
    this.format = format;
    for (int i = 0; i < format.length; i++)
      if (DP.equalsIgnoreCase(this.format[i]))
        this.dpPos = i;
      else if (AD.equalsIgnoreCase(this.format[i]))
        this.adPos = i;
  }

  @Override
  public boolean pass(String t) {
    String[] geno = t.split(":");
    String g = geno[0];
    if(g.startsWith("."))
      return true;
    String[] alleles = g.replace('|', '/').split("\\/");
    int f = new Integer(alleles[0]);
    int s = new Integer(alleles[1]);
    if(f == s)
      return true;
    String[] ads = geno[adPos].split(",");
    double dp = new Integer(geno[dpPos]);
    if(f > 0 && s > 0){
      int ad = Math.max(new Integer(ads[f]), new Integer(ads[s]));
      double ratio = dp == 0 ? 0 : ad/dp;

      return (min <= ratio && ratio <= max);
    }

    int ad = new Integer(ads[f > 0 ? f : s]);
    double ratio = dp == 0 ? 0 : ad/dp;

    return (min <= ratio && ratio <= max);
  }

  @Override
  public String getDetails() {
    return this.min+" <= AD[Variant]/DP <= "+this.max;
  }

}
