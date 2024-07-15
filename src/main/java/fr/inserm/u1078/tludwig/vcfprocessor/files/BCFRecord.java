package fr.inserm.u1078.tludwig.vcfprocessor.files;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.*;

import java.util.*;

/**
 * Representation of one Record Line from a BCF File
 */
public class BCFRecord extends VariantRecord {
  private final BCFHeader header;
  private String chrom;
  private int pos;
  private String id;
  private String ref;
  private String[] alts;
  private String qual;
  private String[] filters;
  private String[][] info;

  private final String[][] genoValues;

  private final ArrayList<IndexedSample> selectedSamples;

  /**
   * Parses a BCF Record from 2 byte array
   * @param header - the header of the BCF file
   * @param inCommon - BCF byte array of the columns CHROM through INFO
   * @param inFormatGeno - BCF byte array of the columns FORMAT and GENOTYPES
   * @throws BCFException if the Record can't be parsed from the arrays
   */
  public BCFRecord(BCFHeader header, BCFByteArray inCommon, BCFByteArray inFormatGeno) throws BCFException {
    this.header = header;

    // Parse Chrom
    this.chrom = readChrom(inCommon);
    // Parse Pos
    this.pos = readPos(inCommon);
    //Ignore (don't understand the use of this variable)
    inCommon.readInt32();
    // Parse Qual
    this.qual = readQual(inCommon);

    // Read sizes
    int nInfo = inCommon.readInt16();
    int nAllele = inCommon.readInt16();
    int nSample = inCommon.readInt24();
    int nFormat = inCommon.readInt8();

    // Parse the ID field
    this.id = readID(inCommon);
    // Parse the REF allele
    this.ref = readRef(inCommon);
    // Parse the ALT alleles
    this.alts = readAlts(inCommon, nAllele - 1);
    // Parse filters
    this.filters = getFilters(readFiltersID(inCommon));
    // Parse INFO fields
    this.info = readInfo(inCommon, nInfo);
    // Parse genotypes
    this.genoValues = readFormatGenotypes(inFormatGeno, nFormat, nSample);
    this.selectedSamples = new ArrayList<>();
    Sample[] rawSamples = header.getRawSamples();

    if(nSample != rawSamples.length)
      throw new BCFException("Number of samples missmatch between header ["+rawSamples.length+"] and record ["+nSample+"]");

    for(int i = 0 ; i < nSample; i++)
      this.selectedSamples.add(new IndexedSample(i, rawSamples[i]));
  }

  /**
   * Reads a chromosome of the variant
   * @param in the array to parse
   * @return the name of the chromosome
   */
  private String readChrom(BCFByteArray in) {
    int chr = in.readInt32();
    return header.getContig(chr);
  }

  /**
   * Reads a position of the variant
   * @param in the array to parse
   * @return the position
   */
  private int readPos(BCFByteArray in) {
    return 1 + in.readInt32();
  }

  /**
   * Reads the quality of the variant
   * @param in the array to parse
   * @return the quality as a String
   */
  private String readQual(BCFByteArray in) {
    return in.readFloatsAsString(1);
  }

  /**
   * Reads the ID of the variants
   * @param in the array to parse
   * @return the ID
   * @throws BCFException if the array can't be parsed
   */
  private String readID(BCFByteArray in) throws BCFException {
    String ret = in.readValues();
    return ret == null ? "." : ret;
  }

  /**
   * Reads the reference allele of the variant
   * @param in the array to parse
   * @return the REF allele
   * @throws BCFException if the array can't be parsed
   */
  private String readRef(BCFByteArray in) throws BCFException {
    return in.readValues();
  }

  /**
   * Reads the alternate alleles of the variant
   * @param in the array to parse
   * @param nAlt the number of alternate alleles
   * @return the alternates alleles
   * @throws BCFException if the array can't be parsed
   */
  private String[] readAlts(BCFByteArray in, int nAlt) throws BCFException {
    String[] ret = new String[nAlt];
    for(int i = 0 ; i < nAlt; i++)
      ret[i] = in.readValues();
    return ret;
  }

