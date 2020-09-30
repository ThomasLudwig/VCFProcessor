package fr.inserm.u1078.tludwig.vcfprocessor.documentation;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-05
 */
public abstract class Element { 

  public abstract String asHTML();
  
  public abstract String asRST();  //TODO LineBuilder asRST(LineBuilder org)
  
  public abstract String asText();  
  
  //TODO link element to cite Function and Filters http://lysine.univ-brest.fr/vcfprocessor/functions.html#getworstconsequence

}
