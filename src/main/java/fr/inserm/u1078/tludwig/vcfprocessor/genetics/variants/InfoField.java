package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants;

import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.InfoDefinition;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.GeneticsException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VEPInfoDefinition;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPInfoField;

public abstract class InfoField {

  public static final String MISSING = ".";

  private String rawValue;
  private String[] stringValues;
  private final InfoDefinition definition;

  public InfoField(String rawValue, InfoDefinition definition) {
    this.rawValue = rawValue;
    this.definition = definition;
  }

  public static InfoField parseField(String key, String value, InfoDefinition definition) throws GeneticsException {
    if(definition == null)
      throw new GeneticsException("INFO field["+key+"] has not been defined in VCF header");

    //VEP
    if(definition instanceof VEPInfoDefinition)
      return new VEPInfoField(value, (VEPInfoDefinition)definition);

    return switch (definition.getType()) {
      case String: yield new StringInfoField(value, definition);
      case Integer: yield new IntegerInfoField(value, definition);
      case Float: yield new FloatInfoField(value, definition);
      case Character: yield new CharacterInfoField(value, definition);
      case Flag: yield new FlagInfoField(definition);
      default : throw new GeneticsException("Unknown InfoField type ["+definition.getType()+"] for [" + key + "="+value+"]");
    };
  }

  private void generateStringValues() {
    this.stringValues = isMissing() ? new String[0] : getRawValue().split(",");
  }

  public String getKey() { return getDefinition().getId(); }
  public InfoDefinition getDefinition() { return definition; }
  public String getRawValue() { return rawValue; }
  public String[] getValuesAsStrings() {
    if(this.stringValues == null)
      generateStringValues();
    return this.stringValues;
  } ;
  public boolean isMissing() { return MISSING.equals(rawValue); }
  public void updateValue(String value) {
    this.rawValue = value;
    this.stringValues =null;
    resetOutput();
  }

  public abstract void resetOutput();
}
