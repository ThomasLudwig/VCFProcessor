package fr.inserm.u1078.tludwig.vcfprocessor.documentation;

import fr.inserm.u1078.tludwig.maok.LineBuilder;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-05
 */
public class ItemizeElement extends Element {

  private final String[] items;

  public ItemizeElement(String[] items) {
    this.items = items;
  }

  @Override
  public String asHTML() {
    if(items.length == 0)
      return "";
    LineBuilder sb = new LineBuilder();
    sb.newLine("<ul>");
    for (String s : items)
      sb.append("<li>").append(s).newLine("</li>");
    sb.newLine("</ul>");
    return sb.toString();
  }

  @Override
  public String asRST() {
    if(items.length == 0)
      return "";
    LineBuilder lb = new LineBuilder();
    return lb.rstItemize(items).toString();
  }

  @Override
  public String asText() {
    if(items.length == 0)
      return "";
    LineBuilder sb = new LineBuilder();
    for (String item : items)
      sb.append("*").addSpace(item).newLine();
    return sb.minus(1);//remove trailing \n
  }

}
