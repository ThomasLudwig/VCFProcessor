package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.GeneticsException;

public class GeneralDefinition {
  public enum Type{Integer, Float, Flag, Character, String}
  public static final String TAG_NUMBER_UNKNOWN = ".";
  public static final String TAG_NUMBER_ALTS = "A";
  public static final String TAG_NUMBER_ALLELES = "R";
  public static final String TAG_NUMBER_GENOTYPES = "G";
  public static final int VALUE_NUMBER_ALLELES = -9; //TODO use enum ?
  public static final int VALUE_NUMBER_ALTS = -8;
  public static final int VALUE_NUMBER_GENOTYPES = -7;
  public static final int VALUE_NUMBER_UNKNOWN = -6;

  private final String id;
  private final Type type;
  private final int number;
  private final String description;

  public GeneralDefinition(String id, Type type, int number, String description) {
    this.id = id;
    this.type = type;
    this.number = number;
    this.description = description;
  }

  public static GeneralDefinition parseLine(String line, String header) throws GeneticsException {
    if (!line.startsWith(header))
      throw new GeneticsException("Line is expected to start with '" + header + "' [" + line + "]");

    String id = null;
    String type = null;
    String number = null;
    String description = null;
    try {
      //TODO, in fact split by "," is dangerous, has description could contain several ,
      for (String split : line.substring(header.length(), line.length() - 1).split(",")) {
        String[] kv = split.split("=");
        switch (kv[0].toLowerCase()) {
          case "id":
            id = kv[1];
            break;
          case "type":
            type = kv[1];
            break;
          case "number":
            number = kv[1];
            break;
          case "description":
            description = removeQuote(kv[1]);
            break;
        }
      }

      if(id == null) throw new GeneticsException("Line does not contain an ID [" + line + "]");
      if(type == null) throw new GeneticsException("Line does not contain a Type [" + line + "]");
      if(number == null) throw new GeneticsException("Line does not contain a Number of values [" + line + "]");
      if(description == null) throw new GeneticsException("Line does not contain a Description [" + line + "]");
      if("flag".equalsIgnoreCase(type) && !"0".equals(number)) throw new GeneticsException("Mismatch in line ["+line+"] Type=Flag implies Number=0");
      if(!"flag".equalsIgnoreCase(type) && "0".equals(number)) throw new GeneticsException("Mismatch in line ["+line+"] Number=0 implies Type=Flag");

      return new GeneralDefinition(id, parseType(type), parseNumber(number), description);
    } catch(GeneticsException e) {
      throw new GeneticsException("Error while parsing line [" + line + "] id("+id+") number("+number+") type("+type+") description("+description+") " + e.getMessage(), e);
    }
  }

  public boolean isUnique() { return this.getNumber() == 1; }

  public static int parseNumber(String number) throws GeneticsException {
    try{
      return Integer.parseInt(number);
    } catch(NumberFormatException e) {
      return switch(number.toUpperCase()){
        case TAG_NUMBER_UNKNOWN -> VALUE_NUMBER_UNKNOWN;
        case TAG_NUMBER_GENOTYPES -> VALUE_NUMBER_GENOTYPES;
        case TAG_NUMBER_ALTS -> VALUE_NUMBER_ALTS;
        case TAG_NUMBER_ALLELES ->  VALUE_NUMBER_ALLELES;
        default  -> throw new GeneticsException("Type number of occurrences [" + number + "]");
      };
    }
  }

  public static String removeQuote(String s) {
    if(s == null) return null;
    String out = s;
    if(out.startsWith("\""))
      out = out.substring(1);
    if(out.endsWith("\""))
      out = out.substring(0, out.length()-1);
    return out;
  }

  public static Type parseType(String type) throws GeneticsException {
    return switch (type.toLowerCase()){
      case "integer" -> Type.Integer;
      case "float" -> Type.Float;
      case "flag" -> Type.Flag;
      case "character" -> Type.Character;
      case "string" -> Type.String;
      default -> throw new GeneticsException("Type unknown [" + type + "]");
    };
  }

  public static String asTag(int number) {
    if(number >= 0)
      return number+"";
    return switch (number) {
      case VALUE_NUMBER_ALLELES -> TAG_NUMBER_ALLELES;
      case VALUE_NUMBER_ALTS -> TAG_NUMBER_ALTS;
      case VALUE_NUMBER_GENOTYPES ->  TAG_NUMBER_GENOTYPES;
      default ->  TAG_NUMBER_UNKNOWN;
    };
  }

  public String getId() { return id; }

  public Type getType() { return type; }

  public int getNumber() { return number; }

  public String getDescription() { return description; }

}
