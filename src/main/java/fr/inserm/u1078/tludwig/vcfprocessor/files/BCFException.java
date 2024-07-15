package fr.inserm.u1078.tludwig.vcfprocessor.files;

public class BCFException extends FileFormatException {

  public BCFException() {
  }

  public BCFException(String message) {
    super(message);
  }

  public BCFException(String message, Throwable cause) {
    super(message, cause);
  }

  public BCFException(Throwable cause) {
    super(cause);
  }

  public BCFException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
