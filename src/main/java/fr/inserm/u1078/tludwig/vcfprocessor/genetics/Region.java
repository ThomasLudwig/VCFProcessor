package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 5 avr. 2016
 */
public class Region {
  public static final int FORMAT_BED = 1;
  public static final int FORMAT_BASE_1 = 2;

  private final String chrom;
  private final int chromNum;
  private int start;
  private int end;

  public Region(String line, int format) {//TODO Bed File Format is chr base0 base1, not chr base1 base1
    this(line.split("\\s+")[0], new Integer(line.split("\\s+")[1]), new Integer(line.split("\\s+")[2]), format);
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

  public int getSize() {
    return 1 + this.end - this.start;
  }

  public String asBed(){
    return this.chrom + "\t" + this.start + "\t" + this.end;
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
}
