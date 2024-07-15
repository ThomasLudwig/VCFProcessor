package fr.inserm.u1078.tludwig.vcfprocessor.files;

import java.nio.BufferUnderflowException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encapsulation of a Byte Array and a pointer, with methods to parse data from a BCF File
 */
public class BCFByteArray {

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
  public static final int MISSING_INT16 = 0x8000;
  public static final int MISSING_INT32 = 0x8000000;
  public static final int MISSING_FLOAT = 0x7F800001;
  public static final int MISSING_CHAR = 0x00;

  private final byte[] data;
  private final AtomicInteger pointer;

  /**
   * Constructor
   * @param array - the underlying Byte Array
   */
  public BCFByteArray(byte[] array){
    this.data = array;
    pointer = new AtomicInteger(0);
  }

  /**
   * Check for Underflow, returns current pointer values, then add an integer
   * @param i - the pointer value to add
   * @return the value before adding
   */
  private int getPointerAndAdd(int i){
    if(pointer.get() >= data.length)
      throw new BufferUnderflowException();
    return pointer.getAndAdd(i);
  }

  /**
   * Check for Underflow, returns current pointer values, then increment
   * @return the pointer value before increment
   */
  private int getPointerAndIncrement(){
    if(pointer.get() >= data.length)
      throw new BufferUnderflowException();
    return pointer.getAndIncrement();
  }

  /**
   * Reads a GT value (./. , 0/0, 0/1, 0|1,...)
   * @param ad - the type of int that encodes the genotype indices and the number of values (ploidy)
   * @return the GT values as in a VCF file
   * @throws BCFException if the buffer can't be read
   */
  public String readGTValues(ArrayDescription ad) throws BCFException {
    String[] sValues = new String[ad.getLength()];
    String phased = "/";
    boolean allMissing = true;
    for(int i = 0 ; i < ad.getLength(); i++) {
      int v = readInt(ad.getType());
      if( v == 0)
        sValues[i] = ".";
      else {
        allMissing = false;
        if(v % 2 == 1)
          phased = "|";
        sValues[i] = "" + (((v/*&0xFE*/) >> 1)-1);
      }
    }
    if(allMissing)
      return ".";
    return String.join(phased, sValues);
  }

  /**
   * Reads values form a Genotype Field
   * @param ad - the type and number of values
   * @return values as in a VCF file
   * @throws BCFException if the buffer can't be read
   */
  public String readValuesFromSampleField(ArrayDescription ad) throws BCFException {
    switch (ad.getType()){
      case INT8:
        return readInts8AsString(ad.getLength());
      case INT16:
        return readInts16AsString(ad.getLength());
      case INT32:
        return readInts32AsString(ad.getLength());
      case FLOAT:
        return readFloatsAsString(ad.getLength());
      case CHAR:
        return readCharsAsString(ad.getLength());
    }
    return null;
  }

  /**
   * Reads an integer of a priori unknown type
   * @return the integer
   * @throws BCFException if the buffer can't be read
   */
  public int readTypedInt() throws BCFException {
    switch (readArrayDescription().getType()) {
      case INT8:
        return readInt8();
      case INT16:
        return readInt16();
      case INT32:
        return readInt32();
      default:
        throw new BCFException("Unexpected type");
    }
  }

