/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCFException;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-03
 */
public class VCFFileParameter extends FileParameter {
  private VCF vcf = null;

  public VCFFileParameter(String key, String example, String description) {
    super(key, example, description);
  }

  @Override
  public String[] getExtensions() {
    return new String[]{"vcf", "vcf.gz"};
  }

  public VCF getVCF(int step) throws VCFException, PedException {
    if(vcf == null)
      vcf = new VCF(this.getFilename(), step);
    return vcf;
  }

  public VCF getVCF() throws VCFException, PedException {
    if(vcf == null)
      vcf = new VCF(this.getFilename());
    return vcf;
  }

  public VCF getVCF(int mode, int step) throws VCFException, PedException {
    if(vcf == null)
      vcf = new VCF(this.getFilename(), mode, step);
    return vcf;
  }

}
