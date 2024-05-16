package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ParameterException;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.Parameter;
import fr.inserm.u1078.tludwig.maok.tools.DateTools;
import fr.inserm.u1078.tludwig.maok.tools.JavaTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.Main;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.BgzipOutputStream;
import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.commandline.CommandParser;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.GzParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.BufferedOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

/**
 * An abstract Function to be implemented
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 20 mai 2015
 */
public abstract class Function {
  public static final String OPT_REF = "--ref"; //TODO OPT as enum
  public static final String OPT_CPU = "--cpu";

  @SuppressWarnings("SpellCheckingInspection")
  public static final String OPT_TPED = "--tped";
  public static final String OPT_FREX = "--frex";
  public static final String OPT_VCF = "--vcf";
  public static final String OPT_SNP = "--snp";
  public static final String OPT_INDEL = "--indel";
  public static final String OPT_BAM = "--bam";
  public static final String OPT_KEEP = "--bam";
  public static final String OPT_POSITION = "--position";
  public static final String OPT_PED = "--ped";
  public static final String OPT_TSV = "--tsv";
  public static final String OPT_PNG = "--png";
  public static final String OPT_OUT = "--out";
  public static final String OPT_PAD = "--pad";
  public static final String OPT_OUTDIR = "--outdir";
  public static final String OPT_PREFIX = "--prefix";
  public static final String OPT_ERR = "--log";
  public static final String OPT_EFF = "--effects";
  public static final String OPT_LNK = "--link";
  public static final String OPT_FILTER = "--filter";
  public static final String OPT_THRESHOLD = "--threshold";
  public static final String OPT_SAMPLE = "--sample";
  public static final String OPT_METHOD = "--method";
  public static final String OPT_TABLE = "--table";
  public static final String OPT_SOURCE = "--source";
  public static final String OPT_FILE = "--file";
  public static final String OPT_OPT = "--opt";
  public static final String OPT_INDEX = "--index";
  public static final String OPT_FILES = "--files";
  public static final String OPT_CHROM = "--chrom";
  public static final String OPT_POS = "--pos";
  public static final String OPT_BED = "--bed";
  public static final String OPT_KIT = "--kit";
  public static final String OPT_EXON = "--exon";
  public static final String OPT_GENE = "--gene";
  public static final String OPT_HOMO = "--homo";
  public static final String OPT_MISSING = "--missing";
  @SuppressWarnings("SpellCheckingInspection")
  public static final String OPT_NO_HOMO = "--nohomo";
  public static final String OPT_MODE = "--mode";
  public static final String OPT_TYPE = "--type";
  public static final String OPT_POP = "--pop";
  public static final String OPT_GENES = "--genes";
  public static final String OPT_CSQ = "--csq";
  public static final String OPT_TITLE = "--title";
  public static final String OPT_LOW = "--low";
  public static final String OPT_HIGH = "--high";
  public static final String OPT_QUERY = "--query";
  public static final String OPT_DP = "--dp";
  public static final String OPT_GQ = "--gq";
  public static final String OPT_P2 = "--p2";
  public static final String OPT_RATIO = "--ratio";
  public static final String OPT_MIN = "--min";
  public static final String OPT_STRICT = "--strict";
  public static final String OPT_OVER = "--overwrite";
  public static final String OPT_NUMBER = "--number";
  public static final String OPT_SIZE = "--size";
  public static final String OPT_FIX = "--fix";
  public static final String OPT_WIDTH = "--width";
  public static final String OPT_HEIGHT = "--height";
  public static final String OPT_NAME = "--name";
  public static final String OPT_X = "--x";
  public static final String OPT_Y = "--y";
  public static final String OPT_MAX = "--max";
  public static final String OPT_JFS = "--jfs";
  public static final String OPT_FRQ = "--frq";
  public static final String OPT_REPORT = "--report";

  public static final String OUT_LOG = "log";
  public static final String OUT_VCF = "vcf";
  public static final String OUT_BED = "bed";
  public static final String OUT_PED = "ped";
  public static final String OUT_TSV = "tsv";
  public static final String OUT_HTML = "html";
  public static final String OUT_NONE = "NONE";
  public static final String OUT_TXT = "txt";
  public static final String OUT_SQL = "sql";
  public static final String OUT_PNG = "png";

  public static final String T = "\t";

