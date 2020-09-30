package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.Parameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ParameterException;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-20
 */
public class DefaultInput extends Input {

  public DefaultInput(Parameter param) {
    super(param);
  }

  private boolean validate(String text) {
    try {
      this.getParameter().parseParameter(text);
    } catch (ParameterException e) {
      return false;
    }
    return true;
  }

  @Override
  public void implement() {
    JTextField t = new JTextField();
    t.setToolTipText("<html><b>Allowed Values</b> :<br/>" + getParameter().showAllowedValues() + "</html>");
    Color c = t.getBackground();
    t.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        this.update(t.getText());
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        this.update(t.getText());
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        this.update(t.getText());
      }

      private void update(String text) {
        if (validate(text)) {
          setValue(text);
          t.setBackground(c);
        } else {
          t.setBackground(Color.red);
          setValue(null);
        }
      }
    });
    this.add(t);
    t.setPreferredSize(new Dimension(200, 50));
  }
}
