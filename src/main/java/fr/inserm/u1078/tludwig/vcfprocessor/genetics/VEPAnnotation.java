package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * One group of VEPAnnotation (comma separated, starting with csq=allele) is an object
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 21 avr. 2016
 */
public class VEPAnnotation { //TODO rewrite this to be 100% compliant with vep91 (and also last version of vep)
  //private final Variant variant;
  
  public static final String POLYPHEN_PROBABLY_DAMAGING = "probably_damaging";
  public static final String POLYPHEN_POSSIBLY_DAMAGING = "possibly_damaging";
  public static final String POLYPHEN_BENIGN = "benign";

  private final VEPFormat format;
  private final String[] values;

  public VEPAnnotation(String annotations, VEPFormat format) throws AnnotationException {
    //this.variant = variant;
    this.format = format;
    String[] tmpValues = annotations.split("\\|", -1);
    if (tmpValues.length != format.size()){//TODO temporarily disabled, should be reenabled : for some annotated 1000g variant this is false !!
      String msg = "Mismatch between number of values (" + tmpValues.length + ") and size of format (" + format.size() + ") : line \n" + annotations;
      Message.warning(msg);
      //throw new AnnotationException(msg);
      this.values = new String[format.size()];
      System.arraycopy(tmpValues, 0, this.values, 0, tmpValues.length);
    } else
      this.values = tmpValues;
  }
/*
  final void attach(Info info) {
    this.info = info;
    this.variant = info.getVariant();
  }
*/
  public String getValue(String key) {
    int idx = format.getIndex(key);
    if (idx < 0){
      Message.warning("Trying to access VEP annotation ["+key+"] which seem to be missing from this VCF file");
      return null;
    }
    return values[idx];
  }

  public String getAllele() {
    return this.getValue(VEPFormat.KEY_ALLELE);
  }

  public String getConsequence() {
    return this.getValue(VEPFormat.KEY_CONSEQUENCE);
  }

  public String getIMPACT() {
    return this.getValue(VEPFormat.KEY_IMPACT);
  }

  public String getSYMBOL() {
    return this.getValue(VEPFormat.KEY_SYMBOL);
  }

  public String getGene() {
    return this.getValue(VEPFormat.KEY_GENE);
  }

  public String getFeature_type() {
    return this.getValue(VEPFormat.KEY_FEATURE_TYPE);
  }

  public String getFeature() {
    return this.getValue(VEPFormat.KEY_FEATURE);
  }

  public String getBIOTYPE() {
    return this.getValue(VEPFormat.KEY_BIOTYPE);
  }

  public String getEXON() {
    return this.getValue(VEPFormat.KEY_EXON);
  }

  public String getINTRON() {
    return this.getValue(VEPFormat.KEY_INTRON);
  }

  public String getHGVSc() {
    return this.getValue(VEPFormat.KEY_HGVSC);
  }

  public String getHGVSp() {
    return this.getValue(VEPFormat.KEY_HGVSP);
  }

  public String getcDNA_position() {
    return this.getValue(VEPFormat.KEY_CDNA_POSITION);
  }

  public String getCDS_position() {
    return this.getValue(VEPFormat.KEY_CDS_POSITION);
  }

  public String getProtein_position() {
    return this.getValue(VEPFormat.KEY_PROTEIN_POSITION);
  }

  public String getAmino_acids() {
    return this.getValue(VEPFormat.KEY_AMINO_ACIDS);
  }

  public String getCodons() {
    return this.getValue(VEPFormat.KEY_CODONS);
  }

  public String getExisting_variation() {
    return this.getValue(VEPFormat.KEY_EXISTING_VARIATION);
  }

  public String getDISTANCE() {
    return this.getValue(VEPFormat.KEY_DISTANCE);
  }

  public String getSTRAND() {
    return this.getValue(VEPFormat.KEY_STRAND);
  }

  public String getFLAGS() {
    return this.getValue(VEPFormat.KEY_FLAGS);
  }

  public String getSYMBOL_SOURCE() {
    return this.getValue(VEPFormat.KEY_SYMBOL_SOURCE);
  }

