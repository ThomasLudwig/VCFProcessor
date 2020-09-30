package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 23 juin 2015
 */
public class GenotypeException extends VariantException {

  public GenotypeException() {
  }

  public GenotypeException(String message) {
    super(message);
  }

  public GenotypeException(String message, Throwable cause) {
    super(message, cause);
  }

  public GenotypeException(Throwable cause) {
    super(cause);
  }

  public GenotypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
