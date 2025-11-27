package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFHandling;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.VCFFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.Date;

/**
 * Merges SNP and INDEL results files from VQSR
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2021-01-14
 * Checked for release on XXXX-XX-XX
 * Unit Test defined on   XXXX-XX-XX
 */
public class MergeVQSR extends Function implements VCFHandling {
  public final VCFFileParameter snpFile = new VCFFileParameter(OPT_SNP, "snp.vcf", "File containing SNP output from VQSR");
  public final VCFFileParameter indelFile = new VCFFileParameter(OPT_INDEL, "indel.vcf", "File containing INDEL output from VQSR");

  @Override
  public String getSummary() {
    return "Merges SNP and INDEL results files from VQSR";
  }

  @Override
  public final Description getDescription() {
    Description desc = this.getDesc();
    if (getVCFPolicies().isNeedVEP())
      desc.addWarning("The input VCF File must have been previously annotated with vep.");
    for(String custom : getVCFPolicies().getCustomRequirements())
      if (custom != null && !custom.isEmpty())
        desc.addWarning(custom);
    VCFPolicies.MultiAllelicPolicy multi = getVCFPolicies().getMultiAllelicPolicies();
    if(!VCFPolicies.MultiAllelicPolicy.NA.equals(multi)) desc.addNote(multi.getDescription());
    return desc;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary());
  }

  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.KEEP_IF_ONE_SATISFY); }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    try(
        UniversalReader sin = snpFile.getReader();
        UniversalReader iin = indelFile.getReader()
    ) {
      String snp;
      String indel;

      boolean run = true;
      int count = 0;
      long start = new Date().getTime();

      while (run) {
        snp = sin.readLine();
        indel = iin.readLine();

        if (snp != null && indel != null) {
          count++;
          if (count % 10000 == 0)
            Message.info(progression("variants", count, snpFile.getFilename()+" / "+indelFile.getFilename(),start));
          process(snp, indel);
        } else {
          run = false;
          if (snp != null)
            Message.error("Reached end of INDEL file before reaching end of SNP file");
          if (indel != null)
            Message.error("Reached end of SNP file before reaching end of INDEL file");
        }
      }
      Message.info(progression("variants", count, snpFile.getFilename()+" / "+indelFile.getFilename(),start));
    }
  }

  /*public void progress(long start, long now, int count){
    double durs = (now - start)*.001;
    int rate = (int)(count/durs);
    Message.info(count+" variants processed in "+durs+"s. Rate: "+rate+" variants per seconds");
  }*/

  private String extract (String snp, String indel){
    return "SNP["+
            snp.substring(0,Math.min(50, snp.length()))+
            "] INDEL["+
            indel.substring(0,Math.min(50, indel.length()))+
            "]";
  }

  private void process(String snp, String indel){
    if(snp.charAt(0) != indel.charAt(0)){
      Message.error("Mismatched lines "+extract(snp, indel));
      return;
    }

    //header
    if(snp.charAt(0) == '#'){
      println(snp);
      if(!snp.equals(indel))
        println(indel);
      return;
    }
    //variant
    ShortVariant snpF = new ShortVariant(snp);
    ShortVariant indelF = new ShortVariant(indel);

    if(!snpF.matches(indelF)) {
      Message.error("Mismatched lines " + extract(snp, indel));
      return;
    }

    boolean skipS = snpF.filter.equals(".");
    boolean skipI = indelF.filter.equals(".");

    if(skipS == skipI){
      Message.error("Both filters are "+snpF.filter+"/"+indelF.filter+" "+extract(snp, indel));
      return;
    }

    if(skipS)
      println(indel);
    else
      println(snp);

  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  public static class ShortVariant {
    private final String chr;
    private final String pos;
    private final String id;
    private final String ref;
    private final String alt;
    private final String filter;

    public ShortVariant(String line){
      int i = 0;
      char c;
      StringBuilder buffer = new StringBuilder();
      while(i < line.length() && (c = line.charAt(i)) != '\t') {
        i++;
        buffer.append(c);
      }
      i++;
      chr = buffer.toString();

      buffer = new StringBuilder();
      while(i < line.length() && (c = line.charAt(i)) != '\t') {
        i++;
        buffer.append(c);
      }
      i++;
      pos = buffer.toString();

      buffer = new StringBuilder();
      while(i < line.length() && (c = line.charAt(i)) != '\t') {
        i++;
        buffer.append(c);
      }
      i++;
      id = buffer.toString();

      buffer = new StringBuilder();
      while(i < line.length() && (c = line.charAt(i)) != '\t') {
        i++;
        buffer.append(c);
      }
      i++;
      ref = buffer.toString();

      buffer = new StringBuilder();
      while(i < line.length() && (c = line.charAt(i)) != '\t') {
        i++;
        buffer.append(c);
      }
      i++;
      alt = buffer.toString();

      buffer = new StringBuilder();
      while(i < line.length() && (c = line.charAt(i)) != '\t') {
        i++;
        buffer.append(c);
      }
      i++;

      buffer = new StringBuilder();
      while(i < line.length() && (c = line.charAt(i)) != '\t') {
        i++;
        buffer.append(c);
      }
      filter = buffer.toString();
    }

    public boolean matches(ShortVariant sv){
      if(!chr.equals(sv.chr))
        return false;
      if(!pos.equals(sv.pos))
        return false;
      if(!id.equals(sv.id))
        return false;
      if(!ref.equals(sv.ref))
        return false;
      return alt.equals(sv.alt);
    }
  }
}
