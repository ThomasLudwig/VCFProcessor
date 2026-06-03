package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import java.util.SortedSet;
import java.util.TreeSet;

public interface HasVEPVariantIDAnnotation extends HasVEPAnnotations {
  default SortedSet<Integer> getAllALLELE_NUMs(){ return getAllIntegerValues(VEPVariantIDFacade.ALLELE_NUM); }
  default SortedSet<Integer> getAllALLELE_NUMs(int allele){ return getAllIntegerValues(VEPVariantIDFacade.ALLELE_NUM, allele); }
  default SortedSet<Integer> getAllPUBMEDs(){ return getAllIntegerValues(VEPVariantIDFacade.PUBMED); }
  default SortedSet<Integer> getAllPUBMEDs(int allele){ return getAllIntegerValues(VEPVariantIDFacade.PUBMED, allele); }
  default SortedSet<String> getAllAlleles(){ return getAllStringValues(VEPVariantIDFacade.ALLELE); }
  default SortedSet<String> getAllAlleles(int allele){ return getAllStringValues(VEPVariantIDFacade.ALLELE, allele); }
  default SortedSet<String> getAllExisting_variations(){ return getAllStringValues(VEPVariantIDFacade.EXISTING_VARIATION); }
  default SortedSet<String> getAllExisting_variations(int allele){ return getAllStringValues(VEPVariantIDFacade.EXISTING_VARIATION, allele); }
  default SortedSet<String> getAllPHENOs(){ return getAllStringValues(VEPVariantIDFacade.PHENO); }
  default SortedSet<String> getAllPHENOs(int allele){ return getAllStringValues(VEPVariantIDFacade.PHENO, allele); }
  default SortedSet<String> getAllSOMATICs(){ return getAllStringValues(VEPVariantIDFacade.SOMATIC); }
  default SortedSet<String> getAllSOMATICs(int allele){ return getAllStringValues(VEPVariantIDFacade.SOMATIC, allele); }
  default SortedSet<String> getAllSVs(){ return getAllStringValues(VEPVariantIDFacade.SV); }
  default SortedSet<String> getAllSVs(int allele){ return getAllStringValues(VEPVariantIDFacade.SV, allele); }

  default boolean hasExistingVariants() { return !getAllExisting_variations().isEmpty(); }
  default boolean hasExistingVariants(int allele) { return !getAllExisting_variations(allele).isEmpty(); }

  default SortedSet<String> getRSs() {
    SortedSet<String> rs = new TreeSet<>();
    for(String s : getAllExisting_variations())
      if(s.startsWith("rs"))
        rs.add(s);
    return rs;
  }
}
