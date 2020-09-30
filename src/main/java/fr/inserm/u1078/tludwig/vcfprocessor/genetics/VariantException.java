package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 23 juin 2015
 */
public class VariantException extends GeneticsException {

  public VariantException() {
  }

  public VariantException(String message) {
    super(message);
  }

  public VariantException(String message, Throwable cause) {
    super(message, cause);
  }

  public VariantException(Throwable cause) {
    super(cause);
  }

  public VariantException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
