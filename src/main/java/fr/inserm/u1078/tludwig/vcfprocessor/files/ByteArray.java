package fr.inserm.u1078.tludwig.vcfprocessor.files;

import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
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
  public ByteArray(byte[] array) {
    this.data = array;
    pointer = new AtomicInteger(0);
  }

  /**
   * Check for Underflow, returns current pointer values, then increment
   * @return the pointer value before increment
   */
  int getPointerAndIncrement() {
    if(pointer.get() >= data.length)
      throw new BufferUnderflowException();
    return pointer.getAndIncrement();
  }

  /**
   * Gets the number bytes left in the buffer
   * @return the buffer's size minus the pointer's position
   */
  int available() {
    return data.length - pointer.get();
  }

  /**
   * Check for Underflow, returns current pointer values, then add an integer
   * @param i - the pointer value to add
   * @return the value before adding
   */
  int getPointerAndAdd(int i){
    if(pointer.get() >= data.length)
      throw new BufferUnderflowException();
    return pointer.getAndAdd(i);
  }

  /**
   * Reads one Unsigned INT8 from the buffer
   * @return the value [0;2^8 - 1]
   */
  public short readUInt8() {
    return (short)(0xff & data[getPointerAndIncrement()]);
  }

  /**
   * Reads one Unsigned INT16 from the buffer
   * @return the value [0;2^16 - 1]
   */
  public int readUInt16() {
    //TDO short
    return (0xff & data[getPointerAndIncrement()]) + 256 * (0xff & data[getPointerAndIncrement()]);
  }

  /**
   * Reads one Unsigned INT24 from the buffer
   * @return the value
   */
  public int readUInt24() {
    return (0xff & data[getPointerAndIncrement()]) + 256 * ((0xff & data[getPointerAndIncrement()]) + 256 * (0xff & data[getPointerAndIncrement()]));
  }

  /**
   * Reads one Signed INT8 from the buffer
   * @return the value [-2^7;2^7 - 1]
   */
  public short readSInt8() {
    int i = readUInt8();
    return i < 128 ? (short)i : (short)(i-256);
  }

  /**
   * Reads one Signed INT16 from the buffer
   * @return the value [-2^15;2^15 - 1]
   */
  public int readSInt16() {
    //TODO short ?
    int i = readUInt16();
    return i < 32768 ? i : i-65536;
  }

  /**
   * Reads one INT32 from the buffer
   * @return the value [-2^31;2^31 - 1]
   */
  public int readInt32() {
    //TODO fasta to implements like int24
    return java.nio.ByteBuffer.wrap(readBytes(4)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
  }

  /**
   * Reads one float value over 32-bits (see float32 IEEE 754-2008)
   * @return the value
   */
  public float readFloat32IEEE754v2008() {
    //look for faster implementation
    return java.nio.ByteBuffer.wrap(readBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getFloat();
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
  public byte[] readBytes(int n){
    int start = getPointerAndAdd(n);
    byte[] bytes = new byte[n];
    System.arraycopy(data, start,bytes, 0, n);
    return bytes;
  }

  /**
   * Reads bytes as characters until a NUL (0) byte is reached
   * @return the characters as a String
   */
  public String readNullTerminatedString() {
    StringBuilder ret = new StringBuilder();
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
}
