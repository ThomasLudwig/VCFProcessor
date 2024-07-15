package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-08-05
 */
public abstract class ParallelVCFVariantFilterFunction extends ParallelVCFVariantFunction<Integer> {
  private int inputLines = 0;
  private int outputLines = 0;

  @Override
  public final String[] processInputVariant(Variant variant) {
    String[] ret = this.processInputVariantForFilter(variant);
    if(ret != null)
      this.pushAnalysis(ret.length);
    return ret;
  }
  
  public abstract String[] processInputVariantForFilter(Variant variant);

  @SuppressWarnings("unused")
  @Override
  public final void processAnalysis(Integer add) {
    this.inputLines++;
    this.outputLines+=add;
  }

  @SuppressWarnings("unused")
  @Override
  public void end() {
    super.end();
    Message.info("Total input lines : "+inputLines + " | Total output lines : " + outputLines + " | Dropped lines : "+(inputLines-outputLines));
  }
}
