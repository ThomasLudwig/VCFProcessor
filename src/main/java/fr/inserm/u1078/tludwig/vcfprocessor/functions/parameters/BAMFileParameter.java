package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.files.BAM;
import fr.inserm.u1078.tludwig.vcfprocessor.files.BAMException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;

import java.io.IOException;

public class BAMFileParameter extends FileParameter {

  public BAMFileParameter(String key, String example, String description) {
    super(key, example, description);
  }

  public BAM getBAM() throws BAMException, IOException {
    return new BAM(this.getFilename());
  }

  public BAM getBAM(Bed bed) throws BAMException, IOException {
    return new BAM(this.getFilename(), bed);
  }

  @Override
  public String[] getExtensions() {
    return new String[]{"bam"};
  }
}
