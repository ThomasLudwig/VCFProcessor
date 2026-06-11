package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.InfoDefinition;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

public class StringInfoField extends InfoField{
  public StringInfoField(String rawValue, InfoDefinition definition) { super(rawValue, definition); }

  public String getUniqueValue(){
    Message.warning(!this.getDefinition().isUnique(), "Trying to fetch a unique value from an array annotation (Number="+this.getDefinition().getNumber()+")");
    String[] out = getValuesAsStrings();
    return out.length == 0 ? null : out[0];
  }

  @Override
  public Println println() { return new Println(this.getKey(),"=", getRawValue()); }

  @Override
  public void resetOutput() {
    //nothing
  }
}
