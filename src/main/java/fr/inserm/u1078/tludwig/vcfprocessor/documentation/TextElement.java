package fr.inserm.u1078.tludwig.vcfprocessor.documentation;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-05
 */
public class TextElement extends Element {
  
  private final String text;

  public TextElement(String text) {
    this.text = text;
  }  

  @Override
  public String asHTML() {
    return text;
  }

  @Override
  public String asRST() {
    return "| " + text;
  }

  @Override
  public String asText() {
    return text;
  }

}
