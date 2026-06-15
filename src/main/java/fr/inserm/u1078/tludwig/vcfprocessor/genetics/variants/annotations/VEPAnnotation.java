package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VEPInfoDefinition;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Printable;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

import java.util.ArrayList;

/**
 * One group of VEPAnnotation (comma separated, starting with csq=allele) is an object
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 21 avr. 2016
 */
public class VEPAnnotation implements VEPConsequenceFacade, VEPFrequencyFacade, VEPLocationFacade, VEPMetaFacade, VEPVariantIDFacade, Printable {
  private String[] values;
  private final String raw;
  private final VEPInfoDefinition vepDefinition;

  public VEPAnnotation(String annotations, VEPInfoDefinition vepDefinition) {
    this.vepDefinition = vepDefinition;
    this.raw = annotations;
  }

  public String[] getStrings () {
    if(values==null) {
      values = raw.split("\\|", -1);
      if (values.length != vepDefinition.size()) {//TODO temporarily disabled, should be reEnabled : for some annotated 1000g variant this is false !!
        String msg = "Mismatch between number of values (" + values.length + ") and size of format (" + vepDefinition.size() + ") : line \n" + raw;
        Message.warning(msg);
        /*//throw new AnnotationException(msg);
        this.values = new String[vepDefinition.size()];
        System.arraycopy(tmpValues, 0, this.values, 0, tmpValues.length);*/
      }
    }
    return values;
  }

  @Override
  public String getRawValue(VEPField field) {
    String key = field.getId();
    int idx = vepDefinition.getIndexFor(key);
    if (idx < 0){
      if(!missingKeys.contains(key)) {
        missingKeys.add(key);
        String message = "Trying to access VEP annotation [" + key + "] which seem to be missing from this VCF file";
        Message.warning(message);
        Message.debug(message, new AnnotationException(message));
      }
      return null;
    }
    return getStrings()[idx];
  }

  /**
   * to print only warning once for each missing key
   */
  private final static ArrayList<String> missingKeys = new ArrayList<>();

  public boolean hasField(VEPField field) { return this.vepDefinition.hasField(field.getId()); }

  @Override
  public String toString(){ return raw; }

  @Override
  public Println println() { return new Println(toString()); }
}