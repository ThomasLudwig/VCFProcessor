package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.tools.JavaTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.StartUpException;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.GzParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.Parameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter.Recode;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * Factory to instantiate Functions from command line arguments
 * <p>
 */
public class FunctionFactory {

  private static ArrayList<Class<?>> ALL_FUNCTIONS = null;
  private static ArrayList<String> PLUGINS=null;

  public static Function getFunction(String[] args) throws StartUpException {//[] and not ... otherwise method might get called without it arguments
    if (args.length < 1 || args[0].startsWith("-"))
      usage();
    String functionName = args[0];

    for (Class<?> clazz : getALL_FUNCTIONS())
      if (clazz.getSimpleName().equalsIgnoreCase(functionName))
        return getFunction(clazz);

    Message.error("Unknown function [" + functionName + "]");

    usage();
    return null;
  }

  public static Function getFunction(Class<?> clazz) {
    try {
      Constructor<?> cons = clazz.getConstructor();
      return (Function) cons.newInstance();
    } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
      throw new StartUpException("Could not instantiate function [" + clazz.getSimpleName() + "]" + e);
    }
  }

  public static ArrayList<Class<?>> getFunctionsFromType(String type) throws StartUpException {
    ArrayList<Class<?>> ret = new ArrayList<>();
    for (Class<?> clazz : getALL_FUNCTIONS())
      try {
        Constructor<?> c = clazz.getConstructor();
        Function f = (Function) c.newInstance();
        //Function f = (Function)clazz.getConstructor().newInstance();
        String fType = f.getFunctionType();
        if (type.equals(fType) && !ret.contains(clazz))
          ret.add(clazz);
      } catch (Exception e) {
        throw new StartUpException("Could not find empty constructor for class " + clazz.getSimpleName() + ". There are " + clazz.getConstructors()[0] + " constructors for this class", e);
      }
    return ret;
  }

  public static void generateTestingScriptForFunction(String functionName) throws StartUpException {

    for (Class<?> clazz : getALL_FUNCTIONS())
      if (clazz.getSimpleName().equalsIgnoreCase(functionName)) {
        Function f = getFunction(clazz);
        TestingScript[] scripts = f.getScripts();
        for (TestingScript script : scripts) {
          String filename = script.getScriptName();
          Message.info("Writing script : " + filename);
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

  public static void generateTestingScripts(String targetDir) throws StartUpException {

    for (String type : Function.TYPES) {
      Message.info("Generating script for functions of type [" + type + "]");

      for (Class<?> clazz : getFunctionsFromType(type)) {
        Function f = getFunction(clazz);
        TestingScript[] scripts = f.getScripts();
        if (scripts.length > 0) {
          String typeDir = f.getPackage();
          String funcDir = targetDir + File.separator + typeDir + File.separator + clazz.getSimpleName();
          if(new File(funcDir).mkdirs())
            Message.info("Directory ["+funcDir+"] created");
          for (TestingScript script : scripts) {
            String filename = funcDir + File.separator + script.getScriptName();
            try {
              Message.info("Writing script : " + filename);
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
      if (param instanceof OutputParameter)
        sb.newLine(param.getRSTCommandLine());

    sb.newLine(GzParameter.GZ_PARAMETER.getRSTCommandLine());
    sb.newLine();
    sb.newLine("Produced ascii files are automatically bgzipped if the filename given by the user ends with .gz");
    sb.newLine();
    sb.newLine("If the user provides the :code:`--gz` arguments, all output files be bgzipped and .gz will be appended automatically to the filenames (if missing). Output streamed to STD_OUT will also be bgzipped.");
    sb.newLine();

    ArrayList<String> types = new ArrayList<>();
    Collections.addAll(types, Function.TYPES);

    for (String type : types) {
      ArrayList<Class<?>> functions = getFunctionsFromType(type);

      if (!functions.isEmpty()) {
        sb.newLine();
        sb.rstSection(type);

        for (Class<?> clazz : functions) {
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
            sb.newLine(".. image:: https://lysine.univ-brest.fr/media/" + f.getFunctionName() + ".png");
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


  public static ArrayList<String> initPlugins(String directory) {// deported method, so the other one can be recursive
    PLUGINS = new ArrayList<>();

    PLUGINS.addAll(getPlugins(new File(directory)));
    Message.info(PLUGINS.size()+" plugins found in ["+directory+"]");
    return PLUGINS;
  }

  private static ArrayList<String> getPlugins(File f) {
    ArrayList<String> plugins = new ArrayList<>();
    if (f.isDirectory())//recursively scan subdirectory
      for (File sub : Objects.requireNonNull(f.listFiles()))
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
        try(JarFile jarFile = new JarFile(filename)){
          Enumeration<JarEntry> e = jarFile.entries();
          URL[] urls = {new URL("jar:file:" + filename + "!/")};
          try(URLClassLoader cl = URLClassLoader.newInstance(urls)) {
            while (e.hasMoreElements()) {
              JarEntry je = e.nextElement();
              if (je.isDirectory() || !je.getName().endsWith(".class"))
                continue;
              String className = je.getName().substring(0, je.getName().length() - ".class".length());
              className = className.replace('/', '.');
              try {
                Class<?> clazz = cl.loadClass(className);
                if (Function.class.isAssignableFrom(clazz))
                  return true;
              } catch (Throwable ignore) {
                //ignore
              }
            }
          }
        }
        Message.info("Jar file ["+filename+"] contains no class extending "+Function.class.getSimpleName());
      } catch (IOException e) {//TODO ignore or StartUpException
        Message.error("file ["+filename+"] could not be loaded as a Plugin", e);
      }
    else
      Message.info("File ["+filename+"] is not a .jar file");
    return false;
  }

  private static void usage() throws StartUpException {
    LineBuilder msg = new LineBuilder("Usage :")
            .addSpace(JavaTools.command(Function.class))
            .addSpace(Message.Color.yellow("FUNCTION")).addSpace("param1...paramN")
            .addSpace("[").append(Message.Color.cyan("--gz")).append("]")
            .addSpace("[").append(Message.Color.cyan("--out")).addSpace(Message.Color.red("results.file")).append("]")
            .addSpace("[").append(Message.Color.cyan("--log")).addSpace(Message.Color.red("LogFile.log")).append("]");

    for (String type : Function.TYPES) {
      ArrayList<Class<?>> functions = getFunctionsFromType(type);

      if (!functions.isEmpty()) {
        msg.newLine();
        msg.newLine();
        msg.newLine(Message.Color.green(type));
        for (Class<?> clazz : functions) {
          Function f = getFunction(clazz);
          msg.newLine("  ").append(f.getShortUsage());
        }
      }
    }

    if (!PLUGINS.isEmpty()) {
      msg.newLine();
      msg.newLine(Message.Color.red("ExternalFunction :"));

      for (String plugin : PLUGINS) {
        ArrayList<Class<?>> functions = getFunctionsFromType(plugin);

        if (!functions.isEmpty()) {
          msg.newLine();
          msg.newLine(Message.Color.green(plugin));
          for (Class<?> clazz : functions) {
            Function f = getFunction(clazz);
            msg.newLine("  ").append(f.getShortUsage());
          }
        }
      }
    }

    Message.fatal(msg.toString(), true);
  }

  private static ArrayList<Class<?>> getFunctionsFromPlugin(String filename) throws IOException {

    ArrayList<Class<?>> functions = new ArrayList<>();
    if (filename.endsWith(".jar")) {
      try(JarFile jarFile = new JarFile(filename)) {
        Enumeration<JarEntry> e = jarFile.entries();
        URL[] urls = {new URL("jar:file:" + filename + "!/")};
        try(URLClassLoader cl = URLClassLoader.newInstance(urls)) {
          while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if (je.isDirectory() || !je.getName().endsWith(".class"))
              continue;
            String className = je.getName().substring(0, je.getName().length() - ".class".length());
            className = className.replace('/', '.');
            try {
              Class<?> clazz = cl.loadClass(className);
              if (Function.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers()))
                functions.add(clazz);
            } catch (Throwable ex) {
              //throw new StartUpException("Function["+className+"] could not be found in file ["+filename+"]", ex);
            }
          }
        }
      }
    }
    return functions;
  }

  private static ArrayList<Class<?>> getALL_FUNCTIONS() {
    if(ALL_FUNCTIONS == null)
      ALL_FUNCTIONS = initAllFunctions();
    return ALL_FUNCTIONS;
  }

  private static ArrayList<Class<?>> initAllFunctions() {
    ArrayList<Class<?>> clazzes = new ArrayList<>();
    try {
      for (String classpathEntry : System.getProperty("java.class.path").split(File.pathSeparator))
        if (classpathEntry.endsWith(".jar")) {
          File jar = new File(classpathEntry);
          JarInputStream is = new JarInputStream(Files.newInputStream(jar.toPath()));
          JarEntry entry;
          while ((entry = is.getNextJarEntry()) != null)
            if (entry.getName().endsWith(".class")) {
              String className = entry.getName().replace("/", ".").replace(".class", "");
              if (className.startsWith("fr.inserm.u1078.tludwig.vcfprocessor")) {
                Class<?> clazz = Class.forName(className);
                if (Function.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
                  for (Class<?> c : clazzes)
                    if (c.getSimpleName().equalsIgnoreCase(clazz.getSimpleName()))
                      throw new StartUpException("Two Functions with the Same Name are present : [" + c.getSimpleName() + "]");
                  clazzes.add(clazz);
                }
              }
            }
        }
      for (String plugin : PLUGINS)
        try {
          clazzes.addAll(getFunctionsFromPlugin(plugin));
        } catch (Exception e) {
          throw new StartUpException("Could not get functions from plugin file [" + plugin + "]", e);
        }

    } catch (IOException | ClassNotFoundException e) {
      throw new StartUpException("Could not load functions", e);
    }
    return clazzes;
  }
}
