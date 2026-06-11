package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants;

import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.InfoDefinition;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

public class FlagInfoField extends InfoField {

  public FlagInfoField(InfoDefinition definition) { super(null, definition); }

  @Override
  public void resetOutput() {
    //nothing
  }

  @Override
  public Println println() { return new Println(this.getDefinition().getId()); }
}
