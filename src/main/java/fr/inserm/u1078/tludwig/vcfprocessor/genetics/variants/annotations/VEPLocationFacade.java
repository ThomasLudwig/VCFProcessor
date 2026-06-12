package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface VEPLocationFacade extends VEPFacade {
  VEPField SYMBOL = new VEPField("SYMBOL", Category.Location, Type.String, "Gene symbol (e.g. BRCA2)");
  VEPField GENE = new VEPField("Gene", Category.Location, Type.String, "Ensembl stable gene ID (ENSG…)");
  VEPField FEATURE_TYPE = new VEPField("Feature_type", Category.Location, Type.String, "Type of feature: Transcript, RegulatoryFeature, or MotifFeature");
  VEPField FEATURE = new VEPField("Feature", Category.Location, Type.String, "Ensembl stable ID of the overlapping feature (ENST…, ENSR…)");
  VEPField BIOTYPE = new VEPField("BIOTYPE", Category.Location, Type.String, "Biotype of transcript or regulatory feature (e.g. protein_coding, lncRNA)");
  VEPField EXON = new VEPField("EXON", Category.Location, Type.String, "Exon number out of total exons (e.g. 2/8); empty for non-exonic variants");
  VEPField INTRON = new VEPField("INTRON", Category.Location, Type.String, "Intron number out of total introns (e.g. 1/5); empty for non-intronic variants");
  VEPField HGVSC = new VEPField("HGVSc", Category.Location, Type.String, "HGVS coding sequence name (e.g. ENST00000419219.1:c.251A>G)");
  VEPField HGVSP = new VEPField("HGVSp", Category.Location, Type.String, "HGVS protein sequence name (e.g. ENSP00000404426.1:p.Asn84Ser)");
  VEPField CDNA_POSITION = new VEPField("cDNA_position", Category.Location, Type.Integer, "Relative position of base pair in cDNA sequence");
  VEPField CDS_POSITION = new VEPField("CDS_position", Category.Location, Type.Integer, "Relative position of base pair in coding sequence (CDS)");
  VEPField PROTEIN_POSITION = new VEPField("Protein_position", Category.Location, Type.Integer, "Relative position of amino acid in protein");
  VEPField AMINO_ACIDS = new VEPField("Amino_acids", Category.Location, Type.String, "Reference/variant amino acids (e.g. T/N); only for coding variants");
  VEPField CODONS = new VEPField("Codons", Category.Location, Type.String, "Reference/variant codon with variant base in upper case (e.g. aCc/aAc)");
  VEPField DISTANCE = new VEPField("DISTANCE", Category.Location, Type.Integer, "Shortest distance from variant to transcript (bp); 0 is possible for flanking insertions");
  VEPField STRAND = new VEPField("STRAND", Category.Location, Type.Integer, "DNA strand of the feature: 1 (forward) or -1 (reverse)");
  VEPField FLAGS = new VEPField("FLAGS", Category.Location, Type.String, "Transcript quality flags: cds_start_NF (5′ CDS incomplete), cds_end_NF (3′ CDS incomplete)");
  VEPField SYMBOL_SOURCE = new VEPField("SYMBOL_SOURCE", Category.Location, Type.String, "Source database of the gene symbol (e.g. HGNC, EntrezGene, Clone_based_ensembl_gene)");
  VEPField HGNC_ID = new VEPField("HGNC_ID", Category.Location, Type.Integer, "HGNC identifier of the gene (numeric part only, e.g. 7527)");
  VEPField VARIANT_CLASS = new VEPField("VARIANT_CLASS", Category.Location, Type.String, "Sequence Ontology variant class (e.g. SNV, insertion, deletion)");
  VEPField ENSP = new VEPField("ENSP", Category.Location, Type.String, "Ensembl protein stable ID of the affected transcript (ENSP…)");
  VEPField SWISSPROT = new VEPField("SWISSPROT", Category.Location, Type.String, "Best-match UniProtKB/Swiss-Prot accession of the protein product");
  VEPField TREMBL = new VEPField("TREMBL", Category.Location, Type.String, "Best-match UniProtKB/TrEMBL accession of the protein product");
  VEPField UNIPARC = new VEPField("UNIPARC", Category.Location, Type.String, "Best-match UniParc accession of the protein product");
  VEPField UNIPROT_ISOFORM = new VEPField("UNIPROT_ISOFORM", Category.Location, Type.String, "UniProt canonical isoform identifier");
  VEPField HGVSG = new VEPField("HGVSg", Category.Location, Type.String, "HGVS genomic sequence name (requires --hgvsg)");
  VEPField HGVS_OFFSET = new VEPField("HGVS_OFFSET", Category.Location, Type.Integer, "Number of bases the HGVS notation was shifted (only when >0, requires --shift_hgvs)");
  VEPField NEAREST = new VEPField("NEAREST", Category.Location, Type.String, "Identifier(s) of nearest transcription start site (requires --nearest)");
  VEPField CANONICAL = new VEPField("CANONICAL", Category.Location, Type.String, "Flag 'YES' if this is the canonical transcript for the gene");
  VEPField MANE_SELECT = new VEPField("MANE_SELECT", Category.Location, Type.String, "MANE Select transcript status and accession (NM_ RefSeq)");
  VEPField MANE_PLUS_CLINICAL = new VEPField("MANE_PLUS_CLINICAL", Category.Location, Type.String, "MANE Plus Clinical transcript status and accession");
  VEPField TSL = new VEPField("TSL", Category.Location, Type.Integer, "Transcript support level (1=highest, 5=lowest); not available for GRCh37");
  VEPField APPRIS = new VEPField("APPRIS", Category.Location, Type.String, "APPRIS isoform annotation (principal or alternative isoform flag); not available for GRCh37");
  VEPField GENCODE_PRIMARY = new VEPField("GENCODE_PRIMARY", Category.Location, Type.String, "Flag 'YES' if transcript belongs to the GENCODE primary transcript set");
  VEPField CCDS = new VEPField("CCDS", Category.Location, Type.String, "CCDS identifier for this transcript, where applicable");
  VEPField DOMAINS = new VEPField("DOMAINS", Category.Location, Type.String, "Source and identifier of overlapping protein domains (e.g. Pfam:PF00001)");
  VEPField SOURCE = new VEPField("SOURCE", Category.Location, Type.String, "Source of the transcript model (Ensembl or RefSeq)");
  VEPField GENE_PHENO = new VEPField("GENE_PHENO", Category.Location, Type.String, "Whether the overlapping gene is associated with a phenotype/disease/trait (1=yes, 0=no)");
  VEPField REFSEQ_MATCH = new VEPField("REFSEQ_MATCH", Category.Location, Type.String, "RefSeq transcript match status flags (e.g. rseq_mrna_match, rseq_cds_mismatch, rseq_ens_match_wt)");
  VEPField MIRNA = new VEPField("miRNA", Category.Location, Type.String, "Variant consequences for miRNA secondary structure positions (plugin/cache dependent)");

  default String getAmino_acids(){ return getStringValue(AMINO_ACIDS); }
  default String getAPPRIS(){ return getStringValue(APPRIS); }
  default String getBIOTYPE(){ return getStringValue(BIOTYPE); }
  default String getCANONICAL(){ return getStringValue(CANONICAL); }
  default String getCCDS(){ return getStringValue(CCDS); }
  default Integer getcDNA_position(){ return getIntegerValue(CDNA_POSITION); }
  default Integer getCDS_position(){ return getIntegerValue(CDS_POSITION); }
  default String getCodons(){ return getStringValue(CODONS); }
  default Integer getDISTANCE(){ return getIntegerValue(DISTANCE); }
  default String[] getDOMAINS(){ return getStringArrayValue(DOMAINS); }
  default String getENSP(){ return getStringValue(ENSP); }
  default String getEXON(){ return getStringValue(EXON); }
  default String getFeature(){ return getStringValue(FEATURE); }
  default String getFeature_type(){ return getStringValue(FEATURE_TYPE); }
  default String[] getFLAGS(){ return getStringArrayValue(FLAGS); }
  default String getGENCODE_PRIMARY(){ return getStringValue(GENCODE_PRIMARY); }
  default String getGene(){ return getStringValue(GENE); }
  default String getGENE_PHENO(){ return getStringValue(GENE_PHENO); }
  default Integer getHGNC_ID(){ return getIntegerValue(HGNC_ID); }
  default Integer getHGVS_OFFSET(){ return getIntegerValue(HGVS_OFFSET); }
  default String getHGVSc(){ return getStringValue(HGVSC); }
  default String getHGVSg(){ return getStringValue(HGVSG); }
  default String getHGVSp(){ return getStringValue(HGVSP); }

  default String getINTRON(){ return getStringValue(INTRON); }
  default String getMANE_PLUS_CLINICAL(){ return getStringValue(MANE_PLUS_CLINICAL); }
  default String getMANE_SELECT(){ return getStringValue(MANE_SELECT); }
  default String getmiRNA(){ return getStringValue(MIRNA); }
  default String getNEAREST(){ return getStringValue(NEAREST); }
  default Integer getProtein_position(){ return getIntegerValue(PROTEIN_POSITION); }
  default String getREFSEQ_MATCH(){ return getStringValue(REFSEQ_MATCH); }
  default String getSOURCE(){ return getStringValue(SOURCE); }
  default Integer getSTRAND(){ return getIntegerValue(STRAND); }
  default String getSWISSPROT(){ return getStringValue(SWISSPROT); }
  default String getSYMBOL(){ return getStringValue(SYMBOL); }
  default String getSYMBOL_SOURCE(){ return getStringValue(SYMBOL_SOURCE); }
  default String getTREMBL(){ return getStringValue(TREMBL); }
  default Integer getTSL(){ return getIntegerValue(TSL); }
  default String getUNIPARC(){ return getStringValue(UNIPARC); }
  default String getUNIPROT_ISOFORM(){ return getStringValue(UNIPROT_ISOFORM); }
  default String getVARIANT_CLASS(){ return getStringValue(VARIANT_CLASS); }

  static List<String> getDistinctSymbols(Collection<VEPAnnotation> veps){
    ArrayList<String> symbols = new ArrayList<>();
    if(veps != null)
      for(VEPAnnotation vep : veps){
        String symbol = vep.getSYMBOL();
        if(symbol != null && !symbol.isEmpty() && !symbols.contains(symbol))
          symbols.add(symbol);
      }
    return symbols;
  }

  static List<VEPAnnotation> getVEPAnnotations(Collection<VEPAnnotation> veps, String symbol) {
    ArrayList<VEPAnnotation> annotations = new ArrayList<>();
    if(veps != null)
      for(VEPAnnotation vep : veps)
        if(symbol.equals(vep.getSYMBOL()))
          annotations.add(vep);
    return annotations;
  }
}
