package fr.inserm.u1078.tludwig.vcfprocessor.documentation;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import java.util.ArrayList;

/**
 * A list of Description elements can will be easily formatted in txt rst html ...
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-02-17
 */
public class Description {
  public static final String HR = "\n----------\n";
  public static final String LEFT_ARROW = "←";
  public static final String RIGHT_ARROW = "→";
  public static final String LEFT_RIGHT_ARROW = "↔";

  private final ArrayList<Element> elements;

  public Description() {
    this.elements = new ArrayList<>();
  }

  public Description(String s) {
    this();
    this.addLine(s);
  }

  public static String italic(String text) { //RST : *text*, txt : , html <i></i>:
    return "<i>" + text.trim() + "</i>";
  }

  public static String bold(String text) { //RST : **text**, txt : , html <b></b>:
    return "<b>" + text.trim() + "</b>";
  }

  public static String code(String text) { //RST : inline code, txt : [], html :
    return "<tt>" + text.trim() + "</tt>";
  }

  public static String function(Class<?> clazz) {
    return "<ref>" + clazz.getSimpleName() + "</ref>";
  }

  public Description addLine(String s) {
    elements.add(new TextElement(s));
    return this;
  }
  
  public Description addLine(Description d){
    this.elements.addAll(d.elements);
    return this;
  }

  public Description addCodeBlock(String... s) {
    elements.add(new CodeBlockElement(s));
    return this;
  }

  public Description addItemize(String... s) {
    elements.add(new ItemizeElement(s));
    return this;
  }

  public Description addEnumerate(String... s) {
    elements.add(new EnumerateElement(s));
    return this;
  }

  public Description addColumns(String... s) {
    elements.add(new ColumnsElement(s));
    return this;
  }

  public Description addTable(String[][] t, boolean hasHeader) {
    elements.add(new TableElement(t, hasHeader));
    return this;
  }

  public Description addWarning(String... s) {
    elements.add(new WarningElement(s));
    return this;
  }
  
  public Description addNote(String... s) {
    elements.add(new NoteElement(s));
    return this;
  }
  
  public Description addDescription(Description d){
    this.elements.addAll(d.elements);
    return this;
  }

  public String asText() {
    LineBuilder sb = new LineBuilder();
    for (Element e : this.elements)
      sb.newLine(e.asText());
    return sb.length() > 0
            ? descriptionToPlainText(sb.minus(1)) //remove last \n
            : "";
  }

  public String asRST() {
    LineBuilder sb = new LineBuilder();
    for (Element e : this.elements)
      sb.newLine(e.asRST());
    return sb.length() > 0
            ? descriptionToRST(sb.minus(1)) //remove last \n
            : "";
  }

  public String asHTML() {
    LineBuilder sb = new LineBuilder("");
    for (Element e : this.elements)
      sb.append(descriptionToHTML(e.asHTML())).newLine("<br/>");
    return sb.toString();
  }

  public static String descriptionToPlainText(String s) {
    return s.replace("<i>", "")
            .replace("</i>", "")
            .replace("<b>", "")
            .replace("</b>", "")
            .replace("<tt>", "")
            .replace("</tt>", "")
            .replace("<ref>", "[")
            .replace("</ref>", "]")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(LEFT_ARROW, "<--")
            .replace(RIGHT_ARROW, "-->")
            .replace(LEFT_RIGHT_ARROW, "<->")
            ;
    
  }

  public static String descriptionToRST(String s) {
    return s.replace("<i>", "*")
            .replace("</i>", "*")
            .replace("<b>", "**")
            .replace("</b>", "**")
            .replace("<tt>", ":code:`")
            .replace("</tt>", "`")
            .replace("<ref>", "")
            .replace("</ref>", "_")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            ;
  }

  public static String descriptionToHTML(String s) {
    return s.replace("<ref>", "[<b>")
            .replace("</ref>", "</b>]");
  }
}
