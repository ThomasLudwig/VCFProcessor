package fr.inserm.u1078.tludwig.vcfprocessor.utils;

import java.util.ArrayList;
import java.util.List;

public class BooleanParser {
  private final String expression;
  private final boolean[] values;
  private final String[] tokens;
  private final String separatedExpression;

  private static final String LEFT = "<<<";
  private static final String RIGHT = ">>>";

  public BooleanParser(String expression) {
    this.expression = expression.replaceAll("\\s+", "");
    this.tokens = tokenize(this.expression);
    this.values = new boolean[tokens.length];
    this.separatedExpression = splitedString(this.expression);
  }

  private static String[] tokenize(String expression) {
    final String C = "¤";
    List<String> tokens = new ArrayList<>();
    String simple = expression
        .replace("(",C)
        .replace(")",C)
        .replace("&&",C)
        .replace("||",C)
        .replaceAll("("+C+")\\1+", "$1");
    for(String s : simple.split(C, 0))
      if(!s.isEmpty())
        tokens.add(s);
    return tokens.toArray(new String[0]);
  }

  private static String splitedString(String string) {
    StringBuilder ret = new StringBuilder("(");
    String original = "("+string+")";

    char prev = original.charAt(0);
    for(int i = 1 ; i < original.length(); i++){
      char cur = original.charAt(i);
      StringBuilder now = new StringBuilder();
      if(isDelim(prev) && !isDelim(cur))
        now.append(LEFT);
      if(!isDelim(prev) && isDelim(cur))
        now.append(RIGHT);
      now.append(cur);

      ret.append(now);
      prev = cur;
    }
    return ret.toString();
  }

  private static boolean isDelim(char c) {
    switch(c) {
      case '(':
      case ')':
      case '&':
      case '|':
        return true;
      default:
        return false;
    }
  }

  public String[] getExpressions() {
    return tokens;
  }

  public void set(int i, boolean value) {
    this.values[i] = value;
  }

  public String getFinalExpression(){
    String ret = this.separatedExpression;
    for(int i = 0 ; i < this.tokens.length; i++)
      ret = ret.replace(LEFT+this.tokens[i]+RIGHT, this.values[i]+"");
    return ret;
  }

  @Override
  public String toString() {
    String exReplace = expression;
    StringBuilder vals = new StringBuilder();
    for(int i = 0 ; i < this.tokens.length; i++) {
      exReplace = exReplace.replace(tokens[i], "" + values[i]);
      vals.append("\n").append(tokens[i]).append(" : ").append(values[i]);
    }

    String out = expression + " ==> " + exReplace;
    return out+vals+"\n\n"+getFinalExpression();
  }
}
