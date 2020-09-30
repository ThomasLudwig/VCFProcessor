package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.tools.JavaTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.Main;
import fr.inserm.u1078.tludwig.vcfprocessor.StartUpException;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.GzParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.Parameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.Recode;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * Factory to instenciate Functions from command line arguments
 * <p>
 */
public class FunctionFactory {

  private static final ArrayList<Class> ALL_FUNCTIONS = getAllFunctions();

  public static Function getFunction(String[] args) throws StartUpException {//[] and not ... otherwise method might get called without it arguments
    if (args.length < 1 || args[0].startsWith("-"))
      usage();
    String functionName = args[0];

    for (Class clazz : ALL_FUNCTIONS)
      if (clazz.getSimpleName().equalsIgnoreCase(functionName))
        return getFunction(clazz);

    Message.error("Unknown function [" + functionName + "]");

    usage();
    return null;
  }

  public static Function getFunction(Class clazz) {
    try {
      Constructor cons = clazz.getConstructor();
      if (cons == null)
        throw new StartUpException("Bug : There is no construction with (String[] args) for function " + clazz.getSimpleName());
      Function f = (Function) cons.newInstance();
      return f;
    } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
      throw new StartUpException("Could not instanciate function [" + clazz.getSimpleName() + "]" + e);
    }
  }

  public static ArrayList<Class> getFunctionsFromType(String type) throws StartUpException {
    ArrayList<Class> ret = new ArrayList<>();
    for (Class clazz : ALL_FUNCTIONS)
      try {
        Constructor c = clazz.getConstructor();
        Function f = (Function) c.newInstance();
        //Function f = (Function)clazz.getConstructor().newInstance();
        String fType = f.getFunctionType();
        if (type.equals(fType))
          ret.add(clazz);
      } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
        throw new StartUpException("Could not find empty constructor for class " + clazz.getSimpleName() + ". There are " + clazz.getConstructors()[0] + " constructors for this class", e);
      }
    return ret;
  }

  public static void generateTestingScriptForFunction(String functionName) throws StartUpException {

    for (Class clazz : getAllFunctions())
      if (clazz.getSimpleName().equalsIgnoreCase(functionName)) {
        Function f = getFunction(clazz);
        TestingScript[] scripts = f.getScripts();
        if (scripts.length > 0)
          for (TestingScript script : scripts) {
            String filename = script.getScriptName();
            Message.info("Writting script : " + filename);
            try {
              PrintWriter out = new PrintWriter(new FileWriter(filename));
              out.println(script.getContent());
              out.close();
            } catch (IOException e) {
              throw new StartUpException("Could not write to script file [" + filename + "]", e);
            }
          }
        return;
      }
  }

  public static void generateTestingScripts(String targetdir) throws StartUpException {

    for (String type : Function.TYPES) {
      Message.info("Generating script for functions of type [" + type + "]");

      for (Class clazz : getFunctionsFromType(type)) {
        Function f = getFunction(clazz);
        TestingScript[] scripts = f.getScripts();
        if (scripts.length > 0) {
          String typeDir = f.getPackage();
          String funcDir = targetdir + File.separator + typeDir + File.separator + clazz.getSimpleName();
          new File(funcDir).mkdirs();
          for (TestingScript script : scripts) {
            String filename = funcDir + File.separator + script.getScriptName();
            try {
              Message.info("Writting script : " + filename);
              PrintWriter out = new PrintWriter(new FileWriter(filename));
              out.println(script.getContent());
              out.close();
            } catch (IOException e) {
              throw new StartUpException("Could not write to script file [" + filename + "]", e);
            }
          }
        }
      }
    }
  }

  public static String getFunctionsDocumentation() { //TODO as MarkDown (Notes/warnings not supported?)
    LineBuilder sb = new LineBuilder();
    sb.rstHeader("functions");
    sb.rstChapter("Functions");
    sb.rstSection("General Information");
    sb.rstSubsection("Optional Arguments for all Functions");
    Function recode = new Recode();
    for (Parameter param : recode.getParameters())
      if (param != null && (param instanceof OutputParameter))
        sb.newLine(param.getRSTCommandLine());

    sb.newLine(GzParameter.GZ_PARAMETER.getRSTCommandLine());
    sb.newLine();
    sb.newLine("Produced ascii files are automatically bgzipped if the filename given by the user ends with .gz");
    sb.newLine();
    sb.newLine("If the user provides the :code:`--gz` arguments, all output files be bgzipped and .gz will be appended automatically to the filenames (if missing). Output streamed to STD_OUT will also be bgzipped.");
    sb.newLine();

    ArrayList<String> types = new ArrayList<>();
    for (String type : Function.TYPES)
      types.add(type);
    /* for (String plugin : getPlugins())
      types.add(plugin);*/

    for (String type : types) {
      ArrayList<Class> functions = getFunctionsFromType(type);

      if (!functions.isEmpty()) {
        sb.newLine();
        sb.rstSection(type);

        for (Class clazz : functions) {
          Function f = getFunction(clazz);
          sb.newLine();
          sb.rstSubsection(f.getClass().getSimpleName());
          sb.newLine(Description.descriptionToRST(f.getSummary()));
          sb.newLine();
          sb.newLine("**Mandatory Arguments**");
          sb.newLine();
          sb.newLine();
          ArrayList<Parameter> params = new ArrayList<>();
          for (Parameter param : f.getParameters())
            if (param != null && !(param instanceof OutputParameter))
              params.add(param);
          if (!params.isEmpty())
            //out.println(".. code-block:: sh\n");
            for (Parameter param : params)
              sb.newLine(param.getRSTCommandLine());

          sb.newLine();
          sb.newLine("**Description**");
          sb.newLine();
          sb.newLine();
          sb.newLine(f.getDescription().asRST());

          if (Function.TYPE_GRAPHS.equals(type)) {
            sb.newLine();
            sb.newLine("**Example**");
            sb.newLine();
            sb.newLine();
            sb.newLine(".. image:: http://lysine.univ-brest.fr/media/" + f.getFunctionName() + ".png");
            sb.newLine("  :width: 600");
            sb.newLine("  :alt: " + f.getFunctionName() + " example");
            sb.newLine();
            sb.newLine();
          }

          if(clazz != functions.get(functions.size() - 1))
            sb.newLine(Description.HR);
        }
      }
    }
    return sb.toString();
  }


  public static ArrayList<String> getPlugins() {
    ArrayList<String> plugins = new ArrayList<>();
    plugins.addAll(getPlugins(new File(Main.getPluginDirectory())));
    return plugins;
  }

  private static ArrayList<String> getPlugins(File f) {
    ArrayList<String> plugins = new ArrayList<>();
    if (f.isDirectory())
      for (File sub : f.listFiles())
        plugins.addAll(getPlugins(sub));
    else {
      String name = f.getAbsolutePath();
      if (isValidPluginJar(name))
        plugins.add(name);
    }
    return plugins;
  }

  private static boolean isValidPluginJar(String filename) {
    if (filename.endsWith(".jar"))
      try {
        JarFile jarFile = new JarFile(filename);
        Enumeration<JarEntry> e = jarFile.entries();
        URL[] urls = {new URL("jar:file:" + filename + "!/")};
        URLClassLoader cl = URLClassLoader.newInstance(urls);
        while (e.hasMoreElements()) {
          JarEntry je = e.nextElement();

          if (je.isDirectory() || !je.getName().endsWith(".class"))
            continue;
          String className = je.getName().substring(0, je.getName().length() - String.valueOf(".class").length());
          className = className.replace('/', '.');
          Class<?> clazz = cl.loadClass(className);
          if (Function.class.isAssignableFrom(clazz))
            return true;
        }
      } catch (IOException | ClassNotFoundException e) {//TODO ignore or StartUpException
      }
    return false;
  }

  private static void usage() throws StartUpException {
    LineBuilder msg = new LineBuilder("Usage :")
            .addSpace(JavaTools.command(Function.class))
            .addSpace(Message.yellow("FUNCTION")).addSpace("param1...paramN")
            .addSpace("[").append(Message.cyan("--gz")).append("]")
            .addSpace("[").append(Message.cyan("--out")).addSpace(Message.red("results.file")).append("]")
            .addSpace("[").append(Message.cyan("--log")).addSpace(Message.red("LogFile.log")).append("]");

    for (String type : Function.TYPES) {
      ArrayList<Class> functions = getFunctionsFromType(type);

      if (!functions.isEmpty()) {
        msg.newLine();
        msg.newLine();
        msg.newLine(Message.green(type));
        for (Class clazz : functions) {
          Function f = getFunction(clazz);
          msg.newLine("  ").append(f.getShortUsage());
        }
      }
    }

    ArrayList<String> plugins = getPlugins();

    if (!plugins.isEmpty()) {
      msg.newLine();
      msg.newLine(Message.red("ExternalFunction :"));

      for (String plugin : plugins) {
        ArrayList<Class> functions = getFunctionsFromType(plugin);

        if (!functions.isEmpty()) {
          msg.newLine();
          msg.newLine(Message.green(plugin));
          for (Class clazz : functions) {
            Function f = getFunction(clazz);
            msg.newLine("  ").append(f.getShortUsage());
          }
        }
      }
    }

    Message.endWithoutException(msg.toString());
  }

  private static ArrayList<Class> getFunctionsFromPlugin(String filename) throws IOException {
    ArrayList<Class> functions = new ArrayList<>();
    if (filename.endsWith(".jar")) {
      JarFile jarFile = new JarFile(filename);
      Enumeration<JarEntry> e = jarFile.entries();
      URL[] urls = {new URL("jar:file:" + filename + "!/")};
      URLClassLoader cl = URLClassLoader.newInstance(urls);
      while (e.hasMoreElements()) {
        JarEntry je = e.nextElement();
        if (je.isDirectory() || !je.getName().endsWith(".class"))
          continue;
        String className = je.getName().substring(0, je.getName().length() - String.valueOf(".class").length());
        className = className.replace('/', '.');
        try{
          Class<?> clazz = cl.loadClass(className);
          if (Function.class.isAssignableFrom(clazz))
            functions.add(clazz);
        } catch(ClassNotFoundException ex){
          throw new StartUpException("Function["+className+"] could not be found in file ["+filename+"]", ex);
        }
      }
    }
    return functions;
  }

  private static ArrayList<Class> getAllFunctions() { //TODO manage plugin
    ArrayList<Class> clazzes = new ArrayList<>();
    try {
      for (String classpathEntry : System.getProperty("java.class.path").split(System.getProperty("path.separator")))
        if (classpathEntry.endsWith(".jar")) {
          File jar = new File(classpathEntry);
          JarInputStream is = new JarInputStream(new FileInputStream(jar));
          JarEntry entry;
          while ((entry = is.getNextJarEntry()) != null)
            if (entry.getName().endsWith(".class")) {
              String className = entry.getName().replace("/", ".").replace(".class", "");
              if (className.startsWith("fr.inserm.u1078.tludwig.vcfprocessor")) {
                Class clazz = Class.forName(className);
                if (Function.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                  for (Class c : clazzes)
                    if (c.getSimpleName().equalsIgnoreCase(clazz.getSimpleName()))
                      throw new StartUpException("Two Functions with the Same Name are present : [" + c.getSimpleName() + "]");
                  clazzes.add(clazz);
                }
              }
            }
        }
      for (String plugin : getPlugins())
        try {
          clazzes.addAll(getFunctionsFromPlugin(plugin));
        } catch (IOException e) {
          throw new StartUpException("Could not get functions from plugin file [" + plugin + "]", e);
        }

    } catch (IOException | ClassNotFoundException e) {
      throw new StartUpException("Could not load functions", e);
    }
    return clazzes;
  }
}
