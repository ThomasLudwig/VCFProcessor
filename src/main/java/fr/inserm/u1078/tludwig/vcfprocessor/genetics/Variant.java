package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.tools.ArrayTools;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * Variant from VCF
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 23 juin 2015
 */
public class Variant implements Comparable<Variant> {
  //TODO use contigs defined in header to manage chromosomes (the shortcut (chr)1->22 X Y M/MT) will not work for non human organisms
  private static final String[] CHROMS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "x", "y", "m", "mt"};

  private final String chrom;
  private final int pos;
  private final String id;
  private final String qual;
  private String filter;
  private final Info info;
  private final GenotypeFormat format;
  private final Genotype[] genotypes;
  private final String[] alleles;
  //private Effect[] effect = null; //TODO rewrite
//  private StringTree tree = null;
  private int[] ac;
  private int an;
  private int[] ploidyCount =null;

  public Variant() {
    chrom = null;
    pos = -1;
    id = null;
    qual = null;
    filter = null;
    info = null;
    format = null;
    genotypes = null;
    alleles = null;
  }

  public Variant(String chrom, int pos, String id, String ref, String alt, String qual, String filter, Info info, GenotypeFormat format, Genotype[] genotypes) throws VariantException {
    this.chrom = chrom;
    this.pos = pos;
    this.id = id;
    this.qual = qual;
    this.filter = filter;
    this.info = info;
    this.format = format;
    this.genotypes = genotypes;
    String[] alter = alt.split(",");
    this.alleles = new String[alter.length + 1];
    this.alleles[0] = ref;
    this.ac = new int[alleles.length];
    
    this.an = 0;

    for (int i = 0 ; i < genotypes.length; i++) {
      Genotype g = genotypes[i];
      if(g == null)
        throw new VariantException("At least one genotype is null ["+i+"th sample]");
      if (g.getNbChrom() > 0)
        for (int a : g.getAlleles())
          if (a > -1) {
            an++;
            ac[a]++;
          }
    }
    System.arraycopy(alter, 0, alleles, 1, alter.length);
//    this.effect = new Effect[this.alleles.length];
    this.link();
  }

  private void link() throws VariantException {
    try {
      this.info.setVariant(this);
    } catch (AnnotationException e) {
      throw new VariantException("Multiple variants for the same annotation for variant " + chrom + ":" + pos + " " + this.getRef() + "/" + this.getAlt(), e);
    }
  }

  public boolean isBiallelic() {
    return this.getAlleleCount() == 2;
  }

  public void intersect(Variant v) throws VariantException {
    String prefix = "Could not intersect the following variants ";
    String shortix = "\nVariant1: " + this.shortString() + "\nVariant2: " + v.shortString();
    String suffix = "\nVariant1: " + this + "\nVariant2: " + v;
    if (!this.getChrom().equals(v.getChrom()))
      throw new VariantException(prefix + "(CHROM mismatch)" + shortix);
    if (this.getPos() != (v.getPos()))
      throw new VariantException(prefix + "(POS mismatch)" + shortix);
    if (!this.getRef().equals(v.getRef()))
      throw new VariantException(prefix + "(Ref mismatch)" + shortix);
    if (this.getGenotypes().length != v.getGenotypes().length)
      throw new VariantException(prefix + "(Different number of samples)" + suffix);
    if (this.getAlt().equals(v.getAlt())) //simple case
      for (int i = 0; i < this.getGenotypes().length; i++) {
        Genotype genotype = this.genotypes[i];
        if (!genotype.isMissing()) {
          Genotype other = v.getGenotypes()[i];
          if (!genotype.isSame(other))
            this.genotypes[i] = Genotype.createNullGenotype(this.getFormat(), genotype.getSample());
        }
      }
    else { //alternative alleles are different between both variants
      int[] indices = new int[this.alleles.length];//TODO WTF?
      indices[0] = 0;
      for (int i = 1; i < indices.length; i++)
        indices[i] = ArrayTools.indexOf(v.getAlleles(), this.alleles[i]);
      for (int i = 0; i < this.getGenotypes().length; i++) {
        Genotype genotype = this.getGenotypes()[i];
        if (!genotype.isMissing()) {
          boolean clear = false;
          Genotype other = v.getGenotypes()[i];//TODO getGenotypes()[indices[i]] ???
          if (other.isMissing())
            clear = true;
          else
            if (genotype.isMissing())
              clear = true;
            else
              if (!genotype.isSame(other))
                clear = true;
          if (clear)
            this.genotypes[i] = Genotype.createNullGenotype(this.getFormat(), genotype.getSample());
        }
      }
    }
  }

  public static final String T = "\t";

  public String[] getFields() {
    String[] ret = new String[9 + genotypes.length];
    ret[0] = this.chrom;
    ret[1] = this.pos + "";
    ret[2] = this.id;
    ret[3] = this.getRef();
    ret[4] = this.getAlt();
    ret[5] = this.qual;
    ret[6] = this.filter;
    ret[7] = this.info.toString();
    ret[8] = this.format.toString();
    for (int i = 0; i < this.genotypes.length; i++)
      ret[9 + i] = this.genotypes[i].toString();
    return ret;
  }
  
  @Override
  public String toString() { // should return the line that was used to construct the variant .... NO ! that would ignore updates
    String[] left = {chrom, pos+"", id, getRef(), getAlt(),this.getQual(), this.getFilter(), this.info.toString(), this.format.toString()};
    LineBuilder ret = new LineBuilder(String.join(T, left));
    for (Genotype genotype : genotypes)
      ret.addColumn(genotype);
    return ret.toString();
  }

  public String shortString() {
    return String.join(T, chrom, pos+"", id, getRef(), getAlt());
  }

  public int getMostFrequentPloidy() {
    if(this.ploidyCount == null){
      int maxPl = 0;
      int[] tmpCount = new int[100];
      for (Genotype g : genotypes) {
        int n = g.getNbChrom();
        tmpCount[n]++;
        if(n > maxPl)
          maxPl = n;
      }
      
      this.ploidyCount = Arrays.copyOfRange(tmpCount, 0, maxPl+1);
    }
    
    int ret = 1;
    int max = this.ploidyCount[1];
    for(int i = 2; i < this.ploidyCount.length; i++){
      if(max <= this.ploidyCount[i]){
        max = this.ploidyCount[i];
        ret = i;
      }
    }
    
    return ret;
  }

  public int getMajorAllele() {//use of AC, that is updates on variant creation
    int major = 0;
    int max = ac[0];
    for (int i = 1; i < ac.length; i++)
      if (ac[i] > max) {
        max = ac[i];
        major = i;
      }

    return major;
  }

  public int[] getNonStarAltAllelesAsArray(){
    ArrayList<Integer> nonStarAltAlleles = this.getNonStarAltAllelesAsList();
    int[] ret = new int[nonStarAltAlleles.size()];
    for(int i = 0 ; i < nonStarAltAlleles.size(); i++)
      ret[i] = nonStarAltAlleles.get(i);
    return ret;
  }

  private ArrayList<Integer> getNonStarAltAllelesAsList() {
    ArrayList<Integer> nonStarAltAlleles = new ArrayList<>();
    for(int a = 1; a < this.alleles.length; a++)
      if(!"*".equals(this.alleles[a]))
        nonStarAltAlleles.add(a);
    return nonStarAltAlleles;
  }

  public boolean isMissingForAll() {
    for (Genotype genotype : this.genotypes)
      if (!genotype.isMissing())
        return false;
    return true;
  }

  public boolean hasAlternate(int a) {
    for (Genotype genotype : this.genotypes)
      if (genotype.hasAllele(a))
        return true;
    return false;
  }

  public boolean hasNoVariants() {
    for (Genotype genotype : this.genotypes)
      if (genotype.hasAlternate())
        return false;
    return true;
  }

  public ArrayList<Sample> getSamplesWithAllele(int a) {
    ArrayList<Sample> ret = new ArrayList<>();
    for (Genotype genotype : this.getGenotypes())
      if (genotype.hasAllele(a))
        ret.add(genotype.getSample());
    return ret;
  }

  public ArrayList<Sample> getSamplesWithAlternateAllele() {
    ArrayList<Sample> ret = new ArrayList<>();
    for (Genotype genotype : this.getGenotypes())
      if (genotype.hasAlternate())
        ret.add(genotype.getSample());
    return ret;
  }

  public String[] getGeneList(int allele) {
    ArrayList<String> tmpGeneList = info.getSYMBOLs(allele);
    if (tmpGeneList.isEmpty())
      return null;

    TreeSet<String> geneList = new TreeSet<>();
    for(String gene : tmpGeneList)
      if(!gene.isEmpty())
        geneList.add(gene);
    
    return geneList.toArray(new String[0]);
  }
  
  public String[] getGeneList() {
    ArrayList<String> tmpGeneList = info.getSYMBOLs();
    if (tmpGeneList.isEmpty())
      return null;

    TreeSet<String> geneList = new TreeSet<>();
    for(String gene : tmpGeneList)
      if(!gene.isEmpty())
        geneList.add(gene);
    
    return geneList.toArray(new String[0]);
  }

  public String getGenes() {
    String[] list = this.getGeneList();
    if (list == null)
      return null;

    return String.join(",", list);
  }
  
  public int[] getAC() {
    return ac;
  }

  public void setAC(int[] ac) {
    this.ac = ac;
  }

  public int getAN() {
    return an;
  }

  public double[] getAF() {
    double[] af = new double[this.alleles.length];
    for (int a = 0; a < af.length; a++)
      af[a] = (1d * ac[a]) / an;
    return af;
  }

  public void setAN(int an) {
    this.an = an;
  }
 
  public boolean isSNP() {
    for (int a = 1; a < this.getAlleleCount(); a++)
      if (!this.isSNP(a))
        return false;
    return true;
  }

  public boolean isSNP(int allele) {
    String ref = this.alleles[0];
    String alt = this.alleles[allele];

    if (ref.length() != alt.length())
      return false;

    if (ref.charAt(0) == '.')
      return false;
    if (alt.charAt(0) == '.')
      return false;

    int diff = 0;
    for (int i = 0; i < ref.length(); i++)
      if (ref.charAt(i) != alt.charAt(i))
        diff++;

    return (diff == 1);
  }

  public boolean hasSNP() {
    for (int a = 1; a < this.alleles.length; a++)
      if (this.isSNP(a))
        return true;
    return false;
  }

  public boolean hasOnlySNP() {
    for (int a = 1; a < this.alleles.length; a++)
      if (!this.isSNP(a))
        return false;
    return true;
  }

  private static final double MYSCORE_MAX = 1.2757287771616133;
  private static final double MYSCORE_SEVERE = 0.9286489853570543;
  private static final double MYSCORE_ALMOST = 0.7838656757629267;
  private static final double MYSCORE_MEDIUM = 0.4306954262433662;

  private static final double LP = 0.91;
  private static final double LS = 0.95;

  private static final double M = LS / LP;
  private static final double N = M * (LS + 1) - LP;
  private static final double N2 = 1 / (N * N);
  private static final double A = LS * LS + LP * LP * N2 - 1;

  public double getMyScore(int allele) {
    double p = this.info.getPolyPhenScore(allele);
    double s = 1 - this.info.getSiftScore(allele);
    double score;

    if (s < .5 * p)
      score = p / LP;
    else if (p < .5 * s)
      score = s / LS;
    else {

      double b = -2 * (LS * p + LP * s * N2);
      double c = p * p + s * s * N2;

      double d = (b * b) - (4 * A * c);
      double r = (-b - Math.sqrt(d)) / (2 * A);

      score = ((1 + LS) * r) / LP;
    }
    score = score / MYSCORE_MAX;
    return score;
  }

  public int getSeverityLevel(int allele) {
    double s = this.getMyScore(allele);
    if (s < MYSCORE_MEDIUM)
      return 0;
    if (s < MYSCORE_ALMOST)
      return 1;
    if (s < MYSCORE_SEVERE)
      return 2;
    return 3;
  }

  /**
   * Gets the total number of alleles
   * @return (REF + ALTS = ALT.length + 1)
   */
  public int getAlleleCount() {
    return alleles.length;
  }

  public Genotype[] getGenotypes() {
    return genotypes;
  }

  public Genotype getGenotype(String sampleID) {
    for (Genotype genotype : this.genotypes)
      if (genotype.getSample().getId().equals(sampleID))
        return genotype;
    return null;
  }

  public Genotype getGenotype(Sample sample) {
    //TODO if this works and not getGenotypes()[sampleIndex], there is a bug in the indexes, and this can lead to serious problems for all algorithms
    for (Genotype genotype : this.genotypes)
      if (genotype.getSample().equals(sample))
        return genotype;
    return null;
  }

  public String getChrom() {
    return chrom;
  }

  public int getChromNumber() {
    return chromToNumber(this.chrom);
  }

  public static int chromToNumber(String chr) {
    String c = chr.replace("chr", "");

    try {
      return Integer.parseInt(c);
    } catch (NumberFormatException e) {
      switch (c.toLowerCase().charAt(0)) {
        case 'x':
          return 23;
        case 'y':
          return 24;
        case 'm':
          return 25;
        default:
          return 26;
      }
    }
  }

  public int getPos() {
    return pos;
  }

  public String getId() {
    return id;
  }

  public String getRef() {
    return this.alleles[0];
  }

  public String getAlt() {
    StringBuilder alt = new StringBuilder(this.alleles[1]);
    for (int i = 2; i < this.alleles.length; i++)
      alt.append(",").append(this.alleles[i]);
    return alt.toString();
  }

  public String getQual() {
    return qual;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public Info getInfo() {
    return info;
  }

  public GenotypeFormat getFormat() {
    return format;
  }

  public String[] getAlleles() {
    return alleles;
  }

  public String getAllele(int a) {
    return alleles[a];
  }

  public double getPercentMissing() {
    double total = this.genotypes.length;
    double missing = 0;
    for (Genotype g : this.genotypes)
      if (g.isMissing())
        missing++;
    return missing / total;
  }

  public double getAlleleFrequencyPresent(int allele) {
    int count = 0;
    double present = 0;
    for (Genotype g : this.genotypes)
      if(!g.isMissing())
        for (int a : g.getAlleles())
          if (a > -1) {
            present++;
            if (a == allele)
              count++;
          }

    if (present == 0)
      return 0;
    return count / present;
  }

  public int getAlleleCount(int allele) {
    int count = 0;
    for (Genotype g : this.genotypes)
      count += g.getCount(allele);
    return count;
  }

  public double getAlleleFrequencyTotal(int allele) {
    return this.getAlleleCount(allele) / (2.0 * this.genotypes.length);
  }

  public static int compare(String chrom1, int pos1, String chrom2, int pos2) {
    int c1 = Variant.chromToNumber(chrom1);
    int c2 = Variant.chromToNumber(chrom2);
    if (c1 == c2)
      return pos1 - pos2;
    return c1 - c2;
  }

  @Override
  public int compareTo(Variant v) {
    return Variant.compare(this.getChrom(), this.getPos(), v.getChrom(), v.getPos());
  }

  public static int getOrder(String chrom) {
    for (int i = 0; i < CHROMS.length; i++)
      if (CHROMS[i].equals(chrom))
        return i;
    return CHROMS.length;
  }

  public ArrayList<String> getGroupsWithAlternateAllele() {
    return Ped.getGroups(this.getSamplesWithAlternateAllele());
  }

  public ArrayList<String> getGroupsWithAllele(int a) {
    return Ped.getGroups(this.getSamplesWithAllele(a));
  }

  public boolean isTransition(int allele) {
    String al = this.alleles[allele].toUpperCase();
    switch (this.alleles[0].toUpperCase()) {
      case "A":
        return al.equals("G");
      case "C":
        return al.equals("T");
      case "G":
        return al.equals("A");
      case "T":
        return al.equals("C");
      default:
        return false;
    }
  }

  public boolean isTransversion(int allele) {
    String al = this.alleles[allele].toUpperCase();
    switch (this.alleles[0].toUpperCase()) {
      case "A":
      case "G":
        return al.equals("C") || al.equals("T");
      case "C":
      case "T":
        return al.equals("A") || al.equals("G");
      default :
        return false;
    }
  }

  public Canonical getCanonical(int allele) {
    return new Canonical(this.getChromNumber(), this.getPos(), this.getRef(), this.getAllele(allele));
  }

  public boolean isHQ(boolean autosomeOnly, int minSumAD, int minGQ, double maxMissingRate, String... allowedFilters) {
    //Variant sites were considered high-quality if they met the following criteria:
    //Only autosome
    if (autosomeOnly && getChromNumber() > 22)
      return false;
    //they were given a PASS filter status by VQSR (see above)
    boolean isFilterAllowed = false;
    for(String filter : allowedFilters)
      if (getFilter().equalsIgnoreCase(filter)){
        isFilterAllowed = true;
        break;
      }

    if(!isFilterAllowed)
      return false;
    int nbHQ = 0;
    int nbVariantHQ = 0;
    for (Genotype g : getGenotypes())
      if (g.getSumADOrElseDP() >= minSumAD && g.getGQ() >= minGQ) {
        nbHQ++;
        if (g.hasAlternate())
          nbVariantHQ++;
      }
    //at least 80% of the individuals in the dataset had at least depth (DP) >= 10 and genotype quality (GQ) >= 20 (i.e. AN_Adj >= 60706*0.8*2 or 97130)
    int nb = (int) (maxMissingRate * this.genotypes.length);
    if (nbHQ < nb)
      return false;
    //there was at least one individual harboring the alternate allele with depth >= 10 and GQ >= 20
    //the variant was not located in the 10 1-kb regions of the genome with the highest levels of multi-allelic (quad-allelic or higher) variation.
    //will be managed later 
    /*
    6       32489038        32490037        147
    6       32551254        32552253        135
    2       239006314       239007313       120
    6       32548723        32549722        89
    6       32556688        32557687        82
    9       66456733        66457732        81
    6       32631890        32632889        81
    7       100550332       100551331       78
    6       31323997        31324996        73
    16      33961029        33962028        72
     */
    return nbVariantHQ > 0;
  }

  public void addInfo(String[] infos) {
    for (String inf : infos)
      this.addInfo(inf);
  }

  public void addInfo(String inf) {
    this.info.addInfo(inf);
  }

  private void updateACANAF(int[] ac, int an) {
    StringBuilder newAC = new StringBuilder("" + ac[1]);
    StringBuilder newAF = new StringBuilder("" + ((ac[1] * 1d) / an));
    for (int i = 2; i < ac.length; i++) {
      newAC.append(",").append(ac[i]);
      newAF.append(",").append((ac[i] * 1d) / an);
    }
    this.getInfo().update("AC", newAC.toString());
    this.getInfo().update("AN", an + "");
    this.getInfo().update("AF", newAF.toString());
  }

  public void recomputeACAN() {
    this.an = 0;
    Arrays.fill(ac, 0);

    for (Genotype g : this.genotypes)
      if (!g.isMissing()) {
        an += g.getNbChrom();
        for (int a : g.getAlleles())
          ac[a]++;
      }
    this.updateACANAF(ac, an);
  }
}