  /**
   * Reads the Filter indices of the variants
   * @param in the array to parse
   * @return an array of indices
   * @throws BCFException if the array can't be parsed
   */
  private int[] readFiltersID(BCFByteArray in) throws BCFException {
    // Parse FILTER
    return in.readTypedInts();
  }

  /**
   * Converts an array of filter indices to an array of filter names
   * @param filtersID the filter indices
   * @return the filter names
   */
  private String[] getFilters(int[] filtersID) {
    String[] filters = new String[filtersID.length];
    for(int i = 0 ; i < filtersID.length; i++){
      if(filtersID[i] == 0)
        filters[i] = "PASS";
      else
        filters[i] = header.getKeyName(filtersID[i]);
    }
    return filters;
  }

  /**
   * Reads the info fields of a variants (minus the filtered ones)
   * @param in the array to parse
   * @param nInfo the number of info fields
   * @return an array of {KEY, VALUE}. VALUE can be null
   * @throws BCFException if the byte array can't be read
   */
  private String[][] readInfo(BCFByteArray in, int nInfo) throws BCFException {
    ArrayList<String[]> ret = new ArrayList<>();
    for (int i = 0; i < nInfo; i++) {
      // Read key
      int key = in.readTypedInt();
      String keyString = header.getKeyName(key);
      if(header.isInfoKept(key)) {
        String value = in.readValues();
        ret.add(new String[]{keyString, value});
      } else {
        in.skipValues();
      }
    }
    return ret.toArray(new String[0][0]);
  }

  /**
   * Reads the FORMAT/GENOTYPES fields
   * @param in the array to parse
   * @param nFormat the number of format fields
   * @param nSample the number of samples
   * @return null if variants is field, String[][nSample + 1][nFormat] otherwise<br/>
   * return[0] is the array of FORMAT names<br/>
   * return[X] is the array of values for the Xth samples (1-based)<br/>
   * Missing/filtered values are stored as "."
   * @throws BCFException if the byte array can't be parsed
   */
  private String[][] readFormatGenotypes(BCFByteArray in, int nFormat, int nSample) throws BCFException {
    if (in == null || !header.pass(this)) {
      this.filter(header.getVCF());
      return null;
    }

    //first line = keys
    //next lines values, one line per sample
    String[][] ret = new String[nSample + 1][nFormat];
    // Parse FORMAT fields
    for (int i = 0; i < nFormat; i++) {
      int key = in.readTypedInt();
      ret[0][i] = header.getKeyName(key);
      BCFByteArray.ArrayDescription ad = in.readArrayDescription();

      //Always read GT, might skip others
      if(key == header.getGTIndex())
        for (int s = 0; s < nSample; s++)
          ret[1 + s][i] = in.readGTValues(ad);
      else if(header.isFormatKept(key))
        for (int s = 0; s < nSample; s++)
          ret[1 + s][i] = in.readValuesFromSampleField(ad);
      else {
        for (int s = 0; s < nSample; s++)
          ret[1 + s][i] = ".";
        in.skip(ad, nSample);
      }
    }
    return ret;
  }

  /**
   * Return the Leftmost columns of the Record (CHROM through INFO)
   * @return the columns as a String Array
   */
  private String[] getLeftColumns() {
    return new String[]{
        getChrom(),
        getPos()+"",
        getID(),
        getRef(),
        String.join(",", getAlts()),
        getQual(),
        String.join(",",getFilters()),
        getInfoString()
    };
  }

  /**
   * Return the rightmost columnq of the Record (FORMAT and samples)
   * @return the columns as a String Array
   */
  private String[] getRightColumns() {
    String[] right = new String[1 + selectedSamples.size()];
    right[0] = String.join(":", genoValues[0]);
    for(int i = 0; i < selectedSamples.size(); i++){
      int s = selectedSamples.get(i).getIndex();
      right[i + 1] = String.join(":", genoValues[s]);
    }
    return right;
  }

