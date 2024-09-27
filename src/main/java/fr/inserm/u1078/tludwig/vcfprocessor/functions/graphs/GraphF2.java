package fr.inserm.u1078.tludwig.vcfprocessor.functions.graphs;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.GraphFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.F2;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.F2Individuals;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ListParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.F2Graph;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.Graph;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

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
  private final ListParameter groupColors = new ListParameter(OPT_COLOR, "group1:#FF0000,group2:#00FF00,group3:#0000FF", "HTML color for some/all groups");

  private final OutputDirectoryParameter outdir = new OutputDirectoryParameter();

  private final HashMap<String, Color> colors = new HashMap<>();

  @Override
  public String getSummary() {
    return "Create a graph for the results of "+F2.class.getSimpleName()+" or "+F2Individuals.class.getSimpleName();
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary());
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    initColors();
    ArrayList<Graph> graphs = this.createGraph();
    Graph graph = graphs.get(0);
    String png = outdir.getDirectory()+tsv.getBasename()+".png";    
    graph.exportGraphAsPNG(png, this.widthP.getIntegerValue(), this.heightP.getIntegerValue());
  }
  
  @Override
  public ArrayList<Graph> createGraph() {
    ArrayList<Graph> graphs = new ArrayList<>();
    Graph g = new F2Graph(tsv.getFilename(), title.getStringValue(), colors);
    graphs.add(g);
    return graphs;
  }

  private final void initColors() {
    for(String keyValues : this.groupColors.getList()) {
      String[] kv = keyValues.split(":");
      Color c = getColor(kv[1]);
      if(c != null)
        colors.put(kv[0], c);
    }
  }

  private Color getColor(String html){
    try{
      return Color.decode(html);
    } catch(Exception e) {
      return null;
    }
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getGraphScript();
  }
}
