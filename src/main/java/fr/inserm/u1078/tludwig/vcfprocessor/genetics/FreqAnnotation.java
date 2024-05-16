package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 9 nov. 2016
 */
public class FreqAnnotation {

  public static final String DBAF_FREX = "dbAF_FREX";
  public static final String DBAF_FREX_BOR = "dbAF_FREX_BOR";
  public static final String DBAF_FREX_BRE = "dbAF_FREX_BRE";
  public static final String DBAF_FREX_DIJ = "dbAF_FREX_DIJ";
  public static final String DBAF_FREX_LIL = "dbAF_FREX_LIL";
  public static final String DBAF_FREX_NAN = "dbAF_FREX_NAN";
  public static final String DBAF_FREX_ROU = "dbAF_FREX_ROU";
  public static final String DBAF_1KG = "dbAF_1KG";
  public static final String DBAF_1KG_EAS = "dbAF_1KG_EAS";
  public static final String DBAF_1KG_EUR = "dbAF_1KG_EUR";
  public static final String DBAF_1KG_AFR = "dbAF_1KG_AFR";
  public static final String DBAF_1KG_AMR = "dbAF_1KG_AMR";
  public static final String DBAF_1KG_SAS = "dbAF_1KG_SAS";
  public static final String DBAF_EXAC = "dbAF_EXAC";
  public static final String DBAF_EXAC_AFR = "dbAF_EXAC_AFR";
  public static final String DBAF_EXAC_AMR = "dbAF_EXAC_AMR";
  public static final String DBAF_EXAC_ADJ = "dbAF_EXAC_Adj";
  public static final String DBAF_EXAC_EAS = "dbAF_EXAC_EAS";
  public static final String DBAF_EXAC_FIN = "dbAF_EXAC_FIN";
  public static final String DBAF_EXAC_NFE = "dbAF_EXAC_NFE";
  public static final String DBAF_EXAC_OTH = "dbAF_EXAC_OTH";
  public static final String DBAF_EXAC_SAS = "dbAF_EXAC_SAS";

  public static final String DBAF_GNOMAD = "dbAF_GNOMAD";
  public static final String DBAF_GNOMAD_AFR = "dbAF_GNOMAD_AFR";
  public static final String DBAF_GNOMAD_AMR = "dbAF_GNOMAD_AMR";
  public static final String DBAF_GNOMAD_ASJ = "dbAF_GNOMAD_ASJ";
  public static final String DBAF_GNOMAD_EAS = "dbAF_GNOMAD_EAS";
  public static final String DBAF_GNOMAD_FIN = "dbAF_GNOMAD_FIN";
  public static final String DBAF_GNOMAD_NFE = "dbAF_GNOMAD_NFE";
  public static final String DBAF_GNOMAD_OTH = "dbAF_GNOMAD_OTH";
  public static final String DBAF_GNOMAD_SAS = "dbAF_GNOMAD_SAS";
  public static final String DBAF_GNOMAD_MALE = "dbAF_GNOMAD_Male";
  public static final String DBAF_GNOMAD_FEMALE = "dbAF_GNOMAD_Female";
  public static final String DBAF_GNOMAD_RAW = "dbAF_GNOMAD_raw";

  public static final String DBAF_ESP = "dbAF_ESP";
  public static final String DBAF_ESP_EA = "dbAF_ESP_EA";
  public static final String DBAF_ESP_AA = "dbAF_ESP_AA";
  public static final String DBAF_UK10K = "dbAF_UK10K";
  public static final String DBAF_UK10K_TWINSUK = "dbAF_UK10K_TWINSUK";
  public static final String DBAF_UK10K_ALSPAC = "dbAF_UK10K_ALSPAC";
  public static final String DBAF_UK10K_TWINSUK_NODUP = "dbAF_UK10K_TWINSUK_NODUP";
  public static final String DBAF_GONL = "dbAF_GONL";

