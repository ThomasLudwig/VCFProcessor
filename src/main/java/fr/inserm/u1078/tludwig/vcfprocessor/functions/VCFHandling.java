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

  @SuppressWarnings("unused")
  Description getDesc();
  /*
  @SuppressWarnings("unused")
  boolean needVEP();
  @SuppressWarnings("unused")
  String getMultiallelicPolicy();
  @SuppressWarnings("unused")
  String getCustomRequirement();
*/
  public VCFPolicies getVCFPolicies();

}
