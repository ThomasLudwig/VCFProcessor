package fr.inserm.u1078.tludwig.vcfprocessor.utils;

public class IntegerEvaluator extends Evaluator{
  public static final char TYPE = 'i';
  public static final int MISSING_INTEGER = "MISSING_INTEGER".hashCode();


  public IntegerEvaluator(String expression, Operator operator, String key, String stringValue) throws EvaluatorParsingException {
    super(expression, operator, key);
    if(stringValue.equals(MISSING_VALUE))
      this.value = MISSING_INTEGER;
    else {
       try {
        this.value = Integer.parseInt(stringValue);
      } catch (NumberFormatException ignore) {
        throw new EvaluatorParsingException("Value ["+stringValue+"] is not parsable as an integer for expression ["+expression+"]");
      }
    }
  }

  public IntegerEvaluator(IntegerEvaluator eval){
    super(eval.expression, eval.operator, eval.key, eval.value);
  }

  @Override
  public int compare(String v) {
    int that = MISSING_INTEGER;
    if(!MISSING_STRING.equals(v))
      try{
        that = Integer.parseInt(v);
      } catch(NumberFormatException ignore){}

    return Integer.compare(that, (Integer)value);
  }
}
