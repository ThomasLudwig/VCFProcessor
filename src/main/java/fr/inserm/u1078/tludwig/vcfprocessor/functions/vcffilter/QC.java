package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.FisherExactTest;
import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Info;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Run a Quality Control on VCF Variants according to INSERM U1078 Best Practices
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2019-10-07
 * Checked for release on 2020-08-03
 * Unit Test defined on 2020-08-03
 */
public class QC extends ParallelVCFVariantFunction {

  //TODO the disabled option doesn't work for missing values. Is this still true ?
  public final PedFileParameter pedfile = new PedFileParameter();
  public final FileParameter parameters = new FileParameter(OPT_OPT, "custom.parameters.tsv", "file containing the various thresholds for the QC (see Documentation)");
  public final TSVFileParameter report = new TSVFileParameter(OPT_REPORT, "fiteredVariant.tsv", "output file listing all the variants that were filtered, and why");

  public static final String FILTER_CALLRATE = "LOW_CALLRATE";
  public static final String FILTER_CALLRATE_DISTRIBUTION = "LOW_CALLRATE_DISTRIBUTION";
  public static final String FILTER_QUAL_BY_DEPTH = "LOW_QUAL_BY_DEPTH";
  public static final String FILTER_INBREEDING_COEF = "LOW_INBREEDING_COEF";
  public static final String FILTER_MQRANKSUM = "LOW_MQRANKSUM";
  public static final String FILTER_FS = "HIGH_FS";
  public static final String FILTER_SOR = "HIGH_SOR";
  public static final String FILTER_MQ = "LOW_MQ";
  public static final String FILTER_READPOSRANKSUM = "LOW_READPOSRANKSUM";
  //public static final String FILTER_LOWQUAL = "LOW_DPGQ_RATIO";
  public static final String FILTER_ALTLOWQUAL = "LOW_ALT_DPGQ";
  public static final String FILTER_ABHET = "ABHET_OFFSET";
  public static final String FILTER_AB_GENO = "AB_GENO_OFFSET";
  public static final String FILTER_AC0 = "AC0";
  public static final String FILTER_AF1 = "AF1";

  public static final String KEY_QD = "QD";
  public static final String KEY_FS = "FS";
  public static final String KEY_SOR = "SOR";
  public static final String KEY_MQ = "MQ";
  public static final String KEY_READPOSRANKSUM = "ReadPosRankSum";
  public static final String KEY_INBREEDING = "InbreedingCoeff";
  public static final String KEY_MQRANKSUM = "MQRankSum";

  public static final String[] KEYS = new String[]{KEY_QD, KEY_FS, KEY_SOR, KEY_MQ, KEY_READPOSRANKSUM, KEY_INBREEDING, KEY_MQRANKSUM};

  public static final double MIN_QD = 2;
  public static final double MAX_ABHET_DEV = 0.25;
  public static final double MAX_AB_GENO_DEV = 0.25;
  public static final double MIN_INBREEDING = -.8;
  public static final double MIN_MQRANKSUM = -12.5;
  public static final double MAX_FS_INDEL = 200;
  public static final double MAX_FS_SNP = 60;
  public static final double MAX_SOR_INDEL = 10;
  public static final double MAX_SOR_SNP = 3;
  public static final double MIN_MQ_INDEL = 10;
  public static final double MIN_MQ_SNP = 40;
  public static final double MIN_RPRS_INDEL = -20;
  public static final double MIN_RPRS_SNP = -8;
  public static final int MIN_GQ = 20;
  public static final int MIN_DP = 10;
  public static final int MAX_DP = Integer.MAX_VALUE;
  public static final double MIN_CALLRATE = .9;
  //public static final double MIN_HQ_RATIO = .8;
  public static final int MIN_ALT_HQ = 1;
  public static final double MIN_FISHER_CALLRATE = 0.001;

  public static final String KW_MIN_QD = "MIN_QD";
  public static final String KW_MAX_ABHET_DEV = "MAX_ABHET_DEV";
  public static final String KW_MAX_AB_GENO_DEV = "MAX_AB_GENO_DEV";
  public static final String KW_MIN_INBREEDING = "MIN_INBREEDING";
  public static final String KW_MIN_MQRANKSUM = "MIN_MQRANKSUM";
  public static final String KW_MAX_FS_INDEL = "MAX_FS_INDEL";
  public static final String KW_MAX_FS_SNP = "MAX_FS_SNP";
  public static final String KW_MAX_SOR_INDEL = "MAX_SOR_INDEL";
  public static final String KW_MAX_SOR_SNP = "MAX_SOR_SNP";
  public static final String KW_MIN_MQ_INDEL = "MIN_MQ_INDEL";
  public static final String KW_MIN_MQ_SNP = "MIN_MQ_SNP";
  public static final String KW_MIN_RPRS_INDEL = "MIN_RPRS_INDEL";
  public static final String KW_MIN_RPRS_SNP = "MIN_RPRS_SNP";
  public static final String KW_MIN_GQ = "MIN_GQ";
  public static final String KW_MIN_DP = "MIN_DP";
  public static final String KW_MAX_DP = "MAX_DP";
  public static final String KW_MIN_CALLRATE = "MIN_CALLRATE";
  public static final String KW_MIN_HQ_RATIO = "MIN_HQ_RATIO";
  public static final String KW_MIN_ALT_HQ = "MIN_ALT_HQ";
  public static final String KW_MIN_FISHER_CALLRATE = "MIN_FISHER_CALLRATE";
  public static final String KW_AC0 = "AC0";
  public static final String KW_AF1 = "AF1";

