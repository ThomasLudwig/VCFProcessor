package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import java.util.SortedSet;

public interface HasVEPRegulatoryAnnotation extends HasVEPAnnotations {
  default SortedSet<String> getAllCELL_TYPEs(){ return getAllStringValues(VEPRegulatoryFacade.CELL_TYPE); }
  default SortedSet<String> getAllCELL_TYPEs(int allele){ return getAllStringValues(VEPRegulatoryFacade.CELL_TYPE, allele); }
  default SortedSet<String> getAllHIGH_INF_POSs(){ return getAllStringValues(VEPRegulatoryFacade.HIGH_INF_POS); }
  default SortedSet<String> getAllHIGH_INF_POSs(int allele){ return getAllStringValues(VEPRegulatoryFacade.HIGH_INF_POS, allele); }
  default SortedSet<String> getAllMOTIF_NAMEs(){ return getAllStringValues(VEPRegulatoryFacade.MOTIF_NAME); }
  default SortedSet<String> getAllMOTIF_NAMEs(int allele){ return getAllStringValues(VEPRegulatoryFacade.MOTIF_NAME, allele); }
  default SortedSet<Integer> getAllMOTIF_POSs(){ return getAllIntegerValues(VEPRegulatoryFacade.MOTIF_POS); }
  default SortedSet<Integer> getAllMOTIF_POSs(int allele){ return getAllIntegerValues(VEPRegulatoryFacade.MOTIF_POS, allele); }
  default SortedSet<Double> getAllMOTIF_SCORE_CHANGEs(){ return getAllFloatValues(VEPRegulatoryFacade.MOTIF_SCORE_CHANGE); }
  default SortedSet<Double> getAllMOTIF_SCORE_CHANGEs(int allele){ return getAllFloatValues(VEPRegulatoryFacade.MOTIF_SCORE_CHANGE, allele); }
  default SortedSet<String> getAllTRANSCRIPTION_FACTORSs(){ return getAllStringValues(VEPRegulatoryFacade.TRANSCRIPTION_FACTORS); }
  default SortedSet<String> getAllTRANSCRIPTION_FACTORSs(int allele){ return getAllStringValues(VEPRegulatoryFacade.TRANSCRIPTION_FACTORS, allele); }
}
