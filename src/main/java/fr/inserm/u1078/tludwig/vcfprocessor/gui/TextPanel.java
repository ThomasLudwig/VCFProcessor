package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import java.awt.Dimension;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-03
 */
class TextPanel extends JPanel {

  public TextPanel(String text, int w, int h) {
    super();
    this.implement(text, w, h);
  }

  private void implement(String text, int w, int h) {
    JEditorPane jep = new JEditorPane("text/html", "<html>" + text + "</html>");
    jep.setEditable(false);
    jep.setPreferredSize(new Dimension(w, h));

    this.add(new JScrollPane(jep, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
  }
}
