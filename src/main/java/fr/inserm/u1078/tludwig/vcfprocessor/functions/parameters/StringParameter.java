/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.gui.DefaultInput;
import fr.inserm.u1078.tludwig.vcfprocessor.gui.Input;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-24
 */
public class StringParameter extends Parameter {

  private String value;

  public StringParameter(String key, String example, String description) {
    super(key, example, description);
  }

  @Override
  public String toString() {
    return this.value;
  }

  public String getStringValue() {
    return this.value;
  }

  @Override
  public String showAllowedValues() {
    return "Any";
  }

  @Override
  public void parseParameter(String s) throws ParameterException {
    this.value = s;
  }

  @Override
  public Input getInputForm() {
    return new DefaultInput(this);
  }
}