  public static final String TYPE_VCF_FILTER = "VCF Filter Functions";
  public static final String TYPE_VCF_TRANSFORM = "VCF Transformation Functions";
  public static final String TYPE_VCF_ANNOTATE = "VCF Annotation Functions";
  public static final String TYPE_ANALYSIS = "Analysis Functions";
  public static final String TYPE_FORMATTING = "Formatting Functions";
  public static final String TYPE_OTHER = "Other Functions";
  public static final String TYPE_GRAPHS = "Graphics";
  public static final String TYPE_UNKNOWN = "Functions of Unknown Type";

  public static final String[] TYPES = new String[]{
    TYPE_VCF_FILTER,
    TYPE_VCF_TRANSFORM,
    TYPE_VCF_ANNOTATE,
    TYPE_ANALYSIS,
    TYPE_FORMATTING,
    TYPE_OTHER,
    TYPE_GRAPHS,
    TYPE_UNKNOWN
  };

  //TODO gradle has a lot of compatibility uses (strong correlation with java version), maybe switch to another compiler ?

  public final String getFunctionType() {
    String jar = Main.getJar(this.getClass());
    String main = Main.getJar();
    if(jar.equals(main)){
      switch (this.getPackage()) {
        case "vcffilter":
          return TYPE_VCF_FILTER;
        case "vcftransform":
          return TYPE_VCF_TRANSFORM;
        case "vcfannotate":
          return TYPE_VCF_ANNOTATE;
        case "analysis":
          return TYPE_ANALYSIS;
        case "format":
          return TYPE_FORMATTING;
        case "other":
          return TYPE_OTHER;
        case "graphs":
          return TYPE_GRAPHS;
      }
      //TODO what about plugins
      return TYPE_UNKNOWN;
    }
    return jar; //TODO maybe jar:packages.name ?
  }

  /**
   * The starting time of the function
   */

  public final OutputParameter outFilename = new OutputParameter(OPT_OUT, this.getOutputExtension(), "ResultFile", "File that will contain the function's results", OutputParameter.TYPE_OUT);
  public final OutputParameter errFilename = new OutputParameter(OPT_ERR, OUT_LOG, "LogFile", "File that will contain the function's log", OutputParameter.TYPE_ERR);

  
  private static boolean FIRST_CALL_OUTPUT_BGZIPPED = true;
  private static boolean BGZIPPED_OUTPUT = false;
  private PrintStream outStream = null;
  private PrintStream errStream = null;
  private static final PrintStream STD_OUT = System.out;
  private static final PrintStream STD_ERR = System.err;

  public abstract String getOutputExtension();
  
  //TODO add --gz to the docs (command line, and rtd
  //TODO hunt all PrintWriters and open them as bgzip if they are VCF and bgzip is activated
  public static void setOutputBgzipped(){ 
    if(FIRST_CALL_OUTPUT_BGZIPPED)
      BGZIPPED_OUTPUT = true;
    else
      Main.die("Call to "+Function.class.getSimpleName()+".setBgzippedOutput() can only made once");
    FIRST_CALL_OUTPUT_BGZIPPED = false;
  }
  
  public static boolean isOutputBgzipped(){
    return BGZIPPED_OUTPUT;
  }

  public final boolean start(String[] args) { //TODO not called, except through reflect... hard to debug
    StringBuilder msg = new StringBuilder();
    for (Parameter p : this.getParameters())
      try {
        p.parseParameter(args);
      } catch (ParameterException e) {
        msg.append("\n").append(e.getMessage());
      }
    
    ArrayList<String> allowed = CommandParser.getAllowedKeys(args);
    for(String a : args){
      if(a.startsWith("-") && !allowed.contains(a.toLowerCase()))
        msg.append("\n" + "Unexpected argument [").append(a).append("]");
    }
    
    if (msg.length() > 0) {
      this.showUsage();
      this.fatalAndQuit("Invalid argument(s)" + msg);
      return false;
    }

    try {
      String out = outFilename.getStringValue();
      if (out != null) {
        if(BGZIPPED_OUTPUT || out.endsWith(".gz"))
          outStream = new PrintStream(new BgzipOutputStream(out.endsWith(".gz") ? out : out+".gz"));
        else
          outStream = new PrintStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(out))));
        
        System.setOut(outStream);
      } else {
        if(BGZIPPED_OUTPUT){
          outStream = new PrintStream(new BgzipOutputStream(STD_OUT));
          System.setOut(outStream);
        }
      }

      String err = errFilename.getStringValue();
      if (err != null) {
        errStream = new PrintStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(err))));
        System.setErr(errStream);
      }
    } catch (IOException e) {
      this.fatalAndQuit("Could not redirect output", e);
      return false;
    }
    return true;
  }

  public OutputParameter getOutFilename() {
    return outFilename;
  }

  public OutputParameter getErrFilename() {
    return errFilename;
  }
