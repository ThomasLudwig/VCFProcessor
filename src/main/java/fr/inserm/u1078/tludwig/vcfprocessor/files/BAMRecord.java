package fr.inserm.u1078.tludwig.vcfprocessor.files;

public class BAMRecord {
  private final BAM bamfile;
  private final int refID;
  private final int pos;
  private final int mapQ;
  private final Cigar cigar;
  private final int bin;
  private final int flag;
  private final int nextRefID;
  private final int nextPos;
  private final int templateLength;
  private final String readName;
  private final String sequence;
  private final String qual;
  private final Tag[] tags;

  public BAMRecord (BAM bamFile, int refID, int pos, long length, BAMByteArray in) {
    this.bamfile = bamFile;
    this.refID = refID;
    this.pos = pos;
    int lReadName = in.readUInt8();
    //Message.debug("lReadName["+lReadName+"]");
    this.mapQ = in.readUInt8();
    //Message.debug("mapQ["+mapQ+"]");
    this.bin = in.readUInt16(); //BAI index bin
    //Message.debug("bin["+bin+"]");
    int cigarOp = in.readUInt16();
    //Message.debug("cigarOp["+cigarOp+"]");
    this.flag = in.readUInt16();
    //Message.debug("flag["+flag+"]");
    int lSeq = in.readInt32(); //TODO unsigned
    //Message.debug("lSeq["+lSeq+"]");
    this.nextRefID = in.readInt32();
    //Message.debug("nextRefID["+nextRefID+"]");
    this.nextPos = 1+in.readInt32();
    //Message.debug("nextPos["+nextPos+"]");
    this.templateLength = in.readInt32();
    //Message.debug("templateLength["+templateLength+"]");
    this.readName = in.readString(lReadName);
    //Message.debug("readName["+readName+"]");
    this.cigar = in.readCigar(cigarOp);
    //Message.debug("cigar["+cigar+"]");
    this.sequence = in.readSequence(lSeq);
    //Message.debug("sequence["+sequence+"]");
    this.qual = in.readQual(lSeq);
    //Message.debug("qual["+qual+"]");
    tags = in.readTags();
    /*for(Tag tag :tags)
      Message.debug("tag["+tag+"]");*/
  }

  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder(this.readName);
    ret.append("\t").append(this.flag);
    ret.append("\t").append(this.getRef());
    ret.append("\t").append(this.pos);
    ret.append("\t").append(this.mapQ);
    ret.append("\t").append(this.cigar);
    ret.append("\t").append(this.getNextRef());
    ret.append("\t").append(this.nextPos);
    ret.append("\t").append(this.templateLength);
    ret.append("\t").append(this.sequence);
    ret.append("\t").append(this.qual);
    for(Tag tag : tags)
      ret.append("\t").append(tag);
    return ret.toString();
  }

  public int getRefID() {
    return refID;
  }

  public String getRef() {
    return refID == -1 ? "-1" : bamfile.getHeader().getReferences()[refID].getName();
  }

  public String getNextRef() {
    if(nextRefID == -1)
      return "-1";
    if(nextRefID == refID)
      return "=";
    return this.bamfile.getHeader().getReferences()[this.nextRefID].getName();
  }

  public int getPos() {
    return pos;
  }

  public int getMapQ() {
    return mapQ;
  }

  public Cigar getCigar() {
    return cigar;
  }

  public int getFlag() {
    return flag;
  }

  public int getNextRefID() {
    return nextRefID;
  }

  public int getNextPos() {
    return nextPos;
  }

  public int getTemplateLength() {
    return templateLength;
  }

  public String getReadName() {
    return readName;
  }

  public String getSequence() {
    return sequence;
  }

  public String getQual() {
    return qual;
  }

  public Tag[] getTags() {
    return tags;
  }

  public static final char[] CIGAR = {'M','I','D','N','S','H','P','=','X'}; // 0 --> 8
  public static class Cigar {
    int[] lengths;
    byte[] types;

    public Cigar(int[] lengths, byte[] types) {
      this.lengths = lengths;
      this.types = types;
    }

    @Override
    public String toString() {
      StringBuilder ret = new StringBuilder();
      for(int i = 0 ; i < lengths.length; i++){
        ret.append(lengths[i]);
        ret.append(CIGAR[types[i]]);
      }
      return ret.toString();
    }
  }

  public static class Tag {
    private final String key;
    private final char type;
    private final String value;

    public Tag(String key, char type, String value) {
      this.key = key;
      this.type = type;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    public char getType() {
      return type;
    }

    @Override
    public String toString() {
      return key+":"+type+":"+value;
    }
  }
}
