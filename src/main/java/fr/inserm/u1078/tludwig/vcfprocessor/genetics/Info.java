package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF.InfoFormatHeader;

import java.util.*;

/**
 * Metaclass contains all the data in the "INFO" field (including VEPAnnotations)
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 21 avr. 2016
 */
public class Info {

  //TODO rewrite to parse correctly (type, number...) get from header, throw exception ....
  public static final String INFO_TYPE_INTEGER = "Integer";
  public static final String INFO_TYPE_FLOAT = "Float";
  public static final String INFO_TYPE_FLAG = "Flag";
  public static final String INFO_TYPE_CHARACTER = "Character";
  public static final String INFO_TYPE_STRING = "String";

  public static final String INFO_NUMBER_UNKNOWN = ".";
  public static final String INFO_NUMBER_ONE = ".";
  public static final String INFO_NUMBER_ALLELE = ".";
  public static final String INFO_NUMBER_GENOTYPE = ".";

  private static final String CSQ_PREFIX = "CSQ";
  public static final String INBREEDING_COEFF = "InbreedingCoeff";

  private Variant variant;

  private final HashMap<Integer, ArrayList<VEPAnnotation>> vepAnnotations;
  private final TreeMap<String, String> infoMap;
  private final VCF vcfFile;

  public Info(String infoField, VCF vcfFile) {
    this.vepAnnotations = new HashMap<>();
    infoMap = new TreeMap<>();
    for (String kvString : infoField.split(";")) {
      String[] kv = kvString.split("=");
      String key = kv[0];
      String value = null;
      if (kv.length == 2)
        value = kv[1];
      if (!infoMap.containsKey(key)) {
        infoMap.put(key, value);
      } else
        Message.warning("Duplicate key [" + kv[0] + "] found for info [" + infoField + "]");

      if (kv[0].equals(CSQ_PREFIX))
        for (String annot : kv[1].split(",")) {
          VEPAnnotation vepAnnotation = new VEPAnnotation(annot, vcfFile.getVepFormat());
          int allele = vepAnnotation.getAlleleNumber();
          ArrayList<VEPAnnotation> list = this.vepAnnotations.computeIfAbsent(allele, k -> new ArrayList<>());
          list.add(vepAnnotation);
          //this.vepAnnotations.add(); //Message.debug("Allele "+this.vepAnnotations.get(this.vepAnnotations.size()-1).getAllele());
        }
    }
    this.vcfFile = vcfFile;
  }

  public final String getAnnot(String key) {
    return this.infoMap.get(key);
  }

  final Variant getVariant() {
    return variant;
  }

  String getRef() {
    return this.variant.getRef();
  }

  void setVariant(Variant variant) throws AnnotationException {
    if (this.variant != null && this.variant != variant)
      throw new AnnotationException("Cannot attach " + this.getClass().getSimpleName() + " object to multiple " + variant.getClass().getSimpleName());
    else
      this.variant = variant; // attach();
  }

  public ArrayList<VEPAnnotation> getAllVEPAnnotations() {
    ArrayList<VEPAnnotation> annotations = new ArrayList<>();
    for(ArrayList<VEPAnnotation> list : this.vepAnnotations.values())
      annotations.addAll(list);
    return annotations;
  }

  public ArrayList<VEPAnnotation> getVEPAnnotations(int allele) {
    return this.vepAnnotations.get(allele);
  }

  ArrayList<String> getAllVEPValues(String key) {
    ArrayList<String> ret = new ArrayList<>();
    for (int allele : this.vepAnnotations.keySet())
      ret.addAll(this.getVEPValues(key, allele));
    return ret;
  }

  ArrayList<String> getVEPValues(String key, int allele) {
    ArrayList<String> ret = new ArrayList<>();
    for (VEPAnnotation annot : this.getVEPAnnotations(allele))
      ret.add(annot.getValue(key));
    return ret;
  }

  public ArrayList<String> getConsequencesRaw() {
    return this.getAllVEPValues(VEPFormat.KEY_CONSEQUENCE);
  }
  
  public ArrayList<Integer> getConsequenceLevels() {
    ArrayList<Integer> ret = new ArrayList<>();
    for(String cons : this.getConsequencesRaw()){
      int level = VEPConsequence.getWorstConsequence(cons).getLevel();
      if (!ret.contains(level))
        ret.add(level);
    }
    return ret;
  }
  
  public ArrayList<Integer> getConsequenceLevels(int a) {
    ArrayList<Integer> ret = new ArrayList<>();
    for(String cons : this.getConsequencesRaw(a)){
      int level = VEPConsequence.getWorstConsequence(cons).getLevel();
      if (!ret.contains(level))
        ret.add(level);
    }
    return ret;
  }

  public ArrayList<String> getConsequencesSplit(int a) {
    ArrayList<String> ret = new ArrayList<>();
    for(String consRaw : getConsequencesRaw(a))
      for (String consequences : consRaw.split("&"))
        if (!ret.contains(consequences))
          ret.add(consequences);
    return ret;
  }
  
  public ArrayList<String> getConsequencesSplit() {
    ArrayList<String> ret = new ArrayList<>();
    for(String consRaw : getConsequencesRaw())
      for (String consequences : consRaw.split("&"))
        if (!ret.contains(consequences))
          ret.add(consequences);
    return ret;
  }

  public ArrayList<String> getIMPACTs() {
    return this.getAllVEPValues(VEPFormat.KEY_IMPACT);
  }

  public ArrayList<String> getSYMBOLs() {
    return this.getAllVEPValues(VEPFormat.KEY_SYMBOL);
  }
  
  public String[] getGeneSymbolSortedList() {
    ArrayList<String> tmpGeneList = getSYMBOLs();
    if (tmpGeneList.isEmpty())
      return null;

    SortedList<String> geneList = new SortedList<>(tmpGeneList, SortedList.Strategy.ADD_INSERT_SORT);
    return geneList.toArray(new String[0]);
  }
  
  public String[] getGeneSymbolSortedList(int allele) {
    ArrayList<String> tmpGeneList = getSYMBOLs(allele);
    if (tmpGeneList.isEmpty())
      return null;

    SortedList<String> geneList = new SortedList<>(tmpGeneList, SortedList.Strategy.ADD_INSERT_SORT);
    return geneList.toArray(new String[0]);
  }

  public ArrayList<String> getGenes() {
    return this.getAllVEPValues(VEPFormat.KEY_GENE);
  }

  public ArrayList<String> getFeature_types() {
    return this.getAllVEPValues(VEPFormat.KEY_FEATURE_TYPE);
  }

  public ArrayList<String> getFeatures() {
    return this.getAllVEPValues(VEPFormat.KEY_FEATURE);
  }

  public ArrayList<String> getBIOTYPEs() {
    return this.getAllVEPValues(VEPFormat.KEY_BIOTYPE);
  }

  public ArrayList<String> getEXONs() {
    return this.getAllVEPValues(VEPFormat.KEY_EXON);
  }

  public ArrayList<String> getINTRONs() {
    return this.getAllVEPValues(VEPFormat.KEY_INTRON);
  }

  public ArrayList<String> getHGVScs() {
    return this.getAllVEPValues(VEPFormat.KEY_HGVSC);
  }

  public ArrayList<String> getHGVSps() {
    return this.getAllVEPValues(VEPFormat.KEY_HGVSP);
  }

  public ArrayList<String> getCDNA_positions() {
    return this.getAllVEPValues(VEPFormat.KEY_CDNA_POSITION);
  }

  public ArrayList<String> getCDS_positions() {
    return this.getAllVEPValues(VEPFormat.KEY_CDS_POSITION);
  }

