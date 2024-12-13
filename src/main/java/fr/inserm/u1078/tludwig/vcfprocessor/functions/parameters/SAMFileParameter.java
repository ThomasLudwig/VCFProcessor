package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.SAM;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.SAMException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;

import java.io.IOException;

public class SAMFileParameter extends FileParameter {
  public SAMFileParameter(String key, String example, String description) {
    super(key, example, description);
  }

  public SAM getSAM() throws SAMException, IOException {
    return new SAM(this.getFilename());
  }

  public SAM getSAM(Bed bed) throws SAMException, IOException {
    return new SAM(this.getFilename(), bed);
  }

  @Override
  public String[] getExtensions() {
    return new String[]{"sam", "bam", "cram"};
  }
}
