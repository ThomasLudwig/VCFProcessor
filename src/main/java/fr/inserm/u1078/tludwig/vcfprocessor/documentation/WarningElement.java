package fr.inserm.u1078.tludwig.vcfprocessor.documentation;

import fr.inserm.u1078.tludwig.maok.LineBuilder;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-05
 */
public class WarningElement extends Element {
  private final String[] text;

  public static final String TEXT = "/!\\";
  public static final int OFF = 1;
  
  public WarningElement(String[] text) {
    this.text = text;
  }

  @Override
  public String asHTML() {
    return ("<font color=\""+HTML_NICE_RED+"\">"+TEXT+ " " + String.join("<br/>\n", text) + "</font>");
  }

  @Override
  public String asRST() {
    LineBuilder lb = new LineBuilder();
    return lb.rstWarning(text).toString();    
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
