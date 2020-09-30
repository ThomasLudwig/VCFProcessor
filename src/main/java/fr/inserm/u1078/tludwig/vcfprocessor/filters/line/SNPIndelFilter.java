package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

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
  public boolean pass(String[] f) {
    int refLength = f[3].replace("-", "").length();
    for (String alt : f[4].split(",")) {
      int altLength = alt.replace("-", "").length();
      if ((refLength == altLength) == isSnp)
        return isKeep();
    }

    return !isKeep();
  }

  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove") + " variants with "+(isSnp ? "SNVs" : "INDELs");
  }
}
