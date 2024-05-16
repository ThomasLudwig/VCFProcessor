package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 15 sept. 2016
 */
public class GeneCards extends Function {

  private final FileParameter geneList = new FileParameter(OPT_FILE, "genes.txt", "file listing genes");

  @Override
  public String getSummary() {
    return "Generates a script to retrieves GeneCards HTML pages for each gene in the given list.";
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary());
  }

  @Override
  public String getOutputExtension() {
    return OUT_TXT;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    println("#!/bin/bash");
    UniversalReader in = this.geneList.getReader();
    String line;
    while ((line = in.readLine()) != null)
      getHTML(line);
  }

  private void getHTML(String gene) {
    println("wget https://www.genecards.org/cgi-bin/carddisp.pl?gene=" + gene + " -O " + gene + ".html;");
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty(); //TODO implement
  }
}
