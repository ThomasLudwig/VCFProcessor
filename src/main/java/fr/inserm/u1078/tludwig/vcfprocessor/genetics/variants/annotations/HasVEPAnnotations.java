package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import java.util.*;

public interface HasVEPAnnotations {
  Set<Integer> getAlleles();
  ArrayList<VEPAnnotation> getAllVEPAnnotations() ;
  ArrayList<VEPAnnotation> getVEPAnnotations(int allele);

  default SortedSet<String> getAllStringValues(VEPFacade.VEPField field) {
    return VEPFacade.getAllStringValues(getAllVEPAnnotations(), field);
  }

  default SortedSet<String> getAllStringValues(VEPFacade.VEPField field, int allele) {
    return VEPFacade.getAllStringValues(getVEPAnnotations(allele), field);
  }

  default SortedSet<Integer> getAllIntegerValues(VEPFacade.VEPField field) {
    return VEPFacade.getAllIntegerValues(getAllVEPAnnotations(), field);
  }

  default SortedSet<Integer> getAllIntegerValues(VEPFacade.VEPField field, int allele) {
    return VEPFacade.getAllIntegerValues(getVEPAnnotations(allele), field);
  }

  default SortedSet<Double> getAllFloatValues(VEPFacade.VEPField field) {
    return VEPFacade.getAllFloatValues(getAllVEPAnnotations(), field);
  }

  default SortedSet<Double> getAllFloatValues(VEPFacade.VEPField field, int allele) {
    return VEPFacade.getAllFloatValues(getVEPAnnotations(allele), field);
  }

  default double firstValueOr0(SortedSet<Double> set) {
    try {
      Double d = set.first();
      if(d != null)
        return d;
    } catch (Exception ignore) { }
    return 0;
  }

  default boolean hasValue(VEPFacade.VEPField field, int allele) {
    return hasValue(getAllFloatValues(field, allele));
  }

