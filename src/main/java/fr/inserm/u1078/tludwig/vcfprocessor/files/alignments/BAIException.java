package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

public class BAIException extends Exception{
  public BAIException() {
  }

  public BAIException(String message) {
    super(message);
  }

  public BAIException(String message, Throwable cause) {
    super(message, cause);
  }

  public BAIException(Throwable cause) {
    super(cause);
  }

  public BAIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