  /**
   * Conversion of a BCF Record to a VCF String for the variant
   * @return the line
   */
  @Override
  public String toString() {
    return String.join(T, asFields());
  }

  public String[] asFields() {
    String[] left = this.getLeftColumns();
    String[] right = this.getRightColumns();

    String[] ret = new String[left.length + right.length];
    System.arraycopy(left, 0, ret, 0, left.length);
    System.arraycopy(right, 0, ret, left.length , right.length);
    return ret;
  }

  @Override
  public String getInfoString() {
    String[] infos = new String[info.length];
    for(int i = 0 ; i < info.length; i++) {
      infos[i] = info[i][1] == null ? info[i][0] : info[i][0] + "=" + info[i][1];
    }
    return String.join(";", infos);
  }

  @Override
  public String[] getFormats() {
    return this.genoValues[0];
  }

  @Override
  public String getFormatString() {
    return String.join(":", getFormats());
  }

  /**
   * Conversion of a BCF Record to a VCF String for the variant, limited to a number of samples
   * @param max the maximum number of samples in the line (the first ones)
   * @return the line
   */
  @Override
  public String summary(int max) {
    StringBuilder sb = new StringBuilder(String.join(T, getLeftColumns()));
    if(genoValues == null)
      sb.append("\t").append("----");
    else {
      int nbSample = genoValues.length - 1;
      int lim = Math.min(max, nbSample);
      for (int i = 0; i <= lim; i++)
        sb.append("\t").append(String.join(":", getFormatAndGenotypes()[i]));
      if (lim < nbSample)
        sb.append("\t").append("...and [").append(nbSample - lim).append("] others");
    }
    return sb.toString();
  }

  public Variant createVariant(VCF vcf) throws VCFException {
    if(this.isFiltered())
      return null;
    NavigableSet<Sample> sampleIndices = vcf.getSampleIndices().navigableKeySet();
    int nbSamples = sampleIndices.size();
    if (nbSamples == 0) //TODO allow this somehow
      throw new VCFException(vcf, "Could not create variant (list of selected sample is empty)", this);
    if (genoValues.length < 1 + nbSamples)
      throw new VCFException(vcf, "Could not create variant (not enough fields " + (genoValues.length - 1) + "/" + nbSamples + " samples)", this);
    try {
      List<Integer> kept = getKeptFormatIndices();
      GenotypeFormat format = getFormat(vcf, kept);
      Genotype[] genotypes = getGenotypes(vcf, format, kept);
      return new Variant(chrom, pos, id, ref, String.join(",", alts), qual, String.join(",", filters), getInfo(vcf), format, genotypes);
    } catch (Exception e) {
      throw new VCFException(vcf, "Could not create variant", this, e);
    }
  }

  @Override
  public boolean[] updateACANAF(String newAC, String newAN, String newAF) {
    boolean replacedAC = false;
    boolean replacedAN = false;
    boolean replacedAF = false;
    for (int i = 0; i < info.length; i++) {
      switch (info[i][0]) {
        case "AC":
          replacedAC = true;
          info[i][1] = String.join(",", newAC);
          break;
        case "AF":
          replacedAF = true;
          info[i][1] = String.join(",", newAF);
          break;
        case "AN":
          replacedAN = true;
          info[i][1] = newAN;
          break;
        default:
          break;
      }
      if (replacedAC && replacedAN && replacedAF)
        break;
    }
    return new boolean[]{replacedAC, replacedAN, replacedAF};
  }

  @Override
  public int[] getAllACs() {
    int[] acs = new int[1 + alts.length];

    for(IndexedSample s : selectedSamples){
      int[] counts = getAC(s.getIndex());
      for(int i = 0; i < 1 + alts.length; i++) {
        acs[i] += counts[i];
      }
    }
    return acs;
  }

