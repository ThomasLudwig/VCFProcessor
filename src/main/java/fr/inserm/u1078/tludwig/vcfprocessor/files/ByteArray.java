package fr.inserm.u1078.tludwig.vcfprocessor.files;

import java.nio.BufferUnderflowException;
import java.util.concurrent.atomic.AtomicInteger;

public class ByteArray {

  /**
   * DataType that can be encoded
   */
  private final byte[] data;
  /**
   * The pointer location on the data
   */
  private final AtomicInteger pointer;

  /**
   * Constructor
   * @param array - the underlying Byte Array
   */
  public ByteArray(final byte[] array) {
    this.data = array;
    pointer = new AtomicInteger(0);
  }

  /**
   * Check for Underflow, returns current pointer values, then increment
   * @return the pointer value before increment
   */
  public int getPointerAndIncrement() {
    if(pointer.get() >= data.length)
      throw new BufferUnderflowException();
    return pointer.getAndIncrement();
  }

  /**
   * Gets the number bytes left in the buffer
   * @return the buffer's size minus the pointer's position
   */
  public int available() {
    return data.length - pointer.get();
  }

  /**
   * Check for Underflow, returns current pointer values, then add an integer
   * @param i - the pointer value to add
   * @return the value before adding
   */
  public int getPointerAndAdd(final int i){
    if(pointer.get() >= data.length)
      throw new BufferUnderflowException();
    return pointer.getAndAdd(i);
  }

  /**
   * Reads one Unsigned INT8 from the buffer
   * @return the value [0;2^8 - 1]
   */
  public short readUInt8() { //faster implementation than ava.nio.ByteBuffer.wrap().getInt()
    return (short)(0xff & data[getPointerAndIncrement()]);
  }

  /**
   * Reads one Unsigned INT16 from the buffer
   * @return the value [0;2^16 - 1]
   */
  public int readLittleEndianUInt16() { //faster implementation than ava.nio.ByteBuffer.wrap().getInt()
    //TDO short
    return (0xff & data[getPointerAndIncrement()]) + 256 * (0xff & data[getPointerAndIncrement()]);
  }

  /**
   * Reads one Unsigned INT24 from the buffer
   * @return the value
   */
  public int readLittleEndianUInt24() { //faster implementation than ava.nio.ByteBuffer.wrap().getInt()
    return
        (0xff & data[getPointerAndIncrement()]) +
        (0xff & data[getPointerAndIncrement()]) * 256 +
        (0xff & data[getPointerAndIncrement()]) * 65536;
  }

  /**
   * Reads one Signed INT8 from the buffer
   * @return the value [-2^7;2^7 - 1]
   */
  public short readSInt8() { //faster implementation than ava.nio.ByteBuffer.wrap().getInt()
    int i = readUInt8();
    return i < 128 ? (short)i : (short)(i-256);
  }

  /**
   * Reads one Signed INT16 from the buffer
   * @return the value [-2^15;2^15 - 1]
   */
  public short readLittleEndianSInt16() { //faster implementation than ava.nio.ByteBuffer.wrap().getInt()
    //TODO short ?
    int i = readLittleEndianUInt16();
    return i < 32768 ? (short)i : (short)(i-65536);
  }

  /**
   * Reads one INT32 from the buffer
   * @return the value [-2^31;2^31 - 1]
   */
  public int readLittleEndianSInt32() { //faster implementation than ava.nio.ByteBuffer.wrap().getInt()
    return
        (0xff & data[getPointerAndIncrement()]) +
            (0xff & data[getPointerAndIncrement()]) * 256 +
            (0xff & data[getPointerAndIncrement()]) * 65536 +
            (0xff & data[getPointerAndIncrement()]) * 16777216;
  }

  public int[] readLittleEndianSInts32(int length) { //faster implementation than ava.nio.ByteBuffer.wrap().getInt()
    int[] ret = new int[length];
    for(int i = 0 ; i < length; i++)
      ret[i] = readLittleEndianSInt32();
    return ret;
  }

  /**
   * Reads one INT32 from the Big Endian buffer
   * @return the value [-2^31;2^31 - 1]
   */
  public int readBigEndianSInt32() { //faster implementation than ava.nio.ByteBuffer.wrap().getInt()
    return
        (0xff & data[getPointerAndIncrement()]) * 16777216 +
        (0xff & data[getPointerAndIncrement()]) * 65536 +
        (0xff & data[getPointerAndIncrement()]) * 256 +
        (0xff & data[getPointerAndIncrement()]) ;
  }

  public int readLittleEndianUInt32() { //faster implementation than ava.nio.ByteBuffer.wrap().getInt()
    final long val = readLittleEndianSInt32() & 0xffffffffL;
    if ( val <= Integer.MAX_VALUE ) {
      return (int)val;
    }
    throw new RuntimeException(" Unsigned Value["+val+"] is large than max value ["+Integer.MAX_VALUE+"]");
  }

  /**
   * Reads one float value over 32-bits (see float32 IEEE 754-2008)
   * @return the value
   */
  public float readFloat32IEEE754v2008() {
    return Float.intBitsToFloat(readBigEndianSInt32());
  }

  /**
   * Reads one byte from the buffer
   * @return - the byte
   */
  public byte readByte() {
    return data[getPointerAndIncrement()];
  }

  /**
   * Reads n bytes from the buffer
   * @param n - the number of bytes to read
   * @return - an array containing the bytes
   */
  public byte[] readBytes(final int n){
    final int start = getPointerAndAdd(n);
    final byte[] bytes = new byte[n];
    System.arraycopy(data, start,bytes, 0, n);
    return bytes;
  }

  /**
   * Reads bytes as characters until a NUL (0) byte is reached
   * @return the characters as a String
   */
  public String readNullTerminatedString() {
    final StringBuilder ret = new StringBuilder();
    byte b;
    while((b = readByte()) != 0)
      ret.append((char)b);
    return ret.toString();
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

  public static String hex(byte[] bs){
    String ret = "";
    for(byte b : bs)
      ret += " "+hex(b);
    return ret;
  }

  public static String hex(byte b){
    String ret = Integer.toHexString(b & 0xff);
    if(ret.length() < 2)
      ret = "0"+ret;
    return ret;
  }
}
