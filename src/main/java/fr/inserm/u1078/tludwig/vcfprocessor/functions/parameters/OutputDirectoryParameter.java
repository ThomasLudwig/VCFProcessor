package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.gui.Input;
import fr.inserm.u1078.tludwig.vcfprocessor.gui.SaveDirectoryInput;
import java.io.File;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-26
 */
public class OutputDirectoryParameter extends Parameter {

  private String value;

  public OutputDirectoryParameter() {
    super(Function.OPT_OUTDIR, "ResultsDirectory", "The directory that will contain results files");
  }

  @Override
  public String toString() {
    return this.value;
  }

  @Override
  public void parseParameter(String v) throws ParameterException {
    this.value = v;
    if (this.value == null)
      this.value = ".";
    if (this.value.length() == 0)
      this.value = ".";
    if (!this.value.endsWith(File.separator))
      this.value += File.separator;

    File d = new File(value);
    if (d.exists()) {
      if (d.isDirectory())
        return;
      throw new ParameterException("A file named [" + value + "] already exists and is not a directory");
    }
    if (d.mkdir())
      return;
    throw new ParameterException("Unable to create new Directory [" + value + "]");

  }

  @Override
  public String showAllowedValues() {
    return "Directory name";
  }

  @Override
  public Input getInputForm() {
    return new SaveDirectoryInput(this);
  }

  public String getDirectory() {
    if (this.value.endsWith(File.separator))
      return value;
    return value + File.separator;
  }

}
