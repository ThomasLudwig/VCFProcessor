package fr.inserm.u1078.tludwig.vcfprocessor.utils;

import java.util.ArrayList;
import java.util.List;

public class BooleanParser {
  private final String expression;
  //private final boolean[] values;
  private final Evaluator[] evaluators;
  private final String separatedExpression;

  private static final String LEFT = "<<<";
  private static final String RIGHT = ">>>";

  public BooleanParser(String expression) throws EvaluatorParsingException {
    this.expression = expression.replaceAll("\\s+", "");
    this.evaluators = tokenize(this.expression);
    this.separatedExpression = splitedString(this.expression);
  }

  private static Evaluator[] tokenize(String expression) throws EvaluatorParsingException {
    final String C = "¤";
    List<Evaluator> tokens = new ArrayList<>();
    String simple = expression
        .replace("(",C)
        .replace(")",C)
        .replace("&&",C)
        .replace("||",C)
        .replaceAll("("+C+")\\1+", "$1");
    for(String s : simple.split(C, 0))
      if(!s.isEmpty())
        tokens.add(new Evaluator(s));
    return tokens.toArray(new Evaluator[0]);
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

  public Evaluator[] getEvaluators() {
    return evaluators;
  }

  public String getFinalExpression() {
    String ret = this.separatedExpression;
    for (Evaluator evaluator : this.evaluators)
      ret = ret.replace(LEFT + evaluator.getExpression() + RIGHT, evaluator.getEvaluation() + "");
    return ret;
  }

  public boolean evaluate() {
    BooleanExpressionEvaluator bee = new BooleanExpressionEvaluator();
    return bee.evaluate(this.getFinalExpression());
  }

  @Override
  public String toString() {
    String exReplace = expression;
    StringBuilder vals = new StringBuilder();
    for (Evaluator evaluator : this.evaluators) {
      exReplace = exReplace.replace(evaluator.getExpression(), evaluator.getEvaluation() + "");
      vals.append("\n").append(evaluator.getExpression()).append(" : ").append(evaluator.getEvaluation());
    }

    String out = expression + " ==> " + exReplace;
    return out+vals+"\n\n"+getFinalExpression();
  }

  public static class BooleanExpressionEvaluator {

    public boolean evaluate(String input) {
      String previous = input.replace("0", "false").replace("1", "true");
      String current;
      while(!(current = simplify(previous)).equals(previous)) {
        previous = current;
        //System.out.println("Current : "+previous);
      }


      return "1".equals(current.replace("false","0").replace("true","1"));
    }

    public static final String T="true";
    public static final String F="false";
    public static final String A="&&";
    public static final String O="||";

    private static String simplify(String s){
      return s
          .replace(F+A+F, F)
          .replace(F+A+T, F)
          .replace(T+A+F, F)
          .replace(T+A+T, T)
          .replace(F+O+F, F)
          .replace(F+O+T, T)
          .replace(T+O+F, T)
          .replace(T+O+T, T)
          .replace("("+F+")", F)
          .replace("("+T+")", T);
    }
  }
}
