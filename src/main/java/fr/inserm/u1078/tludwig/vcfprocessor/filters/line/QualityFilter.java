package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class QualityFilter extends LineFilter {
  private final double minQ;
  private final double maxQ;

  public QualityFilter(double min, double max) {
    super(true);
    this.minQ = min;
    this.maxQ = max;
  }

  @Override
  public boolean pass(VariantRecord record) {
    Double qual = record.getQual();
    if(qual==null) qual=0D;
    return minQ <= qual && qual <= maxQ;
  }

  @Override
  public boolean leftColumnsOnly() {
    return true;
  }

  @Override
  public String getDetails() {
    return this.minQ+" <= Quality <= "+this.maxQ;
  }
}
