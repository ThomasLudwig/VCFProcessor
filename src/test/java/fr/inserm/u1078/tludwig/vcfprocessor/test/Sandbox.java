package fr.inserm.u1078.tludwig.vcfprocessor.test;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.ByteArray;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2021-09-22
 * Checked for release on XXXX-XX-XX
 * Unit Test defined on   XXXX-XX-XX
 */
public class Sandbox {

  public static void main(String[] args) throws Exception {
    testDiv();
    /*int[] sizes = {1,2,3,4,5,6,7,8,9,15,16,17,20,63,64,65};
    for(int size : sizes)
      divide(size, 4);*/
   /* String exp = "((i:DP>12||i:GQ>30) && (s:TYPE=INDEL||s:TYPE=SNP)) || f:FRQ<0.01";
    BooleanParser bp = new BooleanParser(exp);
    HashMap<String, String> kv = new HashMap<>();
    kv.put("DP", "8");
    kv.put("GQ", "31");
    kv.put("TYPE", "SNP");
    kv.put("FRQ", "0.005");

    for(Evaluator evaluator : bp.getEvaluators())
      evaluator.evaluate(kv.get(evaluator.getKey()));

    System.out.println(bp.getFinalExpression());
    System.out.println(bp.evaluate());*/

    /*

    BooleanParser.BooleanExpressionEvaluator bee = new BooleanParser.BooleanExpressionEvaluator();
    String[] exps = {"false",
        "true",
        "false&&true",
        "false&&false",
        "true&&false",
        "true&&true",
        "false||true",
        "false||false",
        "true||false",
        "true||true",
        "0||(0&&0)",
        "0||(0&&1)",
        "0||(1&&0)",
        "0||(1&&1)",
        "1||(0&&0)",
        "1||(0&&1)",
        "1||(1&&0)",
        "1||(1&&1)",
        "0&&(0||0)",
        "0&&(0||1)",
        "0&&(1||0)",
        "0&&(1||1)",
        "1&&(0||0)",
        "1&&(0||1)",
        "1&&(1||0)",
        "1&&(1||1)",
        "(0&&0)||0",
        "(0&&0)||1",
        "(0&&1)||0",
        "(0&&1)||1",
        "(1&&0)||0",
        "(1&&0)||1",
        "(1&&1)||0",
        "(1&&1)||1",
        "(0||0)&&0",
        "(0||0)&&1",
        "(0||1)&&0",
        "(0||1)&&1",
        "(1||0)&&0",
        "(1||0)&&1",
        "(1||1)&&0",
        "(1||1)&&1",
        "0||0||0",
        "0||0||1",
        "0||1||0",
        "0||1||1",
        "1||0||0",
        "1||0||1",
        "1||1||0",
        "1||1||1",
        "0&&0&&0",
        "0&&0&&1",
        "0&&1&&0",
        "0&&1&&1",
        "1&&0&&0",
        "1&&0&&1",
        "1&&1&&0",
        "1&&1&&1",
        "0&&0||0",
        "0&&0||1",
        "0&&1||0",
        "0&&1||1",
        "1&&0||0",
        "1&&0||1",
        "1&&1||0",
        "1&&1||1",
        "0||0&&0",
        "0||0&&1",
        "0||1&&0",
        "0||1&&1",
        "1||0&&0",
        "1||0&&1",
        "1||1&&0",
        "1||1&&1",
        "0||0&&0",
        "0||0&&1",
        "0||1&&0",
        "0||1&&1",
        "1||0&&0",
        "1||0&&1",
        "1||1&&0",
        "1||1&&1",
    };

    for(String exp : exps){
      System.out.println("Evaluating ["+exp+"] -> " + bee.evaluate(exp));
    }*/

     /* byte[] bytes = {
          (byte)0x00, (byte)0x08, (byte)0x0f,
          (byte)0x10, (byte)0x18, (byte)0x1f,
          (byte)0x20, (byte)0x28, (byte)0x2f,
          (byte)0x30, (byte)0x38, (byte)0x3f,
          (byte)0x40, (byte)0x48, (byte)0x4f,
          (byte)0x50, (byte)0x58, (byte)0x5f,
          (byte)0x60, (byte)0x68, (byte)0x6f,
          (byte)0x70, (byte)0x78, (byte)0x7f,
          (byte)0x80, (byte)0x88, (byte)0x8f,
          (byte)0x90, (byte)0x98, (byte)0x9f,
          (byte)0xa0, (byte)0xa8, (byte)0xaf,
          (byte)0xb0, (byte)0xb8, (byte)0xbf,
          (byte)0xc0, (byte)0xc8, (byte)0xcf,
          (byte)0xd0, (byte)0xd8, (byte)0xdf,
          (byte)0xe0, (byte)0xe8, (byte)0xef,
          (byte)0xf0, (byte)0xf8, (byte)0xff};
      for(byte b : bytes){
        byte[] data = {b,b,b,b};
        System.out.println(hex(b)+" ["+getIntBuffered(data)+"] ["+getInt32(data)+"]");
      }*/

   /* byte[] data = {(byte)0x08, (byte)0xa7, (byte)0x28, (byte)0x00};
    Date start = new Date();
    for(long i = 0 ; i < 10*(long)Integer.MAX_VALUE; i++) {
      int v = getInt32(data);
      //int v = getIntBuffered(data);
    }
    Date end = new Date();
    int duration = (int)(end.getTime()-start.getTime());
    System.out.println("Duration "+duration);*/
    /*
    byte[][] datas = {
        {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01},
        {(byte)0x3f,(byte)0x80,(byte)0x00,(byte)0x00},
        {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00},
        {(byte)0x40,(byte)0x00,(byte)0x00,(byte)0x00},
        {(byte)0x41,(byte)0x88,(byte)0x00,(byte)0x00},
        {(byte)0x0f,(byte)0x0f,(byte)0x0f,(byte)0x0f},
        {(byte)0x7f,(byte)0xc0,(byte)0x00,(byte)0x00},
    };
    for(byte[] data : datas)
      decodeFloat(data);*/
    /*int[] bins = BAI.bedRegionToBinArray(14520, 14812);
    for(int bin : bins)
      System.out.println("Bin "+bin);*/
/*    for(int i = 0 ; i < 74; i++)
      System.out.println(i+" "+ Bin.getParentIndex(i));
      */
    //testList();
  }

