package fr.inserm.u1078.tludwig.vcfprocessor.functions.format;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PositiveIntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.HashMap;

/**
 * Converts a TSV to an HTML
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2018-06-07
 * Checked for release on 2020-08-20
 * Unit Test defined on 2020-20-20
 */
public class TSV2HTML extends Function {
  //TODO parallelize
  private final TSVFileParameter filename = new TSVFileParameter(OPT_FILE, "table.tsv", "the input TSV File");
  private final PositiveIntegerParameter link = new PositiveIntegerParameter(OPT_LNK, "put link in header, starting at column INDEX (counting from 0)");//TODO less here, more in desc
  private final StringParameter title = new StringParameter(OPT_TITLE, "MyTitle", "title of the result HTML page");

  @Override
  public String getSummary() {
    return "Converts a TSV to a HTML";
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary());
  }

  @Override
  public String getOutputExtension() {
    return OUT_HTML;
  }

  @SuppressWarnings({"unused"})
  @Override
  public void executeFunction() throws Exception {
    int fixed = 2;
    if (link.getIntegerValue() > 4)
      fixed = 5;

    try(UniversalReader in = this.filename.getReader()) {
      HashMap<Integer, String> headers = new HashMap<>();

      println("<!doctype html>");
      println("<html>");
      println("<head>");
      println("<title>" + this.title + "</title>");
      println("<link rel=\"icon\" type=\"image/png\" href=\"https://lysine.univ-brest.fr/VCFReporter/vcfreporter.logo.16x16.png\" sizes=\"16x16\"/>"
              + "<link rel=\"icon\" type=\"image/png\" href=\"https://lysine.univ-brest.fr/VCFReporter/vcfreporter.logo.32x32.png\" sizes=\"32x32\"/>"
              + "<link rel=\"icon\" type=\"image/png\" href=\"https://lysine.univ-brest.fr/VCFReporter/vcfreporter.logo.1131x1131.png\" sizes=\"1131x1131\"/>");

      println("<link href=\"https://fonts.googleapis.com/css?family=Lato|Comfortaa|Dynalight|Playball|Share|Space+Mono|Ubuntu\" rel=\"stylesheet\">");
      println("<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables.net/1.10.19/css/jquery.dataTables.min.css\" />");
      println("<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables.net/fixedcolumns/3.2.6/css/fixedColumns.dataTables.min.css\" />");
      println("<link rel=\"stylesheet\" type=\"text/css\" href=\"https://lysine.univ-brest.fr/VCFReporter/vcfreporter.css\"/>");

      println("<script src=\"https://code.jquery.com/jquery-3.3.1.js\"></script>");
      println("<script src=\"https://cdn.datatables.net/1.10.19/js/jquery.dataTables.min.js\"></script>");
      println("<script src=\"https://cdn.datatables.net/fixedcolumns/3.2.6/js/dataTables.fixedColumns.min.js\"></script>");
      println("<script type=\"text/javascript\" src=\"https://lysine.univ-brest.fr/VCFReporter/vcfreporter.js\"></script>");
      println("<script type=\"text/javascript\" src=\"https://lysine.univ-brest.fr/VCFReporter/table" + fixed + ".js\"></script>");

      println("</head>");
      println("<body>");
      println("<header><div id=\"logo\"><img src=\"https://lysine.univ-brest.fr/VCFReporter/vcfreporter.logo.png\" height=\"100px\"/></div><div id=\"title\"><h1>" + title + "</h1></div></header>");
      println("<div class=\"wrapper\">");
      println("<div class=\"scroller\">");
      println("<table id=\"scrolltable\" class=\"stripe\" style=\"width:100%\">");
      println("<thead>");
      String sep = "th";
      String line;
      while ((line = in.readLine()) != null) {
        StringBuilder out = new StringBuilder("<tr>");
        int i = -1;
        for (String col : line.split(T, -1)) {
          i++;
          if ("th".equals(sep)) {
            if (i < link.getIntegerValue())
              headers.put(i, col.toLowerCase().replaceAll("[^a-z0-9]", ""));
            else
              headers.put(i, "genotype");
            out.append("<").append(sep).append(" class=\"").append(headers.get(i)).append("\">");
            if (i >= link.getIntegerValue())
              out.append("<a href=\"").append(col).append(".html\">").append(col).append("</a>");
            else
              out.append(col);
            out.append("</").append(sep).append(">");
          } else
            out.append("<").append(sep).append(" class=\"").append(headers.get(i)).append("\">").append(col).append("</").append(sep).append(">");
        }

        out.append("</tr>");
        println(out.toString());
        if ("th".equals(sep)) {
          println("</thead>");
          println("<tbody>");
        }
        sep = "td";
      }
      println("</tbody>");
      println("</table>");
      println("</div>");
      println("</div>");
      println("<div w3-include-html=\"https://lysine.univ-brest.fr/VCFReporter/footer.html\" id=\"pagefooter\" class=\"pagefooter\"></div>");
      println("<script>includeHTML();</script>");
      println("</body>");
      println("</html>");
    }
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileAnalysis();
    scr.addAnonymousFilename("file", "file");
    scr.addAnonymousValue("link", "73");
    scr.addAnonymousValue("title", "TestingScript");
    return new TestingScript[]{scr};
  }
}
