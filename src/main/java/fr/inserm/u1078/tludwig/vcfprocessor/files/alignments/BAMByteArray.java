package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.vcfprocessor.files.ByteArray;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Cigar;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Tag;

import java.util.ArrayList;

public class BAMByteArray extends ByteArray {

  /**
   * Constructor
   * @param array - the underlying Byte Array
   */
  public BAMByteArray(final byte[] array) {
    super(array);
  }

  private static final char[] SEQ = {'=','A','C','M','G','R','S','V','T','W','Y','H','K','D','B','N'};

  public String readSequence(int l) {
    if(l == 0)
      return "*";
    /*
        4-bit encoded read: ‘=ACMGRSVTWYHKDBN’→[0,15]. See Section4.2.3 uint8t[(l seq+1)/2]

        Sequence is encoded in 4-bit values,
        with adjacent bases packed into the same byte starting with the highest 4 bits first.
        When l seq is odd the bottom 4 bits of the last byte are undefined,
        but we recommend writing these as zero.
        The case-insensitive base codes ‘=ACMGRSVTWYHKDBN’ are mapped to [0,15] respectively
        with all other characters mapping to ‘N’ (value 15).
        Omitted sequence, represented in SAM as ‘*’, is represented by l seq being 0
        and seq and qual zero-length. Base qualities are stored as bytes in the range [0,93],
        without any +33 conversion to printable ASCII.
        When base qualities are omitted but the sequence is not,
        qual is filled with 0xFF bytes (to length l seq).
     */

    byte[] buffer = readBytes((l+1)/2);
    StringBuilder ret = new StringBuilder();

    for(int i = 0 ; i < buffer.length - 1; i++) { //process all complete bytes
      byte b = buffer[i];
      ret.append(SEQ[(b & 0xf0) >> 4]);
      ret.append(SEQ[b & 0x0f]);
    }
    ret.append(SEQ[(buffer[buffer.length - 1] & 0xf0) >> 4]);//process half of the last byte
    if(l % 2 == 0) //process second half if complete
      ret.append(SEQ[buffer[buffer.length - 1] & 0x0f]);

    return ret.toString();
  }

  public String readQual(int l) {
    if(l < 1)
      return "";
    StringBuilder ret = new StringBuilder();
    for(int i = 0 ; i < l; i++) {
      char c = (char)readByte();
      c += 33;
      ret.append(c);
    }
    return ret.toString();
  }

  private static final String COULD_NOT_PARSE = "CouldNotParseValue";

  public Tag readTag() {
    /*
      tag Two-charactertag char[2]
      val type Valuetype: AcCsSiIfZHB,seeSection4.2.4 char

      value Tagvalue (byval type)
    */
    String tag = readString(2);
    char type = (char)readByte();
    switch(type) {
      case 'A' : return new Tag(tag, type, ""+(char)readByte());
      case 'c' : return new Tag(tag, type, ""+readSInt8()); //int8
      case 'C' : return new Tag(tag, type, ""+readUInt8()); //uint8
      case 's' : return new Tag(tag, type, ""+readLittleEndianSInt16()); //int16
      case 'S' : return new Tag(tag, type, ""+readLittleEndianUInt16()); //uint16
      case 'i' : //return new BAMRecord.Tag(tag, type, ""); //int32
      case 'I' : return new Tag(tag, type, ""+readLittleEndianSInt32()); //uint32
      case 'f' : return new Tag(tag, type, ""+readFloat32IEEE754v2008()); //float32 IEEE 754-2008
      case 'H' : // byte[] as String
      case 'Z' : return new Tag(tag, type, readNullTerminatedString());
      case 'B' : return new Tag(tag, type, readArrayForTag());
    }
    return new Tag(tag, type, COULD_NOT_PARSE);
  }

  public String readArrayForTag(){
    char type = (char)readByte();
    int count = readLittleEndianUInt32();
    String[] ret = new String[count];
    switch(type){
      case 'c' : for(int i = 0 ; i < count; i++) ret[i] = ""+readSInt8(); break; //int8
      case 'C' : for(int i = 0 ; i < count; i++) ret[i] = ""+readUInt8(); break; //uint8
      case 's' : for(int i = 0 ; i < count; i++) ret[i] = ""+readLittleEndianSInt16(); break; //int16
      case 'S' : for(int i = 0 ; i < count; i++) ret[i] = ""+readLittleEndianUInt16(); break; //uint16
      case 'i' : for(int i = 0 ; i < count; i++) ret[i] = ""+readLittleEndianSInt32(); break; //int32
      case 'I' : for(int i = 0 ; i < count; i++) ret[i] = ""+readLittleEndianUInt32(); break; //uint32
      case 'f' : for(int i = 0 ; i < count; i++) ret[i] = ""+readFloat32IEEE754v2008(); break; //float32 IEEE 754-2008
      default : return COULD_NOT_PARSE;
    }
    return String.join(",", ret);
  }

  public Tag[] readTags() {
    //until the end of the alignment block
    ArrayList<Tag> tags = new ArrayList<>();
    while(this.available() > 0){
      tags.add(readTag());
    }
    return tags.toArray(new Tag[0]);
  }

  public Cigar readCigar(int nOp) {
    /*
     * cigar CIGAR:oplen<<4|op. ‘MIDNSHP=X’→‘012345678’ uint32t[ncigarop]
     * */
    int[] length = new int[nOp];
    byte[] types = new byte[nOp];
    for(int i = 0 ; i < nOp; i++){
      int v = readLittleEndianUInt32();
      types[i] = (byte)(v & 0x000f) ;
      length[i] = (v & 0xfff0) >> 4;
    }
    return new Cigar(length, types);
  }
}