  public ArrayList<String> getProtein_positions() {
    return this.getAllVEPValues(VEPFormat.KEY_PROTEIN_POSITION);
  }

  public ArrayList<String> getAmino_acids() {
    return this.getAllVEPValues(VEPFormat.KEY_AMINO_ACIDS);
  }

  public ArrayList<String> getCodons() {
    return this.getAllVEPValues(VEPFormat.KEY_CODONS);
  }

  public ArrayList<String> getExisting_variations() {
    return this.getAllVEPValues(VEPFormat.KEY_EXISTING_VARIATION);
  }

  public ArrayList<String> getDISTANCEs() {
    return this.getAllVEPValues(VEPFormat.KEY_DISTANCE);
  }

  public ArrayList<String> getSTRANDs() {
    return this.getAllVEPValues(VEPFormat.KEY_STRAND);
  }

  public ArrayList<String> getFLAGSs() {
    return this.getAllVEPValues(VEPFormat.KEY_FLAGS);
  }

  public ArrayList<String> getSYMBOL_SOURCEs() {
    return this.getAllVEPValues(VEPFormat.KEY_SYMBOL_SOURCE);
  }

  public ArrayList<String> getHGNC_IDs() {
    return this.getAllVEPValues(VEPFormat.KEY_HGNC_ID);
  }

  public ArrayList<String> getCANONICALs() {
    return this.getAllVEPValues(VEPFormat.KEY_CANONICAL);
  }

  public ArrayList<String> getSIFTs() {
    return this.getAllVEPValues(VEPFormat.KEY_SIFT);
  }

  public ArrayList<String> getPolyPhens() {
    return this.getAllVEPValues(VEPFormat.KEY_POLYPHEN);
  }

  public ArrayList<String> getHGVS_OFFSETs() {
    return this.getAllVEPValues(VEPFormat.KEY_HGVS_OFFSET);
  }

  public ArrayList<String> getAFs() {
    return this.getAllVEPValues(VEPFormat.KEY_AF);
  }

  public ArrayList<String> getAFR_AFs() {
    return this.getAllVEPValues(VEPFormat.KEY_AFR_AF);
  }

  public ArrayList<String> getAMR_AFs() {
    return this.getAllVEPValues(VEPFormat.KEY_AMR_AF);
  }

  public ArrayList<String> getEAS_AFs() {
    return this.getAllVEPValues(VEPFormat.KEY_EAS_AF);
  }

  public ArrayList<String> getEUR_AFs() {
    return this.getAllVEPValues(VEPFormat.KEY_EUR_AF);
  }

  public ArrayList<String> getSAS_AFs() {
    return this.getAllVEPValues(VEPFormat.KEY_SAS_AF);
  }

  public ArrayList<String> getAA_AFs() {
    return this.getAllVEPValues(VEPFormat.KEY_AA_AF);
  }

  public ArrayList<String> getEA_AFs() {
    return this.getAllVEPValues(VEPFormat.KEY_EA_AF);
  }

  public ArrayList<String> getCLIN_SIGs() {
    return this.getAllVEPValues(VEPFormat.KEY_CLIN_SIG);
  }

  public ArrayList<String> getSOMATICs() {
    return this.getAllVEPValues(VEPFormat.KEY_SOMATIC);
  }

  public ArrayList<String> getPHENOs() {
    return this.getAllVEPValues(VEPFormat.KEY_PHENO);
  }

  public ArrayList<String> getMOTIF_NAMEs() {
    return this.getAllVEPValues(VEPFormat.KEY_MOTIF_NAME);
  }

  public ArrayList<String> getMOTIF_POSs() {
    return this.getAllVEPValues(VEPFormat.KEY_MOTIF_POS);
  }

  public ArrayList<String> getHIGH_INF_POSs() {
    return this.getAllVEPValues(VEPFormat.KEY_HIGH_INF_POS);
  }

