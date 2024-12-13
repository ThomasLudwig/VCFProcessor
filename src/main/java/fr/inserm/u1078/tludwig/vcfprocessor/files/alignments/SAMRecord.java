package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Cigar;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Tag;

import java.util.ArrayList;

public class SAMRecord extends AlignmentRecord {
  public static final int IDX_QNAME = 0;
  public static final int IDX_FLAG = 1;
  public static final int IDX_REF_ID = 2;
  public static final int IDX_POS = 3;
  public static final int IDX_MAPQ = 4;
  public static final int IDX_CIGAR = 5;
  public static final int IDX_RNEXT = 6;
  public static final int IDX_PNEXT = 7;
  public static final int IDX_TLEN = 8;
  public static final int IDX_SEQ = 9;
  public static final int IDX_QUAL = 10;
  public static final int IDX_TAGS = 11;

  private final SAM sam;
  private final String[] fields;
  public SAMRecord(String rawLine, SAM sam) {
    this.sam = sam;
    this.fields = rawLine.split("\t");
  }

  @Override
  public String getQueryName() {
    return this.fields[IDX_QNAME];
  }

  @Override
  public int getFlag() {
    return Integer.parseInt(this.fields[IDX_FLAG]);
  }

  @Override
  public String getRefId() {
    return this.fields[IDX_REF_ID];
  }

  @Override
  public int getPos() {
    return Integer.parseInt(this.fields[IDX_POS]);
  }

  @Override
  public int getMappingQuality() {
    return Integer.parseInt(this.fields[IDX_MAPQ]);
  }

  @Override
  public Cigar getCigar() throws SAMException.InvalidCigarException {
    return generateCigar(this.fields[IDX_CIGAR]);
  }

  @Override
  public String getNextRefId() {
    return this.fields[IDX_RNEXT];
  }

  @Override
  public int getNextPos() {
    return Integer.parseInt(this.fields[IDX_PNEXT]);
  }

  @Override
  public int getTemplateLength() {
    return Integer.parseInt(this.fields[IDX_TLEN]);
  }

  @Override
  public String getSequence() {
    return this.fields[IDX_SEQ];
  }

  @Override
  public String getSeqQuality() {
    return this.fields[IDX_QUAL];
  }

  @Override
  public Tag[] getTags() {
    ArrayList<Tag> tags = new ArrayList<>();
    for(int i = IDX_TAGS; i < this.fields.length; i++)
      tags.add(new Tag(this.fields[i]));
    return tags.toArray(new Tag[0]);
  }

  public Cigar generateCigar(String desc) throws SAMException.InvalidCigarException {
    ArrayList<Integer> ls = new ArrayList<>();
    ArrayList<Byte> ts = new ArrayList<>();
    StringBuilder num = new StringBuilder();
    for(int i = 0 ; i < desc.length(); i++){
      char c = desc.charAt(i);
      if(Character.isDigit(c))
        num.append(c);
      else {
        byte b = Cigar.getByte(c);
        if(b == Cigar.INVALID_BYTE)
          throw new SAMException.InvalidCigarException(this.sam, desc, c+"");
        ts.add(b);
        ls.add(Integer.parseInt(num.toString()));
        num = new StringBuilder();
      }
    }
    return new Cigar(ls, ts);
  }
}
