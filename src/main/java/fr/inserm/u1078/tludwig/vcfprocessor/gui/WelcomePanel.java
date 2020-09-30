package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-08-10
 */
public class WelcomePanel extends JPanel {

  public WelcomePanel(String title) {
    super();
    implement(title);
  }

  private void implement(String title) {
    JLabel welcome = new JLabel("Welcome to", JLabel.CENTER);
    Font font = welcome.getFont();
    welcome.setFont(new Font(font.getFontName(), Font.PLAIN, font.getSize() * 2));
    JLabel ltitle = new JLabel(title, JLabel.CENTER);
    ltitle.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize() * 3));
    JLabel logo = new JLabel(new ImageIcon(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("fr/inserm/u1078/tludwig/vcfprocessor/gui/logo.vcfprocessor494.png"))), JLabel.CENTER);
    JLabel mail = new JLabel("thomas.ludwig@inserm.fr");
    mail.setFont(new Font(Font.MONOSPACED, Font.PLAIN, font.getSize()));
    JLabel inserm = new JLabel("INSERM UMR1078 / Brest / FRANCE");
    inserm.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));

    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    this.setBackground(Color.WHITE);
    this.add(ltitle);
    this.add(new JLabel(" "));
    this.add(new JLabel(" "));
    this.add(new JLabel(" "));
    this.add(logo);
    this.add(new JLabel(" "));
    this.add(new JLabel(" "));
    this.add(new JLabel(" "));
    this.add(new JLabel(" "));
    this.add(mail);
    this.add(new JLabel(" "));
    this.add(inserm);
  }
}
