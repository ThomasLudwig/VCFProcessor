package fr.inserm.u1078.tludwig.vcfprocessor.files;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-15
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class PedException extends FileFormatException {

  public PedException() {
  }

  public PedException(String message) {
    super(message);
  }

  public PedException(String message, Throwable cause) {
    super(message, cause);
  }

  public PedException(Throwable cause) {
    super(cause);
  }

  public PedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
