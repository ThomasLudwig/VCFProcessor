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
  String PREFIX_MULTIALLELIC = "In case of multiallelic variants : ";
  String MULTIALLELIC_NA = "na";
  String MULTIALLELIC_ALLELE_AS_LINE = "Each alternate allele is processed independently.";
  String MULTIALLELIC_FORBIDDEN = "An error will be thrown, as this function expects only monoallelic variants. The affected variant line will be dropped.";
  String MULTIALLELIC_DROP = "The affected variant line will be silently dropped.";
  String MULTIALLELIC_ANNOTATION_FOR_ALL = "Annotation is added/updated for each alternate allele (comma-separated).";
  String MULTIALLELIC_FILTER_ONE = "If at least one alternate allele satisfy all the conditions, the whole variant line is kept.";

  @SuppressWarnings("unused")
  Description getDesc();
  @SuppressWarnings("unused")
  boolean needVEP();
  @SuppressWarnings("unused")
  String getMultiallelicPolicy();

  @SuppressWarnings("unused")
  String getCustomRequirement();
}