  private static final int IDX_UNFILTERED = 0;
  private static final int IDX_CALL = 1;
  private static final int IDX_CALLRATE_DISTRIBUTION = 2;
  private static final int IDX_QUAL_BY_DEPTH = 3;
  private static final int IDX_INBREEDING_COEF = 4;
  private static final int IDX_MQRANKSUM = 5;
  private static final int IDX_FS = 6;
  private static final int IDX_SOR = 7;
  private static final int IDX_MQ = 8;
  private static final int IDX_READPOSRANKSUM = 9;
  //private static final int IDX_LOWQUAL = 10;
  private static final int IDX_ALTLOWQUAL = 10;
  private static final int IDX_ABHET = 11;
  private static final int IDX_AC0 = 12;
  private static final int IDX_AF1 = 13;
  private final int[] count = new int[14];

  HashMap<String, ArrayList<String>> samples;
  private FisherExactTest fisherET;

  private Ped ped = null;

  private double minQD = MIN_QD;
  private double maxABHetDev = MAX_ABHET_DEV;
  private double maxABGenoDev = MAX_AB_GENO_DEV;
  private double minInbreeding = MIN_INBREEDING;
  private double minMQRankSum = MIN_MQRANKSUM;
  private double maxFSIndel = MAX_FS_INDEL;
  private double maxFSSNP = MAX_FS_SNP;
  private double maxSORIndel = MAX_SOR_INDEL;
  private double maxSORSNP = MAX_SOR_SNP;
  private double minMQIndel = MIN_MQ_INDEL;
  private double minMQSNP = MIN_MQ_SNP;
  private double minRPRSIndel = MIN_RPRS_INDEL;
  private double minRPRSSNP = MIN_RPRS_SNP;
  private int minGQ = MIN_GQ;
  private int minDP = MIN_DP;
  private int maxDP = MAX_DP;
  private double minCallRate = MIN_CALLRATE;
  //private double minHQRatio = MIN_HQ_RATIO;
  private int minAltHQ = MIN_ALT_HQ;
  private double minFisherCallRate = MIN_FISHER_CALLRATE;

  private boolean enableMinQD = true;
  private boolean enableMaxABHetDev = true;
  private boolean enableMaxABGenoDev = true;
  private boolean enableMinInbreeding = true;
  private boolean enableMinMQRankSum = true;
  private boolean enableMaxFSIndel = true;
  private boolean enableMaxFSSNP = true;
  private boolean enableMaxSORIndel = true;
  private boolean enableMaxSORSNP = true;
  private boolean enableMinMQIndel = true;
  private boolean enableMinMQSNP = true;
  private boolean enableMinRPRSIndel = true;
  private boolean enableMinRPRSSNP = true;
  private boolean enableMinGQ = true;
  private boolean enableMinDP = true;
  private boolean enableMaxDP = true;
  private boolean enableMinCallRate = true;
  //private boolean enableMinHQRatio = true;
  private boolean enableMinAltHQ = true;
  private boolean enableMinFisherCallRate = true;
  private boolean enableAC0 = true;
  private boolean enableAF1 = true;

  private final String missing = ".";
  private PrintWriter out;

  private SortedList<Export> reportLines;

