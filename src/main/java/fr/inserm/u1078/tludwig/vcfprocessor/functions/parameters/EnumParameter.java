/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-03
 */
public class EnumParameter extends StringParameter {

  private final String[] allowedValues;

  public EnumParameter(String key, String[] values, String example, String description) {
    super(key, example, description);
    this.allowedValues = values;
  }

  public EnumParameter(String key, String values, String example, String description) {
    this(key, values.split(","), example, description);
  }

  @Override
  public String showAllowedValues() {
    return "["+String.join("|", this.allowedValues)+"]";
  }

  @Override
  public String getRSTCommandLine() {
    return super.getRSTCommandLine().replace("|", " | ");
  }
  
  

  public String[] getAllowedValues() {
    return allowedValues;
  }

  @Override
  public void parseParameter(String s) throws ParameterException {
    super.parseParameter(s);
    boolean valid = false;

    for (String v : this.allowedValues)
      if (v.equals(this.getStringValue())) {
        valid = true;
        break;
      }

    if (!valid)
      throw new ParameterException("Value " + this.getStringValue() + " is not valid. Must be "+this.showAllowedValues());
  }

}
