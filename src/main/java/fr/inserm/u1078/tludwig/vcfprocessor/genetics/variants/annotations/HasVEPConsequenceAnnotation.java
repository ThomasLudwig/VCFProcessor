package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import java.util.*;

public interface HasVEPConsequenceAnnotation extends HasVEPAnnotations {

  default SortedSet<String> getAllCLIN_SIGs(){ return getAllStringValues(VEPConsequenceFacade.CLIN_SIG); }
  default SortedSet<String> getAllCLIN_SIGs(int allele){ return getAllStringValues(VEPConsequenceFacade.CLIN_SIG, allele); }
  default SortedSet<String> getAllConsequences(){ return getAllStringValues(VEPConsequenceFacade.CONSEQUENCE); }
  default SortedSet<String> getAllConsequences(int allele){ return getAllStringValues(VEPConsequenceFacade.CONSEQUENCE, allele); }
  default SortedSet<String> getAllIMPACTs(){ return getAllStringValues(VEPConsequenceFacade.IMPACT); }
  default SortedSet<String> getAllIMPACTs(int allele){ return getAllStringValues(VEPConsequenceFacade.IMPACT, allele); }
  default SortedSet<String> getAllPolyPhens(){ return getAllStringValues(VEPConsequenceFacade.POLYPHEN); }
  default SortedSet<String> getAllPolyPhens(int allele){ return getAllStringValues(VEPConsequenceFacade.POLYPHEN, allele); }
  default SortedSet<String> getAllSIFTs(){ return getAllStringValues(VEPConsequenceFacade.SIFT); }
  default SortedSet<String> getAllSIFTs(int allele){ return getAllStringValues(VEPConsequenceFacade.SIFT, allele); }

  /**
   * Gets worst Annotation for each allele
   * @return  the worst annotation for each allele
   */
  default HashMap<Integer, VEPAnnotation> getWorstAnnotationsByAllele(){
    HashMap<Integer, VEPAnnotation> annotations = new HashMap<>();
    for (int a : this.getAlleles())
      annotations.put(a, VEPConsequence.getWorstVEPAnnotation(annotations.get(a)));
    return annotations;
  }

  default SortedSet<VEPConsequence> getAllVEPConsequences(){
    SortedSet<VEPConsequence> consequences = new TreeSet<>();
    for(String csq : this.getAllConsequences())
      consequences.add(VEPConsequence.getConsequence(csq));
    return consequences;
  }

  default SortedSet<VEPConsequence> getAllVEPConsequences(int allele){
    SortedSet<VEPConsequence> consequences = new TreeSet<>();
    for(String csq : this.getAllConsequences(allele))
      consequences.add(VEPConsequence.getConsequence(csq));
    return consequences;
  }

  default SortedSet<Integer> getAllConsequenceLevels(){
    SortedSet<Integer> consequences = new TreeSet<>();
    for(String csq : this.getAllConsequences())
      consequences.add(VEPConsequence.getConsequenceLevel(csq));
    return consequences;
  }

  default SortedSet<Integer> getAllConsequenceLevels(int allele){
    SortedSet<Integer> consequences = new TreeSet<>();
    for(String csq : this.getAllConsequences(allele))
      consequences.add(VEPConsequence.getConsequenceLevel(csq));
    return consequences;
  }

  /**
   * Gets the canonical consequence associated by VEP to a variant allele
   *
   * @param a the alternate allele number (1 to N)
   * @return [CanonicalConsequence],[GeneSymbol]
   */
  default VEPAnnotation getCanonicalVEPAnnotation(int a) {
    Set<VEPAnnotation> canonicals = new HashSet<>();
    Set<VEPAnnotation> nonCanonicals = new HashSet<>();
    for (VEPAnnotation vep : getVEPAnnotations(a))
      if ("YES".equals(vep.getCANONICAL()))
        canonicals.add(vep);
      else
        nonCanonicals.add(vep);

    return canonicals.isEmpty()
        ? VEPConsequence.getWorstVEPAnnotation(nonCanonicals)
        : VEPConsequence.getWorstVEPAnnotation(canonicals);
  }
}
