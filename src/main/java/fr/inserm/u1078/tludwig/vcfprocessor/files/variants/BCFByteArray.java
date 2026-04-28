package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.ByteArray;

import javax.xml.crypto.Data;

/**
 * Encapsulation of a Byte Array and a pointer, with methods to parse data from a BCF File
 */
public class BCFByteArray extends ByteArray {

  /**
   * DataType that can be encoded
   */
  public enum DataType {EMPTY, INT8, INT16, INT32, FLOAT, CHAR};
  /*public static final int INT8 = 1;
  public static final int INT16 = 2;
  public static final int INT32 = 3;
  public static final int FLOAT = 5;
  public static final int CHAR = 7;*/
  public static final int MISSING_INT8 = 0x80;
  public static final int END_OF_VECTOR_INT8 = 0x81;
  public static final int RESERVED_INT8_2 = 0x82;
  public static final int RESERVED_INT8_3 = 0x83;
  public static final int RESERVED_INT8_4 = 0x84;
  public static final int RESERVED_INT8_5 = 0x85;
  public static final int RESERVED_INT8_6 = 0x86;
  public static final int RESERVED_INT8_7 = 0x87;
  public static final int MISSING_INT16 = 0x800;
  public static final int END_OF_VECTOR_INT16 = 0x8001;
  public static final int RESERVED_INT16_2 = 0x8002;
  public static final int RESERVED_INT16_3 = 0x8003;
  public static final int RESERVED_INT16_4 = 0x8004;
  public static final int RESERVED_INT16_5 = 0x8005;
  public static final int RESERVED_INT16_6 = 0x8006;
  public static final int RESERVED_INT16_7 = 0x8007;
  public static final int MISSING_INT32 = 0x8000000;
  public static final int END_OF_VECTOR_INT32 = 0x8000001;
  public static final int RESERVED_INT32_2 = 0x80000002;
  public static final int RESERVED_INT32_3 = 0x80000003;
  public static final int RESERVED_INT32_4 = 0x80000004;
  public static final int RESERVED_INT32_5 = 0x80000005;
  public static final int RESERVED_INT32_6 = 0x80000006;
  public static final int RESERVED_INT32_7 = 0x80000007;
  public static final int MISSING_FLOAT = 0x7F800001;
  public static final int END_OF_VECTOR_FLOAT = 0x7F800002;
  public static final int RESERVED_FLOAT_3 = 0x7F800003;
  public static final int RESERVED_FLOAT_4 = 0x7F800004;
  public static final int RESERVED_FLOAT_5 = 0x7F800005;
  public static final int RESERVED_FLOAT_6 = 0x7F800006;
  public static final int RESERVED_FLOAT_7 = 0x7F800007;
  public static final int MISSING_CHAR = 0x00;
  public static final int MISSING_TYPE_STRING = 0x07;

  /**
   * Constructor
   * @param array - the underlying Byte Array
   */
  public BCFByteArray(byte[] array){
    super(array);
  }

  /**
   * Reads a GT value (./. , 0/0, 0/1, 0|1,...)
   * @param ad - the type of int that encodes the genotype indices and the number of values (ploidy)
   * @return the GT values as in a VCF file
   * @throws BCFException if the buffer can't be read
   */
  public String readGTValues(ArrayDescription ad) throws BCFException {
    /*
     * A Genotype (GT) field is encoded in a typed integer vector (can be 8, 16, or even 32 bit if necessary)
     *   with the number of elements equal to the maximum ploidy among all samples at a site.
     * For one individual,
     *   each integer in the vector is organized as (allele+1) << 1 | phased
     *   where allele is set to −1 if the allele in GT is a dot ‘.’ (thus the higher bits are all 0).
     * The vector is padded with the END OF VECTOR values if the GT having fewer ploidy.
     * We note specifically that except for the END OF VECTOR byte, no other negative values are allowed in the GT array.
     * Examples:
     * 0/1                    in standard format (0 + 1) << 1 | 0 followed by (1 + 1) << 1 | 0     0x02 04
     * 0/1, 1/1, and 0/0      three samples encoded consecutively                                  0x02 04 04 04 02 02
     * 0 | 1                  (1+1) << 1 | 1 = 0x05 preceded by the standard first byte value      0x02 0x02 05
     * ./.                    where both alleles are missing                                       0x00 00
     * 0                      as a haploid it is represented by a single byte                      0x02
     * 1                      as a haploid it is represented by a single byte                      0x04
     * 0/1/2                  is tetraploid, with alleles                                          0x02 04 06
     * 0/1 | 2                is tetraploid with a single phased allele                            0x02 04 07
     * 0 and 0/1              pad out the final allele for the haploid individual                  0x02 81 02 04
     */

    //String[] sValues = new String[ad.getLength()];
    StringBuilder ret = new StringBuilder();
    for(int i = 0; i < ad.getLength(); i++) {
      final int v = readSignedInt(ad.getType());
      final String gt = readGTValue(v);
      Message.debug("Interpreted "+i+"["+v+"] as \""+gt+"\"");
      ret.append(gt);
    }
    return ret.substring(1);
  }

