package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.vcfprocessor.files.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-09-06
 */
public abstract class ParallelVCFVariantFunction extends ParallelVCFFunction {

  @Override
  public String[] processInputLine(String line) {
    if(line == null)
      return null;
    try{
      Variant variant = getVCF().createVariant(line);
      if(variant != null)
        return this.processInputVariant(variant);
    } catch(VCFException e){
      this.fatalAndDie("Unable to parse line \n"+line, e);
    }
    return null;
  }
  
  public abstract String[] processInputVariant(Variant variant);  
}