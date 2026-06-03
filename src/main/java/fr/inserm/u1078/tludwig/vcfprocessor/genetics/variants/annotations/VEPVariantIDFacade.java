package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

public interface VEPVariantIDFacade extends VEPFacade {
  VEPField ALLELE = new VEPField("Allele", Category.VariantID, Type.String, "The variant allele used to calculate the consequence");
  VEPField EXISTING_VARIATION = new VEPField("Existing_variation", Category.VariantID, Type.String, "Identifier(s) of co-located known variants (e.g. rs IDs, COSV IDs)");
  VEPField ALLELE_NUM = new VEPField("ALLELE_NUM", Category.VariantID, Type.Integer, "Allele number from input: 0=reference, 1=first ALT, 2=second ALT, etc.");
  VEPField PUBMED = new VEPField("PUBMED", Category.VariantID, Type.Integer, "PubMed ID(s) of publications citing the co-located existing variant");
  VEPField SOMATIC = new VEPField("SOMATIC", Category.VariantID, Type.String, "Somatic status of co-located variant(s); multi-value corresponds to Existing_variation entries");
  VEPField PHENO = new VEPField("PHENO", Category.VariantID, Type.String, "Whether the existing variant is associated with a phenotype/disease/trait (1=yes, 0=no)");
  VEPField SV = new VEPField("SV", Category.VariantID, Type.String, "IDs of overlapping structural variants from Ensembl Variation");

  default String getAllele(){ return getStringValue(ALLELE); }

  default int getAlleleNumber() {
    Integer n = getIntegerValue(ALLELE_NUM);
    return n == null ? -1 : n;
  }

  default String[] getExisting_variation(){ return getStringArrayValue(EXISTING_VARIATION); }
  default String[] getPHENO(){ return getStringArrayValue(PHENO); }
  default int[] getPUBMED(){ return getIntegerArrayValue(PUBMED); }
  default String[] getSOMATIC(){ return getStringArrayValue(SOMATIC); }
  default String[] getSV(){ return getStringArrayValue(SV); }
}
