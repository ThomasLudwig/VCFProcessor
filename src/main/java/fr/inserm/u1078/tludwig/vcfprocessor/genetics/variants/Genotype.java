package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Printable;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

import java.util.ArrayList;

/**
 * Genotype from VCF File
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 2015/03/17
 */
public class Genotype implements Printable {

  private final GenotypeFormat format;
  private final Sample sample;
  private String rawGenotype;
  private int[] alleles;
  private boolean phased = false;
  private boolean mustBuild = true;

  //TODO only build inner fields when needed, use a status boolean

  public Genotype(String genotype, GenotypeFormat format, Sample sample) {
    this.format = format;
    this.sample = sample;
    this.setTo(genotype);
  }
  
  public final void setTo(String genotype) {
    this.rawGenotype = genotype;
    mustBuild = true;
  }

  private void build(){
    if (rawGenotype.charAt(0) == '.')//missing
      this.alleles = null;
    else {
      int colon = rawGenotype.indexOf(':');
      String gts = colon == -1 ? rawGenotype : rawGenotype.substring(0, colon);
      this.phased = gts.indexOf('|') >= 0;
      this.alleles = getAlleles(gts);
    }
    mustBuild = false;
  }

  public int[] getAlleles() {
    if(mustBuild)
      build();
    return alleles;
  }

  public Sample getSample() {
    return this.sample;
  }

  public int getNbChrom() {
    return this.getAlleles() == null ? 0 : getAlleles().length;
  }

  public boolean isPhased(){
    if(mustBuild)
      build();
    return this.phased;
  }

  public int getFormatSize() {
    return this.format.size();
  }

  /**
   * Gets the alleles for a genotype String
   * @param geno the String representing the genotype
   * @return a array of allele number (one per chromosome)
   */
  public static int[] getAlleles(String geno) {
    if(geno.startsWith("."))
      return null;

    //Harder to read, but faster when repeated numerous times

    // First pass: count alleles (= number of delimiters + 1)
    int count = 1;
    for (int i = 0; i < geno.length(); i++) {
      char c = geno.charAt(i);
      if (c == '/' || c == '|') count++;
    }

    // Second pass: parse each allele directly
    int[] all = new int[count];
    int idx = 0;
    int val = 0;
    for (int i = 0; i < geno.length(); i++) {
      char c = geno.charAt(i);
      if (c == '/' || c == '|') {
        all[idx++] = val;
        val = 0;
      } else if (c < '0' || c > '9') {
        Message.error("Could not get alleles from the genotype [" + geno + "]");
        return null;
      }
      else {
        val = val * 10 + (c - '0'); //int parsing
      }
    }
    all[idx] = val; // last allele

    return all;
  }

  public final void setTo(Genotype replacement) {
    this.setTo(replacement.rawGenotype);
  }

  public final void setToMissing() {
    this.setTo(this.createMissingGenotype());
  }

  public static Genotype createNullGenotype(GenotypeFormat format, Sample sample)  {

    return new Genotype("./.", format, sample);
  }

  public String createMissingGenotype() {
    return "."+":.".repeat(Math.max(0, this.getFormatSize() - 1));
  }

  public String getValue(String key/*, GenotypeFormat format*/) { //TODO possible bug source, don't understand why we had to provide format
    if (this.rawGenotype.charAt(0) == '.')
      return null;
    return format.getValue(rawGenotype, key);
  }



  public int getCount(int allele){
    if(this.isMissing())
      return 0;
    int count = 0;
    for(int a : this.getAlleles())
      if(a == allele)
        count ++;
    return count;
  }

  public boolean hasAllele(int allele) {
    if(this.isMissing())
      return false;
    for(int a : this.getAlleles())
      if(a == allele)
        return true;
    return false;
  }

  public boolean isMissing() {
    return this.getAlleles() == null;
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
   * returns true if all alleles are the same (1, 2 or more chromosomes) and this allele isn't ref.
   * @return true if homozygous/haploid to alt
   */
  public boolean isHomozygousOrHaploidToAlt() {
    if(this.isMissing())
      return false;
    int allele = getAlleles()[0];
    if(allele == 0)
      return false;
    for(int a = 1 ; a < this.getNbChrom(); a++)
      if(getAlleles()[a] != allele)
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
    int allele = getAlleles()[0];
    if(allele == -1)
      return false;
    for(int a = 1 ; a < this.getNbChrom(); a++)
      if(getAlleles()[a] != allele)
        return false;
    return true;
  }

  public boolean isHomozygousToAlt(){
    return isHomozygous() && hasAlternate();
  }

  public boolean isHomozygousToAlt(int a){
    return isHomozygous() && hasAllele(a);
  }

  public boolean isHomozygous(){
    return
        !isMissing()
        && !isHaploid()
        && getAlleles()[0] == getAlleles()[1];
  }
  
  public boolean isHomozygousOrHaploid(int al){
    if(this.isMissing())
      return false;
    int allele = getAlleles()[0];
    if(allele == al)
      return false;
    for(int a = 1 ; a < this.getNbChrom(); a++)
      if(getAlleles()[a] != allele)
        return false;
    return true;
  }
  
  /**
   * returns true if genotype has 2 chromosomes with different alleles
   * @return  if heterozygous and diploid
   */
  public boolean isHeterozygousDiploid() {
    if(this.getNbChrom() != 2)
      return false;
    return this.getAlleles()[0] != this.getAlleles()[1];
  }

  public boolean isHaploid() {
    return this.getNbChrom() == 1;
  }

  public boolean hasAlternate() {
    if(this.isMissing())
      return false;
    for(int a : this.getAlleles())
      if(a > 0)
        return true;
    return false;
  }

  public ArrayList<Integer> getDistinctAlleles(){
    ArrayList<Integer> ret = new ArrayList<>();
    if(this.isMissing())
      return ret;
    for(int a : this.getAlleles())
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

  /**
   * If possible get Sum Of AD, if metrics is unavailable, get DP
   * @return
   */
  public int getSumADOrElseDP() {
    int sumAD = getSumAD();
    if(sumAD > -1)
      return sumAD;
    return getDP();
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
    return this.rawGenotype;
  }

  @Override
  public Println println() { return new Println(this.toString()); }

  /**
   * Add a trailing field, for a newly created annotation
   *
   * @param value the value to add
   */
  public void addField(String value) {
    if(isShortFormatMissing() && ".".equals(value))
      return;
    this.rawGenotype += ":" + value;
  }

  /**
   * returns true if a genotype with GT:DP:AD:PL is simply "." or "./." instead of "./.:.:.:."
   * @return true if a genotype is short missing
   */
  public boolean isShortFormatMissing(){
    String[] f = this.rawGenotype.split(":", -1);
    if(!f[0].startsWith("."))
      return false;
    return this.format.getSize() != f.length;
  }

  public void setMissing() {
    this.rawGenotype = this.createMissingGenotype();
    this.alleles = null;
  }
}
