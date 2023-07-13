package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import fr.inserm.u1078.tludwig.maok.tools.StringTools;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 4 nov. 2016
 */
public class Canonical implements Comparable {
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

  public static Canonical deserialize(String string) {
    String[] f = string.split(":");
    String len = f[2];
    String allele = f[3];

    final int chr = Variant.chromToNumber(f[0]);
    final int pos = Integer.parseInt(f[1]);
    final int length = Integer.parseInt(f[2]);
    return new Canonical(chr, pos, length, allele);
  }

  public static Canonical getCanonical(String line, int a) {
    final String[] f = line.split("\t");
    final int chr = Variant.chromToNumber(f[0]);
    final int pos = Integer.parseInt(f[1]);
    final String ref = f[3];
    final String alt = f[4].split(",")[a];
    return new Canonical(chr, pos, ref, alt);
  }

  public static Canonical[] getCanonicals(String line) {
    final String[] f = line.split("\t");
    final int chr = Variant.chromToNumber(f[0]);
    final int pos = Integer.parseInt(f[1]);
    final String ref = f[3];
    final String[] alt = f[4].split(",");
    Canonical[] ret = new Canonical[alt.length];
    for (int i = 0; i < ret.length; i++)
      ret[i] = new Canonical(chr, pos, ref, alt[i]);
    return ret;
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

    if ((prefix > 0) || suffix == 0) { //prefix+suffix, prefix alone, nothing
      x = pos + prefix;
      l = ref.length() - prefix;
      a = alt.substring(prefix);

    } else { //suffix alone (suffix > 0) && (prefix == 0)
      x = pos;
      l = ref.length() - suffix;
      a = alt.substring(0, alt.length() - suffix);
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
  public int compareTo(Object o) {
    if(o instanceof Canonical){
      Canonical c = (Canonical)o;
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
    return -1;
  }
}
