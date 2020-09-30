package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PositiveIntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.graphs.Graph;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-08-19
 */
public abstract class GraphFunction extends Function {

  public final PositiveIntegerParameter widthP = new PositiveIntegerParameter(OPT_WIDTH, "Graph's Width in Pixels");
  public final PositiveIntegerParameter heightP = new PositiveIntegerParameter(OPT_HEIGHT,"Graph's Height in Pixels");

  @Override
  public final String getOutputExtension() {
    return OUT_PNG;
  }

  
  //public abstract FunctionDescription getDescription();

  public abstract ArrayList<Graph> createGraph();

}
