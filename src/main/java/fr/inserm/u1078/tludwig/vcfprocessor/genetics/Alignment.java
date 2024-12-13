package fr.inserm.u1078.tludwig.vcfprocessor.genetics;


import java.util.ArrayList;

public  class Alignment {
  private String queryName;
  private int flag;
  private String refID;
  private int pos;
  private int mappingQuality;
  private Cigar cigar;
  private String nextRefID;
  private int nextPos;
  private int templateLength;
  private String sequence;
  private String sequenceQuality;
  private final ArrayList<Tag> tags;

  public Alignment(String queryName, int flag, String refID, int pos, int mappingQuality, Cigar cigar, String nextRefID, int nextPos, int templateLength, String sequence, String sequenceQuality, final Tag... tags) {
    this();
    this.setQueryName(queryName);
    this.setFlag(flag);
    this.setRefID(refID);
    this.setPos(pos);
    this.setMappingQuality(mappingQuality);
    this.setCigar(cigar);
    this.setNextRefID(nextRefID);
    this.setNextPos(nextPos);
    this.setTemplateLength(templateLength);
    this.setSequence(sequence);
    this.setSeqQuality(sequenceQuality);
    for(Tag tag : tags)
      this.addTag(tag);
  }

  public Alignment() {
    this.tags = new ArrayList<>();
  }

  public String getQueryName(){ return queryName; }
  public void setQueryName(String name){
    this.queryName = name;
  }
  public int getFlag(){
    return flag;
  }
  public void setFlag(int flag){
    this.flag = flag;
  }
  public String getRefID(){return this.refID;}
  public void setRefID(String refId){
    this.refID = refId;
  }
  public int getPos(){
    return this.pos;
  }
  public void setPos(int pos){
    this.pos = pos;
  }
  public int getMappingQuality(){
    return this.mappingQuality;
  }
  public void setMappingQuality(int qual){
    this.mappingQuality = qual;
  }
  public Cigar getCigar(){
    return this.cigar;
  }
  public void setCigar(Cigar cigar){
    this.cigar = cigar;
  }
  public String getNextRefID(){
    return this.nextRefID;
  }
  public void setNextRefID(String refID){
    this.nextRefID = refID;
  }
  public int getNextPos(){
    return this.nextPos;
  }
  public void setNextPos(int pos){
    this.nextPos = pos;
  }
  public int getTemplateLength(){
    return this.templateLength;
  }
  public void setTemplateLength(int length){
    this.templateLength = length;
  }
  public String getSequence(){
    return this.sequence;
  }
  public void setSequence(String seq){
    this.sequence = seq;
  }
  public String getSeqQuality(){ return this.sequenceQuality; }
  public void setSeqQuality(String qual){
    this.sequenceQuality = qual;
  }

  public Tag[] getTags(){
    return tags.toArray(new Tag[0]);
  }

  public void clearTags(){
    tags.clear();
  }

  public void addTag(Tag tag)
  {
    //TODO check if tag already exists ?
    tags.add(tag);
  }

  public final boolean hasMask(int mask){
    return (this.getFlag() & mask) != 0;
  }

  public final void addMask(int mask) {
    this.setFlag(this.getFlag() | mask);
  }

  public final void removeMask(int mask) {
    this.setFlag(this.getFlag() & ~mask);
  }

  public final boolean isTemplateMultipleSegments(){
    return hasMask(1);
  }

  public final void addTemplateMultipleSegments(){
    addMask(1);
  }

  public final void removeTemplateMultipleSegments(){
    removeMask(1);
  }

  public final boolean isProperlyAligned(){
    return hasMask(2);
  }

  public final void addProperlyAligned(){
    addMask(2);
  }

  public final void removeProperlyAligned(){
    removeMask(2);
  }

  public final boolean isSegmentUnmapped(){
    return hasMask(4);
  }

  public final void addSegmentUnmapped(){
    addMask(4);
  }

  public final void removeSegmentUnmapped(){
    removeMask(4);
  }

  public final boolean isNextSegmentUnmapped(){
    return hasMask(8);
  }

  public final void addNextSegmentUnmapped(){
    addMask(8);
  }

  public final void removeNextSegmentUnmapped(){
    removeMask(8);
  }

  public final boolean isSeqReverseComplemented(){
    return hasMask(16);
  }

  public final void addSeqReverseComplemented(){
    addMask(16);
  }

  public final void removeSeqReverseComplemented(){
    removeMask(16);
  }

  public final boolean isNextSeqReverseComplemented(){
    return hasMask(32);
  }

  public final void addNextSeqReverseComplemented(){
    addMask(32);
  }

  public final void removeNextSeqReverseComplemented(){
    removeMask(32);
  }

  public final boolean isFirstInTemplate(){
    return hasMask(64);
  }

  public final void addFirstInTemplate(){
    addMask(64);
  }

  public final void removeFirstInTemplate(){
    removeMask(64);
  }

  public final boolean isLastInTemplate(){
    return hasMask(128);
  }

  public final void addLastInTemplate(){
    addMask(128);
  }

  public final void removeLastInTemplate(){
    removeMask(128);
  }

  public final boolean isSecondaryAlignment(){
    return hasMask(256);
  }

  public final void addSecondaryAlignment(){
    addMask(256);
  }

  public final void removeSecondaryAlignment(){
    removeMask(256);
  }

  public final boolean isNotPass(){
    return hasMask(512);
  }

  public final void addNotPass(){
    addMask(512);
  }

  public final void removeNotPass(){
    removeMask(512);
  }

  public final boolean isDuplicate(){
    return hasMask(1024);
  }

  public final void addDuplicate(){
    addMask(1024);
  }

  public final void removeDuplicate(){
    removeMask(1024);
  }

  public final boolean isSupplementaryAlignment(){
    return hasMask(2048);
  }

  public final void addSupplementaryAlignment(){
    addMask(2048);
  }

  public final void removeSupplementaryAlignment(){
    removeMask(2048);
  }

  public final String getReadGroup(){
    for(Tag tag : getTags())
      if("RG".equals(tag.getKey()))
        return tag.getValue();
    return null;
  }

  public final int getEndPos() {
    return this.getPos() + this.getSeqLengthOnRef() - 1;
  }
  public final int getSeqLengthOnRef() {
    return this.cigar.getReferenceLength();
  }

  @Override
  public String toString() {
    String T = "\t";
    StringBuilder ret = new StringBuilder(getQueryName())
        .append(T).append(getFlag())
        .append(T).append(getRefID())
        .append(T).append(getPos())
        .append(T).append(getMappingQuality())
        .append(T).append(getCigar())
        .append(T).append(getNextRefID())
        .append(T).append(getNextPos())
        .append(T).append(getTemplateLength())
        .append(T).append(getSequence())
        .append(T).append(getSeqQuality());
    for(Tag tag : getTags())
      ret.append(T).append(tag);
    return ret.toString();
  }
}