  public static void testDiv() {
    for(int batch : new int[]{1,2,3,4,5,6,8,12}){
      for(int cpu : new int[]{1,2,4,6,8,12}){
        int parallelBatches = Math.min(batch, cpu);
        int parallelChromosomes = Math.max(1, cpu / parallelBatches);
        int thread = parallelChromosomes*parallelBatches;
        System.out.println("Batch["+batch+"] cpu["+cpu+"] : pB["+parallelBatches+"]*pC["+parallelChromosomes+"]=thread["+thread+"]");
      }
    }
  }

  public static void testList(){
    HashMap<String, List<Integer>> map = new HashMap<>();
    List<Integer> first = new ArrayList<>();
    first.add(1);
    first.add(2);
    first.add(3);
    map.put("toto", first);
    List<Integer> second = map.get("toto");
    second.add(4);
    second.add(5);
    second.add(6);
    map.put("toto", second);
    second.add(7);
    List<Integer> third = map.remove("toto");
    third.add(8);
    map.put("toto", third);
    third.add(9);

    List<Integer> fin = map.get("toto");
    for(int i : fin)
      System.out.println("list : "+i);
  }


  public static void decodeFloat(byte[] data){
    int i = getInt32(new byte[]{data[3],data[2],data[1],data[0]});
    float f = Float.intBitsToFloat(i);
    System.out.println(ByteArray.hex(data)+" ->  "+i+" -> "+f +" | "+ ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).getFloat());
  }

  public static int getIntBuffered(byte[] data) {
    return java.nio.ByteBuffer.wrap(data).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
  }

  public static int getInt32(byte[] data) {
    return
        (0xff & data[0]) +
            (0xff & data[1]) * 256 +
            (0xff & data[2]) * 65536 +
            (0xff & data[3]) * 16777216;
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
    return 0xffffff & java.nio.ByteBuffer.wrap(d).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
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
