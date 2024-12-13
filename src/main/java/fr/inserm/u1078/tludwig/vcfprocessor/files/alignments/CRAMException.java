package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

public class CRAMException extends Exception {
  public CRAMException() {
  }

  public CRAMException(String message) {
    super(message);
  }

  public CRAMException(String message, Throwable cause) {
    super(message, cause);
  }

  public CRAMException(Throwable cause) {
    super(cause);
  }

  public CRAMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
