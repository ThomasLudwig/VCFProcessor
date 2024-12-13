package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.vcfprocessor.files.ByteArray;
import fr.inserm.u1078.tludwig.vcfprocessor.files.FileFormat;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class BAI implements FileFormat {


  /*
   * R-tree : lower level 16Kbp by bin
   *
   * Example octo-tree on 6 levels
   * root (0)          : 512Mbp (max)
   * lvl1 (1-8)        : 64Mbp
   * lvl2 (9-72)       : 8Mbp
   * lvl3 (73-584)     : 1Mbp
   * lvl4 (585-4680)   : 128Kbp
   * lvl5 (4681-37448) : 16Kbp
   */


  /*
   * magic                                          char[4] BAI\1
   * n_ref #reference sequences                     uint32_t
   * for each n_ref
   *    n_bin #distinct bins                        uint32_t
   *    for each n_bin
   *        bin                                     uint32_t
   *        n_chunk                                 uint32_t
   *        for each n_chunk
   *            chunk_beg                           uint64_t
   *            chunk_end                           uint64_t
   *    n_intv #16bpk intervals                     uint32_t
   *    for each intervals
   *        ioffset                                 uint64_t
   *  n_no_coor (optional) #unplaced unmappedreads  uint64_t
   */

  public static final byte[] BAI_MAGIC_STRING = "BAI\1".getBytes();
  public static final long EMPTY = -1;
  private final String filename;
  private final List<ReferenceBinIndex> refs;
  private final long nNoCoor;

  public static String printLong(byte[] l){
    /*long div2 = l / 100000000000L;
    double div = div2/10.0;
    String modulo = ""+l%10000;
    while(modulo.length() < 4)
      modulo = "0"+modulo;
    return div+"B"+modulo;*/
    return ByteArray.hex(l)+"["+getLong(l)+"]";
  }

  public static long getLong(byte[] data){
        return
        ((0xff & data[0])
            + (0xff & data[1]) * 256
            + (0xff & data[2]) * 65536
            + (0xff & data[3]) * 16777216L
            + (0xff & data[4]) * 4294967296L
            + (0xff & data[5]) * 1099511627776L
            + (0xff & data[6]) * 281474976710656L
            + (0xff & data[7]) * 72057594037927936L );
  }

  public ChunkList getChunks(int ref, List<Region> regions){
    return this.refs.get(ref).getChunks(regions);
  }

  public ChunkList getChunks(int ref, Region region){
    return this.refs.get(ref).getChunks(region);
  }


  public static final int MAGIC_BIN = 37450;
  public static final int MAX_INTERVAL = (int)Math.pow(2, 17);
  public BAI(String filename) throws IOException, BAMException {
    int nRef;
    int nBin;
    int bin;
    int nChunk;
    int nInterval;
    this.filename = filename;
    DEBUG(this.filename);
    final BufferedInputStream in = this.checkValid(filename);
    try {
      nRef = readUint32(in, 0, Integer.MAX_VALUE);
    } catch(BAIException e) {
      throw new BAMException("nRef not valid", e);
    }
    DEBUG("nRef="+nRef);
    refs = new ArrayList<>();
    for(int r = 0 ; r < nRef; r++) {
      try {
        nBin = readUint32(in, 0, 37451);
      } catch(BAIException e) {
        throw new BAMException("nBin not valid ref="+r+"/"+nRef, e);
      }
      DEBUG("#ref("+r+") nBin="+nBin);
      List<Bin> bins = new ArrayList<>();
      long refBeg = EMPTY;
      long refEnd = EMPTY;
      long nMapped = EMPTY;
      long nUnMapped = EMPTY;
      for(int b = 0 ; b < nBin; b++) {
        try{
          bin = readUint32(in, 0, 37450);
        } catch(BAIException e) {
          throw new BAMException("bin not valid #bin="+b+"/"+nBin+" ref="+r+"/"+nRef, e);
        }

        DEBUG("#ref["+r+"]\t#bin("+b+"/"+nBin+") bin="+bin);
        if(bin == MAGIC_BIN){
          try{
            int ignoreEquals2 = readUint32(in, 2, 2);
          } catch(BAIException e) {
            throw new BAMException("should be 2, #bin="+b+"/"+nBin+" ref="+r+"/"+nRef, e);
          }
          refBeg = readUint64(in);
          refEnd = readUint64(in);
          nMapped = readUint64(in);
          nUnMapped = readUint64(in);
          DEBUG("#ref["+r+"]\trefBeg("+(refBeg)+") refEnd("+(refEnd)+") nMapped("+(nMapped)+") nUnMapped("+(nUnMapped)+")");
        } else {
          try{
            nChunk = readUint32(in, 0, Integer.MAX_VALUE);
          } catch(BAIException e) {
            throw new BAMException("nChunk not valid, #bin="+b+"/"+nBin+"("+bin+") ref="+r+"/"+nRef, e);
          }
          //DEBUG("\t#bin("+b+"/"+bin+") nChunk="+nChunks);
          Chunk[] chunks = new Chunk[nChunk];
          for (int c = 0; c < nChunk; c++) {
            chunks[c] = new Chunk(readUint64(in), readUint64(in));
            DEBUG("\t\t"+chunks[c]);
          }
          bins.add(new Bin(bin, chunks));
        }
      }
      try {
        nInterval = readUint32(in,0, MAX_INTERVAL);
      } catch(BAIException e) {
        throw new BAMException("nInterval not valid ref="+r+"/"+nRef, e);
      }
      DEBUG("#ref["+r+"]\tnInterval "+nInterval);
      long[] iOffsets = new long[nInterval];
      for(int i = 0 ; i < nInterval; i++)
        iOffsets[i] = readUint64(in);

      ReferenceBinIndex ref = new ReferenceBinIndex(r, iOffsets, refBeg, refEnd, nMapped, nUnMapped);
      ref.addBins(bins);
      refs.add(ref);
    }
    long tmpNoCoor = EMPTY;
    try{
      tmpNoCoor = readUint64(in);
    } catch(Exception ignore){
      //ignore
    }
    this.nNoCoor = tmpNoCoor;
  }

  public void testOrder(){
    for(ReferenceBinIndex ref : refs){
      ref.printRef();
    }
  }

  public void DEBUG(Object o){
   // System.out.println(o);
  }

  private BufferedInputStream checkValid(String filename) throws IOException, BAMException {
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
    if (!Arrays.equals(BAI_MAGIC_STRING, in.readNBytes(4)))
      throw new BAMException(BAMException.BAI_NO_MAGIC);
    return in;
  }

  public int readUint32(BufferedInputStream in, int min, int max) throws IOException, BAIException {
    byte[] data = in.readNBytes(4);
    int ret = ((0xff & data[0])
            + (0xff & data[1]) * 256
            + (0xff & data[2]) * 65536
            + (0xff & data[3]) * 16777216);
    if(ret < min || ret > max)
      throw new BAIException("invalid value ["+ret+"] for ["+ByteArray.hex(data)+"] min="+min+" max="+max);
    return ret;
  }

  public long readUint64(BufferedInputStream in) throws IOException{
      // BAM index files are always 4-byte aligned, but not necessrily 8-byte aligned.
      // So, rather than fooling with complex page logic we simply read the long in two 4-byte chunks.
    /*  final long lower = readUint32(in);
      final long upper = readUint32(in);
      return (upper << 32) | (lower & 0xFFFFFFFFL);*/

    byte[] data = in.readNBytes(8);
  //  return data;
    return
        ((0xff & data[0])
            + (0xff & data[1]) * 256
            + (0xff & data[2]) * 65536
            + (0xff & data[3]) * 16777216L
            + (0xff & data[4]) * 4294967296L
            + (0xff & data[5]) * 1099511627776L
            + (0xff & data[6]) * 281474976710656L
            + (0xff & data[7]) * 72057594037927936L );
  }

  @Override
  public String[] knownExtensions() {
    return new String[]{"bai"};
  }

  @Override
  public String fileFormatDescription() {
    return "BAM index file";
  }

  public String getFilename() {
    return filename;
  }

  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder(this.getFilename());
    for(ReferenceBinIndex ref : refs)
      ret.append("\n").append(ref.toString());
    ret.append("\nNo coordinates : ").append(nNoCoor);

    return ret.toString();
  }



  /**
   * calculate bin given an alignment covering [beg,end) (zero-based, half-closed-half-open)
   * @param beg 0-based included
   * @param end 0-based excluded
   * @return bin
   */
  public static int bedRegionToLeafBin(int beg, int end){
    --end;
    if (beg>>14 == end>>14) return ((1<<15)-1)/7 + (beg>>14);
    if (beg>>17 == end>>17) return ((1<<12)-1)/7 + (beg>>17);
    if (beg>>20 == end>>20) return ((1<<9)-1)/7 + (beg>>20);
    if (beg>>23 == end>>23) return ((1<<6)-1)/7 + (beg>>23);
    if (beg>>26 == end>>26) return ((1<<3)-1)/7 + (beg>>26);
    return 0;
  }

  public static final int MAX_BIN = ((1<<18)-1)/7;

  /**
   * calculate the list of bins that may overlap with region [beg,end) (zero-based)
   * @param beg 0-based included
   *    * @param end 0-based excluded
   * @return array of overlaping bins
   */
  public static Set<Integer> bedRegionToBinArray(int beg, int end)
  {
    Set<Integer> ret = new LinkedHashSet<>();
    int[] list = new int[MAX_BIN];
    int i = 0, k;
    --end;
    list[i++] = 0;
    for (k = 1 + (beg>>26); k <= 1 + (end>>26); ++k) list[i++] = k;
    for (k = 9 + (beg>>23); k <= 9 + (end>>23); ++k) list[i++] = k;
    for (k = 73 + (beg>>20); k <= 73 + (end>>20); ++k) list[i++] = k;
    for (k = 585 + (beg>>17); k <= 585 + (end>>17); ++k) list[i++] = k;
    for (k = 4681 + (beg>>14); k <= 4681 + (end>>14); ++k) list[i++] = k;

    ret.add(0);
    for(int j = 1 ; j < list.length; j++)
      ret.add(list[j]);

    return ret;
  }
}
