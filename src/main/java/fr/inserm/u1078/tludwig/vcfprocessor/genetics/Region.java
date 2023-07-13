package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 5 avr. 2016
 */
public class Region implements Comparable<Region> {
  public static final int FORMAT_BED = 1;
  public static final int FORMAT_BASE_1 = 2;

  private final String chrom;
  private final int chromNum;
  private int start;
  private int end;
  private String annotation;

  public Region(String line, int format) {//TODO Bed File Format is chr base0 base1, not chr base1 base1
    this(buildFromLine(line, format), format);
  }

  private static Region buildFromLine(String line, int format) {
    String[] f = line.split("\\s+",-1);
    String ch = f[0];
    int st = Integer.parseInt(f[1]);
    int en = Integer.parseInt(f[2]);

    if(f.length > 3){
      StringBuilder an = new StringBuilder("");
      for(int i = 3; i < f.length; i++)
        an.append("\t").append(f[i]);
      String annotation = an.substring(1);
      return new Region(ch, st, en, format, annotation);
    } else
      return new Region(ch, st, en, format);
  }

  public Region(Region r, int format){
    this(r.chrom, r.start, r.end, format, r.annotation);
  }

  public Region(String chrom, int start, int end, int format, String annotation){
    this(chrom, start, end, format);
    this.annotation = annotation;
  }

  public Region(String chrom, int start, int end, int format) {
    this.chrom = chrom;
    this.chromNum = Variant.chromToNumber(chrom);
    if (start <= end) {
      this.start = start;
      this.end = end;
    } else {
      this.start = end;
      this.end = start;
    }
    if(format == FORMAT_BED)
      this.start++;

    this.annotation = null;
  }
  
  public void addPadding(int padding){
    this.start -= padding;
    if(this.start < 1)
      start = 1;
    this.end += padding;
  }

  public String getChrom() {
    return chrom;
  }

  public int getChromAsNum() {
    return chromNum;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public void setAnnotation(String annotation) {
    this.annotation = annotation;
  }

  public int getSize() {
    return 1 + this.end - this.start;
  }

  /**
   * Export Region in the bed format, but without Annotations
   * @return
   */
  public String asBed(){
    return this.chrom + "\t" + this.start + "\t" + this.end;
  }

  public String asBed(boolean withAnnotation) {
    String ret = this.asBed();
    if(withAnnotation && this.annotation != null)
      return ret+"\t"+this.annotation;
    return ret;
  }
  
  @Override
  public String toString() {
    if(this.start == 1 && this.end == Integer.MAX_VALUE)
      return this.chrom;
    return this.chrom + ":" + this.start + "-" + this.end;
  }

  public boolean overlap(Region r) {
    if (this.chromNum != r.chromNum)
      return false;

    if (this.start <= r.start)
      return r.start <= this.end;
    else
      return this.start <= r.end;
  }

  public static Region combine(Region r1, Region r2) throws RegionException {
    if (!r1.overlap(r2))
      throw new RegionException("Could not combine regions " + r1 + " and " + r2 + " as they do not overlap");

    int minStart = Math.min(r1.start, r2.start);
    int maxEnd = Math.max(r1.end, r2.end);

    return new Region(r1.getChrom(), minStart, maxEnd, FORMAT_BASE_1);
  }

  public int compareTo(Region r) {
    if (this.chromNum != r.chromNum)
      return Variant.chromToNumber(this.chrom) - Variant.chromToNumber(r.chrom);
    //this.chrom.compareTo(r.chrom);
    if (this.start != r.start)
      return this.start - r.start;
    return this.end - r.end;
  }

  public boolean contains(String chr, int pos) {
    return this.contains(Variant.chromToNumber(chr), pos);
  }

  public boolean contains(int chr, int pos) {
    return (this.chromNum == chr) && (this.start <= pos) && (this.end >= pos);
  }

  public boolean includes(Region r) {
    return contains(r.chrom, r.start) && contains(r.chrom, r.end);
  }

  public String getAnnotation() {
    return annotation;
  }
}
