package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-24
 */
public class BooleanParameter extends Parameter {

  private boolean value;

  public BooleanParameter(String key, String description) {
    super(key, "TRUE|FALSE", description);
  }

  @Override
  public String toString() {
    return this.value + "";
  }

  public boolean getBooleanValue() {
    return this.value;
  }

  @Override
  public String showAllowedValues() {
    return "0, false, 1, true";
  }

  @Override
  public void parseParameter(String s) throws ParameterException {
    if ("true".equalsIgnoreCase(s) || "1".equalsIgnoreCase(s))
      this.value = true;
    else if ("false".equalsIgnoreCase(s) || "0".equalsIgnoreCase(s))
      this.value = false;
    else
      throw new ParameterException("Value for " + this.getKey() + " must be true|false or 0|1, found [" + s + "]");
  }

}
