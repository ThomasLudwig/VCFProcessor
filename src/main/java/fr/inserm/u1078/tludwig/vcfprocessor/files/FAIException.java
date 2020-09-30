package fr.inserm.u1078.tludwig.vcfprocessor.files;

/**
 * Exception related to FAI
 *
 * @author maria
 */
public class FAIException extends FileFormatException {

  public FAIException() {
  }

  public FAIException(String message) {
    super(message);
  }

  public FAIException(String message, Throwable cause) {
    super(message, cause);
  }

  public FAIException(Throwable cause) {
    super(cause);
  }

  public FAIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