/*
  public final void checkParameters() throws ParameterException {
    ArrayList<String> keys = new ArrayList<>();
    for (Parameter p : this.getParameters()) {
      String key = p.getKey();
      if (keys.contains(key))
        throw new ParameterException("Duplicate key [" + key + "] required by Function " + this.getFunctionName());
      keys.add(key);
    }
  }
*/

  /**
   * Executes the function :
   * <li>
   * <ul>Prints the welcome message (function name/class, function
   * description, arguments</ul>
   * <ul>Prints the start time</ul>
   * <ul>Executes the function</ul>
   * <ul>Prints the end date and duration in seconds of the function</ul>
   * </li>
   */
  public final void execute() {
    Date startDate = new Date();
    this.printWelcomeMessage();
    Message.info("Called from " + System.getProperty("user.name") + "@" + getHostname() + ":" + System.getProperty("user.dir") + " [" + startDate + "]");
    try {
      this.executeFunction();
    } catch (Exception e) {
      Function.this.fatalAndQuit("Unable to execute function " + this.getFunctionName(), e);

    }
    Date endDate = new Date();
    Message.info("Computation Ended at " + endDate + " (duration " + DateTools.durationInSeconds(startDate, endDate) + "s), waiting for output file to be written.");
    this.quit();
  }

  private String getHostname() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "localhost";
    }
  }

  public final void println(Object o) {
    System.out.println(o);
  }

  public void fatalAndQuit(String message, Exception e) {
    Message.fatal(message, e);
    this.quit();
  }
  
  public void fatalAndQuit(String message){
    Message.fatal(message);
    this.quit();
  }
  
  public void quit() {
    if (outStream != null)
      outStream.close();
    System.setOut(STD_OUT);
    if (errStream != null)
      errStream.close();
    System.setErr(STD_ERR);
    Message.setProgressBarActive(true);
    System.exit(-1);
  }

  public final String getPackage() {
    String ret = this.getClass().getPackage().getName();
    ret = ret.substring(ret.lastIndexOf(".") + 1);
    return ret;
  }

  /**
   * Prints the welcome message : Function name / class Function description
   * Function parameters
   */
  private void printWelcomeMessage() {
    Message.info(this.getPackage() + "/" + this.getFunctionName() + " (" + JavaTools.getJarFileName(this.getClass()) + ")");
    this.printSummary();
    this.printParameters();
    this.printMemoryUsage();
  }

  public void printMemoryUsage() {
    // Get current size of heap in bytes
    long heapSize = Runtime.getRuntime().totalMemory();
    // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
    long heapMaxSize = Runtime.getRuntime().maxMemory();
    // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
    long heapFreeSize = Runtime.getRuntime().freeMemory();

    int heapMax = (int)(heapMaxSize / (1024*1024*1024));
    int heapCurrent = (int)(heapSize / (1024*1024*1024));
    int heapFree = (int)(heapFreeSize / (1024*1024*1024));
    Message.info("Max Heap Size "+heapMax+"GB / Current Heap Size "+heapCurrent+"GB / Free Heap Size "+heapFree+"GB.");
  }

  /**
   * Prints the function description
   */
  private void printSummary() {
    Message.info("Description : \n" + Description.descriptionToPlainText(this.getSummary()));
  }

  /**
   * Prints the function parameters
   */
  private void printParameters() {
    StringBuilder msg = new StringBuilder("Parameters :");
    for (Parameter parameter : this.getParameters())
      msg.append("\n").append(parameter.getFullDescription());
    Message.info(msg.toString());
  }

  /**
   * Gets the Function name
   *
   * @return the name of the function
   */
  public final String getFunctionName() {
    return this.getClass().getSimpleName();
  }

  /**
   * Gets the Function summary
   *
   * @return the summary description of the function
   */
  public abstract String getSummary();

  /**
   * Gets the Function Description
   *
   * @return the full description of the function
   */
  public abstract Description getDescription();
  
  public abstract TestingScript[] getScripts();

  public final String getUsage() {
    LineBuilder msg = new LineBuilder();
    msg.newLine(Message.yellow(this.getClass().getSimpleName()));
    
    int keyLength = 0 ;
    int exampleLength = 0;
    for (Parameter param : this.getParameters()){
      keyLength = Math.max(keyLength, param.getKey().length());
      exampleLength = Math.max(exampleLength, param.getExample().length());
    }

    keyLength = Math.max(keyLength, GzParameter.GZ_PARAMETER.getKey().length());
    exampleLength = Math.max(exampleLength, GzParameter.GZ_PARAMETER.getExample().length());
    
    for (Parameter param : this.getParameters())
      msg.newLine(param.getCommandLineDescription(keyLength, exampleLength)); //space is in the getCommandLine()
    msg.newLine(GzParameter.GZ_PARAMETER.getCommandLineDescription(keyLength, exampleLength));
    msg.append(Message.white("(" + Description.descriptionToPlainText(this.getSummary()) + ")"));
    return msg.toString();
  }

  public final String getShortUsage() {
    StringBuilder msg = new StringBuilder(Message.yellow(this.getClass().getSimpleName()));
    for (Parameter param : this.getParameters())
      if (param != null && !(param instanceof OutputParameter))
        msg.append(param.getCommandLine()); //space is in the getCommandLine()
    return msg + " " + Message.white("(" + Description.descriptionToPlainText(this.getSummary()) + ")");
  }

  public void showUsage() {
    Main.die("Usage : " + JavaTools.command(this.getClass()) + "\n" + this.getUsage() + "\n" + this.getDescription().asText());
  }

  public final Parameter[] getParameters() { //there are mention of VCFFunction ... for sorting purpose
    ArrayList<Field> fields = new ArrayList<>();
    ArrayList<Parameter> ret = new ArrayList<>();

    try {
      for (Field field : this.getClass().getFields())
        if (Parameter.class.isAssignableFrom(field.getType()))
          fields.add(field);
      for (Field field : this.getClass().getDeclaredFields())
        if (Parameter.class.isAssignableFrom(field.getType())) {
          field.setAccessible(true);
          fields.add(field);
        }

      //Remove duplicate, and sort parameter :
      //vcf, ped, others, function (out,err)
      ArrayList<Field> removes = new ArrayList<>();
      for (Field field : fields)
        if (field.getDeclaringClass().equals(VCFFunction.class)) {
          removes.add(field);
          Parameter param = (Parameter) field.get(this);
          if (!ret.contains(param))
            ret.add(param);
        }

      fields.removeAll(removes);
      removes.clear();
      for (Field field : fields)
        if (field.getDeclaringClass().equals(VCFPedFunction.class)) {
          removes.add(field);
          Parameter param = (Parameter) field.get(this);
          if (!ret.contains(param))
            ret.add(param);
        }

      fields.removeAll(removes);
      removes.clear();
      for (Field field : fields)
        if (!field.getDeclaringClass().equals(Function.class)) {
          removes.add(field);
          Parameter param = (Parameter) field.get(this);
          if (!ret.contains(param))
            ret.add(param);
        }

      fields.removeAll(removes);
      removes.clear();
      for (Field field : fields) {
        removes.add(field);
        Parameter param = (Parameter) field.get(this);
        if (!ret.contains(param))
          ret.add(param);
      }
      fields.removeAll(removes);

    } catch (IllegalAccessException | IllegalArgumentException | SecurityException e) {
      this.fatalAndQuit("Problem getting field for function " + this.getFunctionName(), e);
    }

    //check validity
    ArrayList<String> keys = new ArrayList<>();
    for (Parameter param : ret) {
      String key = param.getKey();
      if (keys.contains(key))
        this.fatalAndQuit("Duplicate key [" + key + "] in the definition of Function " + this.getFunctionName() + " (" + this.getFunctionType() + ")");
      else
        keys.add(key);
    }

    return ret.toArray(new Parameter[0]);
  }

  /**
   * Really executes the function
   *
   * @throws java.lang.Exception if something went wrong
   */
  @SuppressWarnings("unused")
  public abstract void executeFunction() throws Exception; //TODO should be FunctionException
  
  public static PrintWriter getPrintWriter(String filename, boolean bgzip) throws IOException{
    if(bgzip)
      return new PrintWriter(new BgzipOutputStream(filename.endsWith(".gz") ? filename : filename + ".gz"));
    return new PrintWriter(new FileWriter(filename));
  }

  public static PrintWriter getPrintWriter(String filename) throws IOException{
    if(filename.endsWith(".gz"))
        return getPrintWriter(filename, true);
    return getPrintWriter(filename, BGZIPPED_OUTPUT);
  }  
}
