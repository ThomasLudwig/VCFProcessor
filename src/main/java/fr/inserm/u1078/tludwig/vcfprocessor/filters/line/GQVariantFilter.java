package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class GQVariantFilter extends LineFilter {

  private final double min;
  private final double max;
  public static final int TYPE_MEDIAN = 1;
  public static final int TYPE_MEAN = 2;

  private final int type;

  public GQVariantFilter(double min, double max, int type) {
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
      if ("GQ".equals(formats[i]))
        idx = i;

    if (idx < 0)
      return false;

    NumberSeries gqs = new NumberSeries("gq", SortedList.Strategy.SORT_AFTERWARDS);
    for (int i = 9; i < t.length; i++) {
      try {
        int gq = new Integer(t[i].split(":")[idx]);
        //here is "ignore missing"
        gqs.add(gq);//here is "ignore missing"
      } catch (Exception e) {
        //not parsable because .
        //missing because . instead of ./.:.:.:.:.                
      }
      //gqs.add(gq);//here if missing counts as dp=0
    }

    double value = (TYPE_MEDIAN == type) ? gqs.getMedian() : gqs.getMean();

    return (min <= value && value <= max);
  }
  
  @Override
  public String getDetails() {
    return this.min+" <= GQ("+(type == TYPE_MEDIAN ? "median" : "mean")+") <= "+this.max;
  }
}
