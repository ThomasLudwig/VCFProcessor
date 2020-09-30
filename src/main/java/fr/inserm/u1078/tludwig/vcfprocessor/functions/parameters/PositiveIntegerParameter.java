package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-20
 */
public class PositiveIntegerParameter extends IntegerParameter {

  public PositiveIntegerParameter(String key, String description, Integer max) {
    super(key, "0-"+max, description, 0, max);
  }

  public PositiveIntegerParameter(String key, String description) {
    super(key, "PositiveInteger", description, 0, Integer.MAX_VALUE);
  }

}