  default boolean hasValue(SortedSet<Double> set) {
    return set.first() != null;
  }

/*

  default double getPolyPhenScore(int allele) {
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

  default double getSiftScore(int allele) {
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

  default boolean isProbablyDamaging(int allele) {
    for (String mis : this.getPolyPhens(allele))
      if (mis.contains(VEPAnnotation.POLYPHEN_PROBABLY_DAMAGING))
        return true;
    return false;
  }

  default boolean isPossiblyDamaging(int allele) {
    for (String mis : this.getPolyPhens(allele))
      if (mis.contains(VEPAnnotation.POLYPHEN_POSSIBLY_DAMAGING))
        return true;
    return false;
  }

  default boolean isBenign(int allele) {
    for (String mis : this.getPolyPhens(allele))
      if (mis.contains(VEPAnnotation.POLYPHEN_BENIGN))
        return true;
    return false;
  }

  default ArrayList<Integer> getConsequenceLevels() {
    ArrayList<Integer> ret = new ArrayList<>();
    for(String cons : this.getConsequencesRaw()){
      int level = VEPConsequence.getWorstConsequence(cons).getLevel();
      if (!ret.contains(level))
        ret.add(level);
    }
    return ret;
  }

  default ArrayList<Integer> getConsequenceLevels(int a) {
    ArrayList<Integer> ret = new ArrayList<>();
    for(String cons : this.getConsequencesRaw(a)){
      int level = VEPConsequence.getWorstConsequence(cons).getLevel();
      if (!ret.contains(level))
        ret.add(level);
    }
    return ret;
  }

  default ArrayList<String> getConsequencesSplit(int a) {
    ArrayList<String> ret = new ArrayList<>();
    for(String consRaw : getConsequencesRaw(a))
      for (String consequences : consRaw.split("&"))
        if (!ret.contains(consequences))
          ret.add(consequences);
    return ret;
  }

  default ArrayList<String> getConsequencesSplit() {
    ArrayList<String> ret = new ArrayList<>();
    for(String consRaw : getConsequencesRaw())
      for (String consequences : consRaw.split("&"))
        if (!ret.contains(consequences))
          ret.add(consequences);
    return ret;
  }

  default String[] getGeneSymbolSortedList() {
    ArrayList<String> tmpGeneList = getSYMBOLs();
    if (tmpGeneList.isEmpty())
      return null;

    SortedList<String> geneList = new SortedList<>(tmpGeneList, SortedList.Strategy.ADD_INSERT_SORT);
    return geneList.toArray(new String[0]);
  }

  default String[] getGeneSymbolSortedList(int allele) {
    ArrayList<String> tmpGeneList = getSYMBOLs(allele);
    if (tmpGeneList.isEmpty())
      return null;

    SortedList<String> geneList = new SortedList<>(tmpGeneList, SortedList.Strategy.ADD_INSERT_SORT);
    return geneList.toArray(new String[0]);
  }

  default String getRSs() {
    String[] list = this.getRSList();
    if (list == null)
      return null;

    return String.join("," ,list);
  }

  default String[] getRSList() {
    TreeSet<String> rsList = new TreeSet<>();

    for (String rss : getExisting_variations())
      if(rss != null && ! rss.isEmpty())
        rsList.addAll(Arrays.asList(rss.split("&")));
    if (rsList.isEmpty())
      return null;

    return rsList.toArray(new String[0]);
  }

  default String getRSs(int allele) {
    String[] list = this.getRSList(allele);
    if (list == null)
      return null;

    return String.join("," , list).substring(1);
  }

  default String[] getRSList(int allele) {
    TreeSet<String> rsList = new TreeSet<>();

    for (String rss : getExisting_variations(allele))
      rsList.addAll(Arrays.asList(rss.split("&")));

    if (rsList.isEmpty())
      return null;

    return rsList.toArray(new String[0]);
  }



  default boolean isInDBSNPVEP(int allele){
    try {
      String rs = this.getExisting_variations(allele).get(0);
      return rs != null && !rs.isEmpty();
    } catch (Exception ignore) { }
    return false;
  }

  default boolean isInDBSNPVEP(){
    try {
      String rs = this.getExisting_variations().get(0);
      return rs != null && !rs.isEmpty();
    } catch (Exception ignore) { }
    return false;
  }

  default boolean hasExonic() {
    try {
      for(String exon : this.getEXONs())
        if(!exon.isEmpty())
          return true;
    } catch (Exception ignore) { }
    return false;
  }

  default ArrayList<String> getGenes() { return this.getAllVEPValues(VEPFacade.KEY_GENE); }
  default ArrayList<String> getFeature_types() { return this.getAllVEPValues(VEPFacade.KEY_FEATURE_TYPE); }
  default ArrayList<String> getFeatures() { return this.getAllVEPValues(VEPFacade.KEY_FEATURE); }
  default ArrayList<String> getBIOTYPEs() { return this.getAllVEPValues(VEPFacade.KEY_BIOTYPE); }
  default ArrayList<String> getEXONs() { return this.getAllVEPValues(VEPFacade.KEY_EXON); }
  default ArrayList<String> getINTRONs() { return this.getAllVEPValues(VEPFacade.KEY_INTRON); }
  default ArrayList<String> getHGVScs() { return this.getAllVEPValues(VEPFacade.KEY_HGVSC); }
  default ArrayList<String> getHGVSps() { return this.getAllVEPValues(VEPFacade.KEY_HGVSP); }
  default ArrayList<String> getCDNA_positions() { return this.getAllVEPValues(VEPFacade.KEY_CDNA_POSITION); }
  default ArrayList<String> getCDS_positions() { return this.getAllVEPValues(VEPFacade.KEY_CDS_POSITION); }
  default ArrayList<String> getProtein_positions() { return this.getAllVEPValues(VEPFacade.KEY_PROTEIN_POSITION); }
  default ArrayList<String> getAmino_acids() { return this.getAllVEPValues(VEPFacade.KEY_AMINO_ACIDS); }
  default ArrayList<String> getCodons() { return this.getAllVEPValues(VEPFacade.KEY_CODONS); }
  default ArrayList<String> getExisting_variations() { return this.getAllVEPValues(VEPFacade.KEY_EXISTING_VARIATION); }
  default ArrayList<String> getDISTANCEs() { return this.getAllVEPValues(VEPFacade.KEY_DISTANCE); }
  default ArrayList<String> getSTRANDs() { return this.getAllVEPValues(VEPFacade.KEY_STRAND); }
  default ArrayList<String> getFLAGSs() { return this.getAllVEPValues(VEPFacade.KEY_FLAGS); }
  default ArrayList<String> getSYMBOL_SOURCEs() { return this.getAllVEPValues(VEPFacade.KEY_SYMBOL_SOURCE); }
  default ArrayList<String> getHGNC_IDs() { return this.getAllVEPValues(VEPFacade.KEY_HGNC_ID); }
  default ArrayList<String> getCANONICALs() { return this.getAllVEPValues(VEPFacade.KEY_CANONICAL); }
  default ArrayList<String> getSIFTs() { return this.getAllVEPValues(VEPFacade.KEY_SIFT); }
  default ArrayList<String> getPolyPhens() { return this.getAllVEPValues(VEPFacade.KEY_POLYPHEN); }
  default ArrayList<String> getHGVS_OFFSETs() { return this.getAllVEPValues(VEPFacade.KEY_HGVS_OFFSET); }
  default ArrayList<String> getAFs() { return this.getAllVEPValues(VEPFacade.KEY_AF); }
  default ArrayList<String> getAFR_AFs() { return this.getAllVEPValues(VEPFacade.KEY_AFR_AF); }
  default ArrayList<String> getAMR_AFs() { return this.getAllVEPValues(VEPFacade.KEY_AMR_AF); }
  default ArrayList<String> getEAS_AFs() { return this.getAllVEPValues(VEPFacade.KEY_EAS_AF); }
  default ArrayList<String> getEUR_AFs() { return this.getAllVEPValues(VEPFacade.KEY_EUR_AF); }
  default ArrayList<String> getSAS_AFs() { return this.getAllVEPValues(VEPFacade.KEY_SAS_AF); }
  default ArrayList<String> getAA_AFs() { return this.getAllVEPValues(VEPFacade.KEY_AA_AF); }
  default ArrayList<String> getEA_AFs() { return this.getAllVEPValues(VEPFacade.KEY_EA_AF); }
  default ArrayList<String> getCLIN_SIGs() { return this.getAllVEPValues(VEPFacade.KEY_CLIN_SIG); }
  default ArrayList<String> getSOMATICs() { return this.getAllVEPValues(VEPFacade.KEY_SOMATIC); }
  default ArrayList<String> getPHENOs() { return this.getAllVEPValues(VEPFacade.KEY_PHENO); }
  default ArrayList<String> getMOTIF_NAMEs() { return this.getAllVEPValues(VEPFacade.KEY_MOTIF_NAME); }
  default ArrayList<String> getMOTIF_POSs() { return this.getAllVEPValues(VEPFacade.KEY_MOTIF_POS); }
  default ArrayList<String> getHIGH_INF_POSs() { return this.getAllVEPValues(VEPFacade.KEY_HIGH_INF_POS); }
  default ArrayList<String> getMOTIF_SCORE_CHANGEs() { return this.getAllVEPValues(VEPFacade.KEY_MOTIF_SCORE_CHANGE); }
  default double getCADD_PHRED() { return firstValueAsDouble(this.getCADD_PHREDs()); }
  default double getCADD_RAW() { return firstValueAsDouble(this.getCADD_RAWs()); }
  default double getCADD_PHRED(int allele) { return firstValueAsDouble(this.getCADD_PHREDs(allele)); }
  default double getCADD_RAW(int allele) { return firstValueAsDouble(this.getCADD_RAWs(allele)); }
  default ArrayList<String> getCADD_PHREDs() { return this.getAllVEPValues(VEPFacade.KEY_CADD_PHRED); }
  default ArrayList<String> getCADD_RAWs() { return this.getAllVEPValues(VEPFacade.KEY_CADD_RAW); }
  default ArrayList<String> getFATHMM_MKL_Cs() { return this.getAllVEPValues(VEPFacade.KEY_FATHMM_MKL_C); }
  default ArrayList<String> getFATHMM_MKL_NCs() { return this.getAllVEPValues(VEPFacade.KEY_FATHMM_MKL_NC); }
  default ArrayList<String> getCAROLs() { return this.getAllVEPValues(VEPFacade.KEY_CAROL); }
  default ArrayList<String> getLoFs() { return this.getAllVEPValues(VEPFacade.KEY_LOF); }
  default ArrayList<String> getLoF_filters() { return this.getAllVEPValues(VEPFacade.KEY_LOF_FILTER); }
  default ArrayList<String> getLoF_flags() { return this.getAllVEPValues(VEPFacade.KEY_LOF_FLAGS); }
  default ArrayList<String> getLoF_infos() { return this.getAllVEPValues(VEPFacade.KEY_LOF_INFO); }
  default ArrayList<String> getLoFtools() { return this.getAllVEPValues(VEPFacade.KEY_LOFTOOL); }
  default ArrayList<String> getGeneSplicers() { return this.getAllVEPValues(VEPFacade.KEY_GENESPLICER); }
  default ArrayList<String> getLinkedVariantss() { return this.getAllVEPValues(VEPFacade.KEY_LINKEDVARIANTS); }
  default ArrayList<String> getConsequencesRaw(int allele) { return this.getVEPValues(VEPFacade.KEY_CONSEQUENCE, allele); }
  default ArrayList<String> getIMPACTs(int allele) { return this.getVEPValues(VEPFacade.KEY_IMPACT, allele); }
  default ArrayList<String> getSYMBOLs(int allele) { return this.getVEPValues(VEPFacade.KEY_SYMBOL, allele); }
  default ArrayList<String> getGenes(int allele) { return this.getVEPValues(VEPFacade.KEY_GENE, allele); }
  default ArrayList<String> getFeature_types(int allele) { return this.getVEPValues(VEPFacade.KEY_FEATURE_TYPE, allele); }
  default ArrayList<String> getFeatures(int allele) { return this.getVEPValues(VEPFacade.KEY_FEATURE, allele); }
  default ArrayList<String> getBIOTYPEs(int allele) { return this.getVEPValues(VEPFacade.KEY_BIOTYPE, allele); }
  default ArrayList<String> getEXONs(int allele) { return this.getVEPValues(VEPFacade.KEY_EXON, allele); }
  default ArrayList<String> getINTRONs(int allele) { return this.getVEPValues(VEPFacade.KEY_INTRON, allele); }
  default ArrayList<String> getHGVScs(int allele) { return this.getVEPValues(VEPFacade.KEY_HGVSC, allele); }
  default ArrayList<String> getHGVSps(int allele) { return this.getVEPValues(VEPFacade.KEY_HGVSP, allele); }
  default ArrayList<String> getCDNA_positions(int allele) { return this.getVEPValues(VEPFacade.KEY_CDNA_POSITION, allele); }
  default ArrayList<String> getCDS_positions(int allele) { return this.getVEPValues(VEPFacade.KEY_CDS_POSITION, allele); }
  default ArrayList<String> getProtein_positions(int allele) { return this.getVEPValues(VEPFacade.KEY_PROTEIN_POSITION, allele); }
  default ArrayList<String> getAmino_acids(int allele) { return this.getVEPValues(VEPFacade.KEY_AMINO_ACIDS, allele); }
  default ArrayList<String> getCodons(int allele) { return this.getVEPValues(VEPFacade.KEY_CODONS, allele); }
  default ArrayList<String> getExisting_variations(int allele) { return this.getVEPValues(VEPFacade.KEY_EXISTING_VARIATION, allele); }
  default ArrayList<String> getIMPACTs() { return this.getAllVEPValues(VEPFacade.KEY_IMPACT); }
  default ArrayList<String> getSYMBOLs() { return this.getAllVEPValues(VEPFacade.KEY_SYMBOL); }
  default ArrayList<String> getDISTANCEs(int allele) { return this.getVEPValues(VEPFacade.KEY_DISTANCE, allele); }
  default ArrayList<String> getSTRANDs(int allele) { return this.getVEPValues(VEPFacade.KEY_STRAND, allele); }
  default ArrayList<String> getFLAGSs(int allele) { return this.getVEPValues(VEPFacade.KEY_FLAGS, allele); }
  default ArrayList<String> getSYMBOL_SOURCEs(int allele) { return this.getVEPValues(VEPFacade.KEY_SYMBOL_SOURCE, allele); }
  default ArrayList<String> getHGNC_IDs(int allele) { return this.getVEPValues(VEPFacade.KEY_HGNC_ID, allele); }
  default ArrayList<String> getCANONICALs(int allele) { return this.getVEPValues(VEPFacade.KEY_CANONICAL, allele); }
  default ArrayList<String> getSIFTs(int allele) { return this.getVEPValues(VEPFacade.KEY_SIFT, allele); }
  default ArrayList<String> getPolyPhens(int allele) { return this.getVEPValues(VEPFacade.KEY_POLYPHEN, allele); }
  default ArrayList<String> getHGVS_OFFSETs(int allele) { return this.getVEPValues(VEPFacade.KEY_HGVS_OFFSET, allele); }
  default ArrayList<String> getGMAFs(int allele) { return this.getVEPValues(VEPFacade.KEY_AF, allele); }
  default ArrayList<String> getAFR_MAFs(int allele) { return this.getVEPValues(VEPFacade.KEY_AFR_AF, allele); }
  default ArrayList<String> getAMR_MAFs(int allele) { return this.getVEPValues(VEPFacade.KEY_AMR_AF, allele); }
  default ArrayList<String> getEAS_MAFs(int allele) { return this.getVEPValues(VEPFacade.KEY_EAS_AF, allele); }
  default ArrayList<String> getEUR_MAFs(int allele) { return this.getVEPValues(VEPFacade.KEY_EUR_AF, allele); }
  default ArrayList<String> getSAS_MAFs(int allele) { return this.getVEPValues(VEPFacade.KEY_SAS_AF, allele); }
  default ArrayList<String> getAA_MAFs(int allele) { return this.getVEPValues(VEPFacade.KEY_AA_AF, allele); }
  default ArrayList<String> getEA_MAFs(int allele) { return this.getVEPValues(VEPFacade.KEY_EA_AF, allele); }
  default ArrayList<String> getCLIN_SIGs(int allele) { return this.getVEPValues(VEPFacade.KEY_CLIN_SIG, allele); }
  default ArrayList<String> getSOMATICs(int allele) { return this.getVEPValues(VEPFacade.KEY_SOMATIC, allele); }
  default ArrayList<String> getPHENOs(int allele) { return this.getVEPValues(VEPFacade.KEY_PHENO, allele); }
  default ArrayList<String> getMOTIF_NAMEs(int allele) { return this.getVEPValues(VEPFacade.KEY_MOTIF_NAME, allele); }
  default ArrayList<String> getMOTIF_POSs(int allele) { return this.getVEPValues(VEPFacade.KEY_MOTIF_POS, allele); }
  default ArrayList<String> getHIGH_INF_POSs(int allele) { return this.getVEPValues(VEPFacade.KEY_HIGH_INF_POS, allele); }
  default ArrayList<String> getMOTIF_SCORE_CHANGEs(int allele) { return this.getVEPValues(VEPFacade.KEY_MOTIF_SCORE_CHANGE, allele); }
  default ArrayList<String> getCADD_PHREDs(int allele) { return this.getVEPValues(VEPFacade.KEY_CADD_PHRED, allele); }
  default ArrayList<String> getCADD_RAWs(int allele) { return this.getVEPValues(VEPFacade.KEY_CADD_RAW, allele); }
  default ArrayList<String> getFATHMM_MKL_Cs(int allele) { return this.getVEPValues(VEPFacade.KEY_FATHMM_MKL_C, allele); }
  default ArrayList<String> getFATHMM_MKL_NCs(int allele) { return this.getVEPValues(VEPFacade.KEY_FATHMM_MKL_NC, allele); }
  default ArrayList<String> getCAROLs(int allele) { return this.getVEPValues(VEPFacade.KEY_CAROL, allele); }
  default ArrayList<String> getLoFs(int allele) { return this.getVEPValues(VEPFacade.KEY_LOF, allele); }
  default ArrayList<String> getLoF_filters(int allele) { return this.getVEPValues(VEPFacade.KEY_LOF_FILTER, allele); }
  default ArrayList<String> getLoF_flags(int allele) { return this.getVEPValues(VEPFacade.KEY_LOF_FLAGS, allele); }
  default ArrayList<String> getLoF_infos(int allele) { return this.getVEPValues(VEPFacade.KEY_LOF_INFO, allele); }
  default ArrayList<String> getLoFtools(int allele) { return this.getVEPValues(VEPFacade.KEY_LOFTOOL, allele); }
  default ArrayList<String> getGeneSplicers(int allele) { return this.getVEPValues(VEPFacade.KEY_GENESPLICER, allele); }
  default ArrayList<String> getLinkedVariantss(int allele) { return this.getVEPValues(VEPFacade.KEY_LINKEDVARIANTS, allele); }
  default double getFrequency(String key, int allele){ return firstValueAsDouble(getVEPValues(key, allele)); }
  default double getFreq1kgVEP(int allele) { return getFrequency(VEPFacade.KEY_AF, allele); }
  default double getFreq1kgAFRVEP(int allele) { return getFrequency(VEPFacade.KEY_AFR_AF, allele); }
  default double getFreq1kgAMRVEP(int allele) { return getFrequency(VEPFacade.KEY_AMR_AF, allele);  }
  default double getFreq1kgEASVEP(int allele) { return getFrequency(VEPFacade.KEY_EAS_AF, allele); }
  default double getFreq1kgEURVEP(int allele) { return getFrequency(VEPFacade.KEY_EUR_AF, allele); }
  default double getFreq1kgSASVEP(int allele) { return getFrequency(VEPFacade.KEY_SAS_AF, allele); }
  default double getFreqESP_AAVEP(int allele) { return getFrequency(VEPFacade.KEY_AA_AF, allele); }
  default double getFreqESP_EAVEP(int allele) { return getFrequency(VEPFacade.KEY_EA_AF, allele); }
  default double getFreqGnomadAFRVEP(int allele) { return getFrequency(VEPFacade.KEY_GNOMAD_AFR_AF, allele); }
  default double getFreqGnomadAMRVEP(int allele) { return getFrequency(VEPFacade.KEY_GNOMAD_AMR_AF, allele); }
  default double getFreqGnomadASJVEP(int allele) { return getFrequency(VEPFacade.KEY_GNOMAD_ASJ_AF, allele); }
  default double getFreqGnomadEASVEP(int allele) { return getFrequency(VEPFacade.KEY_GNOMAD_EAS_AF, allele); }
  default double getFreqGnomadFINVEP(int allele) { return getFrequency(VEPFacade.KEY_GNOMAD_FIN_AF, allele); }
  default double getFreqGnomadNFEVEP(int allele) { return getFrequency(VEPFacade.KEY_GNOMAD_NFE_AF, allele); }
  default double getFreqGnomadOTHVEP(int allele) { return getFrequency(VEPFacade.KEY_GNOMAD_OTH_AF, allele); }
  default double getFreqGnomadSASVEP(int allele) { return getFrequency(VEPFacade.KEY_GNOMAD_SAS_AF, allele); }
  default double getFreqGnomadVEP(int allele) { return getFrequency(VEPFacade.KEY_GNOMAD_AF, allele); }
  default boolean hasUTR() { return this.has5UTR() || this.has3UTR(); }
  default boolean hasIntronic() { return this.getConsequencesSplit().contains(VEPConsequence.INTRON_VARIANT.getName()); }
  default boolean has5UTR() { return this.getConsequencesSplit().contains(VEPConsequence.PRIME_5_UTR_VARIANT.getName()); }
  default boolean has3UTR() { return this.getConsequencesSplit().contains(VEPConsequence.PRIME_3_UTR_VARIANT.getName()); }
  default boolean hasMissense(int allele) { return this.getConsequencesSplit().contains(VEPConsequence.MISSENSE_VARIANT.getName()); }
  default boolean hasIntergenic() { return this.getConsequencesSplit().contains(VEPConsequence.INTERGENIC_VARIANT.getName()); }
  default boolean hasNonsense(int allele) { return this.getConsequencesSplit().contains(VEPConsequence.STOP_GAINED.getName()); }
  default boolean hasSynonymous(int allele) { return this.getConsequencesSplit().contains(VEPConsequence.SYNONYMOUS_VARIANT.getName()); }
  //TODO check where this is called, 2-N genes can have the same worst csq
  default VEPAnnotation getWorstVEPAnnotation(int a) { return VEPConsequence.getWorstVEPAnnotation(getVEPAnnotations(a)); }
  default Map<String, VEPAnnotation> getWorstVEPAnnotationsByGene(int a){ return VEPConsequence.getWorstVEPAnnotationsByGene(getVEPAnnotations(a)); }
  default VEPAnnotation getWorstVEPAnnotation() { return VEPConsequence.getWorstVEPAnnotation(getAllVEPAnnotations()); }
  default Map<String, VEPAnnotation> getWorstVEPAnnotationsByGene(){ return VEPConsequence.getWorstVEPAnnotationsByGene(getAllVEPAnnotations()); }
  default boolean isInGnomADVEP(int allele) {return this.getFreqGnomadVEP(allele) > 0; }
  default boolean isIn1KgVEP(int allele) { return this.getFreq1kgVEP(allele) > 0; }
  default ArrayList<String> getConsequencesRaw() {

    return this.getAllVEPValues(VEPFacade.KEY_CONSEQUENCE);
  }
*/

}
