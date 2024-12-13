package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.vcfprocessor.files.FileFormatException;

public class BCFException extends FileFormatException {

  public static final String BCFE_NO_MAGIC = "Not a valid BCF2 file (no BCF Magic String)";
  public static final String BCFE_NOT_GZIP= "Not a valid BCF2 file (not GZIP format)";

  public static final String BCFE_UNEXPECTED_TYPE = "Unexpected type";

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

  public static class UnexpectedTypeException extends BCFException {
    public UnexpectedTypeException(byte b){
      super("Unexpected Type ["+(b & 0x0f)+"]");
    }

    public UnexpectedTypeException(BCFByteArray.DataType dt){
      super("Unexpected Type ["+dt+"]");
    }
  }

  public static class UndeclaredFormatException extends BCFException {
    public UndeclaredFormatException(String format){
      super("The format ["+format+"] was not declared in the header file");
    }
  }

  public static class NullGenotypeException extends BCFException {
    public NullGenotypeException(String sample){
      super("Genotype is null for sample ["+sample+"]");
    }
  }

  public static class SampleNumberException extends BCFException {
    public SampleNumberException(int headerSamples, int recordSamples){
      super("Number of samples missmatch between header ["+headerSamples+"] and record ["+recordSamples+"]");
    }
  }
}
