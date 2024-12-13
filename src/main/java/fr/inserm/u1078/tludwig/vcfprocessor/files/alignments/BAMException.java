package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

public class BAMException extends Exception {
  public static final String BAM_NO_MAGIC = "Not a valid BAM file (no BAM Magic String)";
  public static final String BAI_NO_MAGIC = "Not a valid BAI file (no BAI Magic String)";
  public static final String BAM_NOT_GZIP= "Not a valid BAM file (not GZIP format)";

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
