package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

public class Tag {
  private final String key;
  private final char type;
  private final String value; //TODO store has Object and convert on parse/print ?

  public Tag(final String key, final char type, final String value) {
    this.key = key;
    this.type = type;
    this.value = value;
  }

  public Tag(final String stringTag){
    String[] ktv = stringTag.split(":");
    this.key = ktv[0];
    this.type = ktv[1].charAt(0);
    this.value = ktv[2];
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public char getType() {
    return type;
  }

  @Override
  public String toString() {
    return key+":"+type+":"+value;
  }
}
