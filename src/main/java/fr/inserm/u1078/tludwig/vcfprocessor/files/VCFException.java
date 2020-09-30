package fr.inserm.u1078.tludwig.vcfprocessor.files;

/**
 * Exception related to VCF
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 23 juin 2015
 */
public class VCFException extends Exception {

  public VCFException() {
  }

  public VCFException(String message) {
    super(message);
  }

  public VCFException(String message, Throwable cause) {
    super(message, cause);
  }

  public VCFException(Throwable cause) {
    super(cause);
  }

  public VCFException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
