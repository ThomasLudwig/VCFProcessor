/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-24
 */
public class IntegerParameter extends Parameter {

  private final Integer min, max;
  private int value;

  public IntegerParameter(String key, String example, String description, Integer min, Integer max) {
    super(key, example, description);
    this.min = min;
    this.max = max;
  }

  @Override
  public String toString() {
    return this.value + "";
  }

  public int getIntegerValue() {
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
    return "Integer value. " + n + " " + x;
  }

  @Override
  public void parseParameter(String s) throws ParameterException {
    try {
      this.value = new Integer(s);
    } catch (Exception e) {
      throw new ParameterException("Value for " + this.getKey() + " must be an integer");
    }
    if (min != null && min > this.value)
      throw new ParameterException("Value for " + this.getKey() + " can't be smaller than " + min);

    if (max != null && max < this.value)
      throw new ParameterException("Value for " + this.getKey() + " can't be larger than " + max);
  }

}
