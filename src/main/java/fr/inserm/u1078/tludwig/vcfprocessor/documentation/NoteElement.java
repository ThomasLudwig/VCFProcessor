package fr.inserm.u1078.tludwig.vcfprocessor.documentation;

import fr.inserm.u1078.tludwig.maok.LineBuilder;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-04
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class NoteElement extends Element {
  private final String[] text;

  public static final String TEXT = "Note :";
  public static final int OFF = 1;  
  
  public NoteElement(String[] text) {
    this.text = text;
  }

  @Override
  public String asHTML() {
    return ("<font color=\""+HTML_NICE_BLUE+"\">" + TEXT + " " + String.join("<br/>", text) + "</font>");
  }

  @Override
  public String asRST() {
    LineBuilder lb = new LineBuilder();
    return lb.rstNote(text).toString();
  }
  
  @Override
  public String asText() {    
    if(text.length == 0)
      return "";    
    LineBuilder sb = new LineBuilder(TEXT);
    sb.addSpaces(OFF, text[0]).newLine();
    for(int i = 1; i < text.length; i++)
      sb.addSpaces(TEXT.length()+OFF, text[i]).newLine();    
    return sb.toString();
  }
}
