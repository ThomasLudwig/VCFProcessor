package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-03
 */
public class PedFileParameter extends FileParameter {

  public PedFileParameter() {
    super(Function.OPT_PED, "samples.ped", "File describing the VCF's samples (See File Formats in the documentation)");
  }
  
  public PedFileParameter(String key, String example, String description) {
    super(key, example, description);
  }
  
  public Ped getPed() throws PedException {
    return new Ped(this.getFilename());
  }

  @Override
  public String[] getExtensions() {
    return new String[]{"ped"};
  }
}
