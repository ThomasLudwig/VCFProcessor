package fr.inserm.u1078.tludwig.vcfprocessor.functions.graphs;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import static fr.inserm.u1078.tludwig.vcfprocessor.functions.Function.OPT_TSV;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.GraphFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.CountGenotypes;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ConsequenceParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.CompareGenotypesGraph;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.Graph;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-07
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class GraphCountGenotypes extends GraphFunction {
  private final TSVFileParameter tsv = new TSVFileParameter(OPT_TSV, "input.tsv", "input data");
  private final ConsequenceParameter csq = new ConsequenceParameter();
  private final OutputDirectoryParameter outdir = new OutputDirectoryParameter();

  @Override
  public String getSummary() {
    return "Create a graph for the results of "+CountGenotypes.class.getSimpleName();
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary());
  }

  @SuppressWarnings("unused")
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
    Graph g = new CompareGenotypesGraph(tsv.getFilename(), csq.getStringValue());
    graphs.add(g);
    return graphs;
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newDirectoryAnalysis();
    def.addAnonymousFilename("tsv", "input.tsv");
    def.addAnonymousValue("width", "1200");
    def.addAnonymousValue("height", "800");
    def.addAnonymousValue("csq", "missense_variant");
    return new TestingScript[]{def};
  }
}
