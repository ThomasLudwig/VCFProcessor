package fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype;

import fr.inserm.u1078.tludwig.vcfprocessor.filters.GenotypeFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;

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
  public boolean pass(String[] f) {
    try{
      int[] alleles = Genotype.getAlleles(f[0]);
      if(alleles == null)
        return true;
      if(alleles[0] == alleles[1])
        return true;
      String[] ads = f[this.adPos].split(",");
      int ad1 = Integer.parseInt(ads[alleles[0]]);
      int ad2 = Integer.parseInt(ads[alleles[1]]);
      double sum = ad1 + ad2;
      double ab = ad2 / sum;
      return min <= ab && ab <= max;
    } catch (Exception ignore){
      //Ignore
    }
    return true;
  }

  @Override
  public String getDetails() {
    return this.min+" <= ABHet = AD(alt)/sum(AD) <= "+this.max;
  }
}
