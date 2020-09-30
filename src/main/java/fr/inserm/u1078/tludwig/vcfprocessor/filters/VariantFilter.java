package fr.inserm.u1078.tludwig.vcfprocessor.filters;

import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public abstract class VariantFilter extends Filter<Variant> {

  public VariantFilter(boolean keep) {
    super(keep);
  }
}