  private int[] getAC(int s){
    String geno = this.genoValues[s][0];
    int[] acs = new int[alts.length + 1];
    if (geno != null && !geno.isEmpty()) {
      int[] alleles = Genotype.getAlleles(geno);
      if(alleles != null)
        for(int a : alleles)
          acs[a]++;
    }
    return acs;
  }

  @Override
  public int getNumberOfSamples() {
    return this.selectedSamples.size();
  }

  @Override
  public void applySampleFilters(VCF vcf) throws VCFException {
    if(genoValues == null)
      return;
    if ((vcf.getCommandParser().getSampleFilters().isEmpty()))
      return;
    final TreeMap<Sample , Integer> sampleIndices = vcf.getSampleIndices();
    this.selectedSamples.clear();
    NavigableSet<Sample> samples = sampleIndices.navigableKeySet();
    int max = genoValues.length;
    for (Sample sample : samples) {
      int idx = sampleIndices.get(sample);
      if(idx >= max)
        throw new VCFException(vcf, "Could not Filter Samples (not enough fields: " + (genoValues.length - 1) + " samples)", this);

      selectedSamples.add(new IndexedSample(idx, sample));
    }
  }

  /**
   * Gets the Chromosome of the variant
   * @return the chromosome as a String (contig name)
   */
  public String getChrom() {
    return chrom;
  }


  @Override
  public void setChrom(String chrom) {
    this.chrom = chrom;
  }

  /**
   * Gets the position of the variant
   * @return the position (1-based)
   */
  public int getPos() {
    return pos;
  }

  @Override
  public void setPos(int pos) {
    this.pos = pos;
  }

  /**
   * Gets the ID of the variant
   * @return the ID
   */
  public String getID() {
    return id;
  }

  @Override
  public void setID(String id) {
    this.id = id;
  }

  /**
   * Gets the reference allele of the variant
   * @return the REF
   */
  public String getRef() {
    return ref;
  }

  @Override
  public void setRef(String ref) {
    this.ref = ref;
  }

  @Override
  public void setAlt(String alts) {
    this.alts = alts.split(",");
  }

  @Override
  public void setQual(String qual) {
    this.qual = qual;
  }

  @Override
  public String getAltString() {
    return String.join(",", alts);
  }

  /**
   * Gets the alternate alleles of the variant
   * @return array of alternate alleles
   */
  public String[] getAlts() {
    return alts;
  }

  /**
   * Gets the quality of the variant
   * @return a String representation of the float values, or "." if missing
   */
  public String getQual() {
    return qual;
  }

  @Override
  public String getFiltersString() {
    return String.join(",", filters);
  }

  /**
   * Gets the filters of the variant
   * @return array of filter names
   */
  public String[] getFilters() {
    return filters;
  }

  @Override
  public void clearFilters() {
    this.filters = new String[0];
  }

  @Override
  public void addFilter(String filter) {
    for(String f: filters)
      if(f.equals(filter))
        return;

    String[] newFilters = new String[filters.length + 1];
    System.arraycopy(filters, 0, newFilters, 0, filters.length);
    newFilters[filters.length] = filter;
    this.filters = newFilters;
  }

  @Override
  public void setFilters(String filters) {
    this.filters = filters.split(";");
  }

  /**
   * Gets the INFO fields of the variant (minus the filtered ones)
   * @return an array of {KEY, VALUE}. VALUE can be null
   */
  public String[][] getInfo() {
    return info;
  }

  /**
   * Return the info fields as an Info Object, in order to build a variant
   * @param vcf the VCF file
   * @return the Info object
   */
  public Info getInfo(VCF vcf){
    return new Info(info, vcf);
  }

  @Override
  public void addInfo(String key, String value) {
    String[][] newInfo = new String[this.info.length + 1][2];
    System.arraycopy(info, 0, newInfo, 0, info.length);
    newInfo[info.length] = new String[]{key, value};
    this.info = newInfo;
  }

  @Override
  public void clearInfo() {
    this.info = new String[0][0];
  }

