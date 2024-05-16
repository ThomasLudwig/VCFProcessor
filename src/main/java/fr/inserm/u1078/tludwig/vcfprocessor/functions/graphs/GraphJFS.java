package fr.inserm.u1078.tludwig.vcfprocessor.functions.graphs;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.Function.OPT_NAME;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.Function.OPT_TSV;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.GraphFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.JointFrequencySpectrum;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.Graph;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.JFSGraph;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Graph Function for Joint Frequency Spectrum Data
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-15
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class GraphJFS extends GraphFunction {
  private final TSVFileParameter tsv = new TSVFileParameter(OPT_TSV, "input.tsv", "input data");
  private final StringParameter title = new StringParameter(OPT_NAME, "title", "Title (will be printed on the graph)");
  private final StringParameter x = new StringParameter(OPT_X, "Set1", "Name of the first Set");
  private final StringParameter y = new StringParameter(OPT_Y, "Set2", "Name of the second Set");
  private final StringParameter max = new StringParameter(OPT_MAX, "Scale Max", "Top Number of variant on legend. Enter \"null\" to use the maximal value from data");
  
  private final OutputDirectoryParameter outdir = new OutputDirectoryParameter();
  
  @Override
  public String getSummary() {
    return "Create a graph for the results of "+JointFrequencySpectrum.class.getSimpleName();
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary())
            .addWarning("Expects a NxN matrix, where matrix[a][b] is the number of variants seen a times in the first set and b times in the second set.");
  }
    
  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    ArrayList<Graph> graphs = this.createGraph();
    Graph graph = graphs.get(0);
    String png = outdir.getDirectory()+tsv.getBasename()+".png";    
    //String png = outdir.getDirectory()+"XXX.png";
    graph.exportGraphAsPNG(png, this.widthP.getIntegerValue(), this.heightP.getIntegerValue());
  }
  
  @Override
  public ArrayList<Graph> createGraph() {
    ArrayList<Graph> graphs = new ArrayList<>();
    graphs.add(new JFSGraph(this.tsv.getFilename(), title.getStringValue(), x.getStringValue(), y.getStringValue(), max.getStringValue()));
    return graphs;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newDirectoryAnalysis();
    def.addAnonymousFilename("tsv", "BORDEAUX.BREST.tsv");
    def.addAnonymousValue("name", "JFS Example");
    def.addAnonymousValue("width", "800");
    def.addAnonymousValue("height", "800");
    def.addAnonymousValue("x", "BORDEAUX");
    def.addAnonymousValue("y", "BREST");
    def.addAnonymousValue("max", "1000");    
    return new TestingScript[]{def};
  }
}
