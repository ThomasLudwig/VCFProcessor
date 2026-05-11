package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.AbstractRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NavigableSet;
import java.util.TreeMap;

public class VCFRecord extends VariantRecord {
  private final String[] left;
  private final String[] filteredRight;

  private final String missing;

  public VCFRecord(String line, VCF vcf) throws VCFException {
    super(vcf);
    if (line.charAt(0) == '#')
      throw new VCFException(getVCF(), "Could not create VCFRecord from the following line\n" + line);
    String[] f = line.split(AbstractRecord.T);
    left = new String[VCF.IDX_FORMAT]; //format is outside the boundaries
    System.arraycopy(f, 0, left, 0, VCF.IDX_FORMAT);
    final int nbSamples = f.length - VCF.IDX_SAMPLE;
    String[] rawRight = new String[nbSamples + 1];
    System.arraycopy(f, VCF.IDX_FORMAT, rawRight, 0, nbSamples + 1);
    filteredRight = applySampleFilters(rawRight);
    this.missing = buildMissing();
  }

  @Override
  public String toString() {
    return String.join(AbstractRecord.T, left) + AbstractRecord.T + String.join(AbstractRecord.T, filteredRight);
  }

  @Override
  public String summary(int max) {
    int lim = Math.min(max, filteredRight.length - 1);
    String[] summary = new String[1 + lim];
    System.arraycopy(filteredRight, 0, summary, 0, lim + 1);
    return String.join(AbstractRecord.T, left) + AbstractRecord.T + String.join(AbstractRecord.T, summary);
  }

  @Override
  public String[] asFields() {
    String[] ret = new String[left.length + filteredRight.length];
    System.arraycopy(left, 0, ret, 0, left.length);
    System.arraycopy(filteredRight, 0, ret, left.length, filteredRight.length);
    return ret;
  }

  @Override
  public String getChrom() {
    return left[VCF.IDX_CHROM];
  }

  @Override
  public void setChrom(String chrom) {
    left[VCF.IDX_CHROM] = chrom;
  }

  @Override
  public int getPos() {
    try {
      return Integer.parseInt(left[VCF.IDX_POS]);
    } catch(NumberFormatException e) {
      Message.error("Unable to parse Position in VCF Line:\n"+this.summary(5));
      return -1;
    }
  }

  @Override
  public void setPos(int pos) {
    left[VCF.IDX_POS] = ""+pos;
  }

  @Override
  public String getID() {
    return left[VCF.IDX_ID];
  }

  @Override
  public void setID(String id) {
    left[VCF.IDX_ID] = id;
  }

  @Override
  public String getRef() {
    return left[VCF.IDX_REF];
  }

  @Override
  public void setRef(String ref) {
    left[VCF.IDX_REF] = ref;
  }

  @Override
  public String getAltString() {
    return left[VCF.IDX_ALT];
  }

  @Override
  public void setAlt(String alts) {
    left[VCF.IDX_ALT] = alts;
  }

  @Override
  public String[] getAlts() {
    return getAltString().split(",");
  }

  @Override
  public String getQual() {
    return left[VCF.IDX_QUAL];
  }

  @Override
  public void setQual(String qual) {
    left[VCF.IDX_QUAL] = qual;
  }

  @Override
  public String[] getFilters() {
    return getFiltersString().split(";");
  }

  @Override
  public String getFiltersString() {
    return left[VCF.IDX_FILTER];
  }

  @Override
  public void clearFilters() {
    left[VCF.IDX_FILTER] = "";
  }

  @Override
  public void addFilter(String filter) {
    for(String f : left[VCF.IDX_FILTER].split(";"))
      if(f.equals(filter))
        return;
    left[VCF.IDX_FILTER] += ";"+filter;
  }

  @Override
  public void setFilters(String filters) {
    left[VCF.IDX_FILTER] = filters;
  }

  @Override
  public String getInfoString() {
    return left[VCF.IDX_INFO];
  }

  @Override
  public String[][] getInfoFields() {
    String[] f = getInfoString().split(";");
    String[][] ret = new String[f.length][2];
    for(int i = 0 ; i < f.length; i++) {
      String[] kv = f[i].split("=");
      ret[i][0] = kv[0];
      ret[i][1] = kv.length > 1 ? kv[1] : null;
    }
    return ret;
  }

  /**
   * Gets the value for the INFO filed with the given key
   * @param key the key to look for
   * @return the value for the key
   */
  public String getInfo(String key) {
    for (String f : getInfoString().split(";")) {
      String[] kv = f.split("=");
      if (key.equalsIgnoreCase(kv[0]))
        return kv.length > 1 ? kv[1] : null;
    }
    return null;
  }

  @Override
  public void addInfo(String key, String value) {
    String newInfo = key;
    if(value != null)
      newInfo += "=" + value;
    if(getInfoString().isEmpty())
      left[VCF.IDX_INFO] = newInfo;
    else
      left[VCF.IDX_INFO] += ";" + newInfo;
  }

  @Override
  public void clearInfo() {
    left[VCF.IDX_INFO] = ""; //TODO "" or "." ?
  }

  @Override
  public String[] getFormats() {
    return getFormatString().split(":");
  }

  @Override
  public String getFormatString() {
    if(filteredRight.length == 0)
      return ""; //TODO might have VCF without format/genotypes (ie gnomAD)
    return filteredRight[0];
  }