  /**
   * Gets the FORMAT and GENOTYPES values for the variant
   * @return an array of String[nSample + 1][nFormat]<br/>
   * return[0] is the array of FORMAT names<br/>
   * return[X] is the array of values for the Xth samples (1-based)<br/>
   * Missing/filtered values are stored as "."
   */
  public String[][] getFormatAndGenotypes() {
    return genoValues;
  }

  @Override
  public String getGenotypeValue(int sample, int field) {
    return genoValues[selectedSamples.get(sample).getIndex()][field];
  }

  @Override
  public void updateGT(int sample, String value) {
    genoValues[selectedSamples.get(sample).getIndex()][0] = value;
  }

  @Override
  public String getGenotypeString(int sample) {
    return String.join(":", getGenotypeSplit(sample));
  }

  @Override
  public String[] getGenotypeSplit(int sample) {
    return genoValues[selectedSamples.get(sample).getIndex()];
  }

  @Override
  public void setGenotypeToMissing(int sample) {
    Arrays.fill(genoValues[selectedSamples.get(sample).getIndex()], ".");
  }

  @Override
  public String[] getGenotypeValues(int field) {
    String[] ret = new String[getNumberOfSamples()];
    for(int sample = 0 ; sample < ret.length; sample++)
      ret[sample] = getGenotypeValue(sample, field);
    return ret;
  }

  @Override
  public String[] getGenotypeStrings() {
    String[] ret = new String[getNumberOfSamples()];
    for(int sample = 0 ; sample < ret.length; sample++)
      ret[sample] = getGenotypeString(sample);
    return ret;
  }

  public List<Integer> getKeptFormatIndices() throws BCFException {
    ArrayList<Integer> ret = new ArrayList<>();
    for(int i = 0 ; i < genoValues[0].length; i++)
      if (header.isFormatKept(genoValues[0][i]))
        ret.add(i);
    return ret;
  }

  public GenotypeFormat getFormat(VCF vcf, List<Integer> keptFormatIndices) {
    if (vcf.checkMode(VCF.MODE_QUICK_GENOTYPING))
      return new GenotypeFormat("GT");
    String[] keptFormat = new String[keptFormatIndices.size()];
    for(int i = 0 ; i < keptFormatIndices.size(); i++)
      keptFormat[i] = genoValues[0][keptFormatIndices.get(i)];
    return new GenotypeFormat(keptFormat);
  }

  public Genotype[] getGenotypes(VCF vcf, GenotypeFormat format, List<Integer> keptFormatIndices) throws BCFException {
    Genotype[] genotypes = new Genotype[this.selectedSamples.size()];

    for(int i = 0; i < this.selectedSamples.size(); i++){
      Sample sample = this.selectedSamples.get(i).getSample();
      if(sample == null)
        Message.die("Sample is null for i=" + i +" -> "+this.selectedSamples.get(i)+"\nVariants\n"+this.summary(10));

      int s = this.selectedSamples.get(i).getIndex();
      String geno;
      if (vcf.checkMode(VCF.MODE_QUICK_GENOTYPING))
        geno = genoValues[s][0];
      else {
        StringBuilder sb = new StringBuilder();
        for (Integer keptFormatIndex : keptFormatIndices)
          sb.append(":").append(genoValues[s][keptFormatIndex]);
        if(sb.length() > 0)
          geno = sb.substring(1);
        else
          geno = "";
      }
      genotypes[i] = new Genotype(geno, format, sample);//right index, because samples has already been reduces
      if(genotypes[i] == null) //TODO how can it be null ?
        throw new BCFException("Genotype is null for sample ["+sample+"]");
    }
    return genotypes;
  }

  private static class IndexedSample {
    private final int index;
    private final Sample sample;

    public IndexedSample(int index, Sample sample) {
      this.index = index + 1;
      this.sample = sample;
    }

    public int getIndex() {
      return index;
    }

    public Sample getSample() {
      return sample;
    }

    @Override
    public String toString() {
      return "[col (1-based) "+index+" "+sample.getId()+"]";
    }
  }
}