  public ArrayList<String> getMOTIF_SCORE_CHANGEs() {
    return this.getAllVEPValues(VEPFormat.KEY_MOTIF_SCORE_CHANGE);
  }

  public double getCADD_PHRED() { return firstValueAsDouble(this.getCADD_PHREDs()); }

  public double getCADD_RAW() { return firstValueAsDouble(this.getCADD_RAWs()); }

  public double getCADD_PHRED(int allele) { return firstValueAsDouble(this.getCADD_PHREDs(allele)); }

  public double getCADD_RAW(int allele) { return firstValueAsDouble(this.getCADD_RAWs(allele)); }

  public ArrayList<String> getCADD_PHREDs() { return this.getAllVEPValues(VEPFormat.KEY_CADD_PHRED); }

  public ArrayList<String> getCADD_RAWs() { return this.getAllVEPValues(VEPFormat.KEY_CADD_RAW); }

  public ArrayList<String> getFATHMM_MKL_Cs() {
    return this.getAllVEPValues(VEPFormat.KEY_FATHMM_MKL_C);
  }

  public ArrayList<String> getFATHMM_MKL_NCs() {
    return this.getAllVEPValues(VEPFormat.KEY_FATHMM_MKL_NC);
  }

  public ArrayList<String> getCAROLs() {
    return this.getAllVEPValues(VEPFormat.KEY_CAROL);
  }

  public ArrayList<String> getLoFs() {
    return this.getAllVEPValues(VEPFormat.KEY_LOF);
  }

  public ArrayList<String> getLoF_filters() {
    return this.getAllVEPValues(VEPFormat.KEY_LOF_FILTER);
  }

  public ArrayList<String> getLoF_flags() {
    return this.getAllVEPValues(VEPFormat.KEY_LOF_FLAGS);
  }

  public ArrayList<String> getLoF_infos() {
    return this.getAllVEPValues(VEPFormat.KEY_LOF_INFO);
  }

  public ArrayList<String> getLoFtools() {
    return this.getAllVEPValues(VEPFormat.KEY_LOFTOOL);
  }

  public ArrayList<String> getGeneSplicers() {
    return this.getAllVEPValues(VEPFormat.KEY_GENESPLICER);
  }

  public ArrayList<String> getLinkedVariantss() {
    return this.getAllVEPValues(VEPFormat.KEY_LINKEDVARIANTS);
  }

  public ArrayList<String> getConsequencesRaw(int allele) {
    return this.getVEPValues(VEPFormat.KEY_CONSEQUENCE, allele);
  }

