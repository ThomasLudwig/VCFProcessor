package fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype;

import fr.inserm.u1078.tludwig.vcfprocessor.filters.GenotypeFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-23
 */
public class GenotypeDPFilter extends GenotypeFilter {
  public static final String DP = "DP"; //TODO propose to use sumAD() if DP is missing
  
  private final int min;
  private final int max;
  String[] format;
  int dpPos = -1;

  public GenotypeDPFilter(int min, int max) {
    super(true);
    this.min = min;
    this.max = max;
  }

  @Override
  public void setFormat(String[] format) {
    this.format = format;
    for (int i = 0; i < format.length; i++)
      if (DP.equalsIgnoreCase(this.format[i])){//TODO fallback one SUM(AD) if DP=.
        this.dpPos = i;
        break;
      }
  }

  @Override
  public boolean pass(String[] f) {
    int dp = 0;
    try {
      dp = Integer.parseInt(f[this.dpPos]);
    } catch (Exception ignore) { //ArrayIndex
      //Ignore
    }
    return (min <= dp && dp <= max);
  }
  
  @Override
  public String getDetails() {
    return this.min+" <= DP <= "+this.max;
  }
}
