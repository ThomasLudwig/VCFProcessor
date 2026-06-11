package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VEPInfoDefinition;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.StringInfoField;

import java.util.*;

public class VEPInfoField extends StringInfoField implements HasVEPConsequenceAnnotation, HasVEPFrequencyAnnotation, HasVEPLocationAnnotation, HasVEPMetaAnnotation, HasVEPRegulatoryAnnotation, HasVEPVariantIDAnnotation {
  private HashMap<Integer, ArrayList<VEPAnnotation>> vepAnnotations;

  public VEPInfoField(String rawValue, VEPInfoDefinition definition) { super(rawValue, definition); }

  public HashMap<Integer, ArrayList<VEPAnnotation>> getVEPAnnotations() {
    if(vepAnnotations == null) {
      vepAnnotations = new HashMap<>();
      for (String annot : getValuesAsStrings()) {
        VEPAnnotation vepAnnotation = new VEPAnnotation(annot, getVEPDefinition());
        int allele = vepAnnotation.getAlleleNumber();
        ArrayList<VEPAnnotation> list = this.vepAnnotations.computeIfAbsent(allele, k -> new ArrayList<>());
        list.add(vepAnnotation);
      }
    }
    return vepAnnotations;
  }

  @Override
  public Set<Integer> getAlleles() { return this.getVEPAnnotations().keySet(); }

  @Override
  public ArrayList<VEPAnnotation> getAllVEPAnnotations() {
    ArrayList<VEPAnnotation> annotations = new ArrayList<>();
    for(ArrayList<VEPAnnotation> list : this.getVEPAnnotations().values())
      annotations.addAll(list);
    return annotations;
  }

  @Override
  public ArrayList<VEPAnnotation> getVEPAnnotations(int allele) { return this.getVEPAnnotations().get(allele); }

  public VEPInfoDefinition getVEPDefinition() { return (VEPInfoDefinition)getDefinition(); }
  //public int getIndexFor(String key) { return this.getVEPDefinition().getIndexFor(key); }

  //TODO specialize println() or keep the original content ?
}