  private String readGTValue(int v) throws BCFException {
    if(v < 0) //end of vector byte 0x81 OR 0x80 01 OR 0x80 00 00 01
      return ""; //use to note 1 in a diploid context 1 -> 1_
    final char phase = v % 2 == 0 ? '/' : '|';
    final int c = v/2 - 1;
    final String g = c == -1 ? "." : ""+c;
    return phase + g;
  }

  public static int convert(int v){ return (((v/*&0xFE*/) >> 1)-1); }

  /**
   * Reads an integer of a priori unknown type
   * @return the integer
   * @throws BCFException if the buffer can't be read
   */
  public int readTypedInt() throws BCFException {
    DataType dt = readArrayDescription().getType();
    return switch (dt) {
      case INT8 -> readUInt8();
      case INT16 -> readLittleEndianUInt16();
      case INT32 -> readLittleEndianSInt32();
      default -> throw new BCFException.UnexpectedTypeException(dt);
    };
  }

  /**
   * Reads an array of integers of a priori unknown type and length
   * @return the array of integers
   * @throws BCFException if the buffer can't be read
   */
  public int[] readTypedInts(boolean missingasminus1) throws BCFException {
    ArrayDescription ad = readArrayDescription();
    int[] ints = new int[ad.getLength()];
    for(int i = 0 ; i < ad.getLength(); i++) {
      switch (ad.getType()) {
        case INT8:
          ints[i] = readUInt8();
          if(missingasminus1 && ints[i] == MISSING_INT8)
            ints[i] = -1;
          break;
        case INT16:
          ints[i] = readLittleEndianUInt16();
          if(missingasminus1 && ints[i] == MISSING_INT16)
            ints[i] = -1;
          break;
        case INT32:
          ints[i] = readLittleEndianSInt32();
          break;
      }
    }
    return ints;
  }

  /**
   * Checks if a value is a "missing value"
   * @param value - the value to check
   * @param type - the type to consider
   * @return true if it is a missing value
   */
  public static boolean isMissing(int value, DataType type) {
    return switch (type) {
      case INT8 -> value == MISSING_INT8;
      case INT16 -> value == MISSING_INT16;
      case INT32 -> value == MISSING_INT32;
      case FLOAT -> value == MISSING_FLOAT;
      case CHAR -> value == MISSING_CHAR;
      default ->
        //never reached
          false;
    };
  }

  /**
   * Checks if a value is an "end-of-vector value"
   * @param value - the value to check
   * @param type - the type to consider
   * @return true if it is a missing value
   */
  public static boolean isEndOfVector(int value, DataType type) {
    return switch (type) {
      case INT8 -> value == END_OF_VECTOR_INT8;
      case INT16 -> value == END_OF_VECTOR_INT16;
      case INT32 -> value == END_OF_VECTOR_INT32;
      case FLOAT -> value == END_OF_VECTOR_FLOAT;
      case CHAR -> value == MISSING_CHAR;
      default ->
        //never reached
          false;
    };
  }

  /**
   * Reads and array of numeric values
   * @param ad - the array description
   * @return a String representation of the values (comma separated)
   */
  private String readValuesAsString(ArrayDescription ad) {
    boolean empty = true;
    StringBuilder ret = new StringBuilder();
    for(int i = 0 ; i < ad.getLength(); i++){
      String v = readValueFromVectorAsString(ad.getType());
      ret.append(v);
      if(!v.isEmpty() && !",.".matches(v))
        empty = false;
    }
    if(empty)
      return ".";
    return ret.substring(1);
  }

  private String readValueFromVectorAsString(DataType type) {
      int v = switch(type) {
      case INT8 -> readUInt8();
      case INT16 -> readLittleEndianUInt16();
      case INT32,FLOAT -> readLittleEndianSInt32();
      default -> MISSING_INT32;
    };

    if(isMissing(v, type))
      return ".";
    if(isEndOfVector(v, type))
      return "";
    if(type == DataType.FLOAT)
      return ","+rawFloatToFloat(v);

    return ","+v;
  }

  /**
   * Reads and array of floats
   * @param l - the number of values to read
   * @return a String representation of the values (comma separated)
   */
  public String readFloatsAsString(int l) {
    String[] ret = new String[l];
    for(int i = 0 ; i < l; i++){
      int v = readLittleEndianSInt32();
      if(isMissing(v, DataType.FLOAT))
        ret[i] = ".";
      else
        ret[i] = rawFloatToFloat(v)+"";
    }
    String nRet = String.join(",", ret);
    if(nRet.replace(",","").replace(".","").isEmpty())
      return ".";
    return nRet;
  }

