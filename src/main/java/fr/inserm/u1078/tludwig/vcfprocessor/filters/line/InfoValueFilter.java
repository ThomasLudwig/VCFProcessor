package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.BooleanParser;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Evaluator;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.EvaluatorParsingException;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-09-27
 */
public class InfoValueFilter extends LineFilter {
  private final BooleanParser booleanParser;

  public InfoValueFilter(boolean keep, String query) {
    super(keep);
    BooleanParser tmpParser = null;
    try {
      tmpParser = new BooleanParser(query);
    } catch(EvaluatorParsingException epe) {
      Message.die("Could not parse "+this.getClass().getSimpleName()+" query ["+query+"]. "+epe.getMessage());
    }
    this.booleanParser =tmpParser;
  }


  @Override
  public boolean pass(VariantRecord record) {
    //here parallelisatio problem, the instance of Evaluators are updated/read concurrently be several threads, so we work on copies
    BooleanParser currentParser = new BooleanParser(booleanParser);

    for (Evaluator eval : currentParser.getEvaluators())
      eval.evaluate(record.getInfo(eval.getKey()));

    return currentParser.evaluate() == isKeep();
  }

  @Override
  public boolean leftColumnsOnly() {
    return true;
  }

  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove")+" variants based on the expression on INFO : "+booleanParser.getExpression();
  }
}
