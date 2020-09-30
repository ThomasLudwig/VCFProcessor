package fr.inserm.u1078.tludwig.vcfprocessor.filters;

import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-23
 */
public abstract class SampleFilter extends Filter<Sample> {

  public SampleFilter(boolean keep) {
    super(keep);
  }
}
