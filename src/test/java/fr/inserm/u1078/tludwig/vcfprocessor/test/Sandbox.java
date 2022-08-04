package fr.inserm.u1078.tludwig.vcfprocessor.test;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2021-09-22
 * Checked for release on XXXX-XX-XX
 * Unit Test defined on   XXXX-XX-XX
 */
public class Sandbox {

  public static final void main(String[] args){
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
  }
}
