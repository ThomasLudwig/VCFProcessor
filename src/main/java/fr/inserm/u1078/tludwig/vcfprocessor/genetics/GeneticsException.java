package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-15
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class GeneticsException extends Exception{

  public GeneticsException() {
  }

  public GeneticsException(String message) {
    super(message);
  }

  public GeneticsException(String message, Throwable cause) {
    super(message, cause);
  }

  public GeneticsException(Throwable cause) {
    super(cause);
  }

  public GeneticsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
