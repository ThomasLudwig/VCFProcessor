package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-03
 */
public class BedFileParameter extends FileParameter {

  public BedFileParameter(String key, String example, String description) {
    super(key, example, description);
  }

  public Bed getBed() {
    return new Bed(this.getFilename());
  }

  @Override
  public String[] getExtensions() {
    return new String[]{"bed"};
  }
}
