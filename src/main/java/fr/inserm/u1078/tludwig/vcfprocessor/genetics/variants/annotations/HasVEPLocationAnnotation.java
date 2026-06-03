package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import java.util.SortedSet;

public interface HasVEPLocationAnnotation extends HasVEPAnnotations {
  default SortedSet<String> getAllAmino_acidss(){ return getAllStringValues(VEPLocationFacade.AMINO_ACIDS); }
  default SortedSet<String> getAllAmino_acidss(int allele){ return getAllStringValues(VEPLocationFacade.AMINO_ACIDS, allele); }
  default SortedSet<String> getAllAPPRISs(){ return getAllStringValues(VEPLocationFacade.APPRIS); }
  default SortedSet<String> getAllAPPRISs(int allele){ return getAllStringValues(VEPLocationFacade.APPRIS, allele); }
  default SortedSet<String> getAllBIOTYPEs(){ return getAllStringValues(VEPLocationFacade.BIOTYPE); }
  default SortedSet<String> getAllBIOTYPEs(int allele){ return getAllStringValues(VEPLocationFacade.BIOTYPE, allele); }
  default SortedSet<String> getAllCANONICALs(){ return getAllStringValues(VEPLocationFacade.CANONICAL); }
  default SortedSet<String> getAllCANONICALs(int allele){ return getAllStringValues(VEPLocationFacade.CANONICAL, allele); }
  default SortedSet<String> getAllCCDSs(){ return getAllStringValues(VEPLocationFacade.CCDS); }
  default SortedSet<String> getAllCCDSs(int allele){ return getAllStringValues(VEPLocationFacade.CCDS, allele); }
  default SortedSet<String> getAllcDNA_positions(){ return getAllStringValues(VEPLocationFacade.CDNA_POSITION); }
  default SortedSet<String> getAllcDNA_positions(int allele){ return getAllStringValues(VEPLocationFacade.CDNA_POSITION, allele); }
  default SortedSet<String> getAllCDS_positions(){ return getAllStringValues(VEPLocationFacade.CDS_POSITION); }
  default SortedSet<String> getAllCDS_positions(int allele){ return getAllStringValues(VEPLocationFacade.CDS_POSITION, allele); }
  default SortedSet<String> getAllCodonss(){ return getAllStringValues(VEPLocationFacade.CODONS); }
  default SortedSet<String> getAllCodonss(int allele){ return getAllStringValues(VEPLocationFacade.CODONS, allele); }
  default SortedSet<String> getAllDISTANCEs(){ return getAllStringValues(VEPLocationFacade.DISTANCE); }
  default SortedSet<String> getAllDISTANCEs(int allele){ return getAllStringValues(VEPLocationFacade.DISTANCE, allele); }
  default SortedSet<String> getAllDOMAINSs(){ return getAllStringValues(VEPLocationFacade.DOMAINS); }
  default SortedSet<String> getAllDOMAINSs(int allele){ return getAllStringValues(VEPLocationFacade.DOMAINS, allele); }
  default SortedSet<String> getAllENSPs(){ return getAllStringValues(VEPLocationFacade.ENSP); }
  default SortedSet<String> getAllENSPs(int allele){ return getAllStringValues(VEPLocationFacade.ENSP, allele); }
  default SortedSet<String> getAllEXONs(){ return getAllStringValues(VEPLocationFacade.EXON); }
  default SortedSet<String> getAllEXONs(int allele){ return getAllStringValues(VEPLocationFacade.EXON, allele); }
  default SortedSet<String> getAllFeatures(){ return getAllStringValues(VEPLocationFacade.FEATURE); }
  default SortedSet<String> getAllFeatures(int allele){ return getAllStringValues(VEPLocationFacade.FEATURE, allele); }
  default SortedSet<String> getAllFeature_types(){ return getAllStringValues(VEPLocationFacade.FEATURE_TYPE); }
  default SortedSet<String> getAllFeature_types(int allele){ return getAllStringValues(VEPLocationFacade.FEATURE_TYPE, allele); }
  default SortedSet<String> getAllFLAGSs(){ return getAllStringValues(VEPLocationFacade.FLAGS); }
  default SortedSet<String> getAllFLAGSs(int allele){ return getAllStringValues(VEPLocationFacade.FLAGS, allele); }
  default SortedSet<String> getAllGENCODE_PRIMARYs(){ return getAllStringValues(VEPLocationFacade.GENCODE_PRIMARY); }
  default SortedSet<String> getAllGENCODE_PRIMARYs(int allele){ return getAllStringValues(VEPLocationFacade.GENCODE_PRIMARY, allele); }
  default SortedSet<String> getAllGenes(){ return getAllStringValues(VEPLocationFacade.GENE); }
  default SortedSet<String> getAllGenes(int allele){ return getAllStringValues(VEPLocationFacade.GENE, allele); }
  default SortedSet<String> getAllGENE_PHENOs(){ return getAllStringValues(VEPLocationFacade.GENE_PHENO); }
  default SortedSet<String> getAllGENE_PHENOs(int allele){ return getAllStringValues(VEPLocationFacade.GENE_PHENO, allele); }
  default SortedSet<String> getAllHGNC_IDs(){ return getAllStringValues(VEPLocationFacade.HGNC_ID); }
  default SortedSet<String> getAllHGNC_IDs(int allele){ return getAllStringValues(VEPLocationFacade.HGNC_ID, allele); }
  default SortedSet<String> getAllHGVS_OFFSETs(){ return getAllStringValues(VEPLocationFacade.HGVS_OFFSET); }
  default SortedSet<String> getAllHGVS_OFFSETs(int allele){ return getAllStringValues(VEPLocationFacade.HGVS_OFFSET, allele); }
  default SortedSet<String> getAllHGVScs(){ return getAllStringValues(VEPLocationFacade.HGVSC); }
  default SortedSet<String> getAllHGVScs(int allele){ return getAllStringValues(VEPLocationFacade.HGVSC, allele); }
  default SortedSet<String> getAllHGVSgs(){ return getAllStringValues(VEPLocationFacade.HGVSG); }
  default SortedSet<String> getAllHGVSgs(int allele){ return getAllStringValues(VEPLocationFacade.HGVSG, allele); }
  default SortedSet<String> getAllHGVSps(){ return getAllStringValues(VEPLocationFacade.HGVSP); }
  default SortedSet<String> getAllHGVSps(int allele){ return getAllStringValues(VEPLocationFacade.HGVSP, allele); }
  default SortedSet<String> getAllINTRONs(){ return getAllStringValues(VEPLocationFacade.INTRON); }
  default SortedSet<String> getAllINTRONs(int allele){ return getAllStringValues(VEPLocationFacade.INTRON, allele); }
  default SortedSet<String> getAllMANE_PLUS_CLINICALs(){ return getAllStringValues(VEPLocationFacade.MANE_PLUS_CLINICAL); }
  default SortedSet<String> getAllMANE_PLUS_CLINICALs(int allele){ return getAllStringValues(VEPLocationFacade.MANE_PLUS_CLINICAL, allele); }
  default SortedSet<String> getAllMANE_SELECTs(){ return getAllStringValues(VEPLocationFacade.MANE_SELECT); }
  default SortedSet<String> getAllMANE_SELECTs(int allele){ return getAllStringValues(VEPLocationFacade.MANE_SELECT, allele); }
  default SortedSet<String> getAllmiRNAs(){ return getAllStringValues(VEPLocationFacade.MIRNA); }
  default SortedSet<String> getAllmiRNAs(int allele){ return getAllStringValues(VEPLocationFacade.MIRNA, allele); }
  default SortedSet<String> getAllNEARESTs(){ return getAllStringValues(VEPLocationFacade.NEAREST); }
  default SortedSet<String> getAllNEARESTs(int allele){ return getAllStringValues(VEPLocationFacade.NEAREST, allele); }
  default SortedSet<String> getAllProtein_positions(){ return getAllStringValues(VEPLocationFacade.PROTEIN_POSITION); }
  default SortedSet<String> getAllProtein_positions(int allele){ return getAllStringValues(VEPLocationFacade.PROTEIN_POSITION, allele); }
  default SortedSet<String> getAllREFSEQ_MATCHs(){ return getAllStringValues(VEPLocationFacade.REFSEQ_MATCH); }
  default SortedSet<String> getAllREFSEQ_MATCHs(int allele){ return getAllStringValues(VEPLocationFacade.REFSEQ_MATCH, allele); }
  default SortedSet<String> getAllSOURCEs(){ return getAllStringValues(VEPLocationFacade.SOURCE); }
  default SortedSet<String> getAllSOURCEs(int allele){ return getAllStringValues(VEPLocationFacade.SOURCE, allele); }
  default SortedSet<String> getAllSTRANDs(){ return getAllStringValues(VEPLocationFacade.STRAND); }
  default SortedSet<String> getAllSTRANDs(int allele){ return getAllStringValues(VEPLocationFacade.STRAND, allele); }
  default SortedSet<String> getAllSWISSPROTs(){ return getAllStringValues(VEPLocationFacade.SWISSPROT); }
  default SortedSet<String> getAllSWISSPROTs(int allele){ return getAllStringValues(VEPLocationFacade.SWISSPROT, allele); }
  default SortedSet<String> getAllSYMBOLs(){ return getAllStringValues(VEPLocationFacade.SYMBOL); }
  default SortedSet<String> getAllSYMBOLs(int allele){ return getAllStringValues(VEPLocationFacade.SYMBOL, allele); }
  default SortedSet<String> getAllSYMBOL_SOURCEs(){ return getAllStringValues(VEPLocationFacade.SYMBOL_SOURCE); }
  default SortedSet<String> getAllSYMBOL_SOURCEs(int allele){ return getAllStringValues(VEPLocationFacade.SYMBOL_SOURCE, allele); }
  default SortedSet<String> getAllTREMBLs(){ return getAllStringValues(VEPLocationFacade.TREMBL); }
  default SortedSet<String> getAllTREMBLs(int allele){ return getAllStringValues(VEPLocationFacade.TREMBL, allele); }
  default SortedSet<String> getAllTSLs(){ return getAllStringValues(VEPLocationFacade.TSL); }
  default SortedSet<String> getAllTSLs(int allele){ return getAllStringValues(VEPLocationFacade.TSL, allele); }
  default SortedSet<String> getAllUNIPARCs(){ return getAllStringValues(VEPLocationFacade.UNIPARC); }
  default SortedSet<String> getAllUNIPARCs(int allele){ return getAllStringValues(VEPLocationFacade.UNIPARC, allele); }
  default SortedSet<String> getAllUNIPROT_ISOFORMs(){ return getAllStringValues(VEPLocationFacade.UNIPROT_ISOFORM); }
  default SortedSet<String> getAllUNIPROT_ISOFORMs(int allele){ return getAllStringValues(VEPLocationFacade.UNIPROT_ISOFORM, allele); }
  default SortedSet<String> getAllVARIANT_CLASSs(){ return getAllStringValues(VEPLocationFacade.VARIANT_CLASS); }
  default SortedSet<String> getAllVARIANT_CLASSs(int allele){ return getAllStringValues(VEPLocationFacade.VARIANT_CLASS, allele); }

}
