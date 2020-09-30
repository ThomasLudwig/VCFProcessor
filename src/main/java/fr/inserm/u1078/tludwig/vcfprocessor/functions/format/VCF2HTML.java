package fr.inserm.u1078.tludwig.vcfprocessor.functions.format;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Generates an HTML legible file for the given VCF file
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-03-15
 * Checked for release on 2020-08-20
 * Unit Test defined on 2020-20-20
 */
public class VCF2HTML extends ParallelVCFFunction { 

  private static final String CSQ = "CSQ=";
  private static final String FREX = "FREX=";

  private static final String SYMBOL = "SYMBOL";

  private static final String HEAD = "HEAD";
  private static final String EVEN = "EVEN";
  private static final String ODD = "ODD";

  private ArrayList<String> info = null;
  private ArrayList<String> samples = null;

  @Override
  public String getSummary() {
    return "Generates an HTML legible file for the given VCF file";
  }

  @Override
  public Description getDesc() {
    return new Description("Creates a HTML file, that contains the variants of the VCF file.")
            .addLine("For each variants, all the VCF fields are displayed.")
            .addLine("All vep annotation are formatted and shown.");
  }

  @Override
  public boolean needVEP() {//TODO implements version where CSQ is optionnal
    return true;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
  }

  @Override
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_HTML;
  }

  @Override
  public void begin() {
    super.begin();
    this.info = null;
    this.samples = null;

    for (String line : getVCF().getHeadersWithoutSamples()) { //TODO does this have #CHROM also ?
      if (line.startsWith("##INFO=<ID=CSQ"))
        setInfoHeader(line);
    }

    setSamples(getVCF().getSampleHeader());

    if (samples == null) {
      this.fatalAndDie("No Samples available");
    }
    if (info == null) {
      this.fatalAndDie("No INFO available");
    }
  }

  @Override
  public String[] getHeaders() {
    ArrayList<String> out = new ArrayList<>();
    out.add("<html>");
    out.add("<head>");
    out.add("<title>" + this.vcffile.getFilename() + "</title>");
    out.add("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
    out.add("<link rel=\"stylesheet\" type=\"text/css\" href=\"http://lysine.univ-brest.fr/css/vcf.css\">");
    out.add("</head>");
    out.add("<body>");
    out.add("<table class=\"vcftable\">");
        
    LineBuilder line = new LineBuilder();
    line.openHTML("tr",HEAD);
    for (String common : COMMONS)
      th(line, common, HEAD);
    for (String inf : info)
      th(line, inf, HEAD);
    for (String sample : samples)
      th(line, sample, HEAD);
    line.closeHTML("tr");
    out.add(line.toString());
    
    return out.toArray(new String[out.size()]);
  }

  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    out.add("</table>");
    out.add("</body>");
    out.add("</html>");
    return out.toArray(new String[out.size()]);
  }

  private void setInfoHeader(String line) {
    this.info = new ArrayList<>();
    String tmp = line.split(":")[1].split("\"")[0].trim();
    this.info.addAll(Arrays.asList(tmp.split("\\|")));
    System.err.println(info.size() + " info in the header");
    String msg = "";
    for (String inf : info)
      msg += "|" + inf;
    System.err.println(msg);
  }

  private void setSamples(String line) {
    this.samples = new ArrayList<>();
    String[] f = line.split(T);
    for (int i = 8; i < f.length; i++) //!! For easier processing, FORMAT is considered a SAMPLE
      this.samples.add(f[i]);
    System.err.println((samples.size() - 1) + " samples");
  }

  private static final String[] COMMONS = new String[]{"CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO"};

  //private String clas = ODD; //TODO impossible to use in parallel mode !!

  private static void tag(LineBuilder lb, String tag, String s, String clas){
    lb.openHTML(tag, clas).append(s).closeHTML(tag);
  }
  
  private static void td(LineBuilder lb, String s, String clas) {
    tag(lb, "td", s, clas);
  }
  
  private static void th(LineBuilder lb, String s, String clas) {
    tag(lb, "th", s, clas);
  }

  @Override
  public String[] processInputLine(String line) {
    String[] f = line.split(T);

    String[] infs = f[7].split(";");
    String infoo = infs[0];
    String[] csqlists = null;

    for (int i = 1; i < infs.length; i++)
      if (infs[i].startsWith(CSQ))
        csqlists = infs[i].substring(CSQ.length()).split(",");
      else
        infoo += ";" + infs[i];

    if(csqlists == null)
      return NO_OUTPUT;
    
    boolean first = true;
    String[] outs = new String[csqlists.length];
    for (int c = 0 ; c < csqlists.length; c++) {
      String csqs = csqlists[c];
      LineBuilder out = new LineBuilder();
      out.openHTML("tr");
      for (int i = 0; i < COMMONS.length - 1; i++)
        if (first)
          td(out, f[i], COMMONS[i]);
        else
          td(out, "", COMMONS[i]);
      if (first)
        td(out, infoo, COMMONS[COMMONS.length - 1]);
      else
        td(out, "", COMMONS[COMMONS.length - 1]);
      String[] csq = (csqs + " ").split("\\|");
      if (csq.length != info.size()) {
        fatalAndDie("Mismatch between csq (" + csq.length + ") and info [" + info.size() + "]\n" + csqs);
      }
      for (int i = 0; i < info.size(); i++){
        String infoType = info.get(i).trim();
        if (infoType.equalsIgnoreCase(SYMBOL)){
          out.openHTML("td", infoType);
          out.openHTML("a", new String[][]{{"href", "http://www.genecards.org/cgi-bin/carddisp.pl?gene=" + csq[i]}});
          out.append(csq[i]);
          out.closeHTML("a");
          out.closeHTML("td");
        }
        else
          td(out, csq[i], infoType);
      }

      for (int i = 0; i < samples.size(); i++)
        if (first)
          td(out, f[i + 8], samples.get(i));
        else
          td(out, "", samples.get(i));

      out.closeHTML("tr");
      out.newLine();
      outs[c] = out.toString();
      first = false;
    }
    return outs;
  }
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
