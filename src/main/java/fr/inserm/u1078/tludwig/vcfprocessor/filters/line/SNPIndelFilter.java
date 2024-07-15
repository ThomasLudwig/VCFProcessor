package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.vcfprocessor.files.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class SNPIndelFilter extends LineFilter {

  public static final boolean SNP = true;
  public static final boolean INDEL = false;

  private final boolean isSnp;

  public SNPIndelFilter(boolean isSnp, boolean keep) {
    super(keep);
    this.isSnp = isSnp;
  }

  @Override
  public boolean pass(VariantRecord record) {
    int refLength = record.getRef().replace("-", "").length();
    for (String alt : record.getAlts()) {
      int altLength = alt.replace("-", "").length();
      if ((refLength == altLength) == isSnp)
        return isKeep();
    }

    return !isKeep();
  }

  @Override
  public boolean leftColumnsOnly() {
    return true;
  }

  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove") + " variants with "+(isSnp ? "SNVs" : "INDELs");
  }
}
