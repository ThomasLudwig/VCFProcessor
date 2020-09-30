package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import javax.swing.SwingWorker;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-09
 */
public class FunctionSwingWorker extends SwingWorker {

  private final Function function;

  public FunctionSwingWorker(Function function) {
    this.function = function;
  }

  @Override
  protected Object doInBackground() throws Exception {
    this.function.execute();
    return "ok";
  }

}