  private static final String[] POPS = new String[]{
    DBAF_FREX, DBAF_FREX_BOR, DBAF_FREX_BRE, DBAF_FREX_DIJ, DBAF_FREX_LIL, DBAF_FREX_NAN, DBAF_FREX_ROU,
    DBAF_1KG, DBAF_1KG_EAS, DBAF_1KG_EUR, DBAF_1KG_AFR, DBAF_1KG_AMR, DBAF_1KG_SAS,
    DBAF_EXAC, DBAF_EXAC_AFR, DBAF_EXAC_AMR, DBAF_EXAC_ADJ, DBAF_EXAC_EAS, DBAF_EXAC_FIN, DBAF_EXAC_NFE, DBAF_EXAC_OTH, DBAF_EXAC_SAS,
    DBAF_GNOMAD, DBAF_GNOMAD_AFR, DBAF_GNOMAD_AMR, DBAF_GNOMAD_ASJ, DBAF_GNOMAD_EAS, DBAF_GNOMAD_FIN, DBAF_GNOMAD_NFE, DBAF_GNOMAD_OTH, DBAF_GNOMAD_SAS, DBAF_GNOMAD_MALE, DBAF_GNOMAD_FEMALE, DBAF_GNOMAD_RAW,
    DBAF_ESP, DBAF_ESP_EA, DBAF_ESP_AA,
    DBAF_UK10K, DBAF_UK10K_TWINSUK, DBAF_UK10K_ALSPAC, DBAF_UK10K_TWINSUK_NODUP,
    DBAF_GONL
  };

  private final double[][] frequencies;

  public FreqAnnotation(Info info) {
    int nbAlt = 1;

    for (String pop : POPS) {
      String value = info.getAnnot(pop);
      if (value != null) {
        String[] values = value.split(",", -1);
        nbAlt = Math.max(nbAlt, values.length);
      }
    }
    this.frequencies = new double[POPS.length][nbAlt];
    
    for(int p = 0 ; p < POPS.length; p++){
      String value = info.getAnnot(POPS[p]);
      if(value != null){
        String[] values = value.split(",", -1);
        for (int a = 0; a < nbAlt; a++)
            if (values[a] != null && !values[a].isEmpty())
              this.frequencies[p][a] = new Double(values[a]);
      }
    }    
  }

  public int getIndex(String pop) {
    for (int i = 0; i < POPS.length; i++)
      if (POPS[i].equals(pop))
        return i;
    return -1;
  }

  public double getDbAF_FREX(int a) {
    return this.frequencies[getIndex(DBAF_FREX)][a - 1];
  }

  public double getDbAF_FREX_BOR(int a) {
    return this.frequencies[getIndex(DBAF_FREX_BOR)][a - 1];
  }

  public double getDbAF_FREX_BRE(int a) {
    return this.frequencies[getIndex(DBAF_FREX_BRE)][a - 1];
  }

  public double getDbAF_FREX_DIJ(int a) {
    return this.frequencies[getIndex(DBAF_FREX_DIJ)][a - 1];
  }

  public double getDbAF_FREX_LIL(int a) {
    return this.frequencies[getIndex(DBAF_FREX_LIL)][a - 1];
  }

  public double getDbAF_FREX_NAN(int a) {
    return this.frequencies[getIndex(DBAF_FREX_NAN)][a - 1];
  }

  public double getDbAF_FREX_ROU(int a) {
    return this.frequencies[getIndex(DBAF_FREX_ROU)][a - 1];
  }

  public double getDbAF_1KG(int a) {
    return this.frequencies[getIndex(DBAF_1KG)][a - 1];
  }

  public double getDbAF_1KG_EAS(int a) {
    return this.frequencies[getIndex(DBAF_1KG_EAS)][a - 1];
  }

  public double getDbAF_1KG_EUR(int a) {
    return this.frequencies[getIndex(DBAF_1KG_EUR)][a - 1];
  }

  public double getDbAF_1KG_AFR(int a) {
    return this.frequencies[getIndex(DBAF_1KG_AFR)][a - 1];
  }

