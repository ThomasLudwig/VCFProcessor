package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

/**
 * This class represent a raw data has it is read from the VCF/BCF file
 * It is
 * - either a String line from the VCF file
 * - or two BCFByteArrays from the BCF file
 */
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
