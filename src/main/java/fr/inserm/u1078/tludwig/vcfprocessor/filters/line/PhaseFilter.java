package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

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
  public boolean pass(String[] t) { //TODO untested filter
    for (int i = 9; i < t.length; i++) {
      String gt = t[i].split(":")[0];
      if (gt.contains("/"))
        return !this.isKeep();
    }
    return this.isKeep();
  }

  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove") + " variants with phased genotypes";
  }
}
