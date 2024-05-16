package fr.inserm.u1078.tludwig.vcfprocessor.filters;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 * @param <T>
 */
public abstract class Filter<T> {

  /**
   * To be efficient, filters need to be applied on the VCF in a specific order :
   * <p>
   * We read a new raw_line, and split it into a String Array : read_line[]
   * <p>
   * #1 First, apply the "Quick" filters, that are independent of the number of samples (they are based on chr,pos,id,ref,alt...)
   * #2 Remove Samples (if needed), so the filtered_line[] has fewer columns than the read_line[] //this is fast, but it useless to do it on le lines filters in
   * #1
   * #3 Genotype filters will alter the genotype of some samples and set them to "missing" (based on DP, GQ...) filtered_line[] is altered
   * <p>
   * if FILTER_SAMPLE or FILTER_GENOTYPE filters are present, apply the NonVariantFilter : if a line has only missing and non-variant genotype, the line is
   * filtered
   * implicitly create a new NonVariant filter and add it, it will be executed in 4
   * <p>
   * #4 Then we apply "line" filters, that are based on genotype values
   * <p>
   * if FILTER_SAMPLE or FILTER_GENOTYPE filters are present, update AC,AN,AF
   * do it before or after #5 ?
   * - after is more efficient, since we process fewer lines
   * - before allows to use updated values
   * <p>
   * #5 Finally, for the remaining lines, the Variant Object is created and Filter based on this object's attributes are applied
   */

  private final boolean keep;

  public Filter(boolean keep) {
    this.keep = keep;
  }

  public boolean isKeep() {
    return keep;
  }
  
  public boolean isFilter() {
    return !keep;
  }

  public abstract boolean pass(T t);
  
  public final String getSummary(){
    return this.getClass().getSimpleName()+"{"+this.getDetails()+"}";
  }
  
  /**
   * Gets the Internal parameters of the filter
   * @return the Internal parameters of the filter
   */
  public abstract String getDetails(); 
}
