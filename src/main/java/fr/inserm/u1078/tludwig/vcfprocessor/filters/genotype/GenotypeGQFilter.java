package fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype;

import fr.inserm.u1078.tludwig.vcfprocessor.filters.GenotypeFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-23
 */
public class GenotypeGQFilter extends GenotypeFilter {
  public static final String GQ = "GQ";

  private final int min;
  private final int max;
  String[] format;
  int gqPos = -1;

  public GenotypeGQFilter(int min, int max) {
    super(true);
    this.min = min;
    this.max = max;
  }

  @Override
  public void setFormat(String[] format) {
    this.format = format;
    for (int i = 0; i < format.length; i++)
      if (GQ.equalsIgnoreCase(this.format[i])){
        this.gqPos = i;
        break;
      }
  }

  @Override
  public boolean pass(String t) {
    int gq = 0;
    try {
      gq = new Integer(t.split(":")[this.gqPos]);
    } catch (Exception e) { //ArrayIndex
      //Number
    }
    return (min <= gq && gq <= max);
  }
  
  @Override
  public String getDetails() {
    return this.min+" <= GQ <= "+this.max;
  }
}