  @Override
  public Variant createVariant() throws VCFException {
    if(this.isFiltered())
      return null;
    //NavigableSet<Sample> sampleIndices = getVCF().getSampleIndices().navigableKeySet();
    if (getNumberOfSamples() == 0) //TODO allow this somehow
      throw new VCFException(getVCF(), "Could not create variant (list of selected sample is empty)", this);
/*
    if (this.getNumberOfSamples() < 1) //format + at least 1 sample
      throw new VCFException(getVCF(), "Could not create variant (not enough fields " + this.getNumberOfSamples() + "/" + nbSamples + " samples)", this);
*/
    try {
      String chrom = left[VCF.IDX_CHROM];
      int pos = Integer.parseInt(left[VCF.IDX_POS]);
      String id = left[VCF.IDX_ID];
      String ref = left[VCF.IDX_REF];
      String alt = left[VCF.IDX_ALT];
      String qual = left[VCF.IDX_QUAL];
      String filter = left[VCF.IDX_FILTER];
      Info info = getInfo();
      GenotypeFormat format = getVCF().checkMode(VCF.MODE_QUICK_GENOTYPING) ? new GenotypeFormat("GT") : new GenotypeFormat(filteredRight[0]);

      //limit to selected samples : in fact, there is nothing to do because de input line has already been altered by SampleFilters
      Genotype[] genotypes = new Genotype[getNumberOfSamples()];
      final Sample[] samples = getVCF().getSampleSet().getOutputSamples();
      for(int i = 0 ; i < samples.length; i++) {
        String geno = filteredRight[i+1];//right index, because line has already been cut
        if (getVCF().checkMode(VCF.MODE_QUICK_GENOTYPING))
          geno = geno.split(":")[0];
        genotypes[i] = new Genotype(geno, format, samples[i]);//right index, because samples has already been reduced
      }
      return new Variant(chrom, pos, id, ref, alt, qual, filter, info, format, genotypes);
    } catch (VariantException | NumberFormatException e) {
      throw new VCFException(getVCF(), "Could not create variant ("+ e.getMessage()+")", this, e);
    }
  }

  private String[] applySampleFilters(String[] rawRight) {
    if(rawRight.length == 0)
      return new String[0];

    int[] indices = getVCF().getSampleSet().getOutputSampleIndices();

    String[] filtered = new String[1 + indices.length];
    filtered[0] = rawRight[0];
    for(int i = 0 ; i < indices.length; i++)
      filtered[i + 1] = rawRight[indices[i] + 1];

    return filtered;
  }

  @Override
  public boolean[] updateACANAF(String newAC, String newAN, String newAF) {
    String[] info = left[VCF.IDX_INFO].split(";");
    boolean replacedAC = false;
    boolean replacedAN = false;
    boolean replacedAF = false;
    for (int i = 0; i < info.length; i++) {
      String[] kv = info[i].split("=");
      switch (kv[0]) {
        case "AC":
          replacedAC = true;
          info[i] = "AC=" + newAC;
          break;
        case "AF":
          replacedAF = true;
          info[i] = "AF=" + newAF;
          break;
        case "AN":
          replacedAN = true;
          info[i] = newAN;
          break;
        default:
          break;
      }
      if (replacedAC && replacedAN && replacedAF) {
        left[VCF.IDX_INFO] = String.join(";", info);
        break;
      }
    }
    return new boolean[]{replacedAC, replacedAN, replacedAF};
  }

  @Override
  public int[] getAllACs() {
    int[] acs = new int[1 + left[VCF.IDX_ALT].split(",").length];

    for (int i = 1; i < filteredRight.length; i++) {
      String geno = filteredRight[i].split(":")[0];
      if (geno != null && !geno.isEmpty()) {
        int[] alleles = Genotype.getAlleles(geno);
        if(alleles != null)
          for(int a : alleles)
            acs[a]++;
      }
    }
    return acs;
  }

  @Override
  public String getGenotypeValue(int sample, int field) {
    return getGenotypeString(sample).split(":")[field];
  }

  @Override
  public void updateGT(int sample, String value) {
    String[] g = getGenotypeSplit(sample);
    g[0] = value;
    filteredRight[sample + 1] = String.join(":", g);
  }

  @Override
  public void setGenotypeToMissing(int sample) {
    filteredRight[sample + 1] = this.missing;
  }

  private String buildMissing(){
    if(filteredRight.length == 0)
      return ".";

    String[] f = new String[getFormats().length];
    Arrays.fill(f, ".");
    return String.join(":", f);
  }

  @Override
  public String getGenotypeString(int s) {
    return filteredRight[s + 1];
  }

  @Override
  public String[] getGenotypeSplit(int sample) {
    return getGenotypeString(sample).split(":");
  }

  @Override
  public String[] getGenotypeValues(int field) {
    String[] ret = new String[this.getNumberOfSamples()];
    for(int sample = 0 ; sample < ret.length; sample++)
      ret[sample] = getGenotypeString(sample).split(":")[field];
    return ret;
  }

  @Override
  public String[] getGenotypeStrings() {
    String[] ret = new String[this.getNumberOfSamples()];
    for(int sample = 0 ; sample < ret.length; sample++)
      ret[sample] = getGenotypeString(sample);
    return ret;
  }
}
