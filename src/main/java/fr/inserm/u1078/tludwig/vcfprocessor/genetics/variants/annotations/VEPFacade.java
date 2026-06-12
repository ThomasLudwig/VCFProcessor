package fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations;

import fr.inserm.u1078.tludwig.maok.tools.Message;

import java.util.*;

public interface VEPFacade {

  enum Category{VariantID, Consequence, Location, Meta, Frequency, Regulatory}
  enum Type{String, Integer, Float, Strings, Integers, Floats}

  class VEPField {
    private final String id;
    private final Category category;
    private final Type type;
    private final String description;

    public VEPField(String id, Category category, Type type, String description) {
      this.id = id;
      this.category = category;
      this.type = type;
      this.description = description;
    }
    public String getId() { return id; }
    public Category getCategory() { return category; }
    public Type getType() { return type; }
    public String getDescription() { return description; }
  }

  String getRawValue(VEPField field);

  static SortedSet<String> getAllStringValues(Collection<VEPAnnotation> annotations, VEPField field) {
    if(!checkType(field, Type.String, Type.Strings))
      return null;
    SortedSet<String> result = new TreeSet<>();
    for(VEPAnnotation annotation : annotations)
      if(field.type.equals(Type.String)) {
        String s = annotation.getStringValue(field);
        if(s != null)
          result.add(s);
      } else
        Collections.addAll(result, annotation.getStringArrayValue(field));

    return result;
  }

  static SortedSet<Integer> getAllIntegerValues(Collection<VEPAnnotation> annotations, VEPField field) {
    if(!checkType(field, Type.Integer, Type.Integers))
      return null;
    SortedSet<Integer> result = new TreeSet<>();
    for(VEPAnnotation annotation : annotations)
      if(field.type.equals(Type.Integer)) {
        Integer i = annotation.getIntegerValue(field);
        if(i != null)
          result.add(i);
      }  else
        for(Integer i : annotation.getIntegerArrayValue(field))
          result.add(i);
    return result;
  }

  static SortedSet<Double> getAllFloatValues(Collection<VEPAnnotation> annotations, VEPField field) {
    if(!checkType(field, Type.Float, Type.Floats))
      return null;
    SortedSet<Double> result = new TreeSet<>();
    for(VEPAnnotation annotation : annotations)
      if(annotation.hasField(field)) {
        if (field.type.equals(Type.Float)) {
          Double f = annotation.getFloatValue(field);
          if (f != null)
            result.add(f);
        } else
          for (Double f : annotation.getFloatArrayValue(field))
            result.add(f);
      }
    return result;
  }

  default String getStringValue(VEPField field){
    if(checkType(field, Type.String))
      return getRawValue(field);
    return null;
  }

  default Integer getIntegerValue(VEPField field){
    if(checkType(field, Type.Integer))
      try {
        return Integer.parseInt(getRawValue(field));
      } catch(NumberFormatException e) {
        return null;
      }
    return null;
  }

  default Double getFloatValue(VEPField field){
    if(checkType(field, Type.Float))
      try {
        return Double.parseDouble(getRawValue(field));
      } catch(NumberFormatException e) {
        return null;
      }
    return null;
  }

  default String[] getStringArrayValue(VEPField field){
    if(checkType(field, Type.Strings))
      try{
        return getRawValue(field).split("&", -1);
      } catch(Exception ignore){}
    return new String[0];
  }

  default int[] getIntegerArrayValue(VEPField field){
    if(checkType(field, Type.Integers))
      try{
        String[] f = getRawValue(field).split("&", -1);
        int[] result = new int[f.length];
        for(int i = 0; i < f.length; i++)
          result[i] = Integer.parseInt(f[i]);
        return result;
      } catch(Exception ignore){}
    return new int[0];
  }

  default double[] getFloatArrayValue(VEPField field){
    if(checkType(field, Type.Floats))
      try{
        String[] f = getRawValue(field).split("&", -1);
        double[] result = new double[f.length];
        for(int i = 0; i < f.length; i++)
          result[i] = Double.parseDouble(f[i]);
        return result;
      } catch(Exception ignore){}
    return new double[0];
  }

  static boolean checkType(VEPField field, Type type) {
    if(field.type.equals(type))
      return true;
    Message.warning("Trying to read ["+field.type+"] as a ["+type+"] for Field ["+field.id+"]");
    throw new RuntimeException();
    //return false;
  }

  static boolean checkType(VEPField field, Type type, Type types) {
    if(field.type.equals(type) || field.type.equals(types))
      return true;
    Message.warning("Trying to read ["+field.type+"] as a ["+type+" or "+types+"] for Field ["+field.id+"]");
    return false;
  }
}

