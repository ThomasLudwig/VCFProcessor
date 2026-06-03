package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

public interface VEPMetaFacade extends VEPFacade {
  VEPField REF_ALLELE = new VEPField("REF_ALLELE", Category.Meta, Type.String, "Reference allele after minimisation");
  VEPField UPLOADED_ALLELE = new VEPField("UPLOADED_ALLELE", Category.Meta, Type.String, "Allele string as originally uploaded (before minimisation)");
  VEPField GIVEN_REF = new VEPField("GIVEN_REF", Category.Meta, Type.String, "Reference allele as provided in the input file");
  VEPField USED_REF = new VEPField("USED_REF", Category.Meta, Type.String, "Reference allele actually used to compute consequences");
  VEPField IND = new VEPField("IND", Category.Meta, Type.String, "Individual name when using multi-sample VCF input (--individual)");
  VEPField ZYG = new VEPField("ZYG", Category.Meta, Type.String, "Zygosity of individual genotype at this locus (HOM, HET, HEMIZYGOUS)");
  VEPField PICK = new VEPField("PICK", Category.Meta, Type.String, "Flag '1' if this consequence was picked by --flag_pick or --flag_pick_allele");
  VEPField MINIMISED = new VEPField("MINIMISED", Category.Meta, Type.String, "Flag '1' if alleles were converted to minimal representation before consequence calculation");
  VEPField BAM_EDIT = new VEPField("BAM_EDIT", Category.Meta, Type.String, "Indicates success or failure of BAM-based sequence edit (OK or FAILED)");
  VEPField CHECK_REF = new VEPField("CHECK_REF", Category.Meta, Type.String, "Reports variants where the input reference allele does not match the expected reference");
  VEPField AMBIGUITY = new VEPField("AMBIGUITY", Category.Meta, Type.String, "IUPAC allele ambiguity code for the variant position");
  VEPField OVERLAPBP = new VEPField("OverlapBP", Category.Meta, Type.Integer, "Number of base pairs overlapping the corresponding structural variation feature");
  VEPField OVERLAPPC = new VEPField("OverlapPC", Category.Meta, Type.Float, "Percentage of the structural variation feature overlapped by the input variant");

  default String getAMBIGUITY(){ return getStringValue(AMBIGUITY); }
  default String getBAM_EDIT(){ return getStringValue(BAM_EDIT); }
  default String getCHECK_REF(){ return getStringValue(CHECK_REF); }
  default String getGIVEN_REF(){ return getStringValue(GIVEN_REF); }
  default String getIND(){ return getStringValue(IND); }
  default String getMINIMISED(){ return getStringValue(MINIMISED); }
  default Integer getOverlapBP(){ return getIntegerValue(OVERLAPBP); }
  default Double getOverlapPC(){ return getFloatValue(OVERLAPPC); }
  default String getPICK(){ return getStringValue(PICK); }
  default String getREF_ALLELE(){ return getStringValue(REF_ALLELE); }
  default String getUPLOADED_ALLELE(){ return getStringValue(UPLOADED_ALLELE); }
  default String getUSED_REF(){ return getStringValue(USED_REF); }
  default String getZYG(){ return getStringValue(ZYG); }

}
