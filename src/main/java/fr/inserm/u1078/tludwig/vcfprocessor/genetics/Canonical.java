package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VariantRecord;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 4 nov. 2016
 */
public class Canonical implements Comparable<Canonical> {
//TODO read https://genome.sph.umich.edu/wiki/Variant_Normalization and other doc on left alignment and normalization

  private final int chr;
  private final int pos;
  private final int length;
  private final String allele;

  private Canonical(int chr, int pos, int length, String allele) {
    this.chr = chr;
    this.pos = pos;
    this.length = length;
    this.allele = allele;
  }

  public int getChr() {
    return chr;
  }

  public int getPos() {
    return pos;
  }

  public int getLength() {
    return length;
  }

  public String getAllele() {
    return allele;
  }

  /**
   * Checks if another variant in Canonical Notation overlaps this one
   * @param that - the variant to test
   * @return - true if this overlaps that
   */
  public boolean overlap(Canonical that){
    if(this.chr != that.chr)
      return false;
    return this.pos <= that.pos + Math.max(that.length,that.allele.length()) &&
           that.pos <= this.pos + Math.max(this.length,this.allele.length());
  }

  /**
   * Checks of to variants in Canonical Notation overlap
   * @param a - first serialized canonical
   * @param b - second serialized canonical
   * @return - true if a and b overlap
   */
  public static boolean overlap(String a, String b){
    Canonical ca = deserialize(a);
    Canonical cb = deserialize(b);
    return ca.overlap(cb);
  }

  public static Canonical deserialize(String string) {
    String[] f = string.split(":");
    final int chr = Variant.chromToNumber(f[0]);
    final int pos = Integer.parseInt(f[1]);
    final int length = Integer.parseInt(f[2]);
    return new Canonical(chr, pos, length, f[3]);
  }

  public static Canonical getCanonical(String line, int a) {
    return getCanonicals(line)[a];
  }

  public static Canonical[] getCanonicals(int chr, int pos, String ref, String[] alts){
    Canonical[] ret = new Canonical[alts.length];
    for (int i = 0; i < ret.length; i++)
      ret[i] = new Canonical(chr, pos, ref, alts[i]);
    return ret;
  }

  public static Canonical[] getCanonicals(String line) {
    final String[] f = line.split("\t");
    final int chr = Variant.chromToNumber(f[0]);
    final int pos = Integer.parseInt(f[1]);
    final String ref = f[3];
    final String[] alts = f[4].split(",");
    return getCanonicals(chr, pos, ref, alts);
  }

  public static Canonical[] getCanonicals(VariantRecord record) {
    final int chr = Variant.chromToNumber(record.getChrom());
    final int pos = record.getPos();
    final String ref = record.getRef();
    final String[] alts = record.getAlts();
    return getCanonicals(chr, pos, ref, alts);
  }

  public Canonical(String chr, int pos, String ref, String alt){
    this(Variant.chromToNumber(chr), pos, ref, alt);
  }

  public Canonical(int chr, int pos, String ref, String alt) {
    this.chr = chr;

    int prefix = StringTools.commonPrefix(ref, alt).length();
    int suffix = StringTools.commonSuffix(ref, alt).length();
    int x;
    int l;
    String a;


    if (suffix >= prefix) {
      x = pos;
      l = ref.length() - suffix;
      a = alt.substring(0, alt.length() - suffix);
    } else {
      x = pos + prefix;
      l = ref.length() - prefix;
      a = alt.substring(prefix);
    }

    if (a.isEmpty() || a.equals("."))
      a = "-";

    this.pos = x;
    this.length = l;
    this.allele = a;
  }

  @Override
  public String toString() {
    return this.chr + ":" + this.pos + ":" + this.length + ":" + this.allele;
  }

  public boolean isSNP() {
    return length == 1 && !allele.startsWith("-") && allele.length() == 1;
  }

  @Override
  public int compareTo(Canonical c) {
    if(this.chr == c.chr){
      if(this.pos == c.pos){
        if(this.allele.equals(c.allele))
          return this.length - c.length;
        return this.allele.compareTo(c.allele);
      }
      return this.pos - c.pos;
    }
    return this.chr - c.chr;
  }

  /**
   * UnitTesting, do not delete
   */
  public static void testCanonical() {
    int chr = 1;
    int pos = 100;
    String ref1 = "CT";
    String[] alts1 = {"CA", "CCT", "CTT", "CTCT", "C", "G", "GT"};
    String[] expected1 = {
        "1:101:1:A",
        "1:100:0:C",
        "1:102:0:T",
        "1:100:0:CT",
        "1:101:1:-",
        "1:100:2:G",
        "1:100:1:G"
    };
    String ref2 = "CCC";
    String[] alts2 = {"CC","C","CCCC"};
    String[] expected2 = {
        "1:100:1:-",
        "1:100:2:-",
        "1:100:0:C"
    };
    doTest(chr, pos, ref1, alts1, expected1);
    doTest(chr, pos, ref2, alts2, expected2);
  }

  private static void doTest(int chr, int pos, String ref, String[] alts, String[] expected) {
    Canonical[] cans = Canonical.getCanonicals(chr, pos, ref, alts);
    for(int i = 0 ; i < alts.length; i++) {
      if(expected[i].equals(cans[i].toString()))
        System.out.println(chr+" "+pos+" "+ref + " --> " + alts[i] + "\t\t\t" + cans[i]);
      else
        System.out.println(chr+" "+pos+" "+ref + " --> " + alts[i] + "\t\t\t" + cans[i] +" <<<<should be<<<< "+expected[i]);
    }
  }
}
