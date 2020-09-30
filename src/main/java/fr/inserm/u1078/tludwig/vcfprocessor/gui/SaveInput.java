package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputParameter;
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
public class SaveInput extends Input {

  public SaveInput(OutputParameter param) {
    super(param);
  }

  @Override
  public void implement() {
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    JTextField filename = new JTextField(30);
    filename.setEditable(false);
    JButton choose = new JButton(UIManager.getIcon("FileView.floppyDriveIcon"));
    String ext = ((OutputParameter) getParameter()).getExtension();
    choose.setToolTipText("File type : " + ext);

    choose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(getLastDirectory());
        if (ext != null && ext.length() > 0) {
          String desc = "Default ( *." + ext + " )";
          FileNameExtensionFilter ff = new FileNameExtensionFilter(desc, ext);
          fc.setFileFilter(ff);
        }

        int returnVal = fc.showSaveDialog(SaveInput.this);
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
