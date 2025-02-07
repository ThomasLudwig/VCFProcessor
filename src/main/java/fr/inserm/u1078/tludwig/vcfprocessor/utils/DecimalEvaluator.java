package fr.inserm.u1078.tludwig.vcfprocessor.utils;

public class DecimalEvaluator extends Evaluator {
  public static final char TYPE = 'f';
  public static final double MISSING_DECIMAL = IntegerEvaluator.MISSING_INTEGER+Math.PI;

  public DecimalEvaluator(String expression, Operator operator, String key, String stringValue) throws EvaluatorParsingException {
    super(expression, operator, key);
    if (stringValue.equals(MISSING_VALUE))
      this.value = MISSING_DECIMAL;
    else {
      try {
        this.value = Double.parseDouble(stringValue);
      } catch (NumberFormatException ignore) {
        throw new EvaluatorParsingException("Value [" + stringValue + "] is not parsable as decimal for expression [" + expression + "]");
      }
    }
  }

  public DecimalEvaluator(DecimalEvaluator eval){
    super(eval.expression, eval.operator, eval.key, eval.value);
  }

  @Override
  public int compare(String v) {
    double that = MISSING_DECIMAL;
    if(!MISSING_STRING.equals(v))
      try{
        that = Double.parseDouble(v);
      } catch(NumberFormatException ignore){}

    return Double.compare(that, (Double)value);
  }
}
