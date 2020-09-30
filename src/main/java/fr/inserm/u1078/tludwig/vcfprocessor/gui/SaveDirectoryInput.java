package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.Parameter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-26
 */
public class SaveDirectoryInput extends Input {

  public SaveDirectoryInput(Parameter param) {
    super(param);
  }

  @Override
  public void implement() {
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    JTextField filename = new JTextField(30);
    filename.setEditable(false);
    JButton choose = new JButton(UIManager.getIcon("FileView.floppyDriveIcon"));
    choose.setToolTipText("Select a Directory");

    choose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {

        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(getLastDirectory());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);

        int returnVal = fc.showSaveDialog(SaveDirectoryInput.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          setValue(fc.getSelectedFile().getAbsolutePath());
          filename.setText(getValue());
          setLastDirectory(getValue());
          setLastDirectory(getValue().substring(0, getValue().lastIndexOf(File.separator)));
        }
      }
    });
    this.add(filename);
    this.add(choose);
  }

}
