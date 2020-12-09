package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-10
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public interface VCFHandling {
  public static final String PREFIX_MULTIALLELIC = "In case of multiallelic variants : ";
  public static final String MULTIALLELIC_NA = "na";
  public static final String MULTIALLELIC_ALLELE_AS_LINE = "Each alternate allele is processed independently.";
  public static final String MULTIALLELIC_FORBIDDEN = "An error will be thrown, as this function expects only monoallelic variants. The affected variant line will be dropped.";
  public static final String MULTIALLELIC_ANNOTATION_FOR_ALL = "Annotation is added/updated for each alternate allele (comma-separated).";
  public static final String MULTIALLELIC_FILTER_ONE = "If at least one alternate allele satisfy all the conditions, the whole variant line is kept.";
  
  public Description getDesc();

  public boolean needVEP();
  
  public String getMultiallelicPolicy();

  public String getCustomRequirement();
}
