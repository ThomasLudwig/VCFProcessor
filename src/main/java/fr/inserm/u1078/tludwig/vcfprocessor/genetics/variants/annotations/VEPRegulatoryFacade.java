package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

public interface VEPRegulatoryFacade extends VEPFacade {
  VEPField MOTIF_NAME = new VEPField("MOTIF_NAME", Category.Regulatory, Type.String, "Source and identifier of a transcription factor binding profile (TFBP) aligned at this position");
  VEPField MOTIF_POS = new VEPField("MOTIF_POS", Category.Regulatory, Type.Integer, "Relative position of the variation within the aligned TFBP");
  VEPField HIGH_INF_POS = new VEPField("HIGH_INF_POS", Category.Regulatory, Type.String, "Flag (T/F) indicating if the variant falls at a high-information position of the TFBP");
  VEPField MOTIF_SCORE_CHANGE = new VEPField("MOTIF_SCORE_CHANGE", Category.Regulatory, Type.Float, "Difference in motif score between reference and variant sequences for the TFBP");
  VEPField TRANSCRIPTION_FACTORS = new VEPField("TRANSCRIPTION_FACTORS", Category.Regulatory, Type.String, "Transcription factors binding to the affected motif feature");
  VEPField CELL_TYPE = new VEPField("CELL_TYPE", Category.Regulatory, Type.String, "List of cell types and classifications for the overlapping regulatory feature");

  default String getCELL_TYPE(){ return getStringValue(CELL_TYPE); }
  default String getHIGH_INF_POS(){ return getStringValue(HIGH_INF_POS); }
  default String getMOTIF_NAME(){ return getStringValue(MOTIF_NAME); }
  default Integer getMOTIF_POS(){ return getIntegerValue(MOTIF_POS); }
  default Double getMOTIF_SCORE_CHANGE(){ return getFloatValue(MOTIF_SCORE_CHANGE); }
  default String getTRANSCRIPTION_FACTORS(){ return getStringValue(TRANSCRIPTION_FACTORS); }

}
