package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.files.AbstractRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.GenotypeFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Info;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;

public abstract class VariantRecord extends AbstractRecord {
  private final VCF vcf;

  public VariantRecord(VCF vcf) { this.vcf = vcf; }

  public VCF getVCF() { return vcf; }

  public abstract Variant createVariant() throws VCFException;

  public final boolean applyNonVariantFilters() throws VCFException {
    //Already filtered
    if(isFiltered())
      return true;
    //removing unwanted individuals, automatically done
    //this.applySampleFilters();

    boolean hasFilteredGenotypes = this.applyGenotypeFilters();
    //update line (missing samples or genotypes impact AC,AF,AN
    if (hasFilteredGenotypes || !getVCF().getCommandParser().getSampleFilters().isEmpty())
      if (updateACANAF()) {//if all ACs are 0, drop the line
        this.filter(getVCF());
        return true;
      }

    return this.applyLineFilters();
  }

  //public abstract void applySampleFilters() throws VCFException;

  public boolean applyGenotypeFilters() {
    //apply genotype filters ++ Must be called before lineFilter (max missing geno is part of line filters)
    boolean hasFilteredGenotypes = false;
    if (!getVCF().getCommandParser().getGenotypeFilters().isEmpty()) {
      String[] formatFields = this.getFormats();
      for (GenotypeFilter filter : getVCF().getCommandParser().getGenotypeFilters()) {
        filter.setFormat(formatFields);
        for (int i = 1; i < this.getNumberOfSamples(); i++)
          if (!filter.pass(this.getGenotypeSplit(i))) { //TODO only send the relevant values (if DP and GQ, everything will be split and send twice)
            this.setGenotypeToMissing(i);
            hasFilteredGenotypes = true;
          }
      }
    }
    return hasFilteredGenotypes;
  }

  /**
   *
   * @return true if the variant is filtered
   */
  public boolean applyLineFilters() {
    //applying line filters
    if (!getVCF().getCommandParser().getLineFilters().isEmpty()) {
      /*if (f == null)
        f = filteredLine.split(T);*/
      for (LineFilter filter : getVCF().getCommandParser().getLineFilters())
        if (!filter.pass(this)) {
          this.filter(getVCF());
          return true;
        }
    }
    return false;
  }

  /**
   * Updates AC, AN and AF fields after genotype examination
   * @return true if all SUM(ALT_ACs) == 0
   */
  public final boolean updateACANAF() {
    int an = 0;
    int[] ac = getAllACs();
    String[] newAC = new String[ac.length-1];
    String[] newAF = new String[ac.length-1];
    for(int a : ac)
      an += a;

    //replace old values of AC/AN/AF if present
    String newAN = "AN=" + an;

    int sumAC = 0;
    for (int alt = 1; alt < ac.length; alt++) {
      sumAC += ac[alt];
      newAC[alt-1] = ac[alt]+"";
      newAF[alt-1] = StringTools.scientificFormat((1d * ac[alt]) / an, 4);
    }

    //return true if all ACs are 0
    if(sumAC == 0)
      return true;

    boolean[] updated = updateACANAF(String.join(",", newAC), newAN, String.join(",", newAF));
    if(!updated[0]) {
      Message.warning("Could not update AC value, missing info field, adding it");
      this.addInfo("AC", String.join(",", newAC));
    }
    if(!updated[1]) {
      Message.warning("Could not update AN value, missing info field, adding it");
      this.addInfo("AN", newAN);
    }
    if(!updated[2]) {
      Message.warning("Could not update AF value, missing info field, adding it");
      this.addInfo("AF", String.join(",", newAF));
    }

    return false;
  }

  /**
   * Updates with computed values
   * @param newAC - the new String value of AC INFO field
   * @param newAN - the new String value of AN INFO field
   * @param newAF - the new String value of AF INFO field
   * @return 3 boolean values, has AC, AN /or AF been updated (ie: not a missing INFO field)
   */
  public abstract boolean[] updateACANAF(String newAC, String newAN, String newAF);

  public abstract int[] getAllACs();

  public abstract void setGenotypeToMissing(int sample);

  public final int getNumberOfSamples() { return getVCF().getNumberOfSamples(); }
  public abstract String[] asFields();
  public abstract String getChrom();
  public abstract void setChrom(String chrom);
  public abstract int getPos();
  public abstract void setPos(int pos);
  public abstract String getID();
  public abstract void setID(String id);
  public abstract String getRef();
  public abstract void setRef(String ref);
  public abstract String[] getAlts();
  public abstract String getAltString();
  public abstract void setAlt(String alts);
  public abstract String getQual();
  public abstract void setQual(String qual);
  public abstract String[] getFilters();

  public boolean containsFilter(String filter){
    for(String f : getFilters())
      if(f.equals(filter))
        return true;
    return false;
  }

  public abstract void clearFilters();
  public abstract void addFilter(String filter);
  public abstract void setFilters(String filters);
  public abstract String getFiltersString();
  public abstract String getInfoString();
  public abstract String[][] getInfoFields();
  public abstract String getInfo(String key);
  public abstract void addInfo(String key, String value);
  public void addInfo(String[] kv){
    this.addInfo(kv[0], kv[1]);
  }
  public abstract void clearInfo();
  public abstract String[] getFormats();
  public abstract String getFormatString();

  /**
   * Return the info fields as an Info Object, in order to build a variant
   * @return the Info object
   */
  public Info getInfo(){ return new Info(getInfoFields(), getVCF()); }

  public String getGT(int sample){
    return getGenotypeValue(sample, 0);
  }

  public abstract void updateGT(int sample, String value);

  public abstract String getGenotypeValue(int sample, int field);

  public abstract String getGenotypeString(int sample);

  public abstract String[] getGenotypeSplit(int sample);

  public final String[] getGTs(){
    return getGenotypeValues(0);
  }

  public abstract String[] getGenotypeValues(int field);

  public abstract String[] getGenotypeStrings();

  /**
   * Gets a preview of Record
   * @param max the maximum number of samples in the line (the first ones)
   * @return the line
   */
  public abstract String summary(int max);

}
