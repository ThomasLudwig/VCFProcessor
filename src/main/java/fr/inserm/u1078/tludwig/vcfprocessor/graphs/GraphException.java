package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

/**
 * Exception thrown while building a Graph
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-15
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class GraphException extends Exception {

  public GraphException() {
  }

  public GraphException(String message) {
    super(message);
  }

  public GraphException(String message, Throwable cause) {
    super(message, cause);
  }

  public GraphException(Throwable cause) {
    super(cause);
  }

  public GraphException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
