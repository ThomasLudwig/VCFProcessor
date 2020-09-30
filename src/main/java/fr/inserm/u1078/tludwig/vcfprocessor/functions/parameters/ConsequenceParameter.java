/*
 */
package fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-09-03
 */
public class ConsequenceParameter extends EnumParameter {
  private int level = -1;

  public ConsequenceParameter() {
    super(Function.OPT_CSQ, VEPConsequence.getAllConsequences(), "vep.consequence", "Least severe consequence ["+String.join("|", VEPConsequence.getAllConsequences())+"]");
  }

  public String getConsequenceName() {
    return this.getStringValue();
  }

  public int getConsequenceLevel() {
    if(level == -1)
      level = VEPConsequence.getConsequenceLevel(this.getStringValue());
    return level;
  }
}