  @Override
  public String getSummary() {
    return "Run a Quality Control on VCF Variants";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("A report file gives the reason(s) each variant has been filtered")
            .addLine("For more Details, see https://gitlab.com/gmarenne/ravaq")
            .addLine("For each group G, the info field has new annotations")
            .addItemize(new String[]{
      "G_AN: AlleleNumber for this group",
      "G_AC: AlleleCounts for this group",
      "G_AF: AlleleFrequencies for this group"
    });
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
    return "The VCF File must contain the following INFO : " + String.join(",", KEYS);
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  public void parseParameters(String filename) {
    //At class instantiation, all parameters are set to their default thresholds

    if (!"null".equalsIgnoreCase(filename)) {//If a filename was provided
      try {
        UniversalReader in = new UniversalReader(filename);
        String line;
        while ((line = in.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("#")) {
            //Ignore
          } else {
            String theLine = line.split("#")[0];
            String[] f = theLine.split("\\s+");
            boolean enabled = isEnabled(f[1]);
            switch (f[0].toUpperCase()) { //Process each no empty no comment line
              case KW_AC0 :
                enableAC0 = enabled;
                break;
              case KW_AF1 :
                enableAF1 = enabled;
                break;
              case KW_MIN_QD:
                enableMinQD = enabled;
                minQD = parseMinDouble(filename, f, minQD);
                break;
              case KW_MAX_ABHET_DEV:
                enableMaxABHetDev = enabled;
                maxABHetDev = parseMaxDouble(filename, f, maxABHetDev);
                break;
              case KW_MAX_AB_GENO_DEV:
                enableMaxABGenoDev = enabled;
                maxABGenoDev = parseMaxDouble(filename, f, maxABGenoDev);
                break;
              case KW_MIN_INBREEDING:
                enableMinInbreeding = enabled;
                minInbreeding = parseMinDouble(filename, f, minInbreeding);
                break;
              case KW_MIN_MQRANKSUM:
                enableMinMQRankSum = enabled;
                minMQRankSum = parseMinDouble(filename, f, minMQRankSum);
                break;
              case KW_MAX_FS_INDEL:
                enableMaxFSIndel = enabled;
                maxFSIndel = parseMaxDouble(filename, f, maxFSIndel);
                break;
              case KW_MAX_FS_SNP:
                enableMaxFSSNP = enabled;
                maxFSSNP = parseMaxDouble(filename, f, maxFSSNP);
                break;
              case KW_MAX_SOR_INDEL:
                enableMaxSORIndel = enabled;
                maxSORIndel = parseMaxDouble(filename, f, maxSORIndel);
                break;
              case KW_MAX_SOR_SNP:
                enableMaxSORSNP = enabled;
                maxSORSNP = parseMaxDouble(filename, f, maxSORSNP);
                break;
              case KW_MIN_MQ_INDEL:
                enableMinMQIndel = enabled;
                minMQIndel = parseMinDouble(filename, f, minMQIndel);
                break;
              case KW_MIN_MQ_SNP:
                enableMinMQSNP = enabled;
                minMQSNP = parseMinDouble(filename, f, minMQSNP);
                break;
              case KW_MIN_RPRS_INDEL:
                enableMinRPRSIndel = enabled;
                minRPRSIndel = parseMinDouble(filename, f, minRPRSIndel);
                break;
              case KW_MIN_RPRS_SNP:
                enableMinRPRSSNP = enabled;
                minRPRSSNP = parseMinDouble(filename, f, minRPRSSNP);
                break;
              case KW_MIN_GQ:
                enableMinGQ = enabled;
                minGQ = parseMinInteger(filename, f, minGQ);
                break;
              case KW_MIN_DP:
                enableMinDP = enabled;
                minDP = parseMinInteger(filename, f, minDP);
                break;
              case KW_MAX_DP:
                enableMaxDP = enabled;
                maxDP = parseMaxInteger(filename, f, maxDP);
                break;
              case KW_MIN_CALLRATE:
                enableMinCallRate = enabled;
                minCallRate = parseMinDouble(filename, f, minCallRate);
                break;
              case KW_MIN_HQ_RATIO:
                /*enableMinHQRatio = enabled;
                minHQRatio = parseMinDouble(filename, f, minHQRatio);*/
                Message.warning("QC Parameter ["+KW_MIN_HQ_RATIO+"] is obsolete and will be ignored. ["+KW_MIN_HQ_RATIO+"] has been merge with ["+KW_MIN_CALLRATE+"].");
                break;
              case KW_MIN_ALT_HQ:
                enableMinAltHQ = enabled;
                minAltHQ = parseMinInteger(filename, f, minAltHQ);
                break;
              case KW_MIN_FISHER_CALLRATE:
                enableMinFisherCallRate = enabled;
                minFisherCallRate = parseMinDouble(filename, f, minFisherCallRate);
                break;
              default:
                this.fatalAndDie("Unknown parameter keyword [" + f[0] + "] in file [" + filename + "]");
            }
          }
        }
        in.close();
      } catch (IOException e) {
        this.fatalAndDie("Unable to parse file [" + filename + "] to set the parameter value. Provide a valid file name, or \"null\" to use defaults parameters", e);
      }
    }
  }

  public static boolean isEnabled(String s) {
    return !(s == null || s.isEmpty() || s.toLowerCase().startsWith("disab"));
  }

  public double parseMinDouble(String filename, String[] f, double defau) {
    if (f.length == 1 || f[1].toLowerCase().startsWith("disab"))
      return Double.NEGATIVE_INFINITY;
    return parseElseDouble(filename, f, defau);
  }

  public double parseMaxDouble(String filename, String[] f, double defau) {
    if (f.length == 1 || f[1].toLowerCase().startsWith("disab"))
      return Double.POSITIVE_INFINITY;
    return parseElseDouble(filename, f, defau);
  }

  public double parseElseDouble(String filename, String[] f, double defau) {
    if (!"default".equalsIgnoreCase(f[1]))
      try {
        return new Double(f[1]);
      } catch (NumberFormatException e) {
        this.fatalAndDie("Unable to set value [" + f[1] + "] to parameter [" + f[0] + "], from file [" + filename + "]. Decimal value expected");
      }
    return defau;
  }

/*  public double parseDouble(String filename, String[] f, boolean min, double defau) {
    if (f.length == 1 || f[1].toLowerCase().startsWith("disab"))
      return min ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    else if (!"default".equalsIgnoreCase(f[1]))
      try {
        return new Double(f[1]);
      } catch (NumberFormatException e) {
        this.fatalAndDie("Unable to set value [" + f[1] + "] to parameter [" + f[0] + "], from file [" + filename + "]. Decimal value expected");
      }
    return defau;
  }*/

  public int parseMinInteger(String filename, String[] f, int defau) {
    if (f.length == 1 || f[1].toLowerCase().startsWith("disab"))
      return Integer.MIN_VALUE;
    return parseElseInteger(filename, f, defau);
  }

  public int parseMaxInteger(String filename, String[] f, int defau) {
    if (f.length == 1 || f[1].toLowerCase().startsWith("disab"))
      return Integer.MAX_VALUE;
    return parseElseInteger(filename, f, defau);
  }

  public int parseElseInteger(String filename, String[] f, int defau) {
    if (!"default".equalsIgnoreCase(f[1]))
      try {
        return new Integer(f[1]);
      } catch (NumberFormatException e) {
        this.fatalAndDie("Unable to set value [" + f[1] + "] to parameter [" + f[0] + "], from file [" + filename + "]. Integer value expected");
      }
    return defau;
  }

  /*
  public int parseInteger(String filename, String[] f, boolean min, int defau) {
    if (f.length == 1 || f[1].toLowerCase().startsWith("disab"))
      return min ? Integer.MIN_VALUE : Integer.MAX_VALUE;
    else if (!"default".equalsIgnoreCase(f[1]))
      try {
        return new Integer(f[1]);
      } catch (NumberFormatException e) {
        this.fatalAndDie("Unable to set value [" + f[1] + "] to parameter [" + f[0] + "], from file [" + filename + "]. Integer value expected");
      }
    return defau;
  }*/

  private String getHeader() {
    String[] headers = {
      "#CHROM",
      "POS",
      "ID",
      "REF",
      "ALT",
      "CallRate<" + this.minCallRate,
      "Fisher(CallRate)<" + this.minFisherCallRate,
      "QualByDepth<" + this.minQD,
      "InbreedingCoef<" + this.minInbreeding,
      "MQRankSum<" + this.minMQRankSum,
      "FS>[" + this.maxFSIndel + ";" + this.maxFSSNP + "]",
      "SOR>[" + this.maxSORIndel + ";" + this.maxSORSNP + "]",
      "MQ<[" + this.minMQIndel + ";" + this.minMQSNP + "]",
      "ReadPosRankSum<[" + this.minRPRSIndel + ";" + this.minRPRSSNP + "]",
      //"HQRatio<" + this.minHQRatio,
      "AltHQ<" + this.minAltHQ,
      "AbHetDev(0.5)>" + this.maxABHetDev,
      "AC=0(" + this.minDP + "<DP<" + this.maxDP + ";" + this.minGQ + "<GQ" + "; AB dev <" + this.maxABGenoDev+")",
      "AF=1(" + this.minDP + "<DP<" + this.maxDP + ";" + this.minGQ + "<GQ" + "; AB dev <" + this.maxABGenoDev+")"};
    return String.join(T, headers);
  }

  @Override
  public String[] getExtraHeaders() {
    if (ped == null)
      return null;
    ArrayList<String> groups = ped.getGroups();
    String[] headers = new String[groups.size() * 3];
    for (int i = 0; i < groups.size(); i++) {
      String group = groups.get(i);
      headers[3 * i + 0] = "##INFO=<ID=" + group + "_AC,Number=A,Type=Integer,Description=\"Allele count in genotypes, for each ALT allele for group " + group + ", in the same order as listed\">";
      headers[3 * i + 1] = "##INFO=<ID=" + group + "_AF,Number=A,Type=Float,Description=\"Allele Frequency, for each ALT allele for group " + group + ", in the same order as listed\">";
      headers[3 * i + 2] = "##INFO=<ID=" + group + "_AN,Number=1,Type=Integer,Description=\"Total number of alleles in called genotypes for group " + group + "\">";
    }
    return headers;
  }

  private String[] getAnnotation(ArrayList<String> groups, Variant v) {
    String[] ret = new String[3 * groups.size()];
    int[][] ac = new int[groups.size()][v.getAlleleCount()];
    int[] an = new int[groups.size()];

    for (Genotype g : v.getGenotypes()) {
      int i = groups.indexOf(g.getSample().getGroup());
      int[] as = g.getAlleles();
      if (as != null) //null indicates missing
        for (int a : as)
          if (a > -1) {
            an[i]++;
            ac[i][a]++;
          }
    }
    for (int i = 0; i < groups.size(); i++) {
      String group = groups.get(i);
      ret[3 * i + 0] = group + "_AC=" + ac[i][1];
      for (int a = 2; a < ac[i].length; a++)
        ret[3 * i + 0] += "," + ac[i][a];
      ret[3 * i + 1] = group + "_AF=" + (ac[i][1] / (1.0d * an[i]));
      for (int a = 2; a < ac[i].length; a++)
        ret[3 * i + 1] += "," + (ac[i][a] / (1.0d * an[i]));
      ret[3 * i + 2] = group + "_AN=" + an[i];
    }
    return ret;
  }

  @Override
  public void end() {
    Message.info("Number of Variants [Unfiltered] " + count[IDX_UNFILTERED]);
    Message.info("Number of Variants [" + FILTER_CALLRATE + "] " + count[IDX_CALL]);
    Message.info("Number of Variants [" + FILTER_CALLRATE_DISTRIBUTION + "] " + count[IDX_CALLRATE_DISTRIBUTION]);
    Message.info("Number of Variants [" + FILTER_QUAL_BY_DEPTH + "] " + count[IDX_QUAL_BY_DEPTH]);
    Message.info("Number of Variants [" + FILTER_INBREEDING_COEF + "] " + count[IDX_INBREEDING_COEF]);
    Message.info("Number of Variants [" + FILTER_MQRANKSUM + "] " + count[IDX_MQRANKSUM]);
    Message.info("Number of Variants [" + FILTER_FS + "] " + count[IDX_FS]);
    Message.info("Number of Variants [" + FILTER_SOR + "] " + count[IDX_SOR]);
    Message.info("Number of Variants [" + FILTER_MQ + "] " + count[IDX_MQ]);
    Message.info("Number of Variants [" + FILTER_READPOSRANKSUM + "] " + count[IDX_READPOSRANKSUM]);
    //Message.info("Number of Variants [" + FILTER_LOWQUAL + "] " + count[IDX_LOWQUAL]);
    Message.info("Number of Variants [" + FILTER_ALTLOWQUAL + "] " + count[IDX_ALTLOWQUAL]);
    Message.info("Number of Variants [" + FILTER_ABHET + "] " + count[IDX_ABHET]);
    Message.info("Number of Variants [" + FILTER_AC0 + "] " + count[IDX_AC0]);
    Message.info("Number of Variants [" + FILTER_AF1 + "] " + count[IDX_AF1]);

    for (Export ex : this.reportLines)
      out.println(ex.toString());

    out.close();
  }

  @Override
  public void begin() {
    super.begin();

    this.parseParameters(this.parameters.getFilename());

    try {
      out = getPrintWriter(this.report.getFilename()); //can be bgzipped if output is also bgzipped
      out.println(getHeader());
      reportLines = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_FROM_END);
    } catch (IOException e) {
      this.fatalAndDie("Unable to open report file [" + this.report.getFilename() + "]");
    }

    count[IDX_UNFILTERED] = 0;
    count[IDX_CALL] = 0;
    count[IDX_CALLRATE_DISTRIBUTION] = 0;
    count[IDX_QUAL_BY_DEPTH] = 0;
    count[IDX_INBREEDING_COEF] = 0;
    count[IDX_MQRANKSUM] = 0;
    count[IDX_FS] = 0;
    count[IDX_SOR] = 0;
    count[IDX_MQ] = 0;
    count[IDX_READPOSRANKSUM] = 0;
    //count[IDX_LOWQUAL] = 0;
    count[IDX_ALTLOWQUAL] = 0;
    count[IDX_ABHET] = 0;
    count[IDX_AC0] = 0;
    count[IDX_AF1] = 0;

    this.samples = new HashMap<>();
    if ("null".equals(this.pedfile.getFilename())) {
      String group = "NOGROUP";
      ArrayList<String> ss = new ArrayList<>();
      for (Sample sample : getVCF().getSamples())
        ss.add(sample.getId());
      this.samples.put(group, ss);
    } else {
      try {
        ped = this.pedfile.getPed();
        this.getVCF().bindToPed(ped);
        for (Sample sample : getVCF().getSamples()) {
          String group = "" + sample.getGroup()/* + sample.getPhenotype()*/;
          sample.setGroup(group);
          ped.getSample(sample.getId()).setGroup(group);
          if (!samples.containsKey(group))
            samples.put(group, new ArrayList<>());
          samples.get(group).add(sample.getId());
        }
      } catch (PedException ex) {
        this.fatalAndDie("Could not read ped file", ex);
      }
    }

    fisherET = new FisherExactTest(getVCF().getSamples().size());

    for (String key : KEYS) {
      boolean found = false;
      for (String header : getVCF().getHeadersWithoutSamples())
        if (header.startsWith("##INFO=<ID=" + key + ",")) {
          found = true;
          break;
        }
      if (!found)
        Message.warning("Input VCF seems to be missing the following annotation [" + key + "]");
    }

    getVCF().addFilter(FILTER_CALLRATE, "Call rate < " + this.minCallRate + " in any group");
    getVCF().addFilter(FILTER_CALLRATE_DISTRIBUTION, "Fisher’s exact test comparing number of missing genotypes between groups significant at p <= " + this.minFisherCallRate);
    getVCF().addFilter(FILTER_QUAL_BY_DEPTH, "Qual by depth (" + KEY_QD + ") < " + this.minQD + " or missing");
    getVCF().addFilter(FILTER_INBREEDING_COEF, "Inbreeding coefficient (" + KEY_INBREEDING + ") < " + this.minInbreeding);
    getVCF().addFilter(FILTER_MQRANKSUM, KEY_MQRANKSUM + " (Z-score From Wilcoxon rank sum test of Alt vs. Ref read mapping qualities) either < " + this.minMQRankSum + " or missing");
    getVCF().addFilter(FILTER_FS, KEY_FS + " (phred-scaled p-value using Fisher's exact test to detect strand bias) > " + this.maxFSSNP + " for SNPs or > " + this.maxFSIndel + " for indels or missing");
    getVCF().addFilter(FILTER_SOR, KEY_SOR + " (Symmetric Odds Ratio of 2x2 contingency table to detect strand bias) > " + this.maxSORSNP + " for SNPs or > " + this.maxSORIndel + " for indels or missing");
    getVCF().addFilter(FILTER_MQ, KEY_MQ + " (overall mapping quality of reads supporting a variant call) < " + this.minMQSNP + " for SNPs or < " + this.minMQIndel + " for indels or missing");
    getVCF().addFilter(FILTER_READPOSRANKSUM, KEY_READPOSRANKSUM + " (Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias) either < " + this.minRPRSSNP + " for SNP or < " + this.minRPRSIndel + " for indels");
    //getVCF().addFilter(FILTER_LOWQUAL, "Proportion of genotypes with (" + this.minDP + " <= SUM(AD) <= " + this.maxDP + " and a genotype quality (GQ) >= " + this.minGQ + ") < " + this.minHQRatio);
    getVCF().addFilter(FILTER_ALTLOWQUAL, "Number of genotypes carrying an alternative allele with (" + this.minDP + " <= SUM(AD) <= " + this.maxDP + " and a genotype quality (GQ) >= " + this.minGQ + ") < " + this.minAltHQ);
    getVCF().addFilter(FILTER_ABHET, "Mean allelic balance calculated over heterozygous genotypes was within [" + (0.5 - this.maxABHetDev) + "-" + (0.5 + this.maxABHetDev) + "] in each group (if heterozygous genotypes called)");
    getVCF().addFilter(FILTER_AC0, "AC is equals to 0, genotype with " + this.maxDP + "<SUM(AD) or SUM(AD)<" + this.minDP + " or QC<" + this.minGQ + " or "+this.maxABGenoDev+" < AB dev" + " are set to missing");
    getVCF().addFilter(FILTER_AF1, "AF is equals to 1 (monomorphic site), genotype with " + this.maxDP + "<SUM(AD) or SUM(AD)<" + this.minDP + " or QC<" + this.minGQ + " or "+this.maxABGenoDev+" < AB dev" + " are set to missing");
  }