  /**
   * Reads a String
   * @param l the maximum number of chars in the String
   * @return the String (or "." if empty)
   */
  private String readCharsAsString(int l) {
    StringBuilder ret = new StringBuilder();
    for(int i = 0 ; i < l; i++) {
      byte b = readByte();
      if(!isMissing(b, DataType.CHAR))
        ret.append((char)b);
    }
    if(ret.isEmpty())
      return ".";
    return ret.toString();
  }

  /**
   * Decodes a 4 bytes float
   * @param rawFloat - integer representation of the 4 bytes
   * @return the float
   */
  private static float rawFloatToFloat(final int rawFloat) {
    return Float.intBitsToFloat(rawFloat);
  }

  /**
   * Skips values of a priori unknown type and length in the buffer
   * @throws BCFException if the buffer can't be read
   */
  public void skipValues() throws BCFException {
    ArrayDescription ad = readArrayDescription();
    if(ad.getLength() == 0)
      return;
    this.skip(ad, 1);
  }

  /**
   * Reads a list of values of a priori unknown type and length
   * @param ad - the type and number of values, can be null
   * @return values as in a VCF file
   * @throws BCFException if the buffer can't be read
   */
  public String readValues(ArrayDescription ad) throws BCFException {
    //if no add is provided, read one
    if(ad == null)
      ad = readArrayDescription();
    if(ad.getLength() == 0)
      return null;
    if(ad.getType() == DataType.CHAR)
      return readCharsAsString(ad.getLength());
    return readValuesAsString(ad);
  }

  /**
   * Reads a integer from the buffer
   * @param t - the Type of INT
   * @return - the value
   * @throws BCFException if the buffer can't be read
   */
  private int readInt(DataType t) throws BCFException {
    return switch (t) {
      case INT8 -> readUInt8();
      case INT16 -> readLittleEndianUInt16();
      case INT32 -> readLittleEndianSInt32();
      default -> throw new BCFException.UnexpectedTypeException(t);
    };
  }

  /**
   * Reads a sgined integer from the buffer
   * @param t - the Type of INT
   * @return - the value
   * @throws BCFException if the buffer can't be read
   */
  private int readSignedInt(DataType t) throws BCFException {
    return switch (t) {
      case INT8 -> readSInt8();
      case INT16 -> readLittleEndianSInt16();
      case INT32 -> readLittleEndianSInt32();
      default -> throw new BCFException.UnexpectedTypeException(t);
    };
  }

  /**
   * Skips an array of values in the buffer
   * @param ad - the type and number of values to skip
   * @param l - the number of arrays
   */
  public void skip(ArrayDescription ad, int l){
    int s;
    switch(ad.getType()){
      case INT8:
      case CHAR:
        s = 1;
        break;
      case INT16:
        s = 2;
        break;
      default :
        s = 4;
    }
    getPointerAndAdd(ad.getLength() * s * l);
  }

  /**
   * Reads a new ArrayDescription
   * @return the ArrayDescription of the array that follows in the buffer
   * @throws BCFException if the buffer can't be read
   */
  public ArrayDescription readArrayDescription() throws BCFException {
    return new ArrayDescription(this);
  }

  /**
   * Description of the array that follows in the buffer
   */
  public static class ArrayDescription {
    private final int length;
    private final DataType type;

    public ArrayDescription(BCFByteArray in) throws BCFException {
      byte b = in.readByte();
      this.length = readLength(in, b);
      this.type = readType(b);
    }

    /**
     * Gets the number of values in the array that follows in the buffer
     * @param b - the current byte in the buffer
     * @return the size of the following array
     * @throws BCFException if the buffer can't be read
     */
    private int readLength(BCFByteArray in, byte b) throws BCFException {
      int l =  (b & 0xF0) >> 4;
      if(l == 15)
        return in.readTypedInt();
      return l;
    }

    /**
     * Gets the type of values in the array that follows in the buffer
     * @param b - the current byte in the buffer
     * @return the type of value
     * @throws BCFException  if the buffer can't be read
     */
    private static DataType readType(byte b) throws BCFException {
      switch(b & 0x0F) {
        case 0 : return DataType.EMPTY;
        case 1 : return DataType.INT8;
        case 2 : return DataType.INT16;
        case 3 : return DataType.INT32;
        case 5 : return DataType.FLOAT;
        case 7 : return DataType.CHAR;
      }
      throw new BCFException.UnexpectedTypeException(b);
    }

    public int getLength() {
      return length;
    }

    public DataType getType() {
      return type;
    }


    @Override
    public String toString() {
      return length + " " + type;
    }
  }

}
