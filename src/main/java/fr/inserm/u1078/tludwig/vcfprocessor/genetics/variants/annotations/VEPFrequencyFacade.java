package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import java.util.HashMap;
import java.util.Map;

public interface VEPFrequencyFacade extends VEPFacade {
  VEPField AF = new VEPField("AF", Category.Frequency, Type.Float, "Frequency of existing variant in 1000 Genomes phase 3 combined population");
  VEPField AFR_AF = new VEPField("AFR_AF", Category.Frequency, Type.Float, "Frequency in 1000 Genomes combined African population");
  VEPField AMR_AF = new VEPField("AMR_AF", Category.Frequency, Type.Float, "Frequency in 1000 Genomes combined American population");
  VEPField ASN_AF = new VEPField("ASN_AF", Category.Frequency, Type.Float, "Frequency in 1000 Genomes combined Asian population (legacy field)");
  VEPField EUR_AF = new VEPField("EUR_AF", Category.Frequency, Type.Float, "Frequency in 1000 Genomes combined European population");
  VEPField EAS_AF = new VEPField("EAS_AF", Category.Frequency, Type.Float, "Frequency in 1000 Genomes combined East Asian population");
  VEPField SAS_AF = new VEPField("SAS_AF", Category.Frequency, Type.Float, "Frequency in 1000 Genomes combined South Asian population");
  VEPField GNOMADE_AF = new VEPField("gnomADe_AF", Category.Frequency, Type.Float, "gnomAD exomes combined population allele frequency");
  VEPField GNOMADE_AFR_AF = new VEPField("gnomADe_AFR_AF", Category.Frequency, Type.Float, "gnomAD exomes — African/American population");
  VEPField GNOMADE_AMR_AF = new VEPField("gnomADe_AMR_AF", Category.Frequency, Type.Float, "gnomAD exomes — American (Latino) population");
  VEPField GNOMADE_ASJ_AF = new VEPField("gnomADe_ASJ_AF", Category.Frequency, Type.Float, "gnomAD exomes — Ashkenazi Jewish population");
  VEPField GNOMADE_EAS_AF = new VEPField("gnomADe_EAS_AF", Category.Frequency, Type.Float, "gnomAD exomes — East Asian population");
  VEPField GNOMADE_FIN_AF = new VEPField("gnomADe_FIN_AF", Category.Frequency, Type.Float, "gnomAD exomes — Finnish population");
  VEPField GNOMADE_MID_AF = new VEPField("gnomADe_MID_AF", Category.Frequency, Type.Float, "gnomAD exomes — Mid-Eastern population");
  VEPField GNOMADE_NFE_AF = new VEPField("gnomADe_NFE_AF", Category.Frequency, Type.Float, "gnomAD exomes — Non-Finnish European population");
  VEPField GNOMADE_REMAINING_AF = new VEPField("gnomADe_REMAINING_AF", Category.Frequency, Type.Float, "gnomAD exomes — remaining combined populations");
  VEPField GNOMADE_SAS_AF = new VEPField("gnomADe_SAS_AF", Category.Frequency, Type.Float, "gnomAD exomes — South Asian population");
  VEPField GNOMADG_AF = new VEPField("gnomADg_AF", Category.Frequency, Type.Float, "gnomAD genomes combined population allele frequency");
  VEPField GNOMADG_AFR_AF = new VEPField("gnomADg_AFR_AF", Category.Frequency, Type.Float, "gnomAD genomes — African/American population");
  VEPField GNOMADG_AMI_AF = new VEPField("gnomADg_AMI_AF", Category.Frequency, Type.Float, "gnomAD genomes — Amish population");
  VEPField GNOMADG_AMR_AF = new VEPField("gnomADg_AMR_AF", Category.Frequency, Type.Float, "gnomAD genomes — American (Latino) population");
  VEPField GNOMADG_ASJ_AF = new VEPField("gnomADg_ASJ_AF", Category.Frequency, Type.Float, "gnomAD genomes — Ashkenazi Jewish population");
  VEPField GNOMADG_EAS_AF = new VEPField("gnomADg_EAS_AF", Category.Frequency, Type.Float, "gnomAD genomes — East Asian population");
  VEPField GNOMADG_FIN_AF = new VEPField("gnomADg_FIN_AF", Category.Frequency, Type.Float, "gnomAD genomes — Finnish population");
  VEPField GNOMADG_MID_AF = new VEPField("gnomADg_MID_AF", Category.Frequency, Type.Float, "gnomAD genomes — Mid-Eastern population");
  VEPField GNOMADG_NFE_AF = new VEPField("gnomADg_NFE_AF", Category.Frequency, Type.Float, "gnomAD genomes — Non-Finnish European population");
  VEPField GNOMADG_REMAINING_AF = new VEPField("gnomADg_REMAINING_AF", Category.Frequency, Type.Float, "gnomAD genomes — remaining combined populations");
  VEPField GNOMADG_SAS_AF = new VEPField("gnomADg_SAS_AF", Category.Frequency, Type.Float, "gnomAD genomes — South Asian population");
  VEPField MAX_AF = new VEPField("MAX_AF", Category.Frequency, Type.Float, "Maximum observed allele frequency across 1000 Genomes, ESP and gnomAD");
  VEPField MAX_AF_POPS = new VEPField("MAX_AF_POPS", Category.Frequency, Type.String, "Population(s) in which the maximum allele frequency was observed");
  VEPField FREQS = new VEPField("FREQS", Category.Frequency, Type.String, "Frequencies of overlapping variants used in frequency filtering");

  Map<String, VEPField> ALL_FREQUENCIES = buildMap();

