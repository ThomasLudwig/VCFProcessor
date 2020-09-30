package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.Parameter;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-03
 */
class ParameterPanel extends JPanel {

  private final ArrayList<Input> inputs;
  private final Function function;

  public ParameterPanel(Function function, int width, int height) {
    super();
    this.inputs = new ArrayList<>();
    this.function = function;
    this.implement(width, height);
  }

  private void implement(int width, int height) {
    this.setLayout(new GridBagLayout());
    this.setPreferredSize(new Dimension(width, height));
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(5, 5, 5, 5);

    c.gridx = 0;
    c.gridy = 0;

    for (Parameter p : function.getParameters())
      if (!p.isOutput()) {
        Input input = p.getInputForm();
        this.inputs.add(input);
        c.gridx = 0;
        c.gridwidth = 1;
        this.add(new JLabel(p.getKey()), c);
        c.gridx += c.gridwidth;
        c.gridwidth = 2;
        this.add(input, c);
        c.gridx += c.gridwidth;
        c.gridwidth = 3;
        this.add(new JLabel(p.getDescription()), c);
        c.gridy++;
      }

    if (!Function.OUT_NONE.equals(this.function.getOutputExtension())) {
      Input result = new SaveInput(this.function.getOutfilename());
      inputs.add(result);
      c.gridx = 0;
      c.gridwidth = 1;
      this.add(new JLabel(Function.OPT_OUT), c);
      c.gridx += c.gridwidth;
      c.gridwidth = 2;
      this.add(result, c);
      c.gridx += c.gridwidth;
      c.gridwidth = 3;
      this.add(new JLabel("Result file"), c);
    }

    c.gridy++;
    JButton go = new JButton("Go !");
    go.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String message = "";
        ArrayList<String> args = new ArrayList<>();
        args.add(function.getFunctionName());
        boolean error = false;
        for (Input input : inputs) {
          String v = input.getValue();
          message += input.getKey() + " " + v + "\n";
          args.add(input.getKey());
          args.add(v);
          if (v == null)
            error = true;
        }
        if (error) {
          message = "At least one of the mandatory parameters is not set :\n" + message;
          JOptionPane.showMessageDialog(ParameterPanel.this, message, "Missing Parameters", JOptionPane.ERROR_MESSAGE);
        } else {
          message = "Would you like to launch the command with the following parameters ?\n\n" + function.getFunctionName() + "\n" + message;
          if (JOptionPane.showConfirmDialog(
                  ParameterPanel.this,
                  message,
                  "Launching...",
                  JOptionPane.OK_CANCEL_OPTION,
                  JOptionPane.QUESTION_MESSAGE,
                  new ImageIcon(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("fr/inserm/u1078/tludwig/vcfprocessor/gui/logo.vcfprocessor128.png")))) == JOptionPane.OK_OPTION) {
            ProcessWindow pw = new ProcessWindow(args.toArray(new String[args.size()]));
          }
        }
      }
    });
    c.gridx = 5;
    c.gridwidth = 1;
    this.add(go, c);
  }
}
