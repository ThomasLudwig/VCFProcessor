package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-08-20
 */
public abstract class Graph {

  public static final String T = "\t";

  protected abstract void loadData() throws GraphException;

  protected abstract String getMainTitle();

  public abstract void exportGraphAsPNG(String filename, int width, int height) throws GraphException;

}