  public ArrayList<String> getIMPACTs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_IMPACT, allele);
  }

  public ArrayList<String> getSYMBOLs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_SYMBOL, allele);
  }

  public ArrayList<String> getGenes(int allele) {
    return this.getVEPValues(VEPFormat.KEY_GENE, allele);
  }

  public ArrayList<String> getFeature_types(int allele) {
    return this.getVEPValues(VEPFormat.KEY_FEATURE_TYPE, allele);
  }

  public ArrayList<String> getFeatures(int allele) {
    return this.getVEPValues(VEPFormat.KEY_FEATURE, allele);
  }

  public ArrayList<String> getBIOTYPEs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_BIOTYPE, allele);
  }

  public ArrayList<String> getEXONs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_EXON, allele);
  }

  public ArrayList<String> getINTRONs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_INTRON, allele);
  }

  public ArrayList<String> getHGVScs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_HGVSC, allele);
  }

  public ArrayList<String> getHGVSps(int allele) {
    return this.getVEPValues(VEPFormat.KEY_HGVSP, allele);
  }

  public ArrayList<String> getCDNA_positions(int allele) {
    return this.getVEPValues(VEPFormat.KEY_CDNA_POSITION, allele);
  }

  public ArrayList<String> getCDS_positions(int allele) {
    return this.getVEPValues(VEPFormat.KEY_CDS_POSITION, allele);
  }

  public ArrayList<String> getProtein_positions(int allele) {
    return this.getVEPValues(VEPFormat.KEY_PROTEIN_POSITION, allele);
  }

  public ArrayList<String> getAmino_acids(int allele) {
    return this.getVEPValues(VEPFormat.KEY_AMINO_ACIDS, allele);
  }

  public ArrayList<String> getCodons(int allele) {
    return this.getVEPValues(VEPFormat.KEY_CODONS, allele);
  }

  public ArrayList<String> getExisting_variations(int allele) {
    return this.getVEPValues(VEPFormat.KEY_EXISTING_VARIATION, allele);
  }
  
  public String getRSs() {
    String[] list = this.getRSList();
    if (list == null)
      return null;

    return String.join("," ,list);
  }

  public String[] getRSList() {
    TreeSet<String> rsList = new TreeSet<>();

    for (String rss : getExisting_variations())
      if(rss != null && ! rss.isEmpty())
        rsList.addAll(Arrays.asList(rss.split("&")));
    if (rsList.isEmpty())
      return null;

    return rsList.toArray(new String[0]);
  }
  
  public String getRSs(int allele) {
    String[] list = this.getRSList(allele);
    if (list == null)
      return null;

    return String.join("," , list).substring(1);
  }

  public String[] getRSList(int allele) {
    TreeSet<String> rsList = new TreeSet<>();

    for (String rss : getExisting_variations(allele))
      rsList.addAll(Arrays.asList(rss.split("&")));

    if (rsList.isEmpty())
      return null;

    return rsList.toArray(new String[0]);
  }

  public ArrayList<String> getDISTANCEs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_DISTANCE, allele);
  }

  public ArrayList<String> getSTRANDs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_STRAND, allele);
  }

  public ArrayList<String> getFLAGSs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_FLAGS, allele);
  }

  public ArrayList<String> getSYMBOL_SOURCEs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_SYMBOL_SOURCE, allele);
  }

  public ArrayList<String> getHGNC_IDs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_HGNC_ID, allele);
  }

  public ArrayList<String> getCANONICALs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_CANONICAL, allele);
  }

  public ArrayList<String> getSIFTs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_SIFT, allele);
  }

  public ArrayList<String> getPolyPhens(int allele) {
    return this.getVEPValues(VEPFormat.KEY_POLYPHEN, allele);
  }

  public ArrayList<String> getHGVS_OFFSETs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_HGVS_OFFSET, allele);
  }

  public ArrayList<String> getGMAFs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_AF, allele);
  }

  public ArrayList<String> getAFR_MAFs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_AFR_AF, allele);
  }

  public ArrayList<String> getAMR_MAFs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_AMR_AF, allele);
  }

  public ArrayList<String> getEAS_MAFs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_EAS_AF, allele);
  }

  public ArrayList<String> getEUR_MAFs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_EUR_AF, allele);
  }

  public ArrayList<String> getSAS_MAFs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_SAS_AF, allele);
  }

  public ArrayList<String> getAA_MAFs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_AA_AF, allele);
  }

  public ArrayList<String> getEA_MAFs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_EA_AF, allele);
  }

  public ArrayList<String> getCLIN_SIGs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_CLIN_SIG, allele);
  }

  public ArrayList<String> getSOMATICs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_SOMATIC, allele);
  }

  public ArrayList<String> getPHENOs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_PHENO, allele);
  }

  public ArrayList<String> getMOTIF_NAMEs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_MOTIF_NAME, allele);
  }

  public ArrayList<String> getMOTIF_POSs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_MOTIF_POS, allele);
  }

  public ArrayList<String> getHIGH_INF_POSs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_HIGH_INF_POS, allele);
  }

  public ArrayList<String> getMOTIF_SCORE_CHANGEs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_MOTIF_SCORE_CHANGE, allele);
  }

  public ArrayList<String> getCADD_PHREDs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_CADD_PHRED, allele);
  }

  public ArrayList<String> getCADD_RAWs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_CADD_RAW, allele);
  }

  public ArrayList<String> getFATHMM_MKL_Cs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_FATHMM_MKL_C, allele);
  }

  public ArrayList<String> getFATHMM_MKL_NCs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_FATHMM_MKL_NC, allele);
  }

  public ArrayList<String> getCAROLs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_CAROL, allele);
  }

  public ArrayList<String> getLoFs(int allele) {
    return this.getVEPValues(VEPFormat.KEY_LOF, allele);
  }

  public ArrayList<String> getLoF_filters(int allele) {
    return this.getVEPValues(VEPFormat.KEY_LOF_FILTER, allele);
  }

  public ArrayList<String> getLoF_flags(int allele) {
    return this.getVEPValues(VEPFormat.KEY_LOF_FLAGS, allele);
  }

  public ArrayList<String> getLoF_infos(int allele) {
    return this.getVEPValues(VEPFormat.KEY_LOF_INFO, allele);
  }

  public ArrayList<String> getLoFtools(int allele) {
    return this.getVEPValues(VEPFormat.KEY_LOFTOOL, allele);
  }

  public ArrayList<String> getGeneSplicers(int allele) {
    return this.getVEPValues(VEPFormat.KEY_GENESPLICER, allele);
  }

  public ArrayList<String> getLinkedVariantss(int allele) {
    return this.getVEPValues(VEPFormat.KEY_LINKEDVARIANTS, allele);
  }

  public double getPolyPhenScore(int allele) {
    for (String ann : this.getPolyPhens(allele)) {
      String[] kv = ann.split("\\(");
      if (kv.length > 1) {
        if (kv[0].contains("unknown"))
          return 0.5;
        String s = kv[1].split("\\)")[0];
        try {
          return Double.parseDouble(s);
        } catch (NumberFormatException e) {
          //nothing
        }
      }
    }
    return 0;
  }

  public double getSiftScore(int allele) {
    for (String ann : this.getSIFTs(allele)) {
      String[] kv = ann.split("\\(");
      if (kv.length > 1) {
        String s = kv[1].split("\\)")[0];
        try {
          return Double.parseDouble(s);
        } catch (NumberFormatException ignore) { }
      }
    }
    return 0;
  }

  public double getFrequency(String key, int allele){ return firstValueAsDouble(getVEPValues(key, allele)); }

  public double getFreq1kgVEP(int allele) {
    return getFrequency(VEPFormat.KEY_AF, allele);
  }

  public double getFreq1kgAFRVEP(int allele) { return getFrequency(VEPFormat.KEY_AFR_AF, allele); }

  public double getFreq1kgAMRVEP(int allele) { return getFrequency(VEPFormat.KEY_AMR_AF, allele);  }

  public double getFreq1kgEASVEP(int allele) { return getFrequency(VEPFormat.KEY_EAS_AF, allele); }

  public double getFreq1kgEURVEP(int allele) { return getFrequency(VEPFormat.KEY_EUR_AF, allele); }

  public double getFreq1kgSASVEP(int allele) { return getFrequency(VEPFormat.KEY_SAS_AF, allele); }

  public double getFreqESP_AAVEP(int allele) { return getFrequency(VEPFormat.KEY_AA_AF, allele); }

  public double getFreqESP_EAVEP(int allele) { return getFrequency(VEPFormat.KEY_EA_AF, allele); }

  public double getFreqGnomadAFRVEP(int allele) { return getFrequency(VEPFormat.KEY_GNOMAD_AFR_AF, allele); }

  public double getFreqGnomadAMRVEP(int allele) { return getFrequency(VEPFormat.KEY_GNOMAD_AMR_AF, allele); }

  public double getFreqGnomadASJVEP(int allele) { return getFrequency(VEPFormat.KEY_GNOMAD_ASJ_AF, allele); }

  public double getFreqGnomadEASVEP(int allele) { return getFrequency(VEPFormat.KEY_GNOMAD_EAS_AF, allele); }

  public double getFreqGnomadFINVEP(int allele) { return getFrequency(VEPFormat.KEY_GNOMAD_FIN_AF, allele); }

  public double getFreqGnomadNFEVEP(int allele) { return getFrequency(VEPFormat.KEY_GNOMAD_NFE_AF, allele); }

  public double getFreqGnomadOTHVEP(int allele) { return getFrequency(VEPFormat.KEY_GNOMAD_OTH_AF, allele); }

  public double getFreqGnomadSASVEP(int allele) { return getFrequency(VEPFormat.KEY_GNOMAD_SAS_AF, allele); }
  
  public double getFreqGnomadVEP(int allele) { return getFrequency(VEPFormat.KEY_GNOMAD_AF, allele); }

  private static double firstValueAsDouble(ArrayList<String> list) {
    try {
      return Double.parseDouble(list.get(0));
    } catch (Exception ignore) { }
    return 0;
  }
  
  public boolean isInGnomADVEP(int allele) {
    return this.getFreqGnomadVEP(allele) > 0;
  }
  
  public boolean isIn1KgVEP(int allele) {
    return this.getFreq1kgVEP(allele) > 0;
  }
  
  public boolean isInDBSNPVEP(int allele){
    try {
      String rs = this.getExisting_variations(allele).get(0);
      return rs != null && !rs.isEmpty();
    } catch (Exception ignore) { }
    return false;
  }
  
  public boolean isInDBSNPVEP(){
    try {
      String rs = this.getExisting_variations().get(0);
      return rs != null && !rs.isEmpty();
    } catch (Exception ignore) { }
    return false;
  }

  public boolean hasExonic() {
    try {
      for(String exon : this.getEXONs())
        if(!exon.isEmpty())
          return true;
    } catch (Exception ignore) { }
    return false;
  }

  public boolean hasUTR() {
    return this.has5UTR() || this.has3UTR();
  }

  public boolean hasIntronic() {
    return this.getConsequencesSplit().contains(VEPConsequence.INTRON_VARIANT.getName());
  }

  public boolean has5UTR() {
    return this.getConsequencesSplit().contains(VEPConsequence.PRIME_5_UTR_VARIANT.getName());
  }

  public boolean has3UTR() {
    return this.getConsequencesSplit().contains(VEPConsequence.PRIME_3_UTR_VARIANT.getName());
  }

  public boolean hasMissense(int allele) {
    return this.getConsequencesSplit().contains(VEPConsequence.MISSENSE_VARIANT.getName());
  }

  public boolean hasIntergenic() {
    return this.getConsequencesSplit().contains(VEPConsequence.INTERGENIC_VARIANT.getName());
  }

  public boolean hasNonsense(int allele) {
    return this.getConsequencesSplit().contains(VEPConsequence.STOP_GAINED.getName());
  }

  public boolean isProbablyDamaging(int allele) {
    for (String mis : this.getPolyPhens(allele))
      if (mis.contains(VEPAnnotation.POLYPHEN_PROBABLY_DAMAGING))
        return true;
    return false;
  }

  public boolean isPossiblyDamaging(int allele) {
    for (String mis : this.getPolyPhens(allele))
      if (mis.contains(VEPAnnotation.POLYPHEN_POSSIBLY_DAMAGING))
        return true;
    return false;
  }

  public boolean isBenign(int allele) {
    for (String mis : this.getPolyPhens(allele))
      if (mis.contains(VEPAnnotation.POLYPHEN_BENIGN))
        return true;
    return false;
  }

  public boolean hasSynonymous(int allele) {
    return this.getConsequencesSplit().contains(VEPConsequence.SYNONYMOUS_VARIANT.getName());
  }
  
  public Double getInbreedingCoeff() {
    String value = getAnnot(Info.INBREEDING_COEFF);
    if (value != null)
      try {
        return new Double(value);
      } catch (NumberFormatException ignore) {
        //Nothing
      }
    return null;
  }

  public void addInfo(String s) {
    String[] kv = s.split("=");
    String key = kv[0];
    String value = null;
    if (kv.length == 2)
      value = kv[1];
    this.update(key, value);
  }

  public void update(String key, String value) {
    this.infoMap.put(key, value);
  }
  
  public String getValue(String key){
    return this.infoMap.get(key);
  }
  
  public int[] getIntArray(String key){
    try {
      String[] vals = this.getValue(key).split(",");
      int[] ret = new int[vals.length];
      for(int i = 0; i < vals.length; i++)
        ret[i] = new Integer(vals[i]);
      return ret;
    } catch (Exception ignore) { }
    return null;
  }
  
  public int getInt(String key, int defaultValue){
    try {
      return new Integer(this.getValue(key));
    } catch (Exception ignore) { }
    return defaultValue;
  }
  
  public int[] getACs(){
    return getIntArray("AC");
  }
  
  public int getAN(){
    return getInt("AN", 0);
  }

  public ArrayList<String> getFields() {
    ArrayList<String> fields = new ArrayList<>();
    for (String key : this.infoMap.navigableKeySet()) {
      String value = this.infoMap.get(key);
      if (value == null) {
        InfoFormatHeader infoFormat = vcfFile.getInfoHeader(key);
        if (infoFormat != null && infoFormat.getNumber() == InfoFormatHeader.NUMBER_NONE)
          fields.add(key);
        else
          fields.add(key + "=");
      } else
        fields.add(key + "=" + value);
    }
    return fields;
  }

  @Override
  public String toString() {
    return String.join(";", getFields());
  }

  public VEPAnnotation getWorstVEPAnnotation(int a) { //TODO check where this is called, 2-N genes can have the same worst csq
    return VEPAnnotation.getWorstVEPAnnotation(getVEPAnnotations(a));
  }
  
  public Map<String, VEPAnnotation> getWorstVEPAnnotationsByGene(int a){
    return VEPAnnotation.getWorstVEPAnnotationsByGene(getVEPAnnotations(a));
  }
  
  public VEPAnnotation getWorstVEPAnnotation() {
    return VEPAnnotation.getWorstVEPAnnotation(getAllVEPAnnotations());
  }
  
  public Map<String, VEPAnnotation> getWorstVEPAnnotationsByGene(){
    return VEPAnnotation.getWorstVEPAnnotationsByGene(getAllVEPAnnotations());
  }
  
  /**
   * Gets worst Annotation for each allele
   * @return  the worst annotation for each allele
   */
  public HashMap<Integer, VEPAnnotation> getWorstAnnotationsByAllele(){
    HashMap<Integer, VEPAnnotation> annotations = new HashMap<>();
    for (int a = 1; a < variant.getAlleleCount(); a++) 
      annotations.put(a, getWorstVEPAnnotation(a));
    return annotations;
  }
  
  /**
   * Gets the canonical consequence associated by VEP to a variant allele
   *
   * @param a the alternate allele number (1 to N)
   * @return [CanonicalConsequence],[GeneSymbol]
   */
  public VEPAnnotation getCanonicalVEPAnnotation(int a) {
    ArrayList<VEPAnnotation> canonicals = new ArrayList<>();
    ArrayList<VEPAnnotation> nonCanonicals = new ArrayList<>();
    for (VEPAnnotation vep : getVEPAnnotations(a))
      if ("YES".equals(vep.getCANONICAL()))
        canonicals.add(vep);
      else
        nonCanonicals.add(vep);

    if (!canonicals.isEmpty())
      return VEPAnnotation.getWorstVEPAnnotation(canonicals);

    else
      return VEPAnnotation.getWorstVEPAnnotation(nonCanonicals);
  }
}
