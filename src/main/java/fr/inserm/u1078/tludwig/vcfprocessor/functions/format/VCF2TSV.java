package fr.inserm.u1078.tludwig.vcfprocessor.functions.format;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

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
  public Println[] getHeaders(){
    String[] vh = getVCF().getSampleHeader().split(T);
    Println header = new Println(vh[0]);
    for (int i = 1; i < 8; i++)
      header.append(T, vh[i]);

    if (vepHeaders != null)
      for (String v : vepHeaders)
        header.append(T, v);

    for (int i = 8; i < vh.length; i++)
      header.append(T, vh[i]);

    return new Println[]{new Println(header.toString())};
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
  public Println[] processInputRecord(VariantRecord record) {
    ArrayList<String[]> veps = getVEPs(record.getInfoFields());
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
    Println[] outs = new Println[size];
    for (int l = 0; l < size; l++) {
      Println out = l != 0
          ? new Println(T, T, T, T, T, T)
          : Println.join(T, record.getChrom(), record.getPos(), record.getID(), record.getRef(), record.getAltString(), record.getQual(), record.getFilters());

      if (vepHeaders != null)
        for (int s = 0; s < vepHeaders.length; s++) {
          out.append(T);
          if (l < veps.size())
            if (veps.get(l)[s] != null)
              out.append(veps.get(l)[s]);
        }
      if(l != 0){
        out.append(T);//info
        out.append(T);//format
      }
      for(String geno : record.getGenotypeStrings())
        out.append(T, l == 0 ? geno : "");

      outs[l] =out;
    }
    return outs;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }  
}
