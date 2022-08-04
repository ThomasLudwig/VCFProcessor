package fr.inserm.u1078.tludwig.vcfprocessor.commandline;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.StartUpException;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-06
 */
public class Argument {

  private final String key;
  private final String type;
  private final String value;
  private final String[][] examples;
  private final Description description;
  private static final Collection<String> KEYS = new ArrayList<String>();

  private Argument(String key, String type, String value, String[][] examples, Description description) {
    this.key = key;
    this.type = type;
    this.value = value;
    this.examples = examples;
    this.description = description
            .addLine("")
            .addLine(Description.bold("Syntax :"))
            .addCodeBlock(key + " " + value);
    if(examples.length > 0){
      description.addLine(Description.bold(examples.length > 1 ? "Examples" : "Example"));
      for (String[] ex : examples)
         description.addLine(Description.code(key + " " + ex[0]) + " : " + Description.italic(ex[1]));
    }
  }
  
  public static Argument newArgument(String key, String type, String value, String[][] examples, Description description) {
    Argument arg = new Argument(key, type, value, examples, description);
    add(arg);
    return arg;
  }

  private static void add(Argument arg) {
    String key = arg.getKey().toLowerCase();
    if(KEYS.contains(key))
      throw new StartUpException("Several Argument use the same key ["+arg.getKey()+"]");
    KEYS.add(key);
    /*
    ArrayList<Argument> list = ARGUMENTS.get(arg.type);
    if (list == null) {
      list = new ArrayList<>();
      ARGUMENTS.put(arg.type, list);
    }
    list.add(arg);*/
  }
  
  public static Collection<String> getAllowedKeys(){
    return new ArrayList<>(KEYS);    
  }
  
  public static String getArgumentType(Class argumentClass){
    if(argumentClass.equals(PositionArguments.class))
      return PositionArguments.TYPE;
    if(argumentClass.equals(SampleArguments.class))
      return SampleArguments.TYPE;
    if(argumentClass.equals(GenotypeArguments.class))
      return GenotypeArguments.TYPE;
    if(argumentClass.equals(FrequencyArguments.class))
      return FrequencyArguments.TYPE;
    if(argumentClass.equals(PropertyArguments.class))
      return PropertyArguments.TYPE;
    return "Unknown Filter Type";
  }

  public static ArrayList<Argument> getAllArguments(Class argClass){
    ArrayList<Argument> args = new ArrayList<>();
    Field[] fields = argClass.getDeclaredFields();
    for (Field f : fields) {
      if (Modifier.isStatic(f.getModifiers()) && Argument.class.isAssignableFrom(f.getType())) {
        try {
          args.add((Argument)f.get(null));
        } catch (IllegalAccessException | IllegalArgumentException ex) {
          throw new StartUpException("Could not initialize Argument");
        }
      }
    } 
    return args;
  }
  
  public static String getDocumentation() { //TODO as MarkDown (Notes/warnings not supported?)
    LineBuilder sb = new LineBuilder();
    sb.rstHeader("filters");
    sb.rstChapter("Filters");
    sb.newLine("Here is the list of filters available to limit the variants, genotypes or samples taken into account.");
    sb.newLine();
    
    Class[] argClasses = new Class[]{PositionArguments.class, SampleArguments.class, GenotypeArguments.class, FrequencyArguments.class, PropertyArguments.class};
    for (Class argClass : argClasses) {
      Message.verbose("type "+argClass.getSimpleName());
      ArrayList<Argument> arguments = getAllArguments(argClass);
      if (arguments != null) {
        sb.rstSection(getArgumentType(argClass));
        for (Argument arg : arguments) {
          sb.rstSubsection(arg.key.substring(2));
          sb.newLine(arg.description.asRST());
          if(!argClass.equals(argClasses[argClasses.length-1])){
            sb.rstHorizontalLine();
          }
          sb.newLine();
        }
      }
    }
    return sb.toString();
  }

  public String getKey() {
    return key;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public String[][] getExamples() {
    return examples;
  }

  public Description getDescription() {
    return description;
  }
  
  @Override
  public String toString(){
    return this.key;
  }
}
