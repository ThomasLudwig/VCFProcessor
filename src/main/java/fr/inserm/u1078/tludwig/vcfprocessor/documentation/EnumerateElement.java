package fr.inserm.u1078.tludwig.vcfprocessor.documentation;

import fr.inserm.u1078.tludwig.maok.LineBuilder;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-05
 */
public class EnumerateElement extends Element {

  private final String[] items;

  public EnumerateElement(String[] items) {
    this.items = items;
  }

  @Override
  public String asHTML() {
    if(items.length == 0)
      return "";
    LineBuilder sb = new LineBuilder();
    sb.newLine("<ol>");
    for (String s : items)
      sb.append("<li>").append(s).newLine("</li>");
    sb.newLine("</ol>");
    return sb.toString();
  }

  @Override
  public String asRST() {
    if(items.length == 0)
      return "";
    LineBuilder lb = new LineBuilder();
    return lb.rstEnum(items).toString();
  }

  @Override
  public String asText() {
    if(items.length == 0)
      return "";
    LineBuilder sb = new LineBuilder();
    for (int i = 0; i < items.length; i++)
      sb.append(i + 1).append(".").addSpace(items[i]).newLine();
    return sb.minus(1);//remove trailing \n
  }

}
