package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.StartUpException;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-04
 */
public class ListParameter extends StringParameter {

  public ListParameter(String key, String example, String description) {
    super(key, example, description);
  }

  public String[] getList() {
    try {
      return this.getStringValue().split(",");
    } catch (NullPointerException e) {
      throw new StartUpException("Value is null for Parameter "+this.getKey());
    }
  }
}
