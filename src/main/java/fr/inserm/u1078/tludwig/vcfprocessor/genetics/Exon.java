package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 31 août 2016
 */
public class Exon {

  private final String chr;
  private final int start;
  private final int end;
  private final String name;
  private final int number;

  public Exon(String chr, int start, int end, String name, int number) {
    this.chr = chr;
    this.start = start;
    this.end = end;
    this.name = name;
    this.number = number;
  }

  public String getChr() {
    return chr;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public String getName() {
    return name;
  }

  public int getNumber() {
    return number;
  }

  public int compareTo(Exon exon) {
    int ret = Variant.compare(chr, start, exon.chr, exon.start);
    if (ret != 0)
      return ret;

    ret = this.end - exon.end;
    if (ret != 0)
      return ret;

    ret = this.name.compareTo(exon.name);
    if (ret != 0)
      return ret;

    return this.number - exon.number;
  }

  public boolean isInExon(String chr, int pos) {
    return (this.chr.equals(chr) && this.start <= pos && this.end >= pos);
  }
}
