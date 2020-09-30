/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-24
 */
public class ParameterException extends Exception {

  public ParameterException() {
  }

  public ParameterException(String message) {
    super(message);
  }

  public ParameterException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParameterException(Throwable cause) {
    super(cause);
  }

  public ParameterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
