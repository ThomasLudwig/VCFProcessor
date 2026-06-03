package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.InfoField;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.IntegerInfoField;

public interface HasPopulationAnnotation extends HasAnnotation{
  String AC = "AC";
  String AN = "AN";
  String AF = "AF";

  default int getAN(){
    Integer an = getIntegerValue(AN);
    return an == null ? 0 : an;
  }

  default int[] getACs(){ return getIntegerValues(AC); }
  default double[] getAFs(){ return getFloatValues(AF); }
}
