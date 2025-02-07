package fr.inserm.u1078.tludwig.vcfprocessor.utils;

public abstract class Evaluator {
  public static final String OP_DIFFERENT = "!=";
  public static final String OP_GE = ">=";
  public static final String OP_LE = "<=";
  public static final String OP_EQUALS = "=";
  public static final String OP_GREATER = ">";
  public static final String OP_LESS = "<";
  public static final String MISSING_VALUE = "missing_field";
  public static final String MISSING_STRING = ""; //66fef03312342926bc17b891fc36805a = md5(MISSING)
  public static final String[] OPERATORS = {OP_DIFFERENT, OP_GE, OP_LE, OP_EQUALS, OP_GREATER, OP_LESS};// order is important !
  public enum Operator {EQUALS, DIFFERENT, LESS, LESS_OR_EQUAL, GREATER, GREATER_OR_EQUAL}

  /**
   * The initial expression of the query (e.g. VARIANT:s=SNP DP:i>10 FREQ:f<0.01
   */
  final String expression;
  /**
   * The comparator operator (= != <= < >= >)
   */
  final Operator operator;
  /**
   * The key to look for (VARIANT, DP, FREQ,...)
   */
  final String key;

  /**
   * The current evaluation of the expression
   */
  boolean evaluation = false;

  Object value;

  Evaluator(String expression, Operator operator, String key) {
    this.expression = expression;
    this.operator = operator;
    this.key = key;
  }

  Evaluator(String expression, Operator operator, String key, Object value) {
    this.expression = expression;
    this.operator = operator;
    this.key = key;
    this.value = value;
  }

  public static Evaluator newEvaluator(Evaluator eval) {
    if(eval instanceof StringEvaluator)
      return new StringEvaluator((StringEvaluator) eval);
    if(eval instanceof IntegerEvaluator)
      return new IntegerEvaluator((IntegerEvaluator) eval);
    if(eval instanceof DecimalEvaluator)
      return new DecimalEvaluator((DecimalEvaluator) eval);
    return null;
  }

  public static Evaluator newEvaluator(String expression) throws EvaluatorParsingException {
    if(expression.length() < 5)
      throw new EvaluatorParsingException("Invalid query ["+expression+"]");
    char typeValue = expression.charAt(0);

    String right = expression.substring(2);
    Operator operator = null;
    String key = null;
    String stringValue = null;

    for(String op : OPERATORS) {
      int idx = right.indexOf(op);
      if (idx > -1) {
        operator = Operator.valueOf(getOperatorName(op));
        stringValue = right.substring(idx + op.length());
        key = right.substring(0, idx);
        break;
      }
    }

    if(operator == null)
      throw new EvaluatorParsingException("Invalid query ["+expression+"] could not find a valid operator");

    switch(typeValue) {
      case StringEvaluator.TYPE:
        return new StringEvaluator(expression, operator, key, stringValue);
      case IntegerEvaluator.TYPE:
        return new IntegerEvaluator(expression, operator, key, stringValue);
      case DecimalEvaluator.TYPE:
        return new DecimalEvaluator(expression, operator, key, stringValue);
      default :
        throw new EvaluatorParsingException("Type ["+typeValue+"] unknown for ["+expression+"]");
    }
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
    this.evaluation = false;
    if(v == null)
      v = MISSING_STRING;
    for(String s : v.split(",")) //if true for at least 1 value
      if(!this.evaluation)
        this.evaluation = doEvaluate(s);
  }

  public final boolean doEvaluate(String v) {
    return evaluate(compare(v), isMissing(v), operator);
  }

  public abstract int compare(String v);
  public final boolean isMissing(String v){
    return MISSING_STRING.equals(v);
  }

  public static boolean evaluate(int compare, boolean isMissing, Operator operator) {
    if(operator == Operator.EQUALS)
      return compare == 0;
    if(operator == Operator.DIFFERENT)
      return compare != 0;

    //This is never reached for strings
    if(isMissing)
      return false;

    switch(operator){
      case LESS:
        return compare < 0;
      case LESS_OR_EQUAL:
        return compare <= 0;
      case GREATER:
        return compare > 0;
      case GREATER_OR_EQUAL:
        return compare >= 0;
    }
    return false;
  }

  public String getKey() { return key; }
  public String getExpression() { return this.expression; }
  public boolean getEvaluation() { return this.evaluation; }

  public Object getValue() { return value; }

  public Operator getOperator() { return operator; }

  @Override
  public String toString() {
    return "Evaluator{" +
        "expression='" + expression + '\'' +
        ", operator=" + operator +
        ", key='" + key + '\'' +
        ", Value='" + value + '\'' +
        ", evaluation=" + evaluation +
        '}';
  }
}