  public String getHGNC_ID() {
    return this.getValue(VEPFormat.KEY_HGNC_ID);
  }

  public String getCANONICAL() {
    return this.getValue(VEPFormat.KEY_CANONICAL);
  }

  public String getSIFT() {
    return this.getValue(VEPFormat.KEY_SIFT);
  }

  public String getPolyPhen() {
    return this.getValue(VEPFormat.KEY_POLYPHEN);
  }

  public String getHGVS_OFFSET() {
    return this.getValue(VEPFormat.KEY_HGVS_OFFSET);
  }

  public String getAF() {
    return this.getValue(VEPFormat.KEY_AF);
  }

  public String getAFR_AF() {
    return this.getValue(VEPFormat.KEY_AFR_AF);
  }

  public String getAMR_AF() {
    return this.getValue(VEPFormat.KEY_AMR_AF);
  }

  public String getEAS_AF() {
    return this.getValue(VEPFormat.KEY_EAS_AF);
  }

  public String getEUR_AF() {
    return this.getValue(VEPFormat.KEY_EUR_AF);
  }

  public String getSAS_AF() {
    return this.getValue(VEPFormat.KEY_SAS_AF);
  }

  public String getAA_AF() {
    return this.getValue(VEPFormat.KEY_AA_AF);
  }

  public String getEA_AF() {
    return this.getValue(VEPFormat.KEY_EA_AF);
  }
  
  public String getGNOMAD_AF() {
    return this.getValue(VEPFormat.KEY_GNOMAD_AF);
  }
  
  public String getGNOMAD_AFR_AF() {
    return this.getValue(VEPFormat.KEY_GNOMAD_AFR_AF);
  }
    
  public String getGNOMAD_AMR_AF() {
    return this.getValue(VEPFormat.KEY_GNOMAD_AMR_AF);
  }
  
  public String getGNOMAD_ASJ_AF() {
    return this.getValue(VEPFormat.KEY_GNOMAD_ASJ_AF);
  }
  
  public String getGNOMAD_EAS_AF() {
    return this.getValue(VEPFormat.KEY_GNOMAD_EAS_AF);
  }
  
  public String getGNOMAD_FIN_AF() {
    return this.getValue(VEPFormat.KEY_GNOMAD_FIN_AF);
  }
  
  public String getEGNOMAD_NFE_AF() {
    return this.getValue(VEPFormat.KEY_GNOMAD_NFE_AF);
  }
  
  public String getGNOMAD_OTH_AF() {
    return this.getValue(VEPFormat.KEY_GNOMAD_OTH_AF);
  }
  
  public String getGNOMAD_SAS_AF() {
    return this.getValue(VEPFormat.KEY_GNOMAD_SAS_AF);
  }

  public String getGNOMAD_NFE_AF() {
    return this.getValue(VEPFormat.KEY_GNOMAD_NFE_AF);
  }
  
  public String getMAX_AF_POPS() {
    return this.getValue(VEPFormat.KEY_MAX_AF_POPS);
  }
    
  public String getMAF_AF() {
    return this.getValue(VEPFormat.KEY_MAX_AF);
  }
  
  public String getCLIN_SIG() {
    return this.getValue(VEPFormat.KEY_CLIN_SIG);
  }

  public String getSOMATIC() {
    return this.getValue(VEPFormat.KEY_SOMATIC);
  }

  public String getPHENO() {
    return this.getValue(VEPFormat.KEY_PHENO);
  }

  public String getMOTIF_NAME() {
    return this.getValue(VEPFormat.KEY_MOTIF_NAME);
  }

  public String getMOTIF_POS() {
    return this.getValue(VEPFormat.KEY_MOTIF_POS);
  }

  public String getHIGH_INF_POS() {
    return this.getValue(VEPFormat.KEY_HIGH_INF_POS);
  }

  public String getMOTIF_SCORE_CHANGE() {
    return this.getValue(VEPFormat.KEY_MOTIF_SCORE_CHANGE);
  }

