package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.InfoDefinition;

public class IntegerInfoField extends InfoField {
  public IntegerInfoField(String rawValue, InfoDefinition definition) { super(rawValue, definition); }
  private int[] intValues;

  private void generateIntValues() {
    if(isMissing())
      this.intValues = new int[0];
    String[] values = getValuesAsStrings();
    intValues = new int[values.length];
    for(int i = 0; i < values.length; i++)
      intValues[i] = Integer.parseInt(values[i]);
  }

  public int[] getValuesAsIntegers() {
    if( intValues == null )
      generateIntValues();
    return intValues;
  }

  public Integer getUniqueValue(){
    Message.warning(!this.getDefinition().isUnique(), "Trying to fetch a unique value from an array annotation (Number="+this.getDefinition().getNumber()+")");
    int[] out = getValuesAsIntegers();
    return out.length == 0 ? null : out[0];
  }

  @Override
  public void resetOutput() { this.intValues = null; }
}
