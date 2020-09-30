package fr.inserm.u1078.tludwig.vcfprocessor.functions.graphs;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.Function.OPT_NAME;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.Function.OPT_TSV;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.GraphFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.F2;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.F2Individuals;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.F2Graph;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.Graph;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-07
 * Checked for release on 2020-09-07
 * Unit Test defined on 2020-09-07
 */
public class GraphF2 extends GraphFunction {
  private final TSVFileParameter tsv = new TSVFileParameter(OPT_TSV, "input.tsv", "input data");
  private final StringParameter title = new StringParameter(OPT_NAME, "title", "Title (will be printed on the graph)");
  private final OutputDirectoryParameter outdir = new OutputDirectoryParameter();

  @Override
  public String getSummary() {
    return "Create a graph for the results of "+F2.class.getSimpleName()+" or "+F2Individuals.class.getSimpleName();
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary());
  }

  @Override
  public void executeFunction() throws Exception {
    ArrayList<Graph> graphs = this.createGraph();
    Graph graph = graphs.get(0);
    String png = outdir.getDirectory()+tsv.getBasename()+".png";    
    graph.exportGraphAsPNG(png, this.widthP.getIntegerValue(), this.heightP.getIntegerValue());
  }
  
  @Override
  public ArrayList<Graph> createGraph() {
    ArrayList<Graph> graphs = new ArrayList<>();
    Graph g = new F2Graph(tsv.getFilename(), title.getStringValue());
    graphs.add(g);
    return graphs;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getGraphScript();
  }
}
