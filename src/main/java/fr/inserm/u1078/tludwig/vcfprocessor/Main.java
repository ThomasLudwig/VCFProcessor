package fr.inserm.u1078.tludwig.vcfprocessor;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.commandline.CommandParser;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.commandline.Argument;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.FunctionFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 */
public class Main {

  //DONE replace all method[Blablabla[] args) with method[Blablabla... args) and with a suitable way to apply them to ArrayList<Blablabla> 
  //DONE force the doc to explicitly state what happens in case of multiallelic variants 
  //DONE replace ArrayList<XXXXX> with Collection<XXXXX> in method arguments
  //TODO put assert wherever it should
  //TODO everywhere, return empty List instead of null (what about tables ?)
  //TODO use LineBuilder/Columns everywhere
  //TODO override hashcode if equals is overridden : read all responses in https://stackoverflow.com/questions/2265503/why-do-i-need-to-override-the-equals-and-hashcode-methods-in-java
  //TODO when generating doc, generate doc.v2020-07-09 and symlink doc to the latest
  //TODO Leaking this in constructor --> deport constructor to factory createNewObject()...
  //DONE other versioning system than just a date/build number : VERSION.MAJOR.MINOR VERSION (change of paradigm, change in structure, isolated changes) with real tracking
  public static final String TITLE = "VCFProcessor";
  public static final String KEY_DOC = "--generatedoc";
  public static final String KEY_VERSION = "--version";
  public static final String KEY_TEST = "--test";
  public static final String KEY_SCRIPT = "--alltestingscripts";
  public static final String KEY_SCRIPTS = "--scriptsforfunction";
  public static final String KEY_DEBUG = "--debug";
  public static final String KEY_VERBOSE = "--verbose";
  public static final String KEY_GZ = "--gz";
  public static final String KEY_PLUGIN = "--plugin";
  public static final String[] ALLOWED_KEYS = {KEY_DEBUG, KEY_VERBOSE, KEY_GZ, KEY_PLUGIN};

  private static String[] args = null;
  private static final Date START = new Date();
  private static CommandParser COMMAND_PARSER = null;

  /**
   * @param args the command line arguments
   */
  public static void start(String[] args) {

    try {
      doStart(args);
    } catch (StartUpException e) {
      if (e.getCause() == null)
        die("VCFProcessor could not be started : " + e.getMessage());
      else
        die("VCFProcessor could not be started : " + e.getMessage(), e.getCause());
    }
  }
  
  private static void printVersion(String[] args) throws StartUpException {
    System.out.println(Main.getVersion().split("\\(")[0]);
  }

  private static void launchTest(String[] args) throws StartUpException {
    Message.setVerboseActive(true);
    Message.setDebugActive(true);
    if (args.length < 3)
      die("Usage : --test input.vcf output.vcf.gz");

  }

  public static final String CHANGELOG = "CHANGELOG.md";
  public static final String OVERVIEW = "overview.md";
  @SuppressWarnings("SpellCheckingInspection")
  public static final String File_FORMATS = "fileformats.md";
  public static final String DOWNLOAD = "download.md";
  public static final String CONF = "conf.py";

  private static void launchGenerateDoc(String[] args) {
    String targetDir = getValueFromCommandLine(args, "Directory");

    Message.info("Generating documentation");

    //TODO doc versioning
    writeDocumentation(targetDir + File.separator + "index.rst", getIndexDocumentation());
    writeDocumentation(targetDir + File.separator + "filters.rst", Argument.getDocumentation());
    writeDocumentation(targetDir + File.separator + "functions.rst", FunctionFactory.getFunctionsDocumentation());

    for(String page : new String[]{CHANGELOG, OVERVIEW, File_FORMATS, DOWNLOAD, CONF})
      writeDocumentation(targetDir + File.separator + page.toLowerCase(), getStringFromResource(page));

    copyImages(targetDir);
  }
  
  private static String getIndexDocumentation(){
    LineBuilder out = new LineBuilder();
    out.rstHeader("index");
    out.rstChapter("VCFProcessor "+getVersion());
    out.newLine(getStringFromResource("index.rst"));
    return out.toString();
  }
  
  private static void copyImages(String dir){
    for(String image : new String[]{"logo.vcfprocessorbig.png"})
      copyImageToDir(image, dir);
  }
  
  private static void copyImageToDir(String image, String dir){
    try (
      InputStream is = Main.class.getResourceAsStream("/"+image);
      OutputStream os = Files.newOutputStream(Paths.get(dir + File.separator + image))){
      if(is == null)
        throw new FileNotFoundException("Image ["+image+"] not found");
      int length;
      byte[] bytes = new byte[1024];
      while ((length = is.read(bytes)) != -1) {
        os.write(bytes, 0, length);
    }
    } catch (IOException e) {
      throw new StartUpException("Could not copy image ["+image+"] to dir ["+dir+"]",e);
    }
  }
  
  private static String getStringFromResource(String resource){
    LineBuilder out = new LineBuilder();
    InputStream is = Main.class.getResourceAsStream("/"+resource);
    assert is != null : "Resource not found ["+resource+"]";
    BufferedReader in = new BufferedReader(new InputStreamReader(is));
    String line;
    try {
      while((line = in.readLine()) != null){
        out.newLine(line);
      }
    } catch (IOException e) {
      throw new StartUpException("Could not read resource ["+resource+"]", e);
    }
    return out.toString();
  }

