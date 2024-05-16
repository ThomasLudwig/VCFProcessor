package fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype;

import fr.inserm.u1078.tludwig.vcfprocessor.filters.GenotypeFilter;

/**
 * Filters heterozygous genotypes that do not have 0.5-dev <= AB <= 0.5+dev <br/>
 * with AB = AD2 / (AD1 + AD2)
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2023-08-29
 * Checked for release on XXXX-XX-XX
 * Unit Test defined on   XXXX-XX-XX
 */
public class ABHetMismatch extends GenotypeFilter {
  public static final String AD = "AD";

  private final double min;
  private final double max;

  String[] format;
  private int adPos = -1;


  public ABHetMismatch(double deviation) {
    super(true);
    this.min = 0.5-deviation;
    this.max = 0.5+deviation;
  }

  @Override
  public void setFormat(String[] format) {
    this.format = format;
    for (int i = 0; i < format.length; i++)
      if (AD.equalsIgnoreCase(this.format[i])){
        this.adPos = i;
        break;
      }
  }

  @Override
  public boolean pass(String s) {
    try{
      String[] f = s.split(":");
      if(f[0].startsWith("."))
        return true;
      String[] geno = f[0].replace("|","/").split("/");
      int g1 = Integer.parseInt(geno[0]);
      int g2 = Integer.parseInt(geno[1]);
      if(g1 == g2)
        return true;
      String[] ads = f[this.adPos].split(",");
      int ad1 = Integer.parseInt(ads[g1]);
      int ad2 = Integer.parseInt(ads[g2]);
      double sum = ad1 + ad2;
      double ab = ad2 / sum;
      return min <= ab && ab <= max;
    } catch (Exception e){
      //Ignore
    }

    return true;
  }

  @Override
  public String getDetails() {
    return this.min+" <= ABHet = AD(alt)/sum(AD) <= "+this.max;
  }
}
