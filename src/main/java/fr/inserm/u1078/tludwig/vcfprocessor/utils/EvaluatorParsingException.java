package fr.inserm.u1078.tludwig.vcfprocessor.utils;

public class EvaluatorParsingException extends Exception {
  public EvaluatorParsingException() {
  }

  public EvaluatorParsingException(String message) {
    super(message);
  }

  public EvaluatorParsingException(String message, Throwable cause) {
    super(message, cause);
  }

  public EvaluatorParsingException(Throwable cause) {
    super(cause);
  }

  public EvaluatorParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
