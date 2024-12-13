package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

public class Bin {

  private static final int[] STARTS = {0, 1, 9, 73, 585, 4681, 37449};
  private static final int[] LEVEL = new int[]{
      16384 * 32768,
      16384 * 4096,
      16384 * 512,
      16384 * 64,
      16384 * 8,
      16384
  };
  public static final int[][] POSITIONS = buildPositions();
  private final int binNumber;
  private final int start;
  private final int end;
  private final Chunk[] chunks;

  private ReferenceBinIndex ref;

  public Bin(int bin, Chunk[] chunks) {
    this.binNumber = bin;
    this.chunks = chunks;
    this.start = POSITIONS[this.binNumber][0];
    this.end = POSITIONS[this.binNumber][1];;
  }

  public void setRef(ReferenceBinIndex ref){
    this.ref = ref;
  }

  public Bin getParent(){
    return ref.getBin(getParentIndex(binNumber));
  }

  public int getBinNumber() {
    return binNumber;
  }

  public Chunk[] getChunks() {
    return chunks;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }

  @Override
  public String toString() {

    StringBuilder ret = new StringBuilder("Bin "+ binNumber);
    try {
      ret.append(" {").append(POSITIONS[binNumber][0]+1).append("-").append(POSITIONS[binNumber][1]+1).append("}");
    } catch(Exception e) {
      ret.append(" ********");
    }
    ret.append(" chunks=");
    for(Chunk chunk : chunks)
      ret.append(chunk);

    Bin parent = getParent();
    if(parent != null)
      ret.append("\n\t").append(parent);
    return ret.toString();
  }

  public static int[][] buildPositions(){
    int[][] pos = new int[37449][2]; //0 - 37448
    int start = 0;
    int length = 1;

    for(int level = 0; level < 6; level++) {
      for (int i = 0; i < length; i++) {
        int n = i + start;
        pos[n][0] = i * LEVEL[level] +1 ; //TODO or +1 ?
        pos[n][1] = (i + 1) * LEVEL[level]; //TODO or +0 ?
      }
      start += length;
      length *= 8;
    }
    return pos;
  }

  public static int getParentIndex(int current){
    if(current < 1 || current > 37448)
      return -1;

    //    1  ;     8   minus    1  plus   0
    //    9  ;    72   minus    9  plus   1
    //   73  ;   584   minus   73  plus   9
    //  585  ;  4680   minus  585  plus  73
    // 4681  ; 37448   minus 4681  plus 585

    int i = 0;
    for(int j = 2; j < STARTS.length; j++)
      if(current < STARTS[j])
        i = j-2;

    int ret = current - STARTS[i+1];
    ret = Math.floorDiv(ret, 8);
    return ret + STARTS[i];
  }
}
