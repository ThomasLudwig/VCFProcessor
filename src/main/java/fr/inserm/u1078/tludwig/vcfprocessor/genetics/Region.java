package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 * In this class everything is done as [1-based;1-based]
 * In Bed File : [0-based;0-based[ or [0-based;1-based]
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 5 avr. 2016
 */
public class Region implements Comparable<Region> {
  public enum Format {FULL_1_BASED, BED_FILE, FULL_0_BASED};

  private final String chrom;
  private final int chromNum;
  private int start;
  private int end;
  private String annotation;

  public Region(final Region r){
    this.chrom = r.chrom;
    this.chromNum = r.chromNum;
    this.start =r.start;
    this.end = r.end;
    this.annotation = r.annotation;
  }

  public Region(final String chrom, final int start, final int end, final Format format){
    this(chrom, start, end, format, null);
  }

  public Region(final String chrom, final int start, final int end, final Format format, final String annotation) {
    this.chrom = chrom;
    this.chromNum = Variant.chromToNumber(chrom);

    int tmpStart = start;
    int tmpEnd = end;

    switch(format){
      case FULL_1_BASED:
        break;
      case FULL_0_BASED:
        tmpStart++;
        tmpEnd++;
        break;
      case BED_FILE:
        tmpStart++; //0-BASED included
        //end ; 0-BASED excluded / 1-BASED included
        break;
    }

    if (start <= end) {
      this.start = tmpStart;
      this.end = tmpEnd;
    } else {
      this.start = tmpEnd;
      this.end = tmpStart;
    }
    this.annotation = annotation;
  }

  public Region(final String line, Format format) {
    this(buildFromLine(line, format));
  }

  private static Region buildFromLine(final String line, Format format) {
    String[] f = line.split("\\s+",-1);
    String annotation = null;

    if(f.length > 3) {
      StringBuilder an = new StringBuilder();
      for(int i = 3; i < f.length; i++)
        an.append("\t").append(f[i]);
      annotation = an.substring(1);
    }

    return new Region(f[0], Integer.parseInt(f[1]), Integer.parseInt(f[2]), format, annotation);
  }

  public String getChrom() { return chrom; }

  public int getChromAsNum() { return chromNum; }

  public int getStart1Based() { return start; }

  public int getEnd1Based() { return end; }

  public int getStart0Based() { return start-1; }

  public int getEnd0Based() { return end-1; }

  public String getAnnotation() { return annotation; }

  public void setStart1Based(final int start) { this.start = start; }

  public void setEnd1Based(final int end) { this.end = end; }

  public void setStart0Based(final int start) { this.start = start+1; }

  public void setEnd0Based(final int end) { this.end = end+1; }

  public void setAnnotation(final String annotation) { this.annotation = annotation; }

  public void addPadding(final int padding){
    this.setStart1Based(Math.max(this.getStart1Based() - padding, 1));
    this.setEnd1Based(this.getEnd1Based() + padding);
  }

  public int getSize() { return 1 + this.end - this.start; }

  public boolean overlap(final Region r) {
    if (this.chromNum != r.chromNum)
      return false;
    return overlap(r.start, r.end);
  }

  public boolean overlap(final int rStart, final int rEnd) {
    if(this.start > rEnd)
      return false;
    if(rStart > this.end)
      return false;
    return true;
  }

  public int compareTo(final Region that) {
    if (this.chromNum != that.chromNum)
      return Variant.chromToNumber(this.chrom) - Variant.chromToNumber(that.chrom);
    if (this.start != that.start)
      return this.start - that.start;
    return this.end - that.end;
  }

  public boolean contains(final String chr, final int pos) {
    return this.contains(Variant.chromToNumber(chr), pos);
  }

  public boolean contains(final int chr, final int pos) {
    return (this.chromNum == chr) && (this.start <= pos) && (this.end >= pos);
  }

  public boolean includes(final Region r) {
    return contains(r.chrom, r.start) && contains(r.chrom, r.end);
  }

  public static Region merge(final Region r1, final Region r2) throws RegionException {
    if (!r1.overlap(r2))
      throw new RegionException("Could not combine regions " + r1 + " and " + r2 + " as they do not overlap");

    int minStart = Math.min(r1.start, r2.start);
    int maxEnd = Math.max(r1.end, r2.end);

    return new Region(r1.getChrom(), minStart, maxEnd, Format.FULL_1_BASED);
  }

  /**
   * Export Region in the bed format, but without Annotations
   * start 0-BASED included
   * end   1-based EXCLUDED
   * @return the Bed line for the region
   */
  public String asBed(){
    return this.getChrom() + "\t" + this.getStart0Based() + "\t" + (this.getEnd1Based());
  }

  public String as1Based(){
    return this.getChrom() + "\t" + this.getStart1Based() + "\t" + (this.getEnd1Based());
  }

  public String asBed(final boolean withAnnotation) {
    String ret = this.asBed();
    if(withAnnotation && this.annotation != null)
      return ret+"\t"+this.annotation;
    return ret;
  }

  @Override
  public String toString() {
    if(this.getStart1Based() == 1 && this.getEnd1Based() == Integer.MAX_VALUE)
      return this.getChrom();
    return this.as1Based()+ " (1-based) "+asBed()+" (bedFormat)";
  }
}
