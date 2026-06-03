package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.GeneticsException;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.*;

import java.util.*;

/**
 * Metaclass contains all the data in the "INFO" field (including VEPAnnotations)
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 21 avr. 2016
 */
public class InfoColumn implements HasPopulationAnnotation, HasMetricsAnnotation {

  private Variant variant;

  private final TreeMap<String, InfoField> infoMap;

  public InfoColumn(String[][] infoFields, VCF vcfFile){
    infoMap = new TreeMap<>();
    for (String[] kv : infoFields) {
      String key = kv[0];
      String value = (kv.length == 2)
          ? kv[1]
          : null;
      if (!infoMap.containsKey(key)) {
        try {
          infoMap.put(key, InfoField.parseField(key, value, vcfFile.getInfoHeader(key)));
        } catch (GeneticsException e) {
          Message.error(e.getMessage(), e);
        }
      }
      else
        Message.warning("Duplicate key [" + kv[0] + "] found for info [" + merge(infoFields) + "]");
    }
  }

  public static String merge(String[][] fields) {
    StringBuilder ret = new StringBuilder();
    for(String[] kv : fields){
      ret.append(";").append(kv[0]);
      if(kv[1] != null)
        ret.append("=").append(kv[1]);
    }

    return ret.isEmpty() ? "" : ret.substring(1);
  }

  public final InfoField getInfoField(String key) {
    return this.infoMap.get(key);
  }

  void setVariant(Variant variant) throws AnnotationException {
    if (this.variant != null && this.variant != variant)
      throw new AnnotationException("Cannot attach " + this.getClass().getSimpleName() + " object to multiple " + variant.getClass().getSimpleName());
    else
      this.variant = variant; // attach();
  }

  public void addInfo(String s) {
    //TODO rewrite this
    String[] kv = s.split("=");
    String key = kv[0];
    String value = null;
    if (kv.length == 2)
      value = kv[1];
    this.update(key, value);
  }

  public void update(String key, String value) {
    InfoField field = getInfoField(key);
    if (field != null)
      field.updateValue(value);
    else
      Message.warning("Can't update the value for Info Field["+key+"]. Undefined field");
  }

  public ArrayList<String> getFields() {
    ArrayList<String> fields = new ArrayList<>();
    for (String key : this.infoMap.navigableKeySet()) {
      InfoField field = getInfoField(key);
      if (field instanceof FlagInfoField)
        fields.add(key);
      else
        fields.add(key + "=" + field.getRawValue());
    }
    return fields;
  }

  @Override
  public String toString() {
    return String.join(";", getFields());
  }
}
