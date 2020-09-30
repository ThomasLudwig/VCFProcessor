package fr.inserm.u1078.tludwig.vcfprocessor;

/**
 * Exception thrown when VCFProcessor cannot start properly
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-15
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class StartUpException extends RuntimeException {

  public StartUpException() {
  }

  public StartUpException(String message) {
    super(message);
  }

  public StartUpException(String message, Throwable cause) {
    super(message, cause);
  }

  public StartUpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
  
  

}
