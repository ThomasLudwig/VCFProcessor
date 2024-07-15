package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-09-06
 */
public abstract class ParallelVCFVariantFunction<T> extends ParallelVCFFunction<T> {

  @Override
  public String[] processInputRecord(VariantRecord record) {
    if(record == null)
      return null;
    try{
      Variant variant = record.createVariant(getVCF());
      if (variant != null)
        return this.processInputVariant(variant);
    } catch(VCFException e){
      Message.fatal("Unable to parse line \n"+record.summary(10), e, true);
    }
    return null;
  }
  
  public abstract String[] processInputVariant(Variant variant);  
}