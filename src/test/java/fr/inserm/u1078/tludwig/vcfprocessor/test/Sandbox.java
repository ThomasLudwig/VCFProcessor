package fr.inserm.u1078.tludwig.vcfprocessor.test;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;

import java.util.Date;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2021-09-22
 * Checked for release on XXXX-XX-XX
 * Unit Test defined on   XXXX-XX-XX
 */
public class Sandbox {

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

  public static void main(String[] args) throws Exception {
    //testByte();
    /*byte[] b1 = new byte[1];
    byte[] b2 = new byte[2];
    byte[] b3 = new byte[3];
    Random r = new Random(1560156016);
    for(int i = 0; i < 10; i++){
      System.out.println("******************");
      r.nextBytes(b1);
      String out = "1-bit  Hex"+hex(b1);
      System.out.println(out + "  : "+oreadSInt8(b1)+"|"+readSInt8(b1));

      r.nextBytes(b2);
      out = "2-bits Hex"+hex(b2);
      System.out.println(out + "  : "+oreadSInt16(b2)+"|"+readSInt16(b2));
      System.out.println(out + "  : "+oreadUInt16(b2)+"|"+readUInt16(b2));

      r.nextBytes(b3);
      out = "3-bits Hex"+hex(b3);
      System.out.println(out + "  : "+oreadUInt24(b3)+"|"+readUInt24(b3));

    }*/
    byte[] b2 = new byte[]{(byte)0xfe, 0x07};
    Date start = new Date();
    for(int i = 0 ; i < 1000000000; i++){
      int v = oreadSInt16(b2);
    }
    Date end = new Date();
    System.out.println("Duration = "+(end.getTime() - start.getTime()));
  }

  public static int oreadUInt16(byte[] data) {
    return 0xffff & java.nio.ByteBuffer.wrap(data).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort();
  }

  /**
   * Reads one Unsigned INT24 from the buffer
   * @return the value
   */
  public static int oreadUInt24(byte[] data) {
    byte[] d = {data[0], data[1], data[2], 0};
    return 0xffffff & java.nio.ByteBuffer.wrap(d).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();//TODO test
  }

  /**
   * Reads one Signed INT8 from the buffer
   * @return the value [-2^7;2^7 - 1]
   */
  public static short oreadSInt8(byte[] data) {
    byte[] d = {data[0], 0};
    return (short)(0xffff & java.nio.ByteBuffer.wrap(d).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort());
  }

  /**
   * Reads one Signed INT16 from the buffer
   * @return the value [-2^15;2^15 - 1]
   */
  public static int oreadSInt16(byte[] data) {
    return java.nio.ByteBuffer.wrap(data).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort();
  }

  public static int readUInt16(byte[] data) {
    return (0xFF & data[0]) + 256 * (0xFF & data[1]);
  }

  public static int readUInt24(byte[] data) {
    return (0xff & data[0]) + 256 * ((0xff & data[1]) + 256 * (0xff & data[2]));
  }

  /**
   * Reads one Signed INT8 from the buffer
   * @return the value [-2^7;2^7 - 1]
   */
  public static short readSInt8(byte[] data) {
    short i = (short)(0xff & data[0]);
    return i < 128 ? i : (short)(i - 256);
  }

  /**
   * Reads one Signed INT16 from the buffer
   * @return the value [-2^15;2^15 - 1]
   */
  public static int readSInt16(byte[] data) {
    int i = readUInt16(data);
    return i < 32768 ? i : i-65536;
  }



  public static void intmagic(){
    int[] is = {0,1,7,8,15,16,31,32,63,64,127,-1,-7,-8,-15,-16,-31,-32,-63,-64,-127,-128};
    for(int i : is){
      System.out.println(i +" -> "+Integer.toHexString(i));
    }
  }

  public static void testByte(){
    for(int i = 0 ; i < 256; i++){
      byte b = (byte)i;
      int v1 = b & 0x0f;
      int v2 = (b & 0xf0) >> 4;
      System.out.println(i+"["+b+"] -> "+v1+" | "+v2);
    }
  }

  public static void testCanonical(){
    String[] variants = {"1\t100\t.\tCT\tCA,CCT,C"};
    for(String variant : variants){
      Message.info("For ["+variant+"]");
      Canonical[] canonicals = Canonical.getCanonicals(variant);
      for(Canonical canonical : canonicals)
        Message.info("\t--> "+canonical.toString());
    }

  }

  private static void testAssert(int i){
    assert i > 0 : i+" is not positive !" ;
    System.out.println("Yeah ! "+i+"> 0");
  }

  public static void testClass() {
    checkClass(Sandbox.class);
    checkClass(String.class);
  }

  public static void checkClass(Class<?> c){
    if(c.equals(Sandbox.class))
      System.out.println("yes");
    else
      System.out.println("no");

  }
}
