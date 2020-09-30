package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BooleanParameter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-20
 */
public class BooleanInput extends Input {

  public BooleanInput(BooleanParameter param) {
    super(param);
  }

  @Override
  public void implement() {
    JCheckBox cb = new JCheckBox("true");
    cb.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        setValue("" + cb.isSelected());
      }
    });
    cb.setSelected(true);
    this.add(cb);
  }

}
