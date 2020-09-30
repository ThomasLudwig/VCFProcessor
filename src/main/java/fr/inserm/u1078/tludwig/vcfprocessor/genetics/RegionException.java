package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 5 avr. 2016
 */
public class RegionException extends GeneticsException {

  public RegionException() {
  }

  public RegionException(String message) {
    super(message);
  }

  public RegionException(String message, Throwable cause) {
    super(message, cause);
  }

  public RegionException(Throwable cause) {
    super(cause);
  }

  public RegionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
