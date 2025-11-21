package fr.inserm.u1078.tludwig.vcfprocessor.functions.format;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.RAVAQOutput;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

public class PrepareGnomADFile extends ParallelVCFFunction<PrepareGnomADFile.GnomAD> {
  public static final String[] COLUMNS = {"canonical", "FILTER", "AF", "AF_afr", "AF_amr", "AF_asj", "AF_eas", "AF_fin", "AF_mid", "AF_nfe", "AF_sas"};

  @Override
  public String getSummary() {
    return "Prepares the gnomAD file to be used as an input for "+ RAVAQOutput.class.getSimpleName();
  }

  @Override
  public Description getDesc() {
    return new Description(getSummary()).addLine("Output form is ").addColumns(COLUMNS);
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
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
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @Override
  public String[] getHeaders() {
    return new String[0];
  }

  @Override
  public String[] processInputRecord(VariantRecord record) {
    String[] ret = new String[record.getAlts().length];
    String filter = record.getFiltersString();
    String info = record.getInfoString();
    for(int a = 0 ; a < record.getAlts().length; a++) {
      ret[a] = new GnomAD(new Canonical(record.getChrom(), record.getPos(), record.getRef(), record.getAlts()[a]), filter, info, a).toString();
    }
    return ret;
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  public static class GnomAD {
    private final Canonical canonical;
    private final String filter;
    private final String af;
    private final String af_afr;
    private final String af_amr;
    private final String af_asj;
    private final String af_eas;
    private final String af_fin;
    private final String af_mid;
    private final String af_nfe;
    private final String af_sas;

    @Override
    public String toString() {
      return String.join(T, canonical.toString(), filter, af, af_afr, af_amr, af_asj, af_eas, af_fin, af_mid, af_nfe, af_sas);
    }

    public GnomAD(String line) {
      String[] f = line.split(T, -1);
      canonical = Canonical.deserialize(f[0]);
      filter = f[1];
      af = f[2];
      af_afr = f[3];
      af_amr = f[4];
      af_asj = f[5];
      af_eas = f[6];
      af_fin = f[7];
      af_mid = f[8];
      af_nfe = f[9];
      af_sas = f[10];
    }

    public GnomAD(Canonical canonical, String filter, String info, int alt){
      this.canonical = canonical;
      this.filter = filter;
      String[] f = info.split(";");
      double ac = 0;
      double ac_afr = 0;
      double ac_amr = 0;
      double ac_asj = 0;
      double ac_eas = 0;
      double ac_fin = 0;
      double ac_mid = 0;
      double ac_nfe = 0;
      double ac_sas = 0;
      double an = 0;
      double an_afr = 0;
      double an_amr = 0;
      double an_asj = 0;
      double an_eas = 0;
      double an_fin = 0;
      double an_mid = 0;
      double an_nfe = 0;
      double an_sas = 0;
      for(String field : f){
          String[] kv = field.split("=");
          switch(kv[0].toLowerCase()){
            case "ac":
              ac = parse(kv[1], alt);
              break;
            case "ac_afr":
              ac_afr = parse(kv[1], alt);
              break;
            case "ac_amr":
              ac_amr = parse(kv[1], alt);
              break;
            case "ac_asj":
              ac_asj = parse(kv[1], alt);
              break;
            case "ac_eas":
              ac_eas = parse(kv[1], alt);
              break;
            case "ac_fin":
              ac_fin = parse(kv[1], alt);
              break;
            case "ac_mid":
              ac_mid = parse(kv[1], alt);
              break;
            case "ac_nfe":
              ac_nfe = parse(kv[1], alt);
              break;
            case "ac_sas":
              ac_sas = parse(kv[1], alt);
              break;
            case "an":
              an = parse(kv[1], alt);
              break;
            case "an_afr":
              an_afr = parse(kv[1], alt);
              break;
            case "an_amr":
              an_amr = parse(kv[1], alt);
              break;
            case "an_asj":
              an_asj = parse(kv[1], alt);
              break;
            case "an_eas":
              an_eas = parse(kv[1], alt);
              break;
            case "an_fin":
              an_fin = parse(kv[1], alt);
              break;
            case "an_mid":
              an_mid = parse(kv[1], alt);
              break;
            case "an_nfe":
              an_nfe = parse(kv[1], alt);
              break;
            case "an_sas":
              an_sas = parse(kv[1], alt);
              break;
          }
      }

      af = divide(ac, an);
      af_afr =  divide(ac_afr, an_afr);
      af_amr = divide(ac_amr, an_amr);
      af_asj = divide(ac_asj, an_asj);
      af_eas = divide(ac_eas, an_eas);
      af_fin = divide(ac_fin, an_fin);
      af_mid = divide(ac_mid, an_mid);
      af_nfe = divide(ac_nfe, an_nfe);
      af_sas = divide(ac_sas, an_sas);
    }

    private double parse(String value, int alt){
      if(value == null || value.isEmpty())
        return 0;
      String[] values =  value.split(",");
      try {
        return Double.parseDouble(values[alt]);
      } catch(Exception e){
        return 0;
      }
    }

    private String divide(double ac, double an) {
      return an == 0 ? "" : (ac/an)+"";
    }

    public String getAF() {
      return af;
    }

    public String getFilter() {
      return filter;
    }

    public String getAF(String pop){
      switch(pop.toUpperCase()){
        case "AFR" : return af_afr;
        case "AMR" : return af_amr;
        case "ASJ" : return af_asj;
        case "EAS" : return af_eas;
        case "FIN" : return af_fin;
        case "MID" : return af_mid;
        case "NFE" : return af_nfe;
        case "SAS" : return af_sas;
        default: return "ERR";
      }
    }

    public Canonical getCanonical() {
      return canonical;
    }
  }
}