  /*
    1.	VQSR tranche was either PASS or 90.00to99.00 (regardless variant type – SNP or indel) //will not be implemented
    2.	Call rate ≥ 0.9 in every group
    3.	Fisher’s exact test comparing number of missing genotypes between groups not significant at 0.1% (p>0.001)
    4.	Qual by depth (QD) ≥ 2
    5.	Inbreeding coefficient (InbreedingCoeff) either ≥(-0.8) or not calculated
    6.	MQRankSum (Z-score From Wilcoxon rank sum test of Alt vs. Ref read mapping qualities) either ≥(-12.5) or not calculated
    7.	FS (phred-scaled p-value using Fisher's exact test to detect strand bias) ≤60 for SNPs or ≤200 for indels
    8.	SOR (Symmetric Odds Ratio of 2x2 contingency table to detect strand bias) ≤3 for SNPs or ≤10 for indels
    9.	MQ (overall mapping quality of reads supporting a variant call) ≥40 for SNPs or ≥10 for indels
    10.	ReadPosRankSum (Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias) either ≥(-8) for SNP or ≥(-20) for indels, or not calculated
    11.	Proportion of genotypes with a depth ≥10 and a genotype quality (gq) ≥20 ≥ 0.8
    12.	At least one of the genotypes carrying an alternative allele with a depth ≥10 and a gq ≥20
    13.	Mean allelic balance calculated over heterozygous genotypes was within [25%-75%] in each group (not relevant if no heterozygous genotypes called)
   */
  @Override
  public String[] processInputVariant(Variant variant) {
    Export export = new Export(variant);
    Info info = variant.getInfo();

    //DONE Qual by depth (QD) ≥ 2
    if (this.enableMinQD)
      try {
        double d = new Double(info.getAnnot(KEY_QD));
        if (d < this.minQD)
          export.qualByDepth = d + "";
      } catch (NullPointerException | NumberFormatException e) {
        export.qualByDepth = missing;
      }

    //DONE Inbreeding coefficient (InbreedingCoeff) either ≥(-0.8) or not calculated
    if (this.enableMinInbreeding)
      try {
        double d = new Double(info.getAnnot(KEY_INBREEDING));
        if (d < this.minInbreeding)
          export.inbreedingCoef = d + "";
      } catch (NullPointerException | NumberFormatException e) {
        //Nothing
      }
    //DONE MQRankSum (Z-score From Wilcoxon rank sum test of Alt vs. Ref read mapping qualities) either ≥(-12.5) or not calculated
    if (this.enableMinMQRankSum)
      try {
        double d = new Double(info.getAnnot(KEY_MQRANKSUM));
        if (d < this.minMQRankSum)
          export.mqRankSum = d + "";
      } catch (NullPointerException | NumberFormatException e) {
        //Nothing
      }

    //The following annotations have different threshold values for SNPs and INDEL, but there is only one value per line, so :
    //-if only SNP -> SNP
    //-if only INDEL -> INDEL
    //-if SNP&INDEL -> SNP
    if (variant.hasSNP()) {
      //FS (phred-scaled p-value using Fisher's exact test to detect strand bias) ≤60 for SNPs or ≤200 for indels
      if (enableMaxFSSNP)
        try {
          double d = new Double(info.getAnnot(KEY_FS));
          if (d > this.maxFSSNP)
            export.fs = d + "";
        } catch (NullPointerException | NumberFormatException e) {
          export.fs = missing;
        }
      //SOR (Symmetric Odds Ratio of 2x2 contingency table to detect strand bias) ≤3 for SNPs or ≤10 for indels
      if (enableMaxSORSNP)
        try {
          double d = new Double(info.getAnnot(KEY_SOR));
          if (d > this.maxSORSNP)
            export.sor = d + "";
        } catch (NullPointerException | NumberFormatException e) {
          export.sor = missing;
        }
      //MQ (overall mapping quality of reads supporting a variant call) ≥40 for SNPs or ≥10 for indels
      if (enableMinMQSNP)
        try {
          double d = new Double(info.getAnnot(KEY_MQ));
          if (d < minMQSNP)
            export.mq = d + "";
        } catch (NullPointerException | NumberFormatException e) {
          export.mq = missing;
        }
      //ReadPosRankSum (Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias) either ≥(-8) for SNP or ≥(-20) for indels, or not calculated
      if (enableMinRPRSSNP)
        try {
          double d = new Double(info.getAnnot(KEY_READPOSRANKSUM));
          if (d < minRPRSSNP)
            export.readPosRankSum = d + "";
        } catch (NullPointerException | NumberFormatException e) {
          //Nothing
        }
    } else {
      //FS (phred-scaled p-value using Fisher's exact test to detect strand bias) ≤60 for SNPs or ≤200 for indels
      if (enableMaxFSIndel)
        try {
          double d = new Double(info.getAnnot(KEY_FS));
          if (d > maxFSIndel)
            export.fs = d + "";
        } catch (NullPointerException | NumberFormatException e) {
          export.fs = missing;
        }
      //SOR (Symmetric Odds Ratio of 2x2 contingency table to detect strand bias) ≤3 for SNPs or ≤10 for indels
      if (enableMaxSORIndel)
        try {
          double d = new Double(info.getAnnot(KEY_SOR));
          if (d > maxSORIndel)
            export.sor = d + "";
        } catch (NullPointerException | NumberFormatException e) {
          export.sor = missing;
        }
      //MQ (overall mapping quality of reads supporting a variant call) ≥40 for SNPs or ≥10 for indels
      if (enableMinMQIndel)
        try {
          double d = new Double(info.getAnnot(KEY_MQ));
          if (d < minMQIndel)
            export.mq = d + "";
        } catch (NullPointerException | NumberFormatException e) {
          export.mq = missing;
        }
      //ReadPosRankSum (Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias) either ≥(-8) for SNP or ≥(-20) for indels, or not calculated
      if (enableMinRPRSIndel)
        try {
          double d = new Double(info.getAnnot(KEY_READPOSRANKSUM));
          if (d < minRPRSIndel)
            export.readPosRankSum = d + "";
        } catch (NullPointerException | NumberFormatException e) {
          //Nothing
        }
    }

    //FS (phred-scaled p-value using Fisher's exact test to detect strand bias) ≤60 for SNPs or ≤200 for indels
    //DONE Call rate ≥ 0.9 in every group
    //DONE Fisher’s exact test comparing number of missing genotypes between groups not significant at 0.1% (p>0.001)
    //DONE Proportion of genotypes with a depth ≥10 and a genotype quality (gq) ≥20 ≥ 0.8 //global or per group ?, what about mising ? 
    //DONE At least one of the genotypes carrying an alternative allele with a depth ≥10 and a gq ≥20
    //DONE Mean allelic balance calculated over heterozygous genotypes was within [25%-75%] in each group (not relevant if no heterozygous genotypes called)
    //doesn't match description :
    //must only take into account heterozygous genotypes
    //mean of AB, and not ABhet
    //results per groups
    //a) either mean over (AB)
    //b) or recompute with AD/DP, per group : no need for annotation, more accurate
    /*for (String abhet : ("" + info.getAnnot(KEY_ABHET)).split(","))
      try {
        double d = new Double(abhet);
        if (Math.abs(0.5 - d) > MAX_ABHET_DEV) {
          filter += ";" + FILTER_ABHET;
          break;
        }
      } catch (NumberFormatException e) {
        //Ignore
      }*/
    //In this loop we update the genotype :
    //If SUM(AD) < threshold or GQ < threshold -> genotype is set to missing
    //keep track of genotypes to update AC/AN/AF
    //measure abhet
    //------count-HQ------ not anymore
    //count altHQ
    //measure callrate for each groups
    int altHQ = 0;
    //double nbHQ = 0;
    double called[] = new double[this.samples.keySet().size()];
    double total[] = new double[this.samples.keySet().size()];
    int i = 0;
    for (String group : this.samples.keySet()) {
      total[i] = this.samples.get(group).size();
      double[] numHets = new double[variant.getAlleleCount()];
      double[] denomHets = new double[variant.getAlleleCount()];
      for (String sample : this.samples.get(group)) {
        Genotype g = variant.getGenotype(sample);

        if (!g.isMissing()) {
          if ((this.enableMinDP && g.getSumAD() < this.minDP) ||
                  (this.enableMaxDP && g.getSumAD() > this.maxDP) ||
                  (this.enableMinGQ && g.getGQ() < this.minGQ) ||
                  !passAB(g))
            g.setMissing();
          else {
            //nbHQ++;
            called[i]++;
            if (g.hasAlternate())
              altHQ++;
          }

        }
        if (g.isHeterozygousDiploid()) {
          int gt1 = g.getAlleles()[0];
          int gt2 = g.getAlleles()[1];
          final int[] ad = g.getAD();
          if (ad != null) {
            numHets[gt1] += ad[gt1];
            numHets[gt2] += ad[gt2];
            denomHets[gt1] += ad[gt1] + ad[gt2];
            denomHets[gt2] += ad[gt1] + ad[gt2];
          }
        }
      }
      if (this.enableMaxABHetDev)
        for (int h = 0; h < variant.getAlleleCount(); h++) {
          double val = Math.abs(0.5 - (numHets[h] / denomHets[h]));
          if (denomHets[h] != 0 && val > this.maxABHetDev) {
            if (export.abHet.isEmpty())
              export.abHet = val + "";
            else
              export.abHet += "," + val;
          }
        }
      i++;
    }

    variant.recomputeACAN();

    export.ac0 = "";
    if(enableAC0){    
      export.ac0 = "0";
      int[] acs = variant.getAC();
      for (i = 1; i < acs.length; i++)
        if (acs[i] != 0)
          export.ac0 = "";
    }
    
    export.af1 = "";
    if(enableAF1){
      for (double af : variant.getAF())
        if (af == 1D)
          export.af1 = "1";
    }

    if (this.enableMinCallRate)
      for (i = 0; i < total.length; i++)
        if (total[i] == 0)
          export.callRate += "," + 0;
        else {
          double af = called[i] / total[i];
          if (af < this.minCallRate)
            export.callRate += "," + af;
        }

    if (!export.callRate.isEmpty())
      export.callRate = export.callRate.substring(1);
    else
      if (this.enableMinFisherCallRate) {
        //if(!export.callRate.isEmpty())
        for (i = 0; i < total.length - 1; i++)
          for (int j = i + 1; j < total.length; j++) {
            double fisher = fisherET.twoTailed((int) called[i], (int) called[j], (int) (total[i] - called[i]), (int) (total[j] - called[j]));
            if (fisher <= this.minFisherCallRate)
              export.fisherCallRate += "," + fisher;
          }
        if (!export.fisherCallRate.isEmpty())
          export.fisherCallRate = export.fisherCallRate.substring(1);
      }

    /*double lowQual = nbHQ / variant.getGenotypes().length;
    if (this.enableMinHQRatio)
      if (lowQual < this.minHQRatio)
        export.lowQual = lowQual + "";*/
    if (this.enableMinAltHQ)
      if (altHQ < this.minAltHQ)
        export.altLowQual = altHQ + "";

    this.pushAnalysis(export);
    if (export.isFiltered())
      return NO_OUTPUT;

    if (ped != null)
      variant.addInfo(getAnnotation(new ArrayList<>(this.samples.keySet()), variant));
    return asOutput(variant);
  }

