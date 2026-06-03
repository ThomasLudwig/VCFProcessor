package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import java.util.SortedSet;

public interface HasVEPMetaAnnotation extends HasVEPAnnotations {
  default SortedSet<String> getAllAMBIGUITYs(){ return getAllStringValues(VEPMetaFacade.AMBIGUITY); }
  default SortedSet<String> getAllAMBIGUITYs(int allele){ return getAllStringValues(VEPMetaFacade.AMBIGUITY, allele); }
  default SortedSet<String> getAllBAM_EDITs(){ return getAllStringValues(VEPMetaFacade.BAM_EDIT); }
  default SortedSet<String> getAllBAM_EDITs(int allele){ return getAllStringValues(VEPMetaFacade.BAM_EDIT, allele); }
  default SortedSet<String> getAllCHECK_REFs(){ return getAllStringValues(VEPMetaFacade.CHECK_REF); }
  default SortedSet<String> getAllCHECK_REFs(int allele){ return getAllStringValues(VEPMetaFacade.CHECK_REF, allele); }
  default SortedSet<String> getAllGIVEN_REFs(){ return getAllStringValues(VEPMetaFacade.GIVEN_REF); }
  default SortedSet<String> getAllGIVEN_REFs(int allele){ return getAllStringValues(VEPMetaFacade.GIVEN_REF, allele); }
  default SortedSet<String> getAllINDs(){ return getAllStringValues(VEPMetaFacade.IND); }
  default SortedSet<String> getAllINDs(int allele){ return getAllStringValues(VEPMetaFacade.IND, allele); }
  default SortedSet<String> getAllMINIMISEDs(){ return getAllStringValues(VEPMetaFacade.MINIMISED); }
  default SortedSet<String> getAllMINIMISEDs(int allele){ return getAllStringValues(VEPMetaFacade.MINIMISED, allele); }
  default SortedSet<Integer> getAllOverlapBPs(){ return getAllIntegerValues(VEPMetaFacade.OVERLAPBP); }
  default SortedSet<Integer> getAllOverlapBPs(int allele){ return getAllIntegerValues(VEPMetaFacade.OVERLAPBP, allele); }
  default SortedSet<Double> getAllOverlapPCs(){ return getAllFloatValues(VEPMetaFacade.OVERLAPPC); }
  default SortedSet<Double> getAllOverlapPCs(int allele){ return getAllFloatValues(VEPMetaFacade.OVERLAPPC, allele); }
  default SortedSet<String> getAllPICKs(){ return getAllStringValues(VEPMetaFacade.PICK); }
  default SortedSet<String> getAllPICKs(int allele){ return getAllStringValues(VEPMetaFacade.PICK, allele); }
  default SortedSet<String> getAllREF_ALLELEs(){ return getAllStringValues(VEPMetaFacade.REF_ALLELE); }
  default SortedSet<String> getAllREF_ALLELEs(int allele){ return getAllStringValues(VEPMetaFacade.REF_ALLELE, allele); }
  default SortedSet<String> getAllUPLOADED_ALLELEs(){ return getAllStringValues(VEPMetaFacade.UPLOADED_ALLELE); }
  default SortedSet<String> getAllUPLOADED_ALLELEs(int allele){ return getAllStringValues(VEPMetaFacade.UPLOADED_ALLELE, allele); }
  default SortedSet<String> getAllUSED_REFs(){ return getAllStringValues(VEPMetaFacade.USED_REF); }
  default SortedSet<String> getAllUSED_REFs(int allele){ return getAllStringValues(VEPMetaFacade.USED_REF, allele); }
  default SortedSet<String> getAllZYGs(){ return getAllStringValues(VEPMetaFacade.ZYG); }
  default SortedSet<String> getAllZYGs(int allele){ return getAllStringValues(VEPMetaFacade.ZYG, allele); }
}
