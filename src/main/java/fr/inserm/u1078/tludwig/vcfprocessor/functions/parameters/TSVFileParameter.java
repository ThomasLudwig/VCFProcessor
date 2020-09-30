package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-04
 */
public class TSVFileParameter extends FileParameter {

  public TSVFileParameter(String key, String example, String description) {
    super(key, example, description);
  }

  @Override
  public String[] getExtensions() {
    return new String[]{"tsv"};
  }
}
