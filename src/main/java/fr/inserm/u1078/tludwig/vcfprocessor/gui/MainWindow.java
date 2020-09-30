package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.Main;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.FunctionFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-07-31
 */
public class MainWindow extends JFrame {

  public static final String TITLE = "VCFProcessor " + Main.getVersion();
  public static final int WIN_WIDTH = 1000;
  public static final int WIN_HEIGHT = 800;

  private final JPanel mainPanel;

  public MainWindow() {
    super();
    mainPanel = new JPanel();
    this.implement();
  }

  private void implement() {
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setTitle(TITLE);
    JMenuBar bar = new JMenuBar();
    //bar.add(new JMenu("Test Test 1 2 1 2"));
    JMenu filter = createMenu("Filter", Function.TYPE_VCF_FILTER);
    JMenu transform = createMenu("Transform", Function.TYPE_VCF_TRANSFORM);
    JMenu annotate = createMenu("Annotate", Function.TYPE_VCF_ANNOTATE);
    JMenu analyse = createMenu("Analyse", Function.TYPE_ANALYSIS);
    JMenu format = createMenu("Formatting", Function.TYPE_FORMATTING);
    /*JMenu sql = createMenu("SQL", Function.TYPE_SQL_INJECTOR);
    JMenu oneshot = createMenu("One Shot", Function.TYPE_ONE_SHOT);*/
    JMenu other = createMenu("Other", Function.TYPE_OTHER);
    JMenu graphs = createMenu("Graphics", Function.TYPE_GRAPHS);
    JMenu unknown = createMenu("Unknown", Function.TYPE_UNKNOWN);
    //this.getContentPane().add(mainPanel, BorderLayout.CENTER);

    if (filter.getItemCount() > 0)
      bar.add(filter);
    if (transform.getItemCount() > 0)
      bar.add(transform);
    if (annotate.getItemCount() > 0)
      bar.add(annotate);
    if (analyse.getItemCount() > 0)
      bar.add(analyse);
    if (format.getItemCount() > 0)
      bar.add(format);
    /*if (sql.getItemCount() > 0)
      bar.add(sql);
    if (oneshot.getItemCount() > 0)
      bar.add(oneshot);*/
    if (other.getItemCount() > 0)
      bar.add(other);
    if (graphs.getItemCount() > 0)
      bar.add(graphs);
    if (unknown.getItemCount() > 0)
      bar.add(unknown);

    this.getContentPane().setLayout(new BorderLayout());
    this.add(bar, BorderLayout.NORTH);

    mainPanel.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
    mainPanel.add(new WelcomePanel(TITLE));
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    this.pack();

    try {
      this.setIconImage(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("fr/inserm/u1078/tludwig/vcfprocessor/gui/logo.vcfprocessor128.png")));
    } catch (Exception e) {
      //Nothing
    }

    this.setVisible(true);
  }

  private JMenu createMenu(String title, String type) {
    JMenu menu = new JMenu(title);
    for (Class clazz : FunctionFactory.getFunctionsFromType(type)) {
      JMenuItem item = new JMenuItem(clazz.getSimpleName());
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          mainPanel.removeAll();
          try {
            mainPanel.add(new FunctionPanel((Function) clazz.getConstructor(new Class[]{}).newInstance(new Object[]{})));
            mainPanel.revalidate();
            mainPanel.repaint();
          } catch (Exception ex) {
            Message.error("Unable to create FunctionPanel for function ["+clazz.getSimpleName()+"]", ex);
          }
        }
      });
      menu.add(item);
    }
    return menu;
  }
}
