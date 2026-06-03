package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import java.util.SortedSet;

public interface HasVEPFrequencyAnnotation extends HasVEPAnnotations {
  default SortedSet<String> getAllFREQSs(){ return getAllStringValues(VEPFrequencyFacade.FREQS); }
  default SortedSet<String> getAllFREQSs(int allele){ return getAllStringValues(VEPFrequencyFacade.FREQS, allele); }

  default double getFrequency(VEPFacade.VEPField field, int allele) { return firstValueOr0(getAllFloatValues(field, allele)); }
  default double getFrequency(String key, int allele) { return getFrequency(VEPFrequencyFacade.ALL_FREQUENCIES.get(key), allele); }

  default double getAF(int allele){ return getFrequency(VEPFrequencyFacade.AF, allele); }
  default double getAFR_AF(int allele){ return getFrequency(VEPFrequencyFacade.AFR_AF, allele); }
  default double getAMR_AF(int allele){ return getFrequency(VEPFrequencyFacade.AMR_AF, allele); }
  default double getASN_AF(int allele){ return getFrequency(VEPFrequencyFacade.ASN_AF, allele); }
  default double getEAS_AF(int allele){ return getFrequency(VEPFrequencyFacade.EAS_AF, allele); }
  default double getEUR_AF(int allele){ return getFrequency(VEPFrequencyFacade.EUR_AF, allele); }
  default double getgnomADe_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADE_AF, allele); }
  default double getgnomADe_AFR_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADE_AFR_AF, allele); }
  default double getgnomADe_AMR_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADE_AMR_AF, allele); }
  default double getgnomADe_ASJ_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADE_ASJ_AF, allele); }
  default double getgnomADe_EAS_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADE_EAS_AF, allele); }
  default double getgnomADe_FIN_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADE_FIN_AF, allele); }
  default double getgnomADe_MID_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADE_MID_AF, allele); }
  default double getgnomADe_NFE_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADE_NFE_AF, allele); }
  default double getgnomADe_REMAINING_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADE_REMAINING_AF, allele); }
  default double getgnomADe_SAS_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADE_SAS_AF, allele); }
  default double getgnomADg_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_AF, allele); }
  default double getgnomADg_AFR_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_AFR_AF, allele); }
  default double getgnomADg_AMI_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_AMI_AF, allele); }
  default double getgnomADg_AMR_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_AMR_AF, allele); }
  default double getgnomADg_ASJ_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_ASJ_AF, allele); }
  default double getgnomADg_EAS_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_EAS_AF, allele); }
  default double getgnomADg_FIN_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_FIN_AF, allele); }
  default double getgnomADg_MID_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_MID_AF, allele); }
  default double getgnomADg_NFE_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_NFE_AF, allele); }
  default double getgnomADg_REMAINING_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_REMAINING_AF, allele); }
  default double getgnomADg_SAS_AF(int allele){ return getFrequency(VEPFrequencyFacade.GNOMADG_SAS_AF, allele); }
  default double getMAX_AF(int allele){ return getFrequency(VEPFrequencyFacade.MAX_AF, allele); }
  default double getSAS_AF(int allele){ return getFrequency(VEPFrequencyFacade.SAS_AF, allele); }


  /**
   *
   * Gets the min of gnomadG/gnomadE
   * @param allele
   * @return
   */
  default double getgnomAD_AF(int allele){ return Math.min(getgnomADe_AF(allele), getgnomADg_AF(allele)); }
  /**
   *
   * Gets the min of gnomadG/gnomadE
   * @param allele
   * @return
   */
  default double getgnomAD_NFE_AF(int allele){ return Math.min(getgnomADe_NFE_AF(allele), getgnomADg_NFE_AF(allele)); }




  default String getMAX_AF_POPS(int allele) {
    SortedSet<String> ss = getAllStringValues(VEPFrequencyFacade.MAX_AF_POPS, allele);
    return ss.isEmpty() ? null : ss.first(); //TODO null or "" ?
  }

  default boolean isIn1kg(int allele) {
    return hasValue(VEPFrequencyFacade.AF, allele);
  }

  default boolean isInGnomAD(int allele) { return isInGnomADExome(allele) || isInGnomADGenome(allele); }

  default boolean isInGnomADExome(int allele) {
    return hasValue(VEPFrequencyFacade.GNOMADE_AF, allele);
  }

  default boolean isInGnomADGenome(int allele) {
    return hasValue(VEPFrequencyFacade.GNOMADG_AF, allele);
  }

}
