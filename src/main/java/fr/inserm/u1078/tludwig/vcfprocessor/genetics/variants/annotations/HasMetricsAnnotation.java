package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.FloatInfoField;

public interface HasMetricsAnnotation extends HasAnnotation {

  String QD = "QD";
  String FS = "FS";
  String SOR = "SOR";
  String MQ = "MQ";
  String READPOSRANKSUM = "ReadPosRankSum";
  String INBREEDING_COEFF = "InbreedingCoeff";
  String MQRANKSUM = "MQRankSum";

  default Double getQD() { return getFloatValue(QD); }
  default Double getFS() { return getFloatValue(FS); }
  default Double getSOR() { return getFloatValue(SOR); }
  default Double getMQ() { return getFloatValue(MQ); }
  default Double getReadPosRankSum() { return getFloatValue(READPOSRANKSUM); }
  default Double getInbreedingCoeff() { return getFloatValue(INBREEDING_COEFF); }
  default Double getMQRankSum() { return getFloatValue(MQRANKSUM); }
}
