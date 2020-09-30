package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 23 juin 2015
 */
public class AnnotationException extends VariantException {

  public AnnotationException() {
  }

  public AnnotationException(String message) {
    super(message);
  }

  public AnnotationException(String message, Throwable cause) {
    super(message, cause);
  }

  public AnnotationException(Throwable cause) {
    super(cause);
  }

  public AnnotationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
