package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;

import java.util.*;

public class ReferenceBinIndex {

  private final int id;
  private final Map<Integer, Bin> bins;
  private final long[] iOffsets;
  private final long refBeg;
  private final long refEnd;
  private final long nMapped;
  private final long nUbMapped;

  public ReferenceBinIndex(int id, long[] iOffsets, long refBeg, long refEnd, long nMapped, long nUbMapped) {
    this.id = id;
    this.bins = new HashMap<>();
    this.iOffsets = iOffsets;
    this.refBeg = refBeg;
    this.refEnd = refEnd;
    this.nMapped = nMapped;
    this.nUbMapped = nUbMapped;
  }

  public void addBins(List<Bin> bins){
    for(Bin bin : bins) {
      if(this.bins.get(bin.getBinNumber()) != null){
        Message.die("For ref ["+id+"] try to put a bin:\n"+bin+"\nwhere one is already present :\n"+bins.get(bin.getBinNumber()));
      }
      this.bins.put(bin.getBinNumber(), bin);
      bin.setRef(this);
    }
  }

  public String getRefDescription(){
    return "Ref "+id+
        " iOffset["+iOffsets.length+"]"+
        " refBeg["+refBeg+"]"+
        " refEnd["+refEnd+"]"+
        " nMapped["+nMapped+"]"+
        " nUbMapped["+nUbMapped+"]";
  }

  public void printRef() {
    System.out.println(getRefDescription());
    for(int i = 4681; i < 37449; i++){
      Bin bin = bins.get(i);
      if(bin != null) {
        int c = 0;
        for (Chunk chunk : bin.getChunks())
          System.out.println("bin[" + i + "]{"+bin.getStart()+";"+bin.getStart()+"} chunk[" + c++ + "]  {" + chunk.getBeg() + ";" + chunk.getEnd() + "}");

      } else break;
    }
  }

  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder(getRefDescription());
    for(int i = 4681; i <= 37450; i++) {
      Bin bin = this.bins.get(i);
      if(bin != null)
        ret.append("\n\tREF[").append(id).append("] ").append(bin);
    }
    return ret.toString();
  }

  public Set<Integer> getBins(Region region){
    return BAI.bedRegionToBinArray(region.getStart0Based(), region.getEnd0Based());
  }

  public Set<Integer> getBins(List<Region> regions) {
    Message.debug("Looking for bins for all regions");
    Set<Integer> ret = new LinkedHashSet<>();
    for(Region region : regions)
      ret.addAll(getBins(region));
    Message.debug("found "+ret.size()+" bins for all regions");
    return ret;
  }

  public ChunkList getChunks(Set<Integer> binList) {
    ChunkList chunks = new ChunkList();
    for(int binNumber : binList) {
      Bin bin = getBin(binNumber);
      if(bin != null)
        chunks.addAll(getChunks(bin));
    }
    return chunks;
  }

  public ChunkList getChunks(Region region) {
    return getChunks(getBins(region));
  }

  public ChunkList getChunks(List<Region> regions) {
    return getChunks(getBins(regions));
  }

  public ChunkList getChunks(Bin bin) {
    ChunkList chunks = new ChunkList();
    Bin parent = bin.getParent();
    if(parent != null)
      chunks.addAll(getChunks(parent));
    chunks.addAll(bin.getChunks());
    return chunks;
  }

  public Bin getBin(int binNumber) {
    return bins.get(binNumber);
  }
}