  public double getDbAF_1KG_AMR(int a) {
    return this.frequencies[getIndex(DBAF_1KG_AMR)][a - 1];
  }

  public double getDbAF_1KG_SAS(int a) {
    return this.frequencies[getIndex(DBAF_1KG_SAS)][a - 1];
  }

  public double getDbAF_EXAC(int a) {
    return this.frequencies[getIndex(DBAF_EXAC)][a - 1];
  }

  public double getDbAF_EXAC_AFR(int a) {
    return this.frequencies[getIndex(DBAF_EXAC_AFR)][a - 1];
  }

  public double getDbAF_EXAC_AMR(int a) {
    return this.frequencies[getIndex(DBAF_EXAC_AMR)][a - 1];
  }

  public double getDbAF_EXAC_Adj(int a) {
    return this.frequencies[getIndex(DBAF_EXAC_ADJ)][a - 1];
  }

  public double getDbAF_EXAC_EAS(int a) {
    return this.frequencies[getIndex(DBAF_EXAC_EAS)][a - 1];
  }

  public double getDbAF_EXAC_FIN(int a) {
    return this.frequencies[getIndex(DBAF_EXAC_FIN)][a - 1];
  }

  public double getDbAF_EXAC_NFE(int a) {
    return this.frequencies[getIndex(DBAF_EXAC_NFE)][a - 1];
  }

  public double getDbAF_EXAC_OTH(int a) {
    return this.frequencies[getIndex(DBAF_EXAC_OTH)][a - 1];
  }

  public double getDbAF_EXAC_SAS(int a) {
    return this.frequencies[getIndex(DBAF_EXAC_SAS)][a - 1];
  }

  public double getDbAF_GNOMAD(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD)][a - 1];
  }

  public double getDbAF_GNOMAD_AFR(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_AFR)][a - 1];
  }

  public double getDbAF_GNOMAD_AMR(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_AMR)][a - 1];
  }

  public double getDbAF_GNOMAD_ASJ(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_ASJ)][a - 1];
  }

  public double getDbAF_GNOMAD_EAS(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_EAS)][a - 1];
  }

  public double getDbAF_GNOMAD_FIN(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_FIN)][a - 1];
  }

  public double getDbAF_GNOMAD_NFE(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_NFE)][a - 1];
  }

  public double getDbAF_GNOMAD_OTH(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_OTH)][a - 1];
  }

  public double getDbAF_GNOMAD_SAS(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_SAS)][a - 1];
  }

  public double getDbAF_GNOMAD_Male(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_MALE)][a - 1];
  }

  public double getDbAF_GNOMAD_Female(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_FEMALE)][a - 1];
  }

  public double getDbAF_GNOMAD_raw(int a) {
    return this.frequencies[getIndex(DBAF_GNOMAD_RAW)][a - 1];
  }

  public double getDbAF_ESP(int a) {
    return this.frequencies[getIndex(DBAF_ESP)][a - 1];
  }

  public double getDbAF_ESP_EA(int a) {
    return this.frequencies[getIndex(DBAF_ESP_EA)][a - 1];
  }

  public double getDbAF_ESP_AA(int a) {
    return this.frequencies[getIndex(DBAF_ESP_AA)][a - 1];
  }

  public double getDbAF_UK10K(int a) {
    return this.frequencies[getIndex(DBAF_UK10K)][a - 1];
  }

  public double getDbAF_UK10K_TWINSUK(int a) {
    return this.frequencies[getIndex(DBAF_UK10K_TWINSUK)][a - 1];
  }

  public double getDbAF_UK10K_ALSPAC(int a) {
    return this.frequencies[getIndex(DBAF_UK10K_ALSPAC)][a - 1];
  }

  public double getDbAF_UK10K_TWINSUK_NODUP(int a) {
    return this.frequencies[getIndex(DBAF_UK10K_TWINSUK_NODUP)][a - 1];
  }

  public double getDbAF_GONL(int a) {
    return this.frequencies[getIndex(DBAF_GONL)][a - 1];
  }
}