  /**
   * Reads an array of integers of a priori unknown type and length
   * @return the array of integers
   * @throws BCFException if the buffer can't be read
   */
  public int[] readTypedInts() throws BCFException {
    ArrayDescription ad = readArrayDescription();
    int[] ints = new int[ad.getLength()];
    for(int i = 0 ; i < ad.getLength(); i++) {
      switch (ad.getType()) {
        case INT8:
          ints[i] = readInt8();
          break;
        case INT16:
          ints[i] = readInt16();
          break;
        case INT32:
          ints[i] = readInt32();
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
    //TODO 2024-06-20 why are there 2 missing values for integers ?
    switch(type){
      case INT8:
        return value == MISSING_INT8 || (value-1) == MISSING_INT8;
      case INT16:
        return value == MISSING_INT16 || (value-1) == MISSING_INT16;
      case INT32:
        return value == MISSING_INT32 || (value-1) == MISSING_INT32;
      case FLOAT:
        return value == MISSING_FLOAT;
      case CHAR:
        return value == MISSING_CHAR;
    }
    //never reached
    return false;
  }

  /**
   * Reads and array of INT8 integers
   * @param l - the number of values to read
   * @return a String representation of the values (comma separated)
   */
  private String readInts8AsString(int l) {
    boolean empty = true;
    StringBuilder ret = new StringBuilder();
    for(int i = 0 ; i < l; i++){
      int v = readInt8();
      if(isMissing(v, DataType.INT8))
        ret.append(",.");
      else {
        ret.append(",").append(v);
        empty = false;
      }
    }
    if(empty)
      return ".";
    return ret.substring(1);
  }

  /**
   * Reads and array of INT16 integers
   * @param l - the number of values to read
   * @return a String representation of the values (comma separated)
   */
  private String readInts16AsString(int l) {
    boolean empty = true;
    StringBuilder ret = new StringBuilder();
    for(int i = 0 ; i < l; i++){
      int v = readInt16();
      if(isMissing(v, DataType.INT16))
        ret.append(",.");
      else {
        ret.append(",").append(v);
        empty = false;
      }
    }
    if(empty)
      return ".";
    return ret.substring(1);
  }

  /**
   * Reads and array of INT32 integers
   * @param l - the number of values to read
   * @return a String representation of the values (comma separated)
   */
  private String readInts32AsString(int l) {
    boolean empty = true;
    StringBuilder ret = new StringBuilder();
    for(int i = 0 ; i < l; i++){
      int v = readInt32();
      if(isMissing(v, DataType.INT32))
        ret.append(",.");
      else {
        ret.append(",").append(v);
        empty = false;
      }
    }
    if(empty)
      return ".";
    return ret.substring(1);
  }

  /**
   * Reads a String
   * @param l the maximum number of chars in the String
   * @return the String (or "." if empty)
   */
  private String readCharsAsString(int l) {
    StringBuilder ret = new StringBuilder();
    for(int i = 0 ; i < l; i++) {
      byte b = data[getPointerAndIncrement()];
      if(!isMissing(b, DataType.CHAR))
        ret.append((char)b);
    }
    if(ret.length() < 1)
      return ".";
    return ret.toString();
  }

  /**
   * Reads and array of floats
   * @param l - the number of values to read
   * @return a String representation of the values (comma separated)
   */
  public String readFloatsAsString(int l) {
    String[] ret = new String[l];
    for(int i = 0 ; i < l; i++){
      int v = readInt32();
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
   * @return the string representation of the values
   * @throws BCFException if the buffer can't be read
   */
  public String readValues() throws BCFException {
    ArrayDescription ad = readArrayDescription();
    if(ad.getLength() == 0)
      return null;
    switch(ad.getType()){
      case INT8:
        return readInts8AsString(ad.getLength());
      case INT16:
        return readInts16AsString(ad.getLength());
      case INT32:
        return readInts32AsString(ad.getLength());
      case FLOAT:
        return readFloatsAsString(ad.getLength());
      case CHAR:
        return readString(ad.getLength());
      default :
        return null;
    }
  }

  /**
   * Reads a integer from the buffer
   * @param t - the Type of INT
   * @return - the value
   * @throws BCFException if the buffer can't be read
   */
  private int readInt(DataType t) throws BCFException {
    switch (t) {
      case INT8:
        return readInt8();
      case INT16:
        return readInt16();
      case INT32:
        return readInt32();
      default:
        throw new BCFException("Integer type expected: found["+t+"]");
    }
  }

  /**
   * Reads one byte from the buffer
   * @return - the byte
   */
  public byte readByte() {
    return data[getPointerAndIncrement()];
  }

  public byte[] readBytes(int n){
    int start = getPointerAndAdd(n);
    byte[] bytes = new byte[n];
    System.arraycopy(data, start,bytes, 0, n);
    return bytes;
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
   * Reads one INT8 from the buffer
   * @return the value
   */
  public int readInt8() {
    return 0xFF & data[getPointerAndIncrement()];
  }

  /**
   * Reads one INT16 from the buffer
   * @return the value
   */
  public int readInt16() {
    return (0xFF & data[getPointerAndIncrement()]) + 256 * (0xFF & data[getPointerAndIncrement()]);
  }

  /**
   * Reads one INT24 from the buffer
   * @return the value
   */
  public int readInt24() {
    return (0xFF & data[getPointerAndIncrement()]) + 256 * ((0xFF & data[getPointerAndIncrement()]) + 256 * (0xFF & data[getPointerAndIncrement()]));
  }

  /**
   * Reads one INT32 from the buffer
   * @return the value
   */
  public int readInt32() {
    return java.nio.ByteBuffer.wrap(readBytes(4)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
    /*return data[getPointerAndIncrement()]
        + 256 * ((0xFF & data[getPointerAndIncrement()])
        + 256 * ((0xFF & data[getPointerAndIncrement()])
        + 256 * (0xFF & data[getPointerAndIncrement()])));*/
  }

  /**
   * Reads a String from the buffer
   * @param size the number of char
   * @return the string (or null if empty)
   */
  public String readString(final int size) {
    final byte[] bytes = new byte[size];
    System.arraycopy(data, getPointerAndAdd(size), bytes, 0, size);

    int goodLength = 0;
    for ( ; goodLength < bytes.length ; goodLength++ )
      if ( bytes[goodLength] == 0 ) break;

    return goodLength == 0 ? null : new String(bytes, 0, goodLength);
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
      throw new BCFException("Unkown Data type ["+(b & 0x0F)+"]");
    }

    public int getLength() {
      return length;
    }

    public DataType getType() {
      return type;
    }
  }
}
