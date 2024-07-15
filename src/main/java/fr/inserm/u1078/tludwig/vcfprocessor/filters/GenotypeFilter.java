package fr.inserm.u1078.tludwig.vcfprocessor.filters;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-23
 */
public abstract class GenotypeFilter extends Filter<String[]> {

  public GenotypeFilter(boolean keep) {
    super(keep);
  }

  public abstract void setFormat(String[] format);
}
