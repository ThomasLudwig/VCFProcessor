package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.vcfprocessor.files.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class ThinFilter extends LineFilter { //TODO bugs due to parallelism

  private final int distance;

  private String lastChrom = null;
  private int lastPos = Integer.MIN_VALUE;

  public ThinFilter(int distance) {
    super(true);
    this.distance = distance;
  }

  @Override
  public boolean pass(VariantRecord record) {
    String chr = record.getChrom();
    int pos = record.getPos();
    
    if (chr.equals(this.lastChrom) && (pos - this.lastPos < this.distance))
      return false;
    

    this.lastChrom = chr;
    this.lastPos = pos;
    return true;
  }

  @Override
  public boolean leftColumnsOnly() {
    return true;
  }

  @Override
  public String getDetails() {
    return "Distance="+distance;
  }
}
