package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.files.ByteArray;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Cigar;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Tag;

public class BAMRecord extends AlignmentRecord {
  private final BAM bamfile;
  private final int refID;
  private final int pos;
  private final int mapQ;
  private final int[] cigarIntegers;
  private final int bin;
  private final int flag;
  private final int nextRefID;
  private final int nextPos;
  private final int templateLength;
  private final String queryName;
  private final String sequence;
  private final String qual;
  private final Tag[] tags;

  private final long pointer;

  public BAMRecord (final BAM bamFile, RawAlignmentRecordData raw) throws BAMException {
    final BAMByteArray in = raw.getBAMBytes();
    pointer = raw.getPointer();
    if(in == null)
      throw new BAMException(SAMException.NULL_RAW);
    this.bamfile = bamFile;
    this.refID = in.readLittleEndianSInt32();
    this.pos = 1+in.readLittleEndianSInt32();
    final int lReadName = in.readUInt8();
    this.mapQ = in.readUInt8();
    this.bin = in.readLittleEndianUInt16();
    int cigarOp = in.readLittleEndianUInt16();
    this.flag = in.readLittleEndianUInt16();
    final int lSeq = in.readLittleEndianUInt32();
    this.nextRefID = in.readLittleEndianSInt32();
    this.nextPos = 1+in.readLittleEndianSInt32();
    this.templateLength = in.readLittleEndianSInt32();
    this.queryName = in.readString(lReadName);
    this.cigarIntegers = in.readLittleEndianSInts32(cigarOp);
    this.sequence = in.readSequence(lSeq);
    this.qual = in.readQual(lSeq);
    this.tags = in.readTags();
  }

  @Override
  public String toString() {
    final StringBuilder ret = new StringBuilder(this.queryName);
    ret.append("\t").append(this.flag);
    ret.append("\t").append(getRefId());
    ret.append("\t").append(this.pos);
    ret.append("\t").append(this.mapQ);
    String c = "";
    try {
      c = this.getCigar().toString();
    } catch(SAMException.InvalidCigarException ignore) {

    }
    ret.append("\t").append(c);
    ret.append("\t").append(this.getNextRefId());
    ret.append("\t").append(this.nextPos);
    ret.append("\t").append(this.templateLength);
    ret.append("\t").append(this.sequence);
    ret.append("\t").append(this.qual);
    for(Tag tag : tags)
      ret.append("\t").append(tag);
    return ret.toString();
  }

  public int getBin() {
    return bin;
  }

  public long getPointer() {
    return pointer;
  }

  @Override
  public int getPos() { return pos; }

  @Override
  public int getFlag() {
    return flag;
  }
  @Override
  public int getNextPos() {
    return nextPos;
  }
  @Override
  public int getTemplateLength() {
    return templateLength;
  }

  @Override
  public String getSequence() {
    return sequence;
  }

  @Override
  public String getSeqQuality() { return this.qual; }

  @Override
  public Tag[] getTags() {
    return tags;
  }

  @Override
  public String getQueryName() { return this.queryName; }

  public int getRefIDAsNum() { return refID;}

  @Override
  public int getMappingQuality() {
    return this.mapQ;
  }

  @Override
  public String getRefId() { return this.bamfile.getReferenceName(refID); }

  @Override
  public String getNextRefId() {
    if(refID == nextRefID && nextRefID != -1)
      return "=";
    return this.bamfile.getReferenceName(nextRefID);
  }

  /*
   * cigar CIGAR:oplen<<4|op. ‘MIDNSHP=X’→‘012345678’ uint32t[ncigarop]
   */
  @Override
  public Cigar getCigar() throws SAMException.InvalidCigarException {
    int nOp = this.cigarIntegers.length;
    int[] length = new int[nOp];
    byte[] types = new byte[nOp];
    for(int i = 0 ; i < nOp; i++){
      types[i] = (byte)(this.cigarIntegers[i] & 0x000f) ;
      length[i] = (this.cigarIntegers[i] & 0xfff0) >> 4;
      if(types[i] < 0 || types[i] > 8)
        throw new SAMException.InvalidCigarException(this.bamfile, StringTools.join(",", this.cigarIntegers), ByteArray.hex(types[i]));
    }

    return new Cigar(length, types);
  }
}
