/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.maok.tools.FileTools;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-03
 */
public class VCFFileParameter extends FileParameter {

  public VCFFileParameter(String key, String example, String description) {
    super(key, example, description);
  }

  @Override
  public String[] getExtensions() {
    return new String[]{"vcf", "gz"};
  }

  public VCF getVCF(int step) throws VCFException, PedException {
    return new VCF(this.getFilename(), step);
  }

  public VCF getVCF() throws VCFException, PedException {
    return new VCF(this.getFilename());
  }

  public VCF getVCF(int mode, int step) throws VCFException, PedException {
    return new VCF(this.getFilename(), mode, step);
  }

  public String getBasename() {
    return FileTools.getBasename(this.getFilename(), new String[]{".vcf.gz", ".vcf"});
  }

}
