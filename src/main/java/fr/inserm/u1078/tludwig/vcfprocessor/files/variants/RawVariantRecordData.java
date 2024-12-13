package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.BCFByteArray;

public class RawVariantRecordData {
  private final String line;
  private final BCFByteArray inCommon;
  private final BCFByteArray inFormatGeno;

  public RawVariantRecordData(String line) {
    this.line = line;
    this.inCommon = null;
    this.inFormatGeno = null;
  }

  public RawVariantRecordData(BCFByteArray inCommon, BCFByteArray inFormatGeno){
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
