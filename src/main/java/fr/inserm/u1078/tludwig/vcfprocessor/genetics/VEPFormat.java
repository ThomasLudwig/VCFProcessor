package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import fr.inserm.u1078.tludwig.maok.tools.Message;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 7 avr. 2015
 */
public class VEPFormat {

  private static final String CUT = "Format: ";
  private static final String PREFIX = "##INFO=<ID=CSQ";
  private static final String END = "\">";

  public static final String KEY_ALLELE = "Allele";
  public static final String KEY_CONSEQUENCE = "Consequence";
  public static final String KEY_IMPACT = "IMPACT";
  public static final String KEY_SYMBOL = "SYMBOL";
  public static final String KEY_GENE = "Gene";
  public static final String KEY_FEATURE_TYPE = "Feature_type";
  public static final String KEY_FEATURE = "Feature";
  public static final String KEY_BIOTYPE = "BIOTYPE";
  public static final String KEY_EXON = "EXON";
  public static final String KEY_INTRON = "INTRON";
  public static final String KEY_HGVSC = "HGVSc";
  public static final String KEY_HGVSP = "HGVSp";
  public static final String KEY_CDNA_POSITION = "cDNA_position";
  public static final String KEY_CDS_POSITION = "CDS_position";
  public static final String KEY_PROTEIN_POSITION = "Protein_position";
  public static final String KEY_AMINO_ACIDS = "Amino_acids";
  public static final String KEY_CODONS = "Codons";
  public static final String KEY_EXISTING_VARIATION = "Existing_variation";
  public static final String KEY_DISTANCE = "DISTANCE";
  public static final String KEY_STRAND = "STRAND";
  public static final String KEY_FLAGS = "FLAGS";
  public static final String KEY_SYMBOL_SOURCE = "SYMBOL_SOURCE";
  public static final String KEY_HGNC_ID = "HGNC_ID";
  public static final String KEY_CANONICAL = "CANONICAL";
  public static final String KEY_SIFT = "SIFT";
  public static final String KEY_POLYPHEN = "PolyPhen";
  public static final String KEY_HGVS_OFFSET = "HGVS_OFFSET";
  public static final String KEY_AF = "AF";
  public static final String KEY_AFR_AF = "AFR_AF";
  public static final String KEY_AMR_AF = "AMR_AF";
  public static final String KEY_EAS_AF = "EAS_AF";
  public static final String KEY_EUR_AF = "EUR_AF";
  public static final String KEY_SAS_AF = "SAS_AF";
  public static final String KEY_AA_AF = "AA_AF";
  public static final String KEY_EA_AF = "EA_AF";
  public static final String KEY_GNOMAD_AF = "gnomAD_AF";
  public static final String KEY_GNOMAD_AFR_AF = "gnomAD_AFR_AF";
  public static final String KEY_GNOMAD_AMR_AF = "gnomAD_AMR_AF";
  public static final String KEY_GNOMAD_ASJ_AF = "gnomAD_ASJ_AF";
  public static final String KEY_GNOMAD_EAS_AF = "gnomAD_EAS_AF";
  public static final String KEY_GNOMAD_FIN_AF = "gnomAD_FIN_AF";
  public static final String KEY_GNOMAD_NFE_AF = "gnomAD_NFE_AF";
  public static final String KEY_GNOMAD_OTH_AF = "gnomAD_OTH_AF";
  public static final String KEY_GNOMAD_SAS_AF = "gnomAD_SAS_AF";
  public static final String KEY_MAX_AF = "MAX_AF";
  public static final String KEY_MAX_AF_POPS = "MAX_AF_POPS";  
  
  public static final String KEY_CLIN_SIG = "CLIN_SIG";
  public static final String KEY_SOMATIC = "SOMATIC";
  public static final String KEY_PHENO = "PHENO";
  public static final String KEY_MOTIF_NAME = "MOTIF_NAME";
  public static final String KEY_MOTIF_POS = "MOTIF_POS";
  public static final String KEY_HIGH_INF_POS = "HIGH_INF_POS";
  public static final String KEY_MOTIF_SCORE_CHANGE = "MOTIF_SCORE_CHANGE";
  public static final String KEY_CADD_PHRED = "CADD_PHRED";
  public static final String KEY_CADD_RAW = "CADD_RAW";
  public static final String KEY_FATHMM_MKL_C = "FATHMM_MKL_C";
  public static final String KEY_FATHMM_MKL_NC = "FATHMM_MKL_NC";
  public static final String KEY_CAROL = "CAROL";
  public static final String KEY_LOF = "LoF";
  public static final String KEY_LOF_FILTER = "LoF_filter";
  public static final String KEY_LOF_FLAGS = "LoF_flags";
  public static final String KEY_LOF_INFO = "LoF_info";
  public static final String KEY_LOFTOOL = "LoFtool";
  public static final String KEY_GENESPLICER = "GeneSplicer";
  public static final String KEY_LINKEDVARIANTS = "LinkedVariants";
  public static final String KEY_ALLELE_NUMBER = "ALLELE_NUM";

  private final String[] keys;
  public static final String[] FREQUENCY_KEYS = { 
    KEY_AF,KEY_AFR_AF, KEY_AMR_AF, KEY_EAS_AF, KEY_EUR_AF, KEY_SAS_AF,
    KEY_AA_AF, KEY_EA_AF,
    KEY_GNOMAD_AF, KEY_GNOMAD_AFR_AF, KEY_GNOMAD_AMR_AF, KEY_GNOMAD_ASJ_AF, KEY_GNOMAD_EAS_AF, KEY_GNOMAD_FIN_AF, KEY_GNOMAD_NFE_AF, KEY_GNOMAD_OTH_AF, KEY_GNOMAD_SAS_AF, KEY_MAX_AF
  };
  private final String format;

  private VEPFormat(String format) {
    this.format = format;
    keys = format.split("\\|");
    boolean found = false;
    for(String key : keys)
      if(key.equals(KEY_ALLELE_NUMBER)) {
        found = true;
        break;
      }
    if(!found)
      Message.error("VEP annotations must contain ["+KEY_ALLELE_NUMBER+"]");
  }

  public static VEPFormat createVepFormat(String line) {
    if (!isValid(line))
      return null;

    int cut = line.indexOf(CUT) + CUT.length();
    int end = line.indexOf(END);
    String subLine = line.substring(cut, end);
    return new VEPFormat(subLine);
  }

  public static boolean isValid(String line) {
    if (!line.substring(0, PREFIX.length()).equalsIgnoreCase(PREFIX))
      return false;
    int cut = line.indexOf(CUT);
    int end = line.indexOf(END);
    return (cut != -1 && end != -1);
  }

  protected int getIndex(String key) {
    for (int i = 0; i < keys.length; i++)
      if (keys[i].equals(key))
        return i;
    return -1;
  }

  public int size() {
    return keys.length;
  }

  @Override
  public String toString() {
    return this.format;
  }
}
