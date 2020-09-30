package fr.inserm.u1078.tludwig.vcfprocessor.documentation;

import fr.inserm.u1078.tludwig.maok.LineBuilder;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-05
 */
public class ColumnsElement extends Element {

  private final String[] columns;

  public ColumnsElement(String[] columns) {
    this.columns = columns;
  }

  @Override
  public String asHTML() {
    LineBuilder sb = new LineBuilder("<table><tr>");
    for (String s : columns)
      sb.append("<th>").append(s).append("</th>");
    sb.append("</tr></table>");
    return sb.toString();
  }

  @Override
  public String asRST() {
    LineBuilder lb = new LineBuilder();
    return lb.rstColumns(columns).toString();
  }

  @Override
  public String asText() {
    return "| "+String.join(" | ", columns)+" |";
  }

}
