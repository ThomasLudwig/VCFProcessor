package fr.inserm.u1078.tludwig.vcfprocessor.filters;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public abstract class LineFilter extends Filter<String[]> {

  public LineFilter(boolean keep) {
    super(keep);
  }
}
