package fr.inserm.u1078.tludwig.vcfprocessor.functions.graphs;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.Point;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.GraphFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.CompareToGnomAD;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.FrequencyCorrelation;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PositiveIntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.CompareFrequenciesGraph;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.Graph;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.CompareFrequenciesAreaGraph;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * TODO DESCRIPTION
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2019-08-20
 * Checked for release on 2020-09-08
 * Unit Test defined on 2020-09-08
 */
public class GraphCompareFrequencies extends GraphFunction {

  private final TSVFileParameter tsv = new TSVFileParameter(OPT_TSV, "input.tsv", "input data");
  private final StringParameter title = new StringParameter(OPT_NAME, "dataset", "Graph Title");
  private final OutputDirectoryParameter outdir = new OutputDirectoryParameter();
  private final PositiveIntegerParameter xCol = new PositiveIntegerParameter(OPT_X, "index of the column containing X values 0-based");
  private final PositiveIntegerParameter yCol = new PositiveIntegerParameter(OPT_Y, "index of the column containing Y values 0-based");

  //public CompareFrequenciesGraph(String filename, String datasetName, int absFreqCol, int ordFreqCol, boolean keepMissing, boolean inLog){
  private double getValue(String s) {
    if ("NaN".equals(s) || s == null || s.isEmpty())
      return 0;
    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  @Override
  public ArrayList<Graph> createGraph() {
    ArrayList<Graph> graphs = new ArrayList<>();
    ArrayList<Point> points = new ArrayList<>();
    String xTitle;
    String yTitle;

    Message.info("Parsing Data");
    int abs = xCol.getIntegerValue();
    int ord = yCol.getIntegerValue();
    try (UniversalReader in = tsv.getReader()){
      String line = in.readLine();
      String[] f = line.split(T);
      xTitle = f[abs];
      yTitle = f[ord];

      double minX = 1;
      double minY = 1;

      while ((line = in.readLine()) != null) {
        f = line.split(T, -1);
        double x = getValue(f[abs]);
        double y = getValue(f[ord]);
        if (Double.isNaN(x) || Double.isNaN(y))
          Message.die("Nan in line [" + line + "]");
        points.add(new Point(x, y));
        if (x > 0 && x < minX)
          minX = x;
        if (y > 0 && y < minY)
          minY = y;
      }

      minX *= .9;
      minY *= .9;

      graphs.add(new CompareFrequenciesAreaGraph(points, this.title.getStringValue(), xTitle, yTitle, minX, minY));
      graphs.add(new CompareFrequenciesGraph(points, this.title.getStringValue(), xTitle, yTitle, minX, minY));
    } catch (Exception e) {
      Message.fatal("Could not parse file from [" + tsv.getFilename() + "]", e, true);
    }

    return graphs;
  }

  @Override
  public String getSummary() {
    return "Compares the frequencies of common variants in 2 populations (output of "+FrequencyCorrelation.class.getSimpleName()+" / "+CompareToGnomAD.class.getSimpleName()+")";
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    ArrayList<Graph> graphs = this.createGraph();
    String base = this.tsv.getBasename();

    CompareFrequenciesGraph jfs = (CompareFrequenciesGraph) graphs.get(0);
    CompareFrequenciesGraph graph = (CompareFrequenciesGraph) graphs.get(1);

    jfs.exportPNG(outdir.getDirectory()+base + ".linear.jfs.png", this.widthP.getIntegerValue(), this.heightP.getIntegerValue(), false);
    jfs.exportPNG(outdir.getDirectory()+base + ".log.jfs.png", this.widthP.getIntegerValue(), this.heightP.getIntegerValue(), true);

    graph.exportPNG(outdir.getDirectory()+base + ".linear.graph.png", this.widthP.getIntegerValue(), this.heightP.getIntegerValue(), false);
    graph.exportPNG(outdir.getDirectory()+base + ".log.graph.png", this.widthP.getIntegerValue(), this.heightP.getIntegerValue(), true);
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary())
            .addLine("4 graphs will be created : linear/log JFS/graph");
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newDirectoryAnalysis();
    def.addAnonymousFilename("tsv", "input.tsv");
    def.addAnonymousValue("width", "1200");
    def.addAnonymousValue("height", "800");
    def.addAnonymousValue("name", "`basename $tsv`");
    def.addAnonymousValue("x", "9");
    def.addAnonymousValue("y", "12");
    return new TestingScript[]{def};
  }
}
