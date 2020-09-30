package fr.inserm.u1078.tludwig.vcfprocessor.documentation;

import fr.inserm.u1078.tludwig.maok.LineBuilder;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-05
 */
public class TableElement extends Element {

  private final String[][] table;
  private final boolean hasHeader;

  public TableElement(String[][] table, boolean hasHeader) {
    this.table = table;
    this.hasHeader = hasHeader;
  }

  @Override
  public String asHTML() {
    if (table.length == 0)
      return "";
    LineBuilder sb = new LineBuilder();
    sb.newLine("<table>");
      
    for (int i = 0; i < table.length; i++) {
      sb.append("<tr>");
      String[] list = table[i];
      for (String s : list){
        String tag = hasHeader && i == 0 ? "th" : "td";
        sb.append("<").append(tag).append(">").append(s).append("</").append(tag).append(">");
      }
      sb.newLine("</tr>");
    }
    sb.newLine("</table>");
    return sb.toString();
  }

  @Override
  public String asRST() {
    LineBuilder lb = new LineBuilder();
    return lb.rstTable(table, hasHeader).toString();
  }

  @Override
  public String asText() {
    LineBuilder sb = new LineBuilder();
    for(String[] line : LineBuilder.trimAndPad(table)){
      sb.append("| ").append(String.join(" | ", line)).newLine(" |");
    }
    return sb.minus(1);
  }
}