  public String getCADD_PHRED() {
    return this.getValue(VEPFormat.KEY_CADD_PHRED);
  }

  public String getCADD_RAW() {
    return this.getValue(VEPFormat.KEY_CADD_RAW);
  }

  public String getFATHMM_MKL_C() {
    return this.getValue(VEPFormat.KEY_FATHMM_MKL_C);
  }

  public String getFATHMM_MKL_NC() {
    return this.getValue(VEPFormat.KEY_FATHMM_MKL_NC);
  }

  public String getCAROL() {
    return this.getValue(VEPFormat.KEY_CAROL);
  }

  public String getLoF() {
    return this.getValue(VEPFormat.KEY_LOF);
  }

  public String getLoF_filter() {
    return this.getValue(VEPFormat.KEY_LOF_FILTER);
  }

  public String getLoF_flags() {
    return this.getValue(VEPFormat.KEY_LOF_FLAGS);
  }

  public String getLoF_info() {
    return this.getValue(VEPFormat.KEY_LOF_INFO);
  }

  public String getLoFtool() {
    return this.getValue(VEPFormat.KEY_LOFTOOL);
  }

  public String getGeneSplicer() {
    return this.getValue(VEPFormat.KEY_GENESPLICER);
  }

  public String getLinkedVariants() {
    return this.getValue(VEPFormat.KEY_LINKEDVARIANTS);
  }

  public int getAlleleNumber() {
    try {
      return new Integer(this.getValue(VEPFormat.KEY_ALLELE_NUMBER));
    } catch (NumberFormatException e) {
      return -1;
    }
  }
  
  public static VEPAnnotation getWorstVEPAnnotation(VEPAnnotation... veps){
    if(veps == null)
      return null;
    VEPAnnotation worst = null;
    int level = -1;
    for(VEPAnnotation vep : veps){
      if(worst == null)
        worst = vep;
      else {
        int current = VEPConsequence.getWorstConsequence(vep).getLevel();
        if(current > level){
          level = current;
          worst = vep;
        }
      }
    }
    return worst;
  }
  
  public static VEPAnnotation getWorstVEPAnnotation(Collection<VEPAnnotation> veps) {
    if(veps == null || veps.isEmpty())
      return null;
    
    return VEPAnnotation.getWorstVEPAnnotation(veps.toArray(new VEPAnnotation[veps.size()]));
  }
  
  public static VEPAnnotation getWorstVEPAnnotation(Collection<VEPAnnotation> veps, String symbol) {
    VEPAnnotation worst = null;
    int level = -999;
    for(VEPAnnotation vep : veps){
      if(symbol.equalsIgnoreCase(vep.getSYMBOL())){
        if(worst == null){
          level = VEPConsequence.getWorstConsequence(vep).getLevel();
          worst = vep;
        }
        else {
          int current = VEPConsequence.getWorstConsequence(vep).getLevel();
          if(current > level){
            level = current;
            worst = vep;
          }
        }
      }
    }
    return worst;
  }
  
  public static HashMap<String, VEPAnnotation> getWorstVEPAnnotationsByGene(Collection<VEPAnnotation> veps){
    HashMap<String, VEPAnnotation> ret = new HashMap<>();
    for(String symbol : getDistinctSymbols(veps)){
      ret.put(symbol, getWorstVEPAnnotation(veps, symbol));
    }
    return ret;
  }
  
  public static ArrayList<String> getDistinctSymbols(Collection<VEPAnnotation> veps){
    ArrayList<String> symbols = new ArrayList<>();
    if(veps != null)
      for(VEPAnnotation vep : veps){
        String symbol = vep.getSYMBOL();
        if(symbol != null && !symbol.isEmpty() && !symbols.contains(symbol))
          symbols.add(symbol);
      }      
    return symbols;
  }

  @Override
  public String toString(){
    return String.join("|", values);
  }

  public String getWorstConsequence() {
    String csq = this.getConsequence();
    if(csq == null || csq.isEmpty())
      return VEPConsequence.EMPTY.getName();
    return VEPConsequence.getWorstConsequence(csq.split(("&"))).getName();
  }
}