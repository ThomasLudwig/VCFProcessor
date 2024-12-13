package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import java.util.Objects;

public class Chunk implements Comparable<Chunk> {
  public static final int MERGING_DISTANCE = 4096;
  private final long beg;
  private final long end;

  public Chunk(long beg, long end) {
    this.beg = beg;
    this.end = end;
  }

  public long getBeg() {
    return beg;
  }

  public long getEnd() {
    return end;
  }

  @Override
  public String toString() {
    /*String start = BAI.printLong(beg);
    long length = end - beg;*/
    return "[" + beg + ";" + end + "]";
  }

  @Override
  public int compareTo(Chunk that) {
    if(this.beg == that.beg)
      return (int)Math.signum(this.end - that.end);
    return (int)Math.signum(this.beg - that.beg);
  }

  public boolean isMergeable(Chunk that) {
    if(this.overlap(that))
      return true;
    int compare = this.compareTo(that);
    if(compare < 0)
      return that.beg - this.end < MERGING_DISTANCE;
    return (this.beg - that.end < MERGING_DISTANCE);
  }

  public boolean overlap(Chunk that) {
    if(this.end < that.beg)
      return false;
    if(that.end < this.beg)
      return false;
    return true;
  }

  public static Chunk merge(Chunk a, Chunk b) {
    return new Chunk(Math.min(a.beg, b.beg), Math.max(a.end, b.end));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Chunk chunk = (Chunk) o;
    return beg == chunk.beg && end == chunk.end;
  }

  @Override
  public int hashCode() {
    return Objects.hash(beg, end);
  }
}
