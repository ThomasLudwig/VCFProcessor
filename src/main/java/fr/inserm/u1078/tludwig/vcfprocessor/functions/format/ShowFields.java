package fr.inserm.u1078.tludwig.vcfprocessor.functions.format;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Shows selected fields of a VCF File
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-02-10
 * Checked for release on 2020-08-20
 * Unit Test defined on 2020-20-20
 */
public class ShowFields extends ParallelVCFVariantFunction {

  public static final String PREFIX_INFO = "info:";
  public static final String PREFIX_GENO = "geno:";

  public static final String KEY_CHROM = "CHROM";
  public static final String KEY_POS = "POS";
  public static final String KEY_ID = "ID";
  public static final String KEY_REF = "REF";
  public static final String KEY_ALT = "ALT";
  public static final String KEY_QUAL = "QUAL";
  public static final String KEY_FILTER = "FILTER";
  public static final String KEY_INFO = "INFO";
  public static final String KEY_FORMAT = "FORMAT";

  private final StringParameter query = new StringParameter(OPT_QUERY, "\"field1,field2,...," + PREFIX_INFO + "key1;key2;...," + PREFIX_GENO + "key1;key2;...\"", "Output columns");

  private ArrayList<String> fields;

  @Override
  public String getSummary() {
    return "Shows selected fields of a VCF File";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Query Syntax is "+Description.code("Field_1,Field_2,...,Field_n"))
            .addLine("where Field_x, is one of CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO,FORMAT")
            .addLine("or " + Description.code(PREFIX_INFO + "key1;key2;...;keyN")+" ex: " + Description.code(PREFIX_INFO + "AbHet;AC;AN;AF"))
            .addLine("or " + Description.code(PREFIX_GENO + "key1;key2;...;keyN")+" ex : " + Description.code(PREFIX_GENO + "GT;AD;GQ"));    //TODO extend to vep
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
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
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    LineBuilder sb = new LineBuilder();

    for (String key : this.query.getStringValue().split(",")) {
      if (key.toLowerCase().startsWith(PREFIX_INFO)) {
        String infoQuery = key.split(":")[1];
        for (String k : infoQuery.split(";"))
          sb.addColumn(k);
      } else if (key.toLowerCase().startsWith(PREFIX_GENO)) {
        String genoQuery = key.split(":")[1];
        String[] ks = genoQuery.split(";");
        for (Sample sample : this.getVCF().getSamples())
          for (String k : ks)
            sb.addColumn(sample.getId()).append(":").append(k);
      } else {
        sb.addColumn(key);
      }
    }
    sb.setCharAt(0, '#');
    return new String[]{sb.toString()};
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    this.fields = new ArrayList<>();
    String q = this.query.getStringValue();

    if (q.isEmpty())
      Message.die("Your query is empty");

    for (String key : q.split(","))
      if (key.toLowerCase().startsWith(PREFIX_INFO)) {
        String infoQuery = key.split(":")[1];
        for (String k : infoQuery.split(";"))
          fields.add("I:" + k);
      } else
        fields.add(key);
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    LineBuilder sb = new LineBuilder();

    for (String key : this.fields) {
      switch (key.toUpperCase()) {
        case KEY_CHROM:
          sb.addColumn(variant.getChrom());
          break;
        case KEY_POS:
          sb.addColumn(variant.getPos());
          break;
        case KEY_ID:
          sb.addColumn(variant.getId());
          break;
        case KEY_REF:
          sb.addColumn(variant.getRef());
          break;
        case KEY_ALT:
          sb.addColumn(variant.getAlt());
          break;
        case KEY_QUAL:
          sb.addColumn(variant.getQual());
          break;
        case KEY_FILTER:
          sb.addColumn(variant.getFilter());
          break;
        case KEY_INFO:
          sb.addColumn(variant.getInfo().toString());
          break;
        case KEY_FORMAT:
          sb.addColumn(variant.getFormat());
          break;
        default:
          if (key.startsWith("I:")) {
            String k = key.split("I:")[1];
            sb.addColumn(variant.getInfo().getAnnot(k));
          }
          if (key.toLowerCase().startsWith(PREFIX_GENO)) {
            String[] genoQuery = key.split(":")[1].split(";");
            LineBuilder sg = new LineBuilder();
            for (Genotype g : variant.getGenotypes())
              for (String k : genoQuery)
                sg.addColumn(g.getValue(k));
            
            if (sg.length() > 0)
              sb.addColumn(sg.substring(1));
          }
          break;
      }
    }

    return new String[]{sb.substring(1)};
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileAnalysis();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addAnonymousValue("query", "CHROM,POS,ID,REF,ALT,info:AbHet;AC;AN;AF,geno:GT;AD;GQ");
    return new TestingScript[]{scr};
  }
}
