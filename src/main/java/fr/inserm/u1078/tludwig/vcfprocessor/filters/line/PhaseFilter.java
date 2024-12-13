package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class PhaseFilter extends LineFilter {

  public PhaseFilter(boolean keep) {
    super(keep);
  }

  @Override
  public boolean pass(VariantRecord record) { //TODO untested filter
    for(int s = 0 ; s < record.getNumberOfSamples(); s++) {
      String gt = record.getGT(s);
      if (gt.contains("/"))
        return !this.isKeep();
    }
    return this.isKeep();
  }

  @Override
  public boolean leftColumnsOnly() {
    return false;
  }

  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove") + " variants with phased genotypes";
  }
}
