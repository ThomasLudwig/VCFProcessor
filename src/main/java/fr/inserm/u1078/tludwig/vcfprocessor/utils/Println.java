package fr.inserm.u1078.tludwig.vcfprocessor.utils;

import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;

import java.util.ArrayList;
import java.util.Collection;
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
/*
  public static Println join(String sep, Object... elements) {
    Println out = new Println();
    boolean first = true;
    for (Object element : elements) {
      if(!first)
        out.append(sep);
      first = false;
      if(element instanceof Collection)
        out.append(join(sep, ((Collection<?>)element).toArray()));
      else if(element instanceof Object[])
        out.append(join(sep, (Object[])element));
      else
        out.append(element);
    }
    return out;
  }*/

  private static List<Object> flatElements(String sep, Object element, boolean first) {
    List<Object> out = new ArrayList<>();

    if (!first)
      out.add(sep);

    // Object array or multidimensional array (double[][] is Object[])
    if (element instanceof Object[] arr) {
      boolean f = true;
      for (Object o : arr) {
        out.addAll(flatElements(sep, o, f));
        f = false;
      }
    }
    // Collection
    else if (element instanceof Collection<?> col) {
      boolean f = true;
      for (Object o : col) {
        out.addAll(flatElements(sep, o, f));
        f = false;
      }
    }
    // Primitive arrays
    else if (element instanceof int[] arr) {
      boolean f = true;
      for (int v : arr)    { out.addAll(flatElements(sep, v, f)); f = false; }
    }
    else if (element instanceof double[] arr) {
      boolean f = true;
      for (double v : arr) { out.addAll(flatElements(sep, v, f)); f = false; }
    }
    else if (element instanceof float[] arr) {
      boolean f = true;
      for (float v : arr)  { out.addAll(flatElements(sep, v, f)); f = false; }
    }
    else if (element instanceof long[] arr) {
      boolean f = true;
      for (long v : arr)   { out.addAll(flatElements(sep, v, f)); f = false; }
    }
    else if (element instanceof short[] arr) {
      boolean f = true;
      for (short v : arr)   { out.addAll(flatElements(sep, v, f)); f = false; }
    }
    else if (element instanceof byte[] arr) {
      boolean f = true;
      for (byte v : arr)   { out.addAll(flatElements(sep, v, f)); f = false; }
    }
    else if (element instanceof char[] arr) {
      boolean f = true;
      for (char v : arr)   { out.addAll(flatElements(sep, v, f)); f = false; }
    }
    else if (element instanceof boolean[] arr) {
      boolean f = true;
      for (boolean v : arr)   { out.addAll(flatElements(sep, v, f)); f = false; }
    }
    else {
      out.add(element); // leaf value — Integer, Double, String, etc.
    }
    return out;
  }

  public static Println join(String sep, Object... elements) {
    Println out = new Println();
    boolean first = true;
    for (Object element : elements) {
      out.append(flatElements(sep, element, first));
      first = false;
    }
    return out;
  }

  public Println append(Object... elements) {
    for(Object element : elements)
      doAppend(element);
    return this;
  }

  private Println doAppend(Object element) {
    if(element == null)
      return this;
    if(element instanceof Collection)
      return doAppend((Collection<?>)element);
    if(element instanceof Object[])
      return doAppend((Object[])element);
    this.contents.add(element);
    return this;
  }

  private Println doAppend(Collection<?> elements) {
    for(Object element : elements)
      doAppend(element);
    return this;
  }

  private Println doAppend(Object[] elements) {
    for(Object element : elements)
      doAppend(element);
    return this;
  }

  public static Println getAllValues(NumberSeries series) {
    Println out = new Println(series.getName());
    for(Double v : series.getAllValues())
      out.append("\t", v);
    return out;
  }

  public String print(int precision) {
    StringBuilder sb = new StringBuilder();
    for (Object element : contents)
      sb.append(asString(element, precision));
    return sb.toString();
  }

  public String print() { return print(DEFAULT_PRECISION); }

  @Override
  public String toString() { return print(); }

  public static String asString(Object element, int precision) {
    if(element == null)
      return "null";
    if(element instanceof Double)
      return asString((Double) element, precision);
    if(element instanceof Float)
      return asString((Float) element, precision);
    if(element instanceof String)
      return (String)element;
    if(element instanceof Printable)
      return asString(((Printable)element).println(),precision);
    if(element instanceof Println)
      return ((Println)element).print(precision);
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
