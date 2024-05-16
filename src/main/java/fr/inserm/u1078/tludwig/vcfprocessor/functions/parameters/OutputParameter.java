/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-24
 */
public class OutputParameter extends Parameter {

  private String value;
  private final String ext;
  private final int type;
  public static final int TYPE_OUT = 0;
  public static final int TYPE_ERR = 1;

  public OutputParameter(String key, String ext, String example, String description, int type) {
    super(key, example, description);
    this.ext = ext;
    this.type = type;
  }

  public String getExtension() {
    return ext;
  }

  @Override
  public String toString() {
    if(value == null){
      if(type == TYPE_ERR)
        return "<stderr>";
      else
        return "<stdout>";
    }
    return this.value;
  }

  public String getStringValue() {
    return this.value;
  }

  @Override
  public String getDescription() {
    switch (ext) {
      case Function.OUT_NONE:
        return "Nothing";
      default:
        if(type == TYPE_OUT && !Function.OUT_PNG.equals(ext))
          return super.getDescription() + "." + ext+"(.gz)";
        return super.getDescription() + "." + ext;
    }
  }

  @Override
  public String getCommandLine() {
    if (!Function.OUT_NONE.equals(ext))
      return (" [" + super.getCommandLine() + "]").replace("[ ", "[");
    return "";
  }

  @Override
  public String showAllowedValues() {
    return "";
  }

  @Override
  public void parseParameter(String[] args) {
      try {
        parseParameter(this.getArgument(args, this.getKey()));
      } catch (ParameterException ignore) { }
  }

  @Override
  public void parseParameter(String value) {
    this.value = value;
  }

}
