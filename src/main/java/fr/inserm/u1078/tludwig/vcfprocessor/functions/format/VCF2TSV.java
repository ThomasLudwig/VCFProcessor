package fr.inserm.u1078.tludwig.vcfprocessor.functions.format;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
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

  String[] vepHeaders;

  @Override
  public String getSummary() {
    return "Creates a TSV file, readable in Excel.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Creates a TSV file, that can be opened in Excel.")
            .addLine("For each variants, all the VCF fields are displayed.")
            .addLine("All vep annotation are formatted and shown.");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders(){
    String[] vh = getVCF().getSampleHeader().split(T);
    LineBuilder header = new LineBuilder(vh[0]);
    for (int i = 1; i < 8; i++)
      header.addColumn(vh[i]);

    if (vepHeaders != null)
      for (String v : vepHeaders)
        header.addColumn(v);

    for (int i = 8; i < vh.length; i++)
      header.addColumn(vh[i]);

    return new String[]{header.toString()};
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    vepHeaders = null;
    for (String header : getVCF().getHeadersWithoutSamples())
      if (header.startsWith("##INFO=<ID=CSQ")) {
        String[] f = header.split("\\s+");
        String s = f[f.length - 1];
        s = s.substring(0, s.length() - 2);
        vepHeaders = s.split("\\|");
        break;
      }
  }

  public boolean keep(VariantRecord record, ArrayList<String[]> veps) {
    return true; //TODO always kept ? Method is overriden
  }
  
  public ArrayList<String[]> getVEPs(String[][] info){
    ArrayList<String[]> veps = new ArrayList<>();
    for (String[] inf : info)
      if (inf[0].equals("CSQ")) {
        String[] vep = inf[1].split(",");
        for (String v : vep)
          veps.add(v.split("\\|", -1));
      }
    return veps;
  }

  @Override
  public String[] processInputRecord(VariantRecord record) {
    ArrayList<String[]> veps = getVEPs(record.getInfo());
    //ArrayList<String[]> frexs = new ArrayList<>();

    /*
      if (inf.startsWith("FREX=")) {//TODO bug adding FrEx in records but not in the header
        String[] frex = inf.substring(5).split(",");
        //skip ref allele
        for (int ifr = 1; ifr < frex.length; ifr++) {
          String fr = frex[ifr];
          frexs.add(fr.split("\\|", -1));
        }
      }
     */
    
    if(!keep(record, veps))
      return NO_OUTPUT;
    //Some columns (general ones) are only valued once, other are valued once per line, for multiple vep annotations)
    int size = Math.max(1, veps.size());
    //size = Math.max(size, frexs.size());
    String[] outs = new String[size];
    for (int l = 0; l < size; l++) {
      LineBuilder out = new LineBuilder();
      if(l != 0)
        out.addColumn().addColumn().addColumn().addColumn().addColumn().addColumn().addColumn();
      else {
        out.addColumn(record.getChrom());
        out.addColumn(record.getPos());
        out.addColumn(record.getID() );
        out.addColumn(record.getRef());
        out.addColumn(record.getAltString());
        out.addColumn(record.getQual());
        out.addColumn(record.getFilters());
      }

      if (vepHeaders != null)
        for (int s = 0; s < vepHeaders.length; s++) {
          out.addColumn();
          if (l < veps.size())
            if (veps.get(l)[s] != null)
              out.append(veps.get(l)[s]);
        }
      if(l != 0){
        out.addColumn();//info
        out.addColumn();//format
      }
      for(String geno : record.getGenotypeStrings())
        out.addColumn(l == 0 ? geno : "");

      outs[l] = out.substring(1);
    }
    return outs;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }  
}
