package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import java.io.IOException;

public class CRAM implements AlignmentProducer {
  private final SAM sam;

  public CRAM(String filename, SAM sam) throws CRAMException, IOException {
    this.sam = sam;
  }

  @Override
  public RawAlignmentRecordData readNext() {
    //TODO implement
    throw new UnsupportedOperationException();
  }

  @Override
  public AlignmentRecord build(RawAlignmentRecordData record) {
    //TODO implement
    throw new UnsupportedOperationException();
  }

  @Override
  public long loadHeaders() throws IOException {
    //TODO implement
    throw new UnsupportedOperationException();
  }

  @Override
  public SAMHeader getHeaders() {
    return this.sam.getHeaders();
  }

  @Override
  public void setHeaders(SAMHeader header) {
    this.sam.setHeaders(header);
  }

  @Override
  public String getFilename() {
    return this.sam.getFilename();
  }

  @Override
  public String[] knownExtensions() {
    return new String[]{"cram"};
  }

  @Override
  public String fileFormatDescription() {
    return "Compressed Reference-oriented Alignment Map";
  }

  @Override
  public void filter() {
    this.sam.filter();
  }
}
