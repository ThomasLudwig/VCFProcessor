package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.maok.tools.Message;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-09
 */
public abstract class Parameter {

  private final String key;
  private final String example;
  private final String description;

  public Parameter(String key, String example, String description) {
    this.key = key;
    this.example = example;
    this.description = description;
  }

  public String getKey() {
    return key;
  }

  public String getDescription() {
    return description;
  }

  public String getExample() {
    return example;
  }
  
  public String getCommandLine() {
    return " " + Message.Color.cyan(this.getKey()) + " " + Message.Color.red(this.example);
  }
  
  public String getCommandLineDescription(int keyLength, int exampleLength){
    StringBuilder theKey = new StringBuilder(this.getKey());
    StringBuilder theExample = new StringBuilder(this.getExample());
    while(theKey.length() < keyLength)
      theKey.append(" ");
    while(theExample.length() < exampleLength)
      theExample.append(" ");
    
    return Message.Color.cyan(theKey.toString()) + " " + Message.Color.red(theExample.toString()) + " : " + this.getDescription();
  }
  
  public String getRSTCommandLine() {
    return "* :code:`" + (this.getKey() + " " + this.example).trim() + "` : " + this.getDescription();
  }
  
  public String getMDCommandLine() {
    return "\t" + this.getKey() + " " + this.getDescription();
  }

  public final String getArgument(String[] args, String opt) throws ParameterException {
    for (int i = 0; i < args.length - 1; i++)
      if (args[i].equalsIgnoreCase(opt))
        return args[i + 1];

    throw new ParameterException("Missing argument: " + opt + " ("+this.getDescription()+")");
  }

  public boolean isOutput() {
    return this instanceof OutputParameter;
  }

  public void parseParameter(String[] args) throws ParameterException {
    this.parseParameter(this.getArgument(args, this.getKey()));
  }
  
  public String getFullDescription(){
    StringBuilder ret = new StringBuilder(this.getKey() + " " + this);
    while(ret.length() < 30)
      ret.append(" ");
     ret.append(": ").append(this.getDescription());
     return ret.toString();
  }

  @Override
  public abstract String toString();

  public abstract void parseParameter(String value) throws ParameterException;

  public abstract String showAllowedValues();

}
