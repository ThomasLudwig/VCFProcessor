package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-07-31
 */
public class FunctionPanel extends JPanel {

  public FunctionPanel(Function function) {
    super();
    this.implement(function, MainWindow.WIN_WIDTH, MainWindow.WIN_HEIGHT);
  }

  private void implement(Function function, int pWidth, int pHeight) {
    this.setBorder(BorderFactory.createTitledBorder("Function " + function.getClass().getSimpleName()));
    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    TextPanel summary = new TextPanel(function.getSummary(), pWidth - 100, (int) (pHeight * .1));
    summary.setBorder(BorderFactory.createTitledBorder("Summary"));
    this.add(summary);
    //this.add(new JLabel(("sdfsdfqsmldf kqsmfd kqsdfmlq")));

    TextPanel description = new TextPanel(function.getDescription().asHTML(), pWidth - 100, (int) (pHeight * .2));
    description.setBorder(BorderFactory.createTitledBorder("Description"));
    this.add(description);

    ParameterPanel parameters = new ParameterPanel(function, pWidth - 100, (int) (pHeight * .4));
    parameters.setBorder(BorderFactory.createTitledBorder("Parameters"));
    this.add(parameters);
  }
}
