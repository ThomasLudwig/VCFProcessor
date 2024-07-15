package fr.inserm.u1078.tludwig.vcfprocessor.functions;

public class FunctionException extends Exception {
  public FunctionException(String message) {
    super(message);
  }

  public FunctionException(String message, Throwable cause) {
    super(message, cause);
  }
}
