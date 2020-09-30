/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.gui.DefaultInput;
import fr.inserm.u1078.tludwig.vcfprocessor.gui.Input;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-24
 */
public class FloatParameter extends Parameter {

  private final Double min, max;
  private double value;

  public FloatParameter(String key, String example, String description, Double min, Double max) {
    super(key, example, description);
    this.min = min;
    this.max = max;
  }

  @Override
  public String toString() {
    return this.value + "";
  }

  public double getFloatValue() {
    return this.value;
  }

  @Override
  public String showAllowedValues() {
    String n = "";
    if (null != min)
      n = "Min=" + min;
    String x = "";
    if (null != max)
      x = "Max=" + max;
    return "Float value. " + n + " " + x;
  }

  @Override
  public void parseParameter(String s) throws ParameterException {
    try {
      this.value = new Double(s);
    } catch (Exception e) {
      throw new ParameterException("Value for " + this.getKey() + " must be an float");
    }
    if (min != null && min > this.value)
      throw new ParameterException("Value for " + this.getKey() + " can't be smaller than " + min);

    if (max != null && max < this.value)
      throw new ParameterException("Value for " + this.getKey() + " can't be larger than " + max);
  }

  @Override
  public Input getInputForm() {
    return new DefaultInput(this);
  }
}
