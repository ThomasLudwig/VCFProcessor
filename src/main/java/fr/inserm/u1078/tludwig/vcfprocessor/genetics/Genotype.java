package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import fr.inserm.u1078.tludwig.maok.tools.Message;

import java.util.ArrayList;

/**
 * Genotype from VCF File
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 2015/03/17
 */
public class Genotype {

  private final GenotypeFormat format;
  private final Sample sample;
  private String genotype;  
  private int nbChrom;
  private int[] alleles;
  private boolean phased = false;

  public Genotype(String genotype, GenotypeFormat format, Sample sample) {
    this.format = format;
    this.sample = sample;
    this.setTo(genotype);
  }
  
  public final void setTo(String genotype) {
    this.genotype = genotype;
    if (genotype.charAt(0) == '.') {//missing
      this.nbChrom = 0;
      this.alleles = null;
    } else {
      String gts = this.genotype.split(":")[0];
      this.phased = isPhased(gts);
      this.alleles = getAlleles(gts);
      this.nbChrom = this.alleles == null ? 0 : alleles.length;
    }
  }

  /**
   * Checks if a genotype String is phased
   * @param geno the String representing the genotype
   * @return true if the genotype is phased
   */
  public static boolean isPhased(String geno) {
    return geno.contains("|");
  }

  /**
   * Gets the alleles for a genotype String
   * @param geno the String representing the genotype
   * @return a array of allele number (one per chromosome)
   */
  public static int[] getAlleles(String geno) {
    if(geno.startsWith("."))
      return null;
    String[] genos = geno.split("[/\\|]"); //split by / or |
    int[] all = new int[genos.length];
    for(int i = 0 ; i < all.length; i++) {
      try {
        all[i] = Integer.parseInt(genos[i]);
      } catch(NumberFormatException e){
        Message.error("Could not get alleles from the genotype ["+geno+"]");
      }
    }
    return all;
  }

  public final void setTo(Genotype replacement) {
    this.setTo(replacement.genotype);
  }

  public final void setToMissing() {
    this.setTo(this.createMissingGenotype());
  }

  public static Genotype createNullGenotype(GenotypeFormat format, Sample sample)  {

    return new Genotype("./.", format, sample);
  }
  
  public boolean isPhased(){
    return this.phased;
  }

  public int getFormatSize() {
    return this.format.size();
  }

  public String createMissingGenotype() {
    return "."+":.".repeat(Math.max(0, this.getFormatSize() - 1));
  }

  public String getValue(String key/*, GenotypeFormat format*/) { //TODO possible bug source, don't understand why we had to provide format
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
   * @return true if homozygous/haploid to alt
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
   * @return true if homozygous of haploid
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
   * returns true if genotype has 2 chromosomes with different alleles
   * @return  if heterozygous and diploid
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
      int intDP = Integer.parseInt(dp);
      return intDP < min;
    } catch (Exception e) {
      //Nothing
    }
    return false;
  }

  public boolean isGQBellow(int min) {
    String gq = this.getValue(GenotypeFormat.GQ);
    try {
      int intGQ = Integer.parseInt(gq);
      return intGQ < min;
    } catch (Exception e) {
      //Nothing
    }
    return false;
  }

  public int getDP() {
    String dp = this.getValue(GenotypeFormat.DP);
    try {
      return Integer.parseInt(dp);
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
      return Integer.parseInt(gq);
    } catch (Exception ignore) {
      //Nothing
    }
    return -1;
  }

  public int[] getAD() {
    String ad = this.getValue(GenotypeFormat.AD);
    if (ad != null)
      try {
        String[] strings = ad.split(",");
        int[] intValues = new int[strings.length];
        for (int i = 0; i < strings.length; i++)
          intValues[i] = Integer.parseInt(strings[i]);
        return intValues;
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

  public int[] getPL() {
    String ad = this.getValue(GenotypeFormat.PL);
    if (ad != null)
      try {
        String[] strings = ad.split(",");
        int[] intValues = new int[strings.length];
        for (int i = 0; i < strings.length; i++)
          intValues[i] = Integer.parseInt(strings[i]);
        return intValues;
      } catch (Exception e) {
        //Nothing
      }
    return null;
  }

  @Override
  public String toString() {
    return this.genotype;
  }

  /**
   * Add a trailing field, for a newly created annotation
   *
   * @param value the value to add
   */
  public void addField(String value) {
    if(isShortFormatMissing() && ".".equals(value))
      return;
    this.genotype += ":" + value;
  }

  /**
   * returns true if a genotype with GT:DP:AD:PL is simply "." or "./." instead of "./.:.:.:."
   * @return true if a genotype is short missing
   */
  public boolean isShortFormatMissing(){
    String[] f = this.genotype.split(":", -1);
    if(!f[0].startsWith("."))
      return false;
    return this.format.getSize() != f.length;
  }

  public void setMissing() {
    this.genotype = this.createMissingGenotype();
    this.nbChrom = 0;
    this.alleles = null;
  }
}
