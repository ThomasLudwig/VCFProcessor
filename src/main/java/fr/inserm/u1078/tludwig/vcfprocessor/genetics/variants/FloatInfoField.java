package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.InfoDefinition;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

import java.util.Arrays;

public class FloatInfoField extends InfoField {
  public FloatInfoField(String rawValue, InfoDefinition definition) { super(rawValue, definition); }

  private double[] floatValues;

  private void generateFloatValues() {
    if(isMissing())
      this.floatValues = new double[0];
    String[] values = getValuesAsStrings();
    floatValues = new double[values.length];
    for(int i = 0; i < values.length; i++)
      floatValues[i] = Float.parseFloat(values[i]);
  }

  public double[] getValuesAsFloats() {
    if( floatValues == null )
      generateFloatValues();
    return floatValues;
  }

  public Double getUniqueValue(){
    Message.warning(!this.getDefinition().isUnique(), "Trying to fetch a unique value from an array annotation (Number="+this.getDefinition().getNumber()+")");
    double[] out = getValuesAsFloats();
    return out.length == 0 ? null : out[0];
  }

  @Override
  public void resetOutput() { this.floatValues = null; }

  @Override
  public Println println() {
    Println out = new Println(this.getKey(),"=");
    out.append(Println.join(",", floatValues));
    return out;
  }
}
