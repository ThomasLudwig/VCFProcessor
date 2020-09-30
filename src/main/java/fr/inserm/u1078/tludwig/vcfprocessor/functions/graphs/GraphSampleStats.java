package fr.inserm.u1078.tludwig.vcfprocessor.functions.graphs;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.Function.OPT_NAME;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.Function.OPT_TSV;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.GraphFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.SampleStats;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.Graph;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.SampleStatsGraph;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-08
 * Checked for release on 2020-09-08
 * Unit Test defined on 2020-09-08
 */
public class GraphSampleStats extends GraphFunction {
  private final TSVFileParameter tsv = new TSVFileParameter(OPT_TSV, "input.tsv", "input data");
  private final StringParameter title = new StringParameter(OPT_NAME, "title", "Title (will be printed on the graph)");
  private final OutputDirectoryParameter outdir = new OutputDirectoryParameter();

  @Override
  public String getSummary() {
    return "Create a graph for the results of "+SampleStats.class.getSimpleName();
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary())
            .addLine("There will be a graph for each of the following values")
            .addItemize(SampleStatsGraph.TITLE);
  }
  
  @Override
  public void executeFunction() throws Exception {
    
    for(Graph graph : this.createGraph()){
      String ext = ((SampleStatsGraph)graph).getExtensions();
      String png = outdir.getDirectory()+tsv.getBasename()+"."+ext+".png";    
      graph.exportGraphAsPNG(png, this.widthP.getIntegerValue(), this.heightP.getIntegerValue());
    }
  }
  
  @Override
  public ArrayList<Graph> createGraph() {
    ArrayList<Graph> graphs = new ArrayList<>();
    for(int type : SampleStatsGraph.TYPES){
      Graph graph = new SampleStatsGraph(this.tsv.getFilename(), title.getStringValue(), type);
      graphs.add(graph);
    }    
    return graphs;
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newDirectoryAnalysis();
    def.addAnonymousFilename("tsv", "input.tsv");
    def.addAnonymousValue("width", "1000");
    def.addAnonymousValue("height", "800");
    def.addAnonymousValue("name", "$func for `basename $tsv`");
    return new TestingScript[]{def};
  }



}
