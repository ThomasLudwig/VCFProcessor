package fr.inserm.u1078.tludwig.vcfprocessor.files;

public class RawRecordData {
  private final String line;
  private final BCFByteArray inCommon;
  private final BCFByteArray inFormatGeno;

  public RawRecordData(String line) {
    this.line = line;
    this.inCommon = null;
    this.inFormatGeno = null;
  }

  public RawRecordData(BCFByteArray inCommon, BCFByteArray inFormatGeno){
    this.line = null;
    this.inCommon = inCommon;
    this.inFormatGeno = inFormatGeno;

  }

  public String getLine() {
    return line;
  }

  public BCFByteArray getInCommon() {
    return inCommon;
  }

  public BCFByteArray getInFormatGeno() {
    return inFormatGeno;
  }
}
