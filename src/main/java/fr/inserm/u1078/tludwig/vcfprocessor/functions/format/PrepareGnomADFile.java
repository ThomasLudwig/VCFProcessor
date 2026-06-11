package fr.inserm.u1078.tludwig.vcfprocessor.functions.format;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis.RAVAQOutput;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Printable;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

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

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.ALLELE_AS_LINE); }

  @Override
  public Println[] getHeaders() {
    return NO_OUTPUT;
  }

  @Override
  public Println[] processInputRecord(VariantRecord record) {
    Println[] ret = new Println[record.getAlts().length];
    String filter = record.getFiltersString();
    String info = record.getInfoString();
    for(int a = 0 ; a < record.getAlts().length; a++) {
      ret[a] = new GnomAD(new Canonical(record.getChrom(), record.getPos(), record.getRef(), record.getAlts()[a]), filter, info, a).println();
    }
    return ret;
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  public static class GnomAD implements Printable {
    private final Canonical canonical;
    private final String filter;
    private final Double af;
    private final Double af_afr;
    private final Double af_amr;
    private final Double af_asj;
    private final Double af_eas;
    private final Double af_fin;
    private final Double af_mid;
    private final Double af_nfe;
    private final Double af_sas;

    @Override
    public Println println() {
      return Println.join(T, canonical.toString(), filter, af, af_afr, af_amr, af_asj, af_eas, af_fin, af_mid, af_nfe, af_sas);
    }

    public GnomAD(String line) {
      String[] f = line.split(T, -1);
      canonical = Canonical.deserialize(f[0]);
      filter = f[1];
      af = parseFrequency(f[2]);
      af_afr = parseFrequency(f[3]);
      af_amr = parseFrequency(f[4]);
      af_asj = parseFrequency(f[5]);
      af_eas = parseFrequency(f[6]);
      af_fin = parseFrequency(f[7]);
      af_mid = parseFrequency(f[8]);
      af_nfe = parseFrequency(f[9]);
      af_sas = parseFrequency(f[10]);
    }

    static Double parseFrequency(String f) {
      try{
        return Double.parseDouble(f);
      } catch(NumberFormatException e){
        return 0D;
      }
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

    private double divide(double ac, double an) {
      return an == 0 ? 0 : (ac/an);
    }

    public Double getAF() {
      return af;
    }

    public String getFilter() {
      return filter;
    }

    public Double getAF(String pop){
      return switch (pop.toUpperCase()) {
        case "AFR" -> af_afr;
        case "AMR" -> af_amr;
        case "ASJ" -> af_asj;
        case "EAS" -> af_eas;
        case "FIN" -> af_fin;
        case "MID" -> af_mid;
        case "NFE" -> af_nfe;
        case "SAS" -> af_sas;
        //default: return "ERR";
        default -> null;
      };
    }

    public Canonical getCanonical() {
      return canonical;
    }
  }
}
