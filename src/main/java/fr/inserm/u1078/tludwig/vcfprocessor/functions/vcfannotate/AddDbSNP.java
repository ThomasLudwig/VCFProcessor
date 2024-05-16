package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcfannotate;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.util.HashMap;

/**
 * Adds/updates dbSNP information to the VCF from a dbSNP release file
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-03-13
 * Checked for release on 2020-05-25
 * Unit Test defined on   2020-08-04
 */
public class AddDbSNP extends ParallelVCFFunction {

  public static final String RS_KEY = "RS";
  public static final String BUILD_KEY = "dbSNPBuildID";

  public static final String RS_HEADER = "##INFO=<ID=" + RS_KEY + ",Number=1,Type=String,Description=\"dbSNP RS ID\">";
  public static final String BUILD_HEADER = "##INFO=<ID=" + BUILD_KEY + ",Number=1,Type=String,Description=\"First version of dbSNP with this RS\">";
  
  public static final String KEY_RS = "RS=";
  public static final String KEY_BUILD = "dbSNPBuildID=";

  private final FileParameter refFile = new FileParameter(OPT_REF, "dbsnp.vcf", "dbSNP reference VCF File (can be gzipped)");

  private HashMap<String, String[]> dbsnp;

  @Override
  public String getSummary() {
    return "Adds/updates dbSNP information to the VCF from a dbSNP release file";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Adds dbSNP RS in ID field and INFO field")
            .addLine("Adds "+Description.code(KEY_RS)+" and "+Description.code(KEY_BUILD)+" in INFO field from the input file "+Description.code(refFile.getKey())+".");
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ANNOTATION_FOR_ALL; //TODO needs to be comma separated
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    dbsnp = new HashMap<>();
    try {
      UniversalReader in = this.refFile.getReader();
      String line;
      Message.progressInfo("Loading");
      int read = 0;
      while ((line = in.readLine()) != null) {
        if (line.startsWith("#"))
          continue;

        read++;
        if ((read % 10000) == 0)
          Message.progressInfo("Loading : " + read);

        String[] f = line.split(T);
        String key = f[VCF.IDX_CHROM] + "_" + f[VCF.IDX_POS];
        String[] info = f[VCF.IDX_INFO].split(";");

        String rs = "";
        String build = "";

        for (String inf : info) {
          if (inf.startsWith(KEY_RS))
            rs = inf.substring(KEY_RS.length());
          if (inf.startsWith(KEY_BUILD))
            build = inf.substring(KEY_BUILD.length());
        }
        dbsnp.put(key, new String[]{rs,build});
      }
      in.close();
    } catch (IOException e) {
      this.fatalAndQuit("Unable to read dbSNP file " + this.refFile.getFilename());
    }

    Message.info(("Loading done"));
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getExtraHeaders(){
    return new String[]{RS_HEADER, BUILD_HEADER};
  }

  @Override
  public String[] processInputLine(String line) {
    String[] f = line.split(T);
    String key = f[VCF.IDX_CHROM] + "_" + f[VCF.IDX_POS];
    String[] value = dbsnp.get(key);
    if (value != null) {
      f[VCF.IDX_ID] = "rs" + value[0];
      f[VCF.IDX_INFO] += ";" + RS_KEY + "=" + value[0] + ";" + BUILD_KEY + "=" + value[1];
      return new String[]{String.join(T, f)};
    }
    return new String[]{line};
  }
  
  @SuppressWarnings("unused")
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newFileTransform();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("ref", "ref");
    return new TestingScript[]{def};
  }
}
