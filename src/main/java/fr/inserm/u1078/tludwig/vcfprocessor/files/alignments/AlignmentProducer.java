package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.vcfprocessor.files.RecordProducer;

import java.io.IOException;

public interface AlignmentProducer extends RecordProducer {

  String getFilename();

  RawAlignmentRecordData readNext() throws IOException;

  AlignmentRecord build(RawAlignmentRecordData record) throws SAMException;

  long loadHeaders() throws IOException;

  SAMHeader getHeaders();

  void setHeaders(SAMHeader header);
}
