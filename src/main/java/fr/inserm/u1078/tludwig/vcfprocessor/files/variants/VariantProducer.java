package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.vcfprocessor.files.RecordProducer;

import java.io.IOException;

public interface VariantProducer extends RecordProducer {
  RawVariantRecordData readNext()  throws IOException;

  VariantRecord build(RawVariantRecordData record) throws VCFException;
}
