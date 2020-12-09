package fr.inserm.u1078.tludwig.vcfprocessor.functions.format;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Creates a TSV file, readable in Excel.
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-06-01
 * Checked for release on 2020-08-20
 * Unit Test defined on 2020-20-20
 */
public class VCF2TSV extends ParallelVCFFunction { 

  String[] hvep;

  @Override
  public String getSummary() {
    return "Creates a TSV file, readable in Excel.";
  }

  @Override
  public Description getDesc() {
    return new Description("Creates a TSV file, that can be opened in Excel.")
            .addLine("For each variants, all the VCF fields are displayed.")
            .addLine("All vep annotation are formatted and shown.");
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public String[] getHeaders(){
    String[] vh = getVCF().getSampleHeader().split(T);
    LineBuilder header = new LineBuilder(vh[0]);
    for (int i = 1; i < 8; i++)
      header.addColumn(vh[i]);

    if (hvep != null)
      for (String v : hvep)
        header.addColumn(v);

    for (int i = 8; i < vh.length; i++)
      header.addColumn(vh[i]);

    return new String[]{header.toString()};
  }

  @Override
  public void begin() {
    super.begin();
    hvep = null;
    for (String header : getVCF().getHeadersWithoutSamples())
      if (header.startsWith("##INFO=<ID=CSQ")) {
        String[] f = header.split("\\s+");
        String s = f[f.length - 1];
        s = s.substring(0, s.length() - 2);
        hvep = s.split("\\|");
        break;
      }
  }

  public boolean keep(String[] fields, ArrayList<String[]> veps) {
    return true; //TODO ????
  }
  
  public ArrayList<String[]> getVEPs(String info){
    ArrayList<String[]> veps = new ArrayList<>();
    for (String inf : info.split(";"))
      if (inf.startsWith("CSQ=")) {
        String[] vep = inf.substring(4).split(",");
        for (String v : vep)
          veps.add(v.split("\\|", -1));
      }
    return veps;
  }

  @Override
  public String[] processInputLine(String line) {
    String[] fields = line.split(T);
    ArrayList<String[]> veps = getVEPs(fields[7]);
    //ArrayList<String[]> frexs = new ArrayList<>();

    /*
      if (inf.startsWith("FREX=")) {//TODO bug adding FrEx in reccords but not in the header
        String[] frex = inf.substring(5).split(",");
        //skip ref allele
        for (int ifr = 1; ifr < frex.length; ifr++) {
          String fr = frex[ifr];
          frexs.add(fr.split("\\|", -1));
        }
      }
     */
    
    if(!keep(fields, veps))
      return NO_OUTPUT;
    //Some columns (general ones) are only valued once, other are valued once per line, for multiple vep annotations)
    int size = Math.max(1, veps.size());
    //size = Math.max(size, frexs.size());
    String[] outs = new String[size];
    for (int l = 0; l < size; l++) {
      LineBuilder out = new LineBuilder();
      for (int c = 0; c < 8; c++) {
        out.addColumn();
        if (l == 0)
          out.append(fields[c]);
      }

      if (hvep != null)
        for (int s = 0; s < hvep.length; s++) {
          out.addColumn();
          if (l < veps.size())
            if (veps.get(l)[s] != null)
              out.append(veps.get(l)[s]);
        }

      for (int j = 8; j < fields.length; j++) {
        out.addColumn();
        if (l == 0)
          out.append(fields[j]);
      }

      outs[l] = out.substring(1);
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