  private boolean passAB(Genotype g){
    if(this.enableMaxABGenoDev) {
      if (g.isHeterozygousDiploid()) {
        int gt1 = g.getAlleles()[0];
        int gt2 = g.getAlleles()[1];
        final int[] ad = g.getAD();
        if (ad != null) {
          double num = ad[gt1];
          double denom = ad[gt1] + ad[gt2];
          double val = Math.abs(0.5 - (num / denom));

          return denom != 0 && val <= this.maxABGenoDev;
        }
      }
    }
    return true;
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) { //TODO unsorted output
    try {
      Export export = (Export) analysis;
      if (!export.isFiltered())
        count[IDX_UNFILTERED]++;
      else {
        if (!export.callRate.isEmpty())
          count[IDX_CALL]++;
        if (!export.fisherCallRate.isEmpty())
          count[IDX_CALLRATE_DISTRIBUTION]++;
        if (!export.qualByDepth.isEmpty())
          count[IDX_QUAL_BY_DEPTH]++;
        if (!export.inbreedingCoef.isEmpty())
          count[IDX_INBREEDING_COEF]++;
        if (!export.mqRankSum.isEmpty())
          count[IDX_MQRANKSUM]++;
        if (!export.fs.isEmpty())
          count[IDX_FS]++;
        if (!export.sor.isEmpty())
          count[IDX_SOR]++;
        if (!export.mq.isEmpty())
          count[IDX_MQ]++;
        if (!export.readPosRankSum.isEmpty())
          count[IDX_READPOSRANKSUM]++;
   /*     if (!export.lowQual.isEmpty())
          count[IDX_LOWQUAL]++;*/
        if (!export.altLowQual.isEmpty())
          count[IDX_ALTLOWQUAL]++;
        if (!export.abHet.isEmpty())
          count[IDX_ABHET]++;
        if (!export.ac0.isEmpty())
          count[IDX_AC0]++;
        if (!export.af1.isEmpty())
          count[IDX_AF1]++;

        reportLines.add(export);
      }
      return true;
    } catch (Exception e) {
      //Ignores
    }
    return false;
  }

