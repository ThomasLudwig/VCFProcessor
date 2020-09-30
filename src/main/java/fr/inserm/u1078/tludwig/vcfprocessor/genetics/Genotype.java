package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import java.util.ArrayList;

/**
 * Genotype from VCF File
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 17 mars 2015
 */
public class Genotype {

  private final GenotypeFormat format;
  private final Sample sample;
  private String genotype;  
  private int nbChrom;
  private int[] alleles;
  private boolean phased = false;

  public Genotype(String genotype, GenotypeFormat format, Sample sample) throws GenotypeException {
    this.format = format;
    this.sample = sample;
    this.setTo(genotype);
  }
  
  public final void setTo(String genotype){
    this.genotype = genotype;
    if (genotype.charAt(0) == '.') {//missing
      this.nbChrom = 0;
      this.alleles = null;
    } else {
      String gts = this.genotype.split(":")[0];
      String[] genos = gts.split("\\|",-1);
      if(genos.length == 1)
        genos = gts.split("/",-1);
      else
        phased = true;
      
      this.nbChrom = genos.length;
      this.alleles = new int[this.nbChrom];
      for(int c = 0 ; c < this.nbChrom; c++)
        this.alleles[c] = Integer.parseInt(genos[c]);
    }
  }

  public static Genotype createNullGenotype(GenotypeFormat format, Sample sample) throws GenotypeException {

    return new Genotype("./.", format, sample);
  }
  
  public boolean isPhased(){
    return this.phased;
  }

  public int getFormatSize() {
    return this.format.size();
  }

  public String createMissingGenotype() {
    String ret = ".";
    for (int i = 1; i < this.getFormatSize(); i++)
      ret += ":.";
    return ret;
  }

  public String getValue(String key/*, GenotypeFormat format*/) { //TODO posible bug source, don't understand why we had to provide format
    if (this.genotype.charAt(0) == '.')
      return null;
    return format.getValue(genotype, key);
  }

  public Sample getSample() {
    return this.sample;
  }

  public int getNbChrom() {
    return nbChrom;
  }

  public int[] getAlleles() {
    return alleles;
  }
  
  public int getCount(int allele){
    if(this.isMissing())
      return 0;
    int count = 0;
    for(int a : this.alleles)
      if(a == allele)
        count ++;
    return count;
  }

  public boolean hasAllele(int allele) {
    if(this.isMissing())
      return false;
    for(int a : this.alleles)
      if(a == allele)
        return true;
    return false;
  }

  public boolean isMissing() {
    return this.nbChrom == 0;
  }
  
  public int getNbAlleles(){
    if(this.isMissing())
      return 0;
    ArrayList<Integer> al  = this.getDistinctAlleles();
    if(al.size() == 1)
      return (al.get(0) > -1) ? 1 : 0;
    return al.size();
  }

  /**
   * returns true if all alleles are the same (1, 2 or more chromosomes) and this allele isn't ref
   * @return 
   */
  public boolean isHomozygousOrHaploidToAlt() {
    if(this.isMissing())
      return false;
    int allele = alleles[0];
    if(allele == 0)
      return false;
    for(int a = 1 ; a < this.nbChrom; a++)
      if(alleles[a] != allele)
        return false;
    return true;
  }
  
  /**
   * returns true if all alleles are the same (1, 2 or more chromosomes) 
   * @return 
   */
  public boolean isHomozygousOrHaploid(){
    if(this.isMissing())
      return false;
    int allele = alleles[0];
    if(allele == -1)
      return false;
    for(int a = 1 ; a < this.nbChrom; a++)
      if(alleles[a] != allele)
        return false;
    return true;
  }
  
  public boolean isHomozygousOrHaploid(int al){
    if(this.isMissing())
      return false;
    int allele = alleles[0];
    if(allele == al)
      return false;
    for(int a = 1 ; a < this.nbChrom; a++)
      if(alleles[a] != allele)
        return false;
    return true;
  }
  
  /**
   * returns true if genotype has 2 chromosomes with differents alleles
   * @return 
   */
  public boolean isHeterozygousDiploid() {
    if(this.nbChrom != 2)
      return false;
    return this.alleles[0] != this.alleles[1];
  }

  public boolean hasAlternate() {
    if(this.isMissing())
      return false;
    for(int a : this.alleles)
      if(a > 0)
        return true;
    return false;
  }

  public ArrayList<Integer> getDistinctAlleles(){
    ArrayList<Integer> ret = new ArrayList<>();
    if(this.isMissing())
      return ret;
    for(int a : this.alleles)
      if(!ret.contains(a))
        ret.add(a);
    return ret;
  }

  public boolean isSame(Genotype g) {
    for(int a : this.getDistinctAlleles())
      if(this.getCount(a) != g.getCount(a))
        return false;
    return true;
  }

  public boolean isDPBellow(int min) {
    String dp = this.getValue(GenotypeFormat.DP);
    try {
      int intdp = Integer.parseInt(dp);
      return intdp < min;
    } catch (Exception e) {
      //Nothing
    }
    return false;
  }

  public boolean isGQBellow(int min) {
    String gq = this.getValue(GenotypeFormat.GQ);
    try {
      int intgq = Integer.parseInt(gq);
      return intgq < min;
    } catch (Exception e) {
      //Nothing
    }
    return false;
  }

  public int getDP() {
    String dp = this.getValue(GenotypeFormat.DP);
    try {
      int intdp = Integer.parseInt(dp);
      return intdp;
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public int getSumAD() {
    int[] ads = getAD();
    if (ads == null)
      return -1;
    int sum = 0;
    for (int ad : getAD())
      sum += ad;
    return sum;
  }

  public int getGQ() {
    String gq = this.getValue(GenotypeFormat.GQ);
    try {
      int intgq = Integer.parseInt(gq);
      return intgq;
    } catch (Exception e) {
      //Nothing
    }
    return -1;
  }

  public int[] getAD() {
    String ad = this.getValue(GenotypeFormat.AD);
    if (ad != null)
      try {
        String[] strings = ad.split(",");
        int[] ints = new int[strings.length];
        for (int i = 0; i < strings.length; i++)
          ints[i] = Integer.parseInt(strings[i]);
        return ints;
      } catch (Exception e) {
        //Nothing
      }
    return null;
  }

  public int getAD(int allele) {
    int[] ads = this.getAD();
    if (ads != null)
      return ads[allele];
    return -1;
  }

  @Override
  public String toString() {
    return this.genotype;
  }

  /**
   * Add a trailing filed, for a newly created annotation
   *
   * @param value
   */
  public void addField(String value) {
    this.genotype += ":" + value;
  }

  public void setMissing() {
    this.genotype = this.createMissingGenotype();
    this.nbChrom = 0;
    this.alleles = null;
  }
}
