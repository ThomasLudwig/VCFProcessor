package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.maok.tools.Message;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-08-05
 */
public abstract class ParallelVCFFilterFunction extends ParallelVCFFunction {
  
  private int inputLines = 0;
  private int outputLines = 0;

  @Override
  public final String[] processInputLine(String line) {
    String[] ret = this.processInputLineForFilter(line);
    if(ret != null)
      this.pushAnalysis(ret.length);
    return ret;
  }
  
  public abstract String[] processInputLineForFilter(String line);

  @Override
  public final boolean checkAndProcessAnalysis(Object analysis) {
    try {
      int add = (Integer)analysis;
      this.inputLines++;
      this.outputLines+=add;
      return true;
    } catch (Exception e) {
    }
    return false;
  }

  @Override
  public void end() {
    super.end();
    Message.info("Total input lines : "+inputLines + " | Total output lines : " + outputLines + " | Dropped lines : "+(inputLines-outputLines));
  }
}
