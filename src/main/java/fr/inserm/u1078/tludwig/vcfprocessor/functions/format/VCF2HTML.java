package fr.inserm.u1078.tludwig.vcfprocessor.functions.format;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
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

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Creates a HTML file, that contains the variants of the VCF file.")
            .addLine("For each variants, all the VCF fields are displayed.")
            .addLine("All vep annotation are formatted and shown.");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {//TODO implements version where CSQ is optional
    return true;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_HTML;
  }

  @SuppressWarnings("unused")
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
      Message.die("No Samples available");
    }
    if (info == null) {
      Message.die("No INFO available");
    }
  }

  @SuppressWarnings({"unused"})
  @Override
  public String[] getHeaders() {
    ArrayList<String> out = new ArrayList<>();
    out.add("<html>");
    out.add("<head>");
    out.add("<title>" + this.vcfFile.getFilename() + "</title>");
    out.add("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
    out.add("<link rel=\"stylesheet\" type=\"text/css\" href=\"https://lysine.univ-brest.fr/css/vcf.css\">");
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
    
    return out.toArray(new String[0]);
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    out.add("</table>");
    out.add("</body>");
    out.add("</html>");
    return out.toArray(new String[0]);
  }

  private void setInfoHeader(String line) {
    this.info = new ArrayList<>();
    String tmp = line.split(":")[1].split("\"")[0].trim();
    this.info.addAll(Arrays.asList(tmp.split("\\|")));
    System.err.println(info.size() + " info in the header");
    String msg = "|" + String.join("|", info);
    System.err.println(msg);
  }

  private void setSamples(String line) {
    this.samples = new ArrayList<>();
    String[] f = line.split(T);
    this.samples.addAll(Arrays.asList(f).subList(8, f.length));
    System.err.println((samples.size() - 1) + " samples");
  }

  private static final String[] COMMONS = new String[]{"CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO"};

  //private String clas = ODD; //TODO impossible to use in parallel mode !!

  private static void tag(LineBuilder lb, String tag, String s, String clas){
    lb.openHTML(tag, clas).append(s).closeHTML(tag);
  }
  
  private static void td(LineBuilder lb, String s, String clas, boolean first) {
    tag(lb, "td", first ? s : "", clas);
  }
  
  private static void th(LineBuilder lb, String s, String clas) {
    tag(lb, "th", s, clas);
  }

  @Override
  public String[] processInputRecord(VariantRecord record) {
    String[][] inf = record.getInfo();
    /*String[] infs = f[7].split(";");*/
    StringBuilder infoSB = new StringBuilder(inf[0][0]);
    if(inf[0][1] != null)
      infoSB.append("=").append(inf[0][1]);
    String[] csqLists = null;

    for (int i = 1; i < inf.length; i++)
      if (inf[i][0].equals(CSQ))
        csqLists = inf[i][1].split(",");
      else {
        infoSB.append(";").append(inf[i][0]);
        if(inf[i][1] != null)
          infoSB.append("=").append(inf[i][1]);
      }

    if(csqLists == null)
      return NO_OUTPUT;
    
    boolean first = true;
    String[] outs = new String[csqLists.length];
    for (int c = 0 ; c < csqLists.length; c++) {
      String csqs = csqLists[c];
      LineBuilder out = new LineBuilder();
      out.openHTML("tr");
      td(out, record.getChrom(), COMMONS[0], first);
      td(out, record.getPos()+"", COMMONS[1], first);
      td(out, record.getID(), COMMONS[2], first);
      td(out, record.getRef(), COMMONS[3], first);
      td(out, record.getAltString(), COMMONS[4], first);
      td(out, record.getQual(), COMMONS[5], first);
      td(out, record.getFiltersString(), COMMONS[6], first);
      td(out, infoSB.toString(), COMMONS[COMMONS.length - 1], first);
      String[] csq = (csqs + " ").split("\\|");
      if (csq.length != info.size()) {
        Message.die("Mismatch between csq (" + csq.length + ") and info [" + info.size() + "]\n" + csqs);
      }
      for (int i = 0; i < info.size(); i++){
        String infoType = info.get(i).trim();
        if (infoType.equalsIgnoreCase(SYMBOL)){
          out.openHTML("td", infoType);
          out.openHTML("a", new String[][]{{"href", "https://www.genecards.org/cgi-bin/carddisp.pl?gene=" + csq[i]}});
          out.append(csq[i]);
          out.closeHTML("a");
          out.closeHTML("td");
        }
        else
          td(out, csq[i], infoType, true);
      }
      for (int i = 0; i < samples.size(); i++)
          td(out, record.getGenotypeString(i), samples.get(i), first);

      out.closeHTML("tr");
      out.newLine();
      outs[c] = out.toString();
      first = false;
    }
    return outs;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
