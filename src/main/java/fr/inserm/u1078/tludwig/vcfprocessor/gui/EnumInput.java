package fr.inserm.u1078.tludwig.vcfprocessor.gui;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.EnumParameter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-20
 */
public class EnumInput extends Input {

  public EnumInput(EnumParameter param) {
    super(param);
  }

  @Override
  public void implement() {
    JComboBox box = new JComboBox();
    box.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        setValue(box.getSelectedItem().toString());
      }
    });
    for (String val : ((EnumParameter) getParameter()).getAllowedValues())
      box.addItem(val);
    box.setSelectedIndex(0);
    this.add(box);
  }

}
