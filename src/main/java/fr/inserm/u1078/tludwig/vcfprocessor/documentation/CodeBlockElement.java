package fr.inserm.u1078.tludwig.vcfprocessor.documentation;

import fr.inserm.u1078.tludwig.maok.LineBuilder;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-05
 */
public class CodeBlockElement extends Element {

  private final String[] lines;

  public CodeBlockElement(String[] lines) {
    this.lines = lines;
  }

  @Override
  public String asHTML() {
    LineBuilder sb = new LineBuilder();
    sb.newLine("<pre>");
    sb.newLine(String.join("\n", lines));
    sb.newLine("</pre>");
    return sb.toString();
  }

  @Override
  public String asRST() {
    LineBuilder lb = new LineBuilder();
    return lb.rstCode("bash", lines).toString(); //TODO fail here
  }

  @Override
  public String asText() {
    return String.join("\n", lines);
  }

}
