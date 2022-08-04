
package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Compares the variants present in a VCF file to those present in a GnomAD VCF file
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2019-08-14
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-14
 */
public class CompareToGnomAD extends ParallelVCFVariantFunction {

  HashMap<String, String> gnomadData;

  private final FileParameter gnomad = new FileParameter(OPT_FILE, "GnomAD.site.vcf.gz", "GnomAD VCF File (can be gzipped)");

  public static final String[] HEADER = {"#CHR","POS","ID","REF","ALT","QUAL","CSQ","GENE","AC","AF","AN","GnomAD_AC","GnomAD_AF","GnomAD_AN"};

  @Override
  public String getSummary() {
    return "Compares the variants present in a VCF file to those present in a GnomAD VCF file";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output format will be:")
            .addColumns(HEADER);
  }

  @Override
  public boolean needVEP() {
    return true;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T, HEADER)};
  }

  @Override
  public void begin() {
    gnomadData = new HashMap<>();
    try {
      Message.info("Loading data from GnomAD");
      UniversalReader in = this.gnomad.getReader();
      String line;
      long start = new Date().getTime();
      int read = 0;
      while ((line = in.readLine()) != null)
        if (line.charAt(0) != '#') {
          read++;
          if (read % 10000 == 0) {
            double dur = (new Date().getTime() - start) / 1000.0;
            Message.progressInfo(read + " variants in " + dur + "s. (" + (int)(read / dur) + " variant/s)");
          }
          String[] f = line.split(T);
          int chr = Variant.chromToNumber(f[VCF.IDX_CHROM]);
          int pos = new Integer(f[VCF.IDX_POS]);
          String ref = f[VCF.IDX_REF];
          String[] alts = f[VCF.IDX_ALT].split(",");
          String[] infos = f[VCF.IDX_INFO].split(";");
          int[] acs = getACs(infos);
          int an = getAN(infos);
          //String[] acs = f[VCF.IDX_INFO].split(";")[0].split("=")[1].split(","); 
          //int an = new Integer(f[VCF.IDX_INFO].split(";")[2].split("=")[1]);
          for (int i = 0; i < alts.length; i++) {
            String canonical = new Canonical(chr, pos, ref, alts[i]).toString();
            int ac = acs[i];
            double af = new Double(ac) / an;
            gnomadData.put(canonical, ac + T + af + T + an);
          }
        }
      double dur = (new Date().getTime() - start) / 1000.0;
      Message.info(read + " variants in " + dur + "s. (" + (int)(read / dur) + " variant/s)");
      Message.info("GnomAD data loaded (" + gnomadData.size() + " variants). Start reading VCF file");
      in.close();
    } catch (IOException | NumberFormatException e) {
      this.fatalAndDie("Unable to read gnomAD file "+this.gnomad.getFilename(), e);
    }
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }
  
  @Override
  public String[] processInputVariant(Variant variant) {
    try {
      String[] infos = variant.getInfo().toString().split(";");
      int[] acs = getACs(infos);
      int an = getAN(infos);
      //String[] acs = info.split(";")[0].split("=")[1].split(",");
      //int an = new Integer(info.split(";")[2].split("=")[1]);
      String[] outs = new String[variant.getAlleleCount() - 1];
      for (int a = 1; a < variant.getAlleleCount(); a++) {
        int ac = acs[a - 1];
        double af = new Double(ac) / an;
        String canonical = new Canonical(variant.getChromNumber(), variant.getPos(), variant.getRef(), variant.getAllele(a)).toString();
        String gnom = gnomadData.get(canonical);
        if (gnom == null)
          gnom = T + T;
        String qual = variant.getFilter();
        if (variant.isHQ())
          qual = "HQ";
        Map<String, VEPAnnotation> csqGenes = variant.getInfo().getWorstVEPAnnotationsByGene(a);
        VEPConsequence worst = VEPConsequence.getWorst(csqGenes.values());
        ArrayList<String> genes = new ArrayList<>();
        for(String gene: csqGenes.keySet()){
          VEPAnnotation vep = csqGenes.get(gene);
          if(VEPConsequence.getWorstConsequence(vep).equals(worst))
            genes.add(gene);
        }
        outs[a - 1] = variant.getChrom() + T + variant.getPos() + T + variant.getId() + T + variant.getRef() + T + variant.getAllele(a) + T + qual + T + worst.getName() + T + String.join(",", genes) + T + acs[a - 1] + T + af + T + an + T + gnom;
      }
      return outs;
    } catch (NumberFormatException e) {
      Message.error("Problem while processing line [" + variant.toString() + "]");
      return NO_OUTPUT;
    }
  }
  
  private static int[] getACs(String[] infos){
    for(String info : infos)
      if(info.startsWith("AC=")){
        String acs[] = info.substring(3).split(",");
        int[] ret = new int[acs.length];
        for(int i = 0 ; i < acs.length; i++)
          ret[i] = new Integer(acs[i]);
        return ret;
    }
    return new int[]{-1};
  }
  
  private static int getAN(String[] infos){
    for(String info : infos)
      if(info.startsWith("AN="))
        return new Integer(info.substring(3));    
    return -1;
  }
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("file", "file");
    return new TestingScript[]{def};
  }
}
