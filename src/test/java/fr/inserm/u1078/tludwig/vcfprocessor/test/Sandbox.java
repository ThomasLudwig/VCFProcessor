package fr.inserm.u1078.tludwig.vcfprocessor.test;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2021-09-22
 * Checked for release on XXXX-XX-XX
 * Unit Test defined on   XXXX-XX-XX
 */
public class Sandbox {

  public static void main(String[] args) {

    for(int i = 0 ; i < 10; i++)
      System.out.println("i = "+i);

    System.out.println("*********************");

    for(int i = 0 ; i < 10; ++i)
      System.out.println("i = "+i);

    /*
    ArrayList<String> list = new ArrayList<>();
    TreeMap<String, ArrayList<String>> map = new TreeMap<>();
    list.add("a");
    map.put("list", list);
    list.add("b");
    list.add("c");
    list.add("d");
    list.add("e");
    for(String key : map.navigableKeySet()){
      System.out.println("Key ["+key+"]");
      for(String element : map.get(key))
        System.out.println("   "+element);
    }

     */


    /*String s = "0/1";
    String[] f = s.split("/");
    System.out.println("f length "+f.length);
    for(String st : f)
      System.out.println("--> "+st);*/
    testClass();
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
