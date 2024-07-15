package fr.inserm.u1078.tludwig.vcfprocessor;

import fr.inserm.u1078.tludwig.maok.tools.Message;

/**
 * Program entry point (deported from Main, to enforce "assert"
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-23
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */

public class Run {
  public static void main(String[] args) {
    try {
      Main.start(args);
    } catch(Exception e){
      Message.fatal("Unexcpected problem", e , true);
    }
  }
}
