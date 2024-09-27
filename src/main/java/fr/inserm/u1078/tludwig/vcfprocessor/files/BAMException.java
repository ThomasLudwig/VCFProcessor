package fr.inserm.u1078.tludwig.vcfprocessor.files;

public class BAMException extends Exception {
  public BAMException() {
  }

  public BAMException(String message) {
    super(message);
  }

  public BAMException(String message, Throwable cause) {
    super(message, cause);
  }

  public BAMException(Throwable cause) {
    super(cause);
  }

  public BAMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
