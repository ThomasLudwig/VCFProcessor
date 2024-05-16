package fr.inserm.u1078.tludwig.vcfprocessor.testing;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Class that generates the Testing Script for a Function
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-08-17
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 * Last Tested on xxxx-xx-xx
 */
public class TestingScript {

  public static final boolean FILE = true;
  public static final boolean DIR = false;
  public static final int TAB = 1;
  public static final int TAB2 = TAB+TAB;

  private final ArrayList<Parameter> parameters;
  private final boolean singleFile;
  private final int mismatchingLines;
  private String end;

  public TestingScript(boolean outputType, int mismatchingLines) {
    this.singleFile = outputType;
    this.mismatchingLines = mismatchingLines;
    this.parameters = new ArrayList<>();
  }

  public static TestingScript newFileAnalysis() {
    return new TestingScript(FILE, 0);
  }

  public static TestingScript newDirectoryAnalysis() {
    return new TestingScript(DIR, 0);
  }

  public static TestingScript newFileTransform() {
    return new TestingScript(FILE, 1);
  }

  public static TestingScript newDirectoryTransform() {
    return new TestingScript(DIR, 1);
  }

  public void addAnonymousFilename(String name, String value) {
    this.addParameter(new Parameter(name, value, Type.ANONYMOUS_FILENAME));
  }

  public void addAnonymousValue(String name, String value) {
    this.addParameter(new Parameter(name, value, Type.ANONYMOUS_VALUE));
  }

  public void addNamingFilename(String name, String value) {
    this.addParameter(new Parameter(name, value, Type.NAMING_FILENAME));
  }

  public void addNamingValue(String name, String value) {
    this.addParameter(new Parameter(name, value, Type.NAMING_VALUE));
  }

  private void addParameter(Parameter parameter) throws RuntimeException {
    for (Parameter cur : this.parameters)
      if (cur.name.equals(parameter.name))
        throw new RuntimeException("Script already has a parameter named [" + cur.name + "]");
    this.parameters.add(parameter);
  }

  public String getScriptName() {
    String filename = "generated.test";
    StringBuilder theEnd = new StringBuilder();
    for (Parameter p : parameters)
      theEnd.append(p.getResults());
    end = theEnd.toString();
    filename += end+".sh";
    return filename;
  }

  public LineBuilder getHeader() {
    LineBuilder out = new LineBuilder();
    out.newLine("#!/bin/bash");
    out.newLine();
    out.newLine("DIR=\"$( cd \"$( dirname \"${BASH_SOURCE[0]}\" )\" >/dev/null 2>&1 && pwd )\";");
    out.newLine("func=`basename $DIR`;");
    out.newLine();
    out.newLine("r=`date +\"%Y-%m-%d\"`;");
    return out;
  }

  public LineBuilder getParameterDeclaration() {
    LineBuilder out = new LineBuilder();
    for (Parameter p : parameters)
      out.newLine(p.getDefinition());
    out.newLine();
    if (this.singleFile)
      out.newLine("out=$DIR/results" + end + ".$r;");
    else
      out.newLine("outdir=$DIR/results" + end + ".$r;");
    out.newLine("log=$DIR/log" + end + ".$r;");
    out.newLine("exp=$DIR/expectedresults" + end + ";");
    out.newLine();
    return out;
  }

  public String getArguments() {
    StringBuilder ret = new StringBuilder();
    for (Parameter p : parameters)
      ret.append(" ").append(p.getArgument());
    return ret.toString();
  }

  public LineBuilder getCommand() {
    LineBuilder out = new LineBuilder();
    out.append("/PROGS/bin/VCFProcessor.dev $func").addSpace(getArguments());

    if (this.singleFile)
      out.addSpace("--out $out");
    else
      out.addSpace("--outdir $outdir");
    out.addSpace("2> $log;");
    out.newLine();
    return out;
  }

  public LineBuilder getTest() {
    if (this.singleFile)
      return testSingleFile();
    else
      return testDirectory();
  }

