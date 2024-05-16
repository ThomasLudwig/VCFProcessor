package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class DPVariantFilter extends LineFilter {

  private final double min;
  private final double max;
  public static final int TYPE_MEDIAN = 1;
  public static final int TYPE_MEAN = 2;

  private final int type;

  public DPVariantFilter(double min, double max, int type) {
    super(true);
    this.min = min;
    this.max = max;
    this.type = type;
  }

  @Override
  public boolean pass(String[] t) {
    String[] formats = t[8].split(":");
    int idx = -1;
    for (int i = 0; i < formats.length; i++)
      if ("DP".equals(formats[i]))
        idx = i;

    if (idx < 0)
      return false;

    NumberSeries dps = new NumberSeries("dp", SortedList.Strategy.SORT_AFTERWARDS);
    for (int i = 9; i < t.length; i++) {
      int dp;
      try {
        dp = new Integer(t[i].split(":")[idx]);
        dps.add(dp);//here is "ignore missing"
      } catch (Exception e) {
        //not parsable because .
        //missing because . instead of ./.:.:.:.:.                
      }
      //dps.add(dp);//here if missing counts as dp=0
    }

    double value = (type == TYPE_MEDIAN) ? dps.getMedian() :dps.getMean();

    return (min <= value && value <= max);
  }
  
  @Override
  public String getDetails() {
    return this.min+" <= DP("+(type == TYPE_MEDIAN ? "median" : "mean")+") <= "+this.max;
  }
}
