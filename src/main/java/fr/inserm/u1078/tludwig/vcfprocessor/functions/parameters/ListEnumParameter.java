/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.StartUpException;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-03
 */
public class ListEnumParameter extends Parameter {

  String value = null;

  private final String[] allowedValues;

  public ListEnumParameter(String key, String[] values, String example, String description) {
    super(key, example, description);
    this.allowedValues = values;
  }

  public ListEnumParameter(String key, String values, String example, String description) {
    this(key, values.split(","), example, description);
  }

  @Override
  public String toString() {
    return this.value;
  }

  @Override
  public String showAllowedValues() {
    String ret = String.join("|", this.allowedValues);
    return "List form the following : " + ret;
  }

  @Override
  public void parseParameter(String s) throws ParameterException {
    value = s;
    for (String v : this.getList()) {
      boolean valid = false;
      for (String av : this.allowedValues)
        if (av.equals(v)) {
          valid = true;
          break;
        }

      if (!valid)
        throw new ParameterException("Value " + v + " is not valid");
    }    
  }

  public String[] getList() {
    try {
      return this.value.split(",");
    } catch (NullPointerException e) {
      throw new StartUpException("Value is null for Parameter "+this.getKey(), e);
    }
  }
}
