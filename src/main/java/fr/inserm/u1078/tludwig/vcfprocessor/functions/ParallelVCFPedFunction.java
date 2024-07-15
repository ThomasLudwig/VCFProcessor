package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;


/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-09-09
 */
public abstract class ParallelVCFPedFunction<T> extends ParallelVCFVariantFunction<T> {
  public final PedFileParameter pedFile = new PedFileParameter();
}
