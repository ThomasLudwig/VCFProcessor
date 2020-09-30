package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-20
 */
public class RatioParameter extends FloatParameter {

  public RatioParameter(String key, String description) {
    super(key, "0.0-1.0", description, 0d, 1d);
  }
}