  private class Export implements Comparable<Export> {

    private final String chrom;
    private final String pos;
    private final String id;
    private final String ref;
    private final String alt;

    private String callRate = "";
    private String fisherCallRate = "";
    private String qualByDepth = "";
    private String inbreedingCoef = "";
    private String mqRankSum = "";
    private String fs = "";
    private String sor = "";
    private String mq = "";
    private String readPosRankSum = "";
    //private String lowQual = "";
    private String altLowQual = "";
    private String abHet = "";
    private String ac0 = "";
    private String af1 = "";

    Export(Variant variant) {
      this(variant.getChrom(), variant.getPos() + "", variant.getId(), variant.getRef(), variant.getAlt());
    }

    Export(String chrom, String pos, String id, String ref, String alt) {
      this.chrom = chrom;
      this.pos = pos;
      this.id = id;
      this.ref = ref;
      this.alt = alt;
    }

    boolean isFiltered() {
      return !callRate.isEmpty()
              || !fisherCallRate.isEmpty()
              || !qualByDepth.isEmpty()
              || !inbreedingCoef.isEmpty()
              || !mqRankSum.isEmpty()
              || !fs.isEmpty()
              || !sor.isEmpty()
              || !mq.isEmpty()
              || !readPosRankSum.isEmpty()
      //        || !lowQual.isEmpty()
              || !altLowQual.isEmpty()
              || !abHet.isEmpty()
              || !ac0.isEmpty()
              || !af1.isEmpty();
    }

