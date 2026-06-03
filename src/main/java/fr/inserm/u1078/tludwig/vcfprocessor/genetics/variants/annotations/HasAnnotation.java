package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VEPInfoDefinition;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.*;

public interface HasAnnotation {
  public InfoField getInfoField(String key);

  default Boolean hasFlag(String key){
    return this.getInfoField(key) != null;
  }

  default Double getFloatValue(String key) {
    try{
      return ((FloatInfoField)getInfoField(key)).getUniqueValue();
    } catch(Exception e){return null;}
  }

  default Integer getIntegerValue(String key) {
    try{
      return ((IntegerInfoField)getInfoField(key)).getUniqueValue();
    } catch(Exception e){return null;}
  }

  default Character getCharacterValue(String key) {
    try{
      return ((CharacterInfoField)getInfoField(key)).getUniqueValue();
    } catch(Exception e){return null;}
  }

  default String getStringValue(String key) {
    try{
      return ((StringInfoField)getInfoField(key)).getUniqueValue();
    } catch(Exception e){return null;}
  }

  default double[] getFloatValues(String key) {
    try{
      return ((FloatInfoField)getInfoField(key)).getValuesAsFloats();
    } catch(Exception e){return new double[0];}
  }

  default int[] getIntegerValues(String key) {
    try{
      return ((IntegerInfoField)getInfoField(key)).getValuesAsIntegers();
    } catch(Exception e){return new int[0];}
  }

  default char[] getCharacterValues(String key) {
    try{
      return ((CharacterInfoField)getInfoField(key)).getValuesAsChars();
    } catch(Exception e){return new char[0];}
  }

  default String[] getStringValues(String key) {
    try{
      return getInfoField(key).getValuesAsStrings();
    } catch(Exception e){return new String[0];}
  }

  /**
   * Gets the INFO Field matching the VEP annotation if it exists
   * @return null if absent
   */
  default VEPInfoField getVEPInfo() {
    InfoField ret = getInfoField(VEPInfoDefinition.CSQ);
    if(ret == null)
      ret = getInfoField(VEPInfoDefinition.ANN);
    return (VEPInfoField) ret;
  }
}
