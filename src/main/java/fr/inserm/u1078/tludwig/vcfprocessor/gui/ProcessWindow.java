package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.vcfprocessor.StartUpException;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.FunctionFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-09
 */
public class ProcessWindow extends JFrame {

  public static final int LOG_WIDTH = 800;
  public static final int LOG_HEIGHT = 600;

  private Function function;
  private String[] args;

  public ProcessWindow(String[] args) throws StartUpException {
    super();
    this.function = FunctionFactory.getFunction(args);
    this.args = args;
    if(this.function.start(args))
      implement();
  }

  private void implement() {
    JTextArea logBrowser = new JTextArea(function.getSummary());
    TextAreaOutputStream guiStream = new TextAreaOutputStream(logBrowser);
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setPreferredSize(new Dimension(LOG_WIDTH, LOG_HEIGHT));
    panel.add(new JScrollPane(logBrowser, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);
    panel.setBorder(BorderFactory.createTitledBorder("Log"));
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(panel, BorderLayout.CENTER);
    this.pack();
    try {
      this.setIconImage(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("fr/inserm/u1078/tludwig/vcfprocessor/gui/gear.png")));
    } catch (Exception e) {
      //Nothing
    }
    this.setVisible(true);
    this.function.setGuiStream(guiStream);
    FunctionSwingWorker fsw = new FunctionSwingWorker(function);
    fsw.execute();
  }
}