  public LineBuilder getFooter() {
    LineBuilder out = new LineBuilder();
    out.newLine("exit 0");
    out.newLine();
    out.newLine("#Script generated on " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    return out;
  }

  public LineBuilder getContent() {
    LineBuilder out = new LineBuilder();
    out.newLine(this.getHeader());
    out.newLine(this.getParameterDeclaration());
    out.newLine(this.getCommand());
    out.newLine(this.getTest());
    out.newLine(this.getFooter());
    return out;
  }

  public LineBuilder testSingleFile() {
    LineBuilder out = new LineBuilder(TAB2);
    out.newLine("if [ ! -f \"$out\" ];");
    out.newLine("then");
    out.newLine(TAB, ">&2 echo \"${BASH_SOURCE[0]} KO missing output $out\";");
    out.newLine(TAB, "mv $out $out.KO;");
    out.newLine("fi");
    out.newLine("dif=`diff $exp $out | wc -l`;");
    out.newLine();
    out.newLine("if [ \"$dif\" -eq \"" + (4 * this.mismatchingLines) + "\" ]");
    out.newLine("then");
    out.newLine(TAB, "echo \"${BASH_SOURCE[0]} OK\";");
    out.newLine(TAB, "rm -rf $DIR/results" + end + ".*.OK;");
    out.newLine(TAB, "rm $log;");
    out.newLine(TAB, "mv $out $out.OK;");
    out.newLine("else");
    out.newLine(TAB, ">&2 echo \"${BASH_SOURCE[0]} KO\";");
    out.newLine(TAB, "mv $out $out.KO;");
    out.newLine("fi");
    return out;
  }

  public LineBuilder testDirectory() { //TODO force LineBuilder as return object everywhere
    LineBuilder out = new LineBuilder(TAB2);
    out.newLine("for f in $exp/*;");
    out.newLine("do");
    out.newLine(TAB, "base=`basename $f`");
    out.newLine(TAB, "if [ ! -f \"$outdir/$base\" ];");
    out.newLine(TAB, "then");
    out.newLine(TAB2, ">&2 echo \"${BASH_SOURCE[0]} KO missing output $outdir/$base\";");
    out.newLine(TAB2, "mv $outdir $outdir.KO;");
    out.newLine(TAB2, "exit 0;");
    out.newLine(TAB, "fi");
    out.newLine(TAB, "dif=`diff $exp/$base $outdir/$base | wc -l`;");
    out.newLine(TAB, "if [ \"$dif\" -ne \"" + (4 * this.mismatchingLines) + "\" ]");
    out.newLine(TAB, "then");
    out.newLine(TAB2, ">&2 echo \"${BASH_SOURCE[0]} KO\";");
    out.newLine(TAB2, "mv $outdir $outdir.KO;");
    out.newLine(TAB2, "exit 0;");
    out.newLine(TAB, "fi");
    out.newLine("done");
    out.newLine();
    out.newLine("echo \"${BASH_SOURCE[0]} OK\";");
    out.newLine("rm -rf $DIR/results" + end + ".*.OK;");
    out.newLine("rm $log;");
    out.newLine("mv $outdir $outdir.OK;");
    return out;
  }

  public static TestingScript[] getEmpty() {
    return new TestingScript[]{};
  }

  public static TestingScript[] getGraphScript() {
    TestingScript def = TestingScript.newDirectoryAnalysis();
    def.addAnonymousFilename("tsv", "input.tsv");
    def.addAnonymousValue("width", "800");
    def.addAnonymousValue("height", "800");
    def.addAnonymousValue("name", "$func for `basename $tsv`");
    return new TestingScript[]{def};
  }

  public static TestingScript[] getSimpleVCFAnalysisScript() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    return new TestingScript[]{def};
  }

  public static TestingScript[] getSimpleVCFPedAnalysisScript() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("ped", "ped");
    return new TestingScript[]{def};
  }

  public static TestingScript[] getSimpleVCFTransformScript() {
    TestingScript def = TestingScript.newFileTransform();
    def.addAnonymousFilename("vcf", "vcf");
    return new TestingScript[]{def};
  }

  public static TestingScript[] getSimpleVCFPedTransformScript() {
    TestingScript def = TestingScript.newFileTransform();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("ped", "ped");
    return new TestingScript[]{def};
  }

  public enum Type {
    ANONYMOUS_VALUE,
    NAMING_VALUE,
    ANONYMOUS_FILENAME,
    NAMING_FILENAME,
  }

  private static class Parameter {

    private final String name;
    private final String value;
    private final Type type;

    Parameter(String name, String value, Type type) {
      this.name = name;
      this.value = value;
      this.type = type;
    }

    boolean isFilename() {
      switch (type) {
        case ANONYMOUS_FILENAME:
        case NAMING_FILENAME:
          return true;
      }
      return false;
    }

    boolean isAnonymous() {
      switch (type) {
        case ANONYMOUS_FILENAME:
        case ANONYMOUS_VALUE:
          return true;
      }
      return false;
    }

    String getDefinition() {
      if (isFilename())
        return name + "=$DIR/" + value + ";";
      else
        return name + "=\"" + value + "\"";
    }

    String getArgument() {
      return "--" + name + " \"$" + name + "\"";
    }

    String getResults() {
      if (this.isAnonymous())
        return "";
      else {
        if (value.startsWith("vcf."))
          return "." + value.substring(4);
        if (value.endsWith(".vcf"))
          return "." + value.substring(0, value.length() - 4);
        if (value.endsWith(".tsv"))
          return "." + value.substring(0, value.length() - 4);
        return "." + value.replace(",", "-").replace("/", "-").replace("\\", "-").replace(":", "-");
      }
    }

    @Override
    public String toString() {
      return value;
    }
  }
}
