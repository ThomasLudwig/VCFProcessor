package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface VEPConsequenceFacade extends VEPFacade {
  String POLYPHEN_PROBABLY_DAMAGING = "probably_damaging";
  String POLYPHEN_POSSIBLY_DAMAGING = "possibly_damaging";
  String POLYPHEN_BENIGN = "benign";

  VEPField CONSEQUENCE = new VEPField("Consequence", Category.Consequence, Type.String, "Consequence type (SO term, e.g. missense_variant, stop_gained)");
  VEPField IMPACT = new VEPField("IMPACT", Category.Consequence, Type.String, "Subjective impact modifier: HIGH, MODERATE, LOW, or MODIFIER");
  VEPField SIFT = new VEPField("SIFT", Category.Consequence, Type.String, "SIFT prediction and/or score for missense variants, e.g. deleterious(0.02)");
  VEPField POLYPHEN = new VEPField("PolyPhen", Category.Consequence, Type.String, "PolyPhen-2 prediction and/or score, e.g. probably_damaging(0.998)");
  VEPField CLIN_SIG = new VEPField("CLIN_SIG", Category.Consequence, Type.String, "ClinVar clinical significance of co-located dbSNP variant (e.g. pathogenic, benign)");

  default String[] getCLIN_SIG(){ return getStringArrayValue(CLIN_SIG); }
  default List<String> getConsequenceList(){
    ArrayList<String> list = new ArrayList<>();
    Collections.addAll(list, getConsequence());
    return list;
  }

  default String[] getConsequence(){ return getStringArrayValue(CONSEQUENCE); }

  default String getIMPACT(){ return getStringValue(IMPACT); }
  default String getPolyPhen(){ return getStringValue(POLYPHEN); }
  default String getSIFT(){ return getStringValue(SIFT); }

  default String getWorstConsequence() { return VEPConsequence.getWorstConsequence(this.getConsequence()).getName(); }
}
