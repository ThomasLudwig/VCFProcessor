package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.vcfprocessor.files.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class MissingFilter extends LineFilter {

  private final int minNb;
  private final int maxNb;

  public MissingFilter(int minMissingNb, int maxMissingNb) {
    super(true);
    this.minNb = minMissingNb;
    this.maxNb = maxMissingNb;
  }

  @Override
  public boolean pass(VariantRecord record) {
    int missing = 0;

    for(int s = 0 ; s < record.getNumberOfSamples(); s++)
      if (record.getGT(s).startsWith(".")) {
        missing++;
        if (missing > this.maxNb)
          return !isKeep();
      }

    if(missing < this.minNb)
      return !isKeep();

    return isKeep();
  }

  @Override
  public boolean leftColumnsOnly() {
    return false;
  }

  @Override
  public String getDetails() {
    return this.minNb+" <= missingGenotypes <= "+this.maxNb;
  }
}
