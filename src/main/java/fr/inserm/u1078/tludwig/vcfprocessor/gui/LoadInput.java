package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-20
 */
public class LoadInput extends Input {

  public LoadInput(FileParameter param) {
    super(param);
  }

  @Override
  public void implement() {
    String[] ext = ((FileParameter) this.getParameter()).getExtensions();

    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    JTextField filename = new JTextField(30);
    filename.setEditable(false);
    JButton choose = new JButton(UIManager.getIcon("FileView.directoryIcon"));

    choose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(getLastDirectory());
        if (ext != null && ext.length > 0) {
          String desc = "Default (";
          for (String ex : ext)
            desc += " *." + ex;
          desc += " )";
          FileNameExtensionFilter ff = new FileNameExtensionFilter(desc, ext);
          fc.setFileFilter(ff);
        }
        int returnVal = fc.showOpenDialog(LoadInput.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
          setValue(fc.getSelectedFile().getAbsolutePath());
          filename.setText(getValue());
          setLastDirectory(getValue().substring(0, getValue().lastIndexOf(File.separator)));
        }
      }
    });
    this.add(filename);
    this.add(choose);

  }
}