  private static void writeDocumentation(String filename, String documentation) {
    try {
      PrintWriter out = new PrintWriter(new FileWriter(filename));
      out.println(documentation);
      out.close();
    } catch (IOException ex) {
      throw new StartUpException("Could not write documentation to [" + filename + "]", ex);
    }
  }

  private static String getValueFromCommandLine(String[] args, String needle) {
    if (args.length < 2)
      throw new StartUpException("Option [" + args[0] + "] must be followed be a "+needle+".");
    String targetDirectory = args[1];
    if (targetDirectory.startsWith("-"))
      throw new StartUpException("Option [" + args[0] + "] must be followed be a "+needle+". Invalid value {" + targetDirectory + "}");
    return targetDirectory;
  }

  private static void launchAllTestingScripts(String[] args) {
    Message.setVerboseActive(true);
    Message.setDebugActive(true);
    String targetDirectory = getValueFromCommandLine(args, "Directory");
    FunctionFactory.generateTestingScripts(targetDirectory);
  }

  private static void launchScriptsForFunction(String[] args) {
    Message.setVerboseActive(true);
    Message.setDebugActive(true);
    String functionName = getValueFromCommandLine(args, "Function Name");
    FunctionFactory.generateTestingScriptForFunction(functionName);
  }

  private static void doStart(String[] args) throws StartUpException {
    Message.info("Welcome to " + TITLE + " " + getVersion() + " on " + System.getProperty("os.name"));

    //get plugin directory
    String pDir = getDefaultPluginDirectory("VCFProcessor_Plugins");
    for (int i = 0; i < args.length-1; i++) {
      String arg = args[i];
      String val = args[i+1];
      if(KEY_PLUGIN.equals(args[i])) {
        if (val == null)
          throw new StartUpException("Argument following " + arg + " must be followed be a directory.");
        if (val.startsWith("-"))
          throw new StartUpException("Argument following " + arg + " must be followed be a directory. Invalid value {" + val + "}");
        pDir = val;
        break;
      }
    }

    Message.info("Will look for plugins in " + pDir);

    for (String plugin : FunctionFactory.initPlugins(pDir))
      Message.info("Plugin : " + plugin);

    //check for Non Function usage    
    if (args.length > 0) {
      String arg = args[0];
      if (arg != null && !arg.isEmpty())
        switch (arg.toLowerCase()) {
          case KEY_TEST:
            Main.launchTest(args);
            return;
          case KEY_VERSION:
            Main.printVersion(args);
            return;
          case KEY_DOC:
            Main.launchGenerateDoc(args);
            return;
          case KEY_SCRIPT:
            Main.launchAllTestingScripts(args);
            return;
          case KEY_SCRIPTS:
            Main.launchScriptsForFunction(args);
            return;
        }
    }

    //check for extra arguments
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      assert arg != null : "Argument "+i+" is null";
      switch (arg) {
        case KEY_VERBOSE:
          Message.setVerboseActive(true);
          break;
        case KEY_DEBUG:
          Message.setDebugActive(true);
          break;
        case KEY_GZ:
          Function.setOutputBgzipped();
          break;
      }
    }

    Main.args = args;
    Function f = FunctionFactory.getFunction(args);
    assert f != null : "Function is null for ["+ Arrays.toString(args) +"]";
    if (f.start(args))
      f.execute();
  }

  public static String getJar() throws StartUpException {
    return getJar(Main.class);
  }

  public static String getJar(Class<?> clazz) throws StartUpException {
    try {
      String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
      String decoded = URLDecoder.decode(path, "UTF-8");
      if (decoded.endsWith("!/"))
        decoded = decoded.substring(0, decoded.length() - 2);
      if (decoded.startsWith("file:"))
        decoded = decoded.substring(5);
      return decoded;

    } catch (UnsupportedEncodingException e) {
      throw new StartUpException("Unable to get jar from class : " + clazz.getSimpleName(), e);
    }
  }

  public static String getDefaultPluginDirectory(String dir) throws StartUpException {
    String jar = getJar();
    int index = jar.lastIndexOf(File.separator);
    if (index > -1) {
      return jar.substring(0, index + 1) + dir;
    }

    return ".";
  }

  public static CommandParser getCommandParser() {
    if (COMMAND_PARSER == null)
      COMMAND_PARSER = new CommandParser(args);
    return COMMAND_PARSER;
  }

  public static Date getStart() {
    return START;
  }

  public static String getVersion() {
    final String filename = "/CHANGELOG.md";
    try {
      String line;
      InputStream is = Main.class.getResourceAsStream(filename);
      assert is != null : filename + " not found";
      BufferedReader in = new BufferedReader(new InputStreamReader(is));
      while ((line = in.readLine()) != null) {
        if (line.toLowerCase().startsWith("## "))
          return line.substring(3);
      }
    } catch (IOException e) {
      //Ignore
    }
    return "v?.?.?(????-??-??)";
  }

  public static void die(String fatalMessage){
    Message.fatal(fatalMessage);
    System.exit(-1);
  }

  public static void die(String fatalMessage, Throwable throwable){
    Message.fatal(fatalMessage, throwable);
    System.exit(-1);
  }

}
