package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-04
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public final class GzParameter extends Parameter {
  
  public static final GzParameter GZ_PARAMETER = new GzParameter();

  private GzParameter() {
    super("--gz", "", "Force all outputs to be bgzipped");
  }

  @Override
  public String toString() {
    return null;
  }

  @Override
  public void parseParameter(String value) {}

  @Override
  public String showAllowedValues() {
    return null;
  }

}
