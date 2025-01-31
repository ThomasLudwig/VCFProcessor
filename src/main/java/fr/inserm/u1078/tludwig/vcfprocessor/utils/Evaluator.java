package fr.inserm.u1078.tludwig.vcfprocessor.utils;

public class Evaluator {
  public static final String OP_DIFFERENT = "!=";
  public static final String OP_GE = ">=";
  public static final String OP_LE = "<=";
  public static final String OP_EQUALS = "=";
  public static final String OP_GREATER = ">";
  public static final String OP_LESS = "<";
  public static final String[] OPERATORS = {OP_DIFFERENT, OP_GE, OP_LE, OP_EQUALS, OP_GREATER, OP_LESS};// order is important !


  public enum Type {i, f, s}
  public enum Operator {EQUALS, DIFFERENT, LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL}


  private final String expression;
  private final Type type;
  private final Operator operator;
  private final String key;
  private final String stringValue;
  private final int intValue;
  private final double floatValue;
  private boolean evaluation = false;

  public Evaluator(String expression) throws EvaluatorParsingException {
    this.expression = expression;
    if(expression.length() < 5)
      throw new EvaluatorParsingException("Invalid query ["+expression+"]");
    Type type = Type.valueOf(expression.substring(0, 1));
    String right = expression.substring(2);

    Operator operator = null;
    String key = null;
    String stringValue = null;

    for(String op : OPERATORS) {
      int idx = right.indexOf(op);
      if (idx > -1) {
        operator = Operator.valueOf(getOperatorName(op));
        if(type == Type.s && operator != Operator.DIFFERENT && operator != Operator.EQUALS) //no arithmetic operator for strings
          throw new EvaluatorParsingException("Invalid query ["+expression+"]");
        stringValue = right.substring(idx + op.length());
        key = right.substring(0, idx);
        break;
      }
    }
    if(operator == null)
      throw new EvaluatorParsingException("Invalid query ["+expression+"]");

    this.type = type;
    this.operator = operator;
    this.key = key;
    this.stringValue = stringValue;
    int intV = -1;
    double floatV = Double.NaN;
    if(type == Type.i)
      try {
        intV = Integer.parseInt(stringValue);
      } catch(NumberFormatException ignore){}
    if(type == Type.f)
      try {
        floatV = Double.parseDouble(stringValue);
      } catch(NumberFormatException ignore){}

    this.intValue = intV;
    this.floatValue = floatV;
  }

  public static String getOperatorName(String symbol){
    switch(symbol) {
      case OP_EQUALS:
        return Operator.EQUALS.name();
      case OP_DIFFERENT:
        return Operator.DIFFERENT.name();
      case OP_LESS:
        return Operator.LESS.name();
      case OP_LE:
        return Operator.LESS_OR_EQUAL.name();
      case OP_GREATER:
        return Operator.GREATER.name();
      case OP_GE:
        return Operator.GREATER_OR_EQUAL.name();
      default:
        return symbol;
    }
  }

  public void evaluate(String v) {
    this.evaluation = doEvaluate(v);
  }

  private boolean doEvaluate(String v) {
    switch(type) {
      case s:
        return evalString(v);
      case i:
        return evalInt(Integer.parseInt(v));
      case f:
        return evalDouble(Double.parseDouble(v));
    }
    return false;
  }

  private boolean evalString(String v){
    switch(operator){
      case EQUALS:
        return this.stringValue.equals(v);
      case DIFFERENT:
        return !this.stringValue.equals(v);
    }
    return false;
  }

  private boolean evalInt(int i){
    switch(operator){
      case EQUALS:
        return i == this.intValue;
      case DIFFERENT:
        return i != this.intValue;
      case LESS:
        return i < this.intValue;
      case LESS_OR_EQUAL:
        return i <= this.intValue;
      case GREATER:
        return i > this.intValue;
      case GREATER_OR_EQUAL:
        return i >= this.intValue;
    }
    return false;
  }

  private boolean evalDouble(double d){
    switch(operator){
      case EQUALS:
        return d == this.floatValue;
      case DIFFERENT:
        return d != this.floatValue;
      case LESS:
        return d < this.floatValue;
      case LESS_OR_EQUAL:
        return d <= this.floatValue;
      case GREATER:
        return d > this.floatValue;
      case GREATER_OR_EQUAL:
        return d >= this.floatValue;
    }
    return false;
  }

  public String getKey() { return key; }

  public String getExpression() { return this.expression; }

  public boolean getEvaluation() { return this.evaluation; }
}
