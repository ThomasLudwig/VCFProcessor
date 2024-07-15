package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.ArrayList;

public class ExtractCanonical extends ParallelVCFFunction {

  @Override
  public String getSummary() {
    return "Function that convert a VCF to a list of canonical variant";
  }

  @Override
  public Description getDesc() {
    return new Description("Only variants with AC > 0 are kept");
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @Override
  public boolean needVEP() {
    return false;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T,"#Canonical","AC>0")};
  }

  @Override
  public String[] processInputRecord(VariantRecord record) {
    ArrayList<String> ret = new ArrayList<>();
    String[] alt = record.getAlts();
    boolean[] AC = areACPositive(record);

    for (int a = 0; a < AC.length; a++)
      ret.add(new Canonical(record.getChrom(), record.getPos(), record.getRef(), alt[a]).toString()+"\t"+AC[a]);

    return ret.toArray(new String[0]);
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  public boolean[] areACPositive(VariantRecord record){
    String[][] infoField = record.getInfo();
    int l = record.getAlts().length;
    boolean[] AC0 = new boolean[l]; //initialized to false

    if(record.containsFilter("AC0"))
      return AC0;

    String[] AC = null;
    String[] AF = null;
    String AN = null;
    for (String[] inf : infoField) {
      if (inf[0].equals("AC"))
        AC = inf[1].split(",");
      if (inf[0].equals("AF"))
        AF = inf[1].split(",");
      if (inf[0].equals("AN"))
        AN = inf[1];
    }
    if(AC != null) {
      for (int i = 0; i < l; i++)
        AC0[i] = !"0".equals(AC[i]);
      return AC0;
    }

    Message.warning("Missing AC annotation for line [" + record + "]");
    if (AN == null)
      Message.die("Missing AN annotations for record ["+record+"]");
    if ("0".equals(AN))
      return AC0;
    if (AF == null)
      Message.die("Missing AF annotations for record ["+record+"]");
    else
      for(int i = 0 ; i < l; i++)
        AC0[i] = Double.parseDouble(AF[i]) != 0d;


    return AC0;
  }
}