    @Override
    public String toString() {
      LineBuilder sb = new LineBuilder(chrom);
      sb.addColumn(pos);
      sb.addColumn(id);
      sb.addColumn(ref);
      sb.addColumn(alt);
      sb.addColumn(callRate);
      sb.addColumn(fisherCallRate);
      sb.addColumn(qualByDepth);
      sb.addColumn(inbreedingCoef);
      sb.addColumn(mqRankSum);
      sb.addColumn(fs);
      sb.addColumn(sor);
      sb.addColumn(mq);
      sb.addColumn(readPosRankSum);
   //   sb.addColumn(lowQual);
      sb.addColumn(altLowQual);
      sb.addColumn(abHet);
      sb.addColumn(ac0);
      sb.addColumn(af1);
      return sb.toString();
    }

    @Override
    public int compareTo(Export exp) {
      if (exp == null)
        return 1;

      int comp = Variant.compare(this.chrom, new Integer(this.pos), exp.chrom, new Integer(exp.pos));
      if (comp == 0)
        return (this.ref + " " + this.alt).compareTo(exp.ref + " " + exp.alt);
      return comp;
    }
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[]{
      new CustomScript("vcf", "ped", "default.tsv"),
      new CustomScript("vcf.ozvan", "ped.ozvan", "ozvan.tsv"),
      new CustomScript("vcf", "ped", "noACAF.tsv")
    };
  }