  static Map<String, VEPField> buildMap(){
    Map<String, VEPField> map = new HashMap<String, VEPField>();
    map.put(AF.getId(), AF);
    map.put(AFR_AF.getId(), AFR_AF);
    map.put(AMR_AF.getId(), AMR_AF);
    map.put(ASN_AF.getId(), ASN_AF);
    map.put(EAS_AF.getId(), EAS_AF);
    map.put(EUR_AF.getId(), EUR_AF);
    map.put(SAS_AF.getId(), SAS_AF);
    map.put(GNOMADE_AF.getId(), GNOMADE_AF);
    map.put(GNOMADE_AFR_AF.getId(), GNOMADE_AFR_AF);
    map.put(GNOMADE_AMR_AF.getId(), GNOMADE_AMR_AF);
    map.put(GNOMADE_ASJ_AF.getId(), GNOMADE_ASJ_AF);
    map.put(GNOMADE_EAS_AF.getId(), GNOMADE_EAS_AF);
    map.put(GNOMADE_FIN_AF.getId(), GNOMADE_FIN_AF);
    map.put(GNOMADE_MID_AF.getId(), GNOMADE_MID_AF);
    map.put(GNOMADE_NFE_AF.getId(), GNOMADE_NFE_AF);
    map.put(GNOMADE_REMAINING_AF.getId(), GNOMADE_REMAINING_AF);
    map.put(GNOMADE_SAS_AF.getId(), GNOMADE_SAS_AF);
    map.put(GNOMADG_AF.getId(), GNOMADG_AF);
    map.put(GNOMADG_AFR_AF.getId(), GNOMADG_AFR_AF);
    map.put(GNOMADG_AMI_AF.getId(), GNOMADG_AMI_AF);
    map.put(GNOMADG_AMR_AF.getId(), GNOMADG_AMR_AF);
    map.put(GNOMADG_ASJ_AF.getId(), GNOMADG_ASJ_AF);
    map.put(GNOMADG_EAS_AF.getId(), GNOMADG_EAS_AF);
    map.put(GNOMADG_FIN_AF.getId(), GNOMADG_FIN_AF);
    map.put(GNOMADG_MID_AF.getId(), GNOMADG_MID_AF);
    map.put(GNOMADG_NFE_AF.getId(), GNOMADG_NFE_AF);
    map.put(GNOMADG_REMAINING_AF.getId(), GNOMADG_REMAINING_AF);
    map.put(GNOMADG_SAS_AF.getId(), GNOMADG_SAS_AF);
    map.put(MAX_AF.getId(), MAX_AF);
    map.put(MAX_AF_POPS.getId(), MAX_AF_POPS);
    map.put(FREQS.getId(), FREQS);
    return map;
  }

  default Double getAF(){ return getFloatValue(AF); }
  default Double getAFR_AF(){ return getFloatValue(AFR_AF); }
  default Double getAMR_AF(){ return getFloatValue(AMR_AF); }
  default Double getASN_AF(){ return getFloatValue(ASN_AF); }
  default Double getEAS_AF(){ return getFloatValue(EAS_AF); }
  default Double getEUR_AF(){ return getFloatValue(EUR_AF); }
  default String getFREQS(){ return getStringValue(FREQS); }
  default Double getgnomADe_AF(){ return getFloatValue(GNOMADE_AF); }
  default Double getgnomADe_AFR_AF(){ return getFloatValue(GNOMADE_AFR_AF); }
  default Double getgnomADe_AMR_AF(){ return getFloatValue(GNOMADE_AMR_AF); }
  default Double getgnomADe_ASJ_AF(){ return getFloatValue(GNOMADE_ASJ_AF); }
  default Double getgnomADe_EAS_AF(){ return getFloatValue(GNOMADE_EAS_AF); }
  default Double getgnomADe_FIN_AF(){ return getFloatValue(GNOMADE_FIN_AF); }
  default Double getgnomADe_MID_AF(){ return getFloatValue(GNOMADE_MID_AF); }
  default Double getgnomADe_NFE_AF(){ return getFloatValue(GNOMADE_NFE_AF); }
  default Double getgnomADe_REMAINING_AF(){ return getFloatValue(GNOMADE_REMAINING_AF); }
  default Double getgnomADe_SAS_AF(){ return getFloatValue(GNOMADE_SAS_AF); }
  default Double getgnomADg_AF(){ return getFloatValue(GNOMADG_AF); }
  default Double getgnomADg_AFR_AF(){ return getFloatValue(GNOMADG_AFR_AF); }
  default Double getgnomADg_AMI_AF(){ return getFloatValue(GNOMADG_AMI_AF); }
  default Double getgnomADg_AMR_AF(){ return getFloatValue(GNOMADG_AMR_AF); }
  default Double getgnomADg_ASJ_AF(){ return getFloatValue(GNOMADG_ASJ_AF); }
  default Double getgnomADg_EAS_AF(){ return getFloatValue(GNOMADG_EAS_AF); }
  default Double getgnomADg_FIN_AF(){ return getFloatValue(GNOMADG_FIN_AF); }
  default Double getgnomADg_MID_AF(){ return getFloatValue(GNOMADG_MID_AF); }
  default Double getgnomADg_NFE_AF(){ return getFloatValue(GNOMADG_NFE_AF); }
  default Double getgnomADg_REMAINING_AF(){ return getFloatValue(GNOMADG_REMAINING_AF); }
  default Double getgnomADg_SAS_AF(){ return getFloatValue(GNOMADG_SAS_AF); }
  default Double getMAX_AF(){ return getFloatValue(MAX_AF); }
  default String getMAX_AF_POPS(){ return getStringValue(MAX_AF_POPS); }
  default Double getSAS_AF(){ return getFloatValue(SAS_AF); }
}
