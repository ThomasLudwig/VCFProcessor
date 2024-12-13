package fr.inserm.u1078.tludwig.vcfprocessor.filters;

import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public abstract class LineFilter extends Filter<VariantRecord> {

  public LineFilter(boolean keep) {
    super(keep);
  }

  /**
   * Returns true if only the date from CHROM,POS,ID,REF,ALT,QUAL,FILTER and INFO are used
   * @return true if the filter only relies on the 8 leftmost columns
   */
  public abstract boolean leftColumnsOnly();
}