  class CustomScript extends TestingScript {

    private final String datedReportFilename;
    private final String undatedReportFilename;

    CustomScript(String vcf, String ped, String paramfile) {
      super(TestingScript.FILE, 1);
      this.addAnonymousFilename("vcf", vcf);
      this.addAnonymousFilename("ped", ped);
      this.addNamingFilename("opt", paramfile);
      
      String tmpParam = (paramfile.endsWith(".tsv")) ? paramfile.substring(0, paramfile.length() - 4) : paramfile;
      this.undatedReportFilename  = "$DIR/report." + tmpParam;
      this.datedReportFilename = this.undatedReportFilename + ".$r";
    }

    @Override
    public String getArguments() {      
      return super.getArguments() + " --report " + datedReportFilename;
    }

    @Override
    public LineBuilder testSingleFile() {
      LineBuilder out = super.testSingleFile();
      out.newLine();
      out.newLine();
      out.newLine("dif=`diff $exp.report " + datedReportFilename + " | wc -l`;");
      out.newLine();
      out.newLine("if [ \"$dif\" -eq \"" + 0 + "\" ]");
      out.newLine("then");
      out.newLine(TAB,"echo \"${BASH_SOURCE[0]} OK\";");
      out.newLine(TAB,"rm -rf " + this.undatedReportFilename + ".*.OK;");
      out.newLine(TAB,"mv "+datedReportFilename+" "+datedReportFilename+".OK;");
      out.newLine("else");
      out.newLine(TAB,">&2 echo \"${BASH_SOURCE[0]} KO\";");
      out.newLine(TAB,"mv "+datedReportFilename+" "+datedReportFilename+".KO;");
      out.newLine("fi");
      return out;
    }
  }
}
