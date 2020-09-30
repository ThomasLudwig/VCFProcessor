package fr.inserm.u1078.tludwig.vcfprocessor.files;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-15
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class FileFormatException extends Exception {

  public FileFormatException() {
  }

  public FileFormatException(String message) {
    super(message);
  }

  public FileFormatException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileFormatException(Throwable cause) {
    super(cause);
  }

  public FileFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
