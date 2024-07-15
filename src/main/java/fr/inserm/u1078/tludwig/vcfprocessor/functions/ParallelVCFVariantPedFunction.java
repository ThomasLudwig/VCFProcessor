package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.vcfprocessor.files.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-09-09
 */
public abstract class ParallelVCFVariantPedFunction<T> extends ParallelVCFVariantFunction<T> {

  public final PedFileParameter pedFile = new PedFileParameter();
  private Ped ped;

  @Override
  public void openVCF() throws VCFException, PedException { //TODO why, should be the same in VCFPedFunction and ParallelVCFLinePedFunction ? should avoid also direct contact with this.pedFile and get ped through the VCF ?
    this.setVCF(new VCF(this.vcfFile.getFilename(), VCF.STEP_OFF));
    this.setPed(pedFile.getPed());
  }

  public final Ped getPed() {
    return ped;
  }

  public final void setPed(Ped ped) {
    this.ped = ped;
    this.getVCF().bindToPed(ped);
  }
}
