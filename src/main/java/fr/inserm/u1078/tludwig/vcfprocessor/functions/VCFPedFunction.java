package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 20 nov. 2015
 */
public abstract class VCFPedFunction extends VCFFunction {
  public final PedFileParameter pedfile = new PedFileParameter();
}
