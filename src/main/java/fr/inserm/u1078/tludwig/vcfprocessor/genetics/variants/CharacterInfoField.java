package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.InfoDefinition;

public class CharacterInfoField extends InfoField{
  public CharacterInfoField(String rawValue, InfoDefinition definition) { super(rawValue, definition); }
  private char[] charValues;

  private void generateCharValues() {
    if(isMissing())
      this.charValues = new char[0];
    String[] values = getValuesAsStrings();
    charValues = new char[values.length];
    for(int i = 0; i < values.length; i++)
      charValues[i] = values[i].charAt(0);
  }

  public char[] getValuesAsChars() {
    if (charValues == null)
      generateCharValues();
    return charValues;
  }

  public Character getUniqueValue(){
    Message.warning(!this.getDefinition().isUnique(), "Trying to fetch a unique value from an array annotation (Number="+this.getDefinition().getNumber()+")");
    char[] out = getValuesAsChars();
    return out.length == 0 ? null : out[0];
  }

  @Override
  public void resetOutput() { this.charValues = null; }
}
