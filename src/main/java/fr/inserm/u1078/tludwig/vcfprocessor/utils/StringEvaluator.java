package fr.inserm.u1078.tludwig.vcfprocessor.utils;

public class StringEvaluator extends Evaluator {
  public static final char TYPE = 's';

  public StringEvaluator(String expression, Operator operator, String key, String stringValue) throws EvaluatorParsingException {
    super(expression, operator, key);

    if(operator != Operator.DIFFERENT && operator != Operator.EQUALS) //no arithmetic operator for strings
      throw new EvaluatorParsingException("Invalid query ["+expression+"], Operator ["+operator+"] is not valid for a String value");

    if(stringValue.equals(MISSING_VALUE))
      this.value = MISSING_STRING;
    else
      this.value = stringValue;
  }

  public StringEvaluator(StringEvaluator eval){
    super(eval.expression, eval.operator, eval.key, eval.value);
  }

  @Override
  public int compare(String v) {
    return v.compareTo((String)value);
  }
}
