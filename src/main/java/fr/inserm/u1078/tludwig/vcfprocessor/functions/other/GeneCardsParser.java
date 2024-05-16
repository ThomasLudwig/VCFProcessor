package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.FileTools;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 5 oct. 2016
 */
public class GeneCardsParser extends Function {

  private final FileParameter html = new FileParameter(OPT_FILE, "input.html", "input genecards HTML file");

  private final static String[] HEADERS = {"#Gene","GeneCards","Entrez Gene","UniProtKB/Swiss-Prot"};

  @Override
  public String getSummary() {
    return "Exports summary data from a genecards HTML files as an unformatted table";
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary())
            .addLine("All HTML markup are removed.")
            .addLine("The data are formatted as such :")
            .addColumns(HEADERS);
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    File f = new File(this.html.getFilename());
    String gene = f.getName().split(" ")[0];
    String outfile = gene + ".genecards.tsv";

    ArrayList<String> lines = FileTools.getFileLines(this.html.getFilename());

    String genecards = getGeneCards(lines);
    String entrez = getEntrez(lines);
    String uniprot = getUniProt(lines);

    PrintWriter out = getPrintWriter(outfile);
    out.println(String.join(T,HEADERS));
    out.println(gene + T + genecards + T + entrez + T + uniprot);
    out.close();
  }

  private static String getGeneCards(ArrayList<String> lines) {
    StringBuilder out = new StringBuilder();

    for (int i = 0; i < lines.size(); i++) {
      String head = lines.get(i);
      if (head.trim().startsWith("GeneCards Summary for")) {
        for (int j = i + 1; j < lines.size(); j++) {
          String line = lines.get(j);
          out.append(" ").append(line);
          if (line.contains("</p>"))
            break;
        }
        break;
      }
    }

    return trimHTML(out.toString());
  }

  private static String getEntrez(ArrayList<String> lines) {
    StringBuilder out = new StringBuilder();

    for (int i = 0; i < lines.size(); i++) {
      String head = lines.get(i);
      if (head.trim().startsWith("<h3>Entrez Gene Summary for")) {
        for (int j = i + 1; j < lines.size(); j++) {
          String line = lines.get(j);
          out.append(" ").append(line);
          if (line.contains("</p>"))
            break;
        }
        break;
      }
    }

    return trimHTML(out.toString());
  }

  private static String getUniProt(ArrayList<String> lines) {
    StringBuilder out = new StringBuilder();

    for (int i = 0; i < lines.size(); i++) {
      String head = lines.get(i);
      if (head.trim().startsWith("<h3>UniProtKB/Swiss-Prot for")) {
        for (int j = i + 1; j < lines.size(); j++) {
          String line = lines.get(j);
          out.append(" ").append(line);
          if (line.contains("</p>"))
            break;
        }
        break;
      }
    }

    return trimHTML(out.toString());
  }

  private static String trimHTML(String s) {
    //replace "<xxxxx>" with ""
    //replace "\\s+" with " "
    return s.replaceAll("\\<[^>]*>", "").replaceAll("\\s+", " ").trim();
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty(); //TODO implement
  }
}
