package fr.inserm.u1078.tludwig.vcfprocessor.utils;

import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Println {
  public static final int DEFAULT_PRECISION = 4;

  private final List<Object> contents;
  public Println() {
    this.contents = new ArrayList<>();
  }

  public Println(Object... elements) {
    this();
    append(elements);
  }

  public static Println[] asLines(String... ss) {
    Println[] ret = new  Println[ss.length];
    for(int i = 0; i < ret.length; i++)
      ret[i] = new Println(ss[i]);
    return ret;
  }

  public static Println join(String sep, Object... elements) {
    Println out = new Println();
    boolean first = true;
    for (Object element : elements) {
      if(!first)
        out.append(sep);
      first = false;
      if(element instanceof Println)
        out.append(join(sep, element));
      else
        out.append(element);
    }
    return out;
  }

  public static Println join(String sep, String... elements) {
    return join(sep, (Object[]) elements);
  }

  public static Println getAllValues(NumberSeries series) {
    Println out = new Println(series.getName());
    for(Double v : series.getAllValues())
      out.append("\t").append(v);
    return out;
  }

  public static Println join(String sep, Collection elements) {
    //string
    //ojbects
    //Println
    //TODO implements
    return  join(sep, (Object[]) elements.toArray(new String[0]));
  }



  public Println append(Printable printable) {
    this.contents.add(printable.println());
    return this;
  }

  public Println append(Object element) {
    this.contents.add(element);
    return this;
  }

  public Println append(Object... elements) {
    for(Object element : elements)
      append(element);
    return this;
  }

  public Println append(Println println) {
    Collections.addAll(this.contents, println.contents);
    return this;
  }

  public Println append(boolean b) {
    this.contents.add(b);
    return this;
  }

  public Println append(int i) {
    this.contents.add(i);
    return this;
  }

  public Println append(char c) {
    this.contents.add(c);
    return this;
  }

  public Println append(float f) {
    this.contents.add(f);
    return this;
  }

  public Println append(double d) {
    this.contents.add(d);
    return this;
  }

  public Println append(short s) {
    this.contents.add(s);
    return this;
  }

  public Println append(long  l) {
    this.contents.add(l);
    return this;
  }

  public String print(int precision) {
    StringBuilder sb = new StringBuilder();
    for (Object element : contents)
      sb.append(asString(element, precision));
    return sb.toString();
  }

  public static String asString(Object element, int precision) {
    if (element == null)
      return "null";
    if (element instanceof Double)
      return asString((Double) element, precision);
    if (element instanceof Float)
      return asString((Float) element, precision);
    if (element instanceof String)
      return (String)element;
    return element.toString();
  }

  public static String asString(Float element, int precision) {
    return element == null ?  "null" : StringTools.scientificFormat(element, precision);
  }

  public static String asString(Double element, int precision) {
    return element == null ?  "null" : StringTools.scientificFormat(element, precision);
  }

  public boolean isEmpty() { return contents.isEmpty(); }
}
