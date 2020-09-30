package fr.inserm.u1078.tludwig.vcfprocessor.files;

/**
 * Exception related to Fasta
 *
 * @author maria
 */
public class FastaException extends Exception {

  public FastaException() {
  }

  public FastaException(String message) {
    super(message);
  }

  public FastaException(String message, Throwable cause) {
    super(message, cause);
  }

  public FastaException(Throwable cause) {
    super(cause);
  }

  public FastaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
