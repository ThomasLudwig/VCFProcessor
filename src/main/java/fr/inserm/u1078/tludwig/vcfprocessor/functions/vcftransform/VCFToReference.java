package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.FastaException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Fasta;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FastaFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Outputs the given VCF File and reverts genotypes when ref/alt alleles are inverted according to given reference (as a fasta file)
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-09-09
 * Checked for release on 2020-05-26
 * Unit Test defined on   2020-09-22
 */
public class VCFToReference extends ParallelVCFFunction {

  private final FastaFileParameter reffile = new FastaFileParameter();

  private int kept = 0;
  private int reverted = 0;
  private Fasta fasta;
  
  @Override
  public String getSummary() {
    return "Outputs the given VCF File and reverts genotypes when ref/alt alleles are inverted according to given reference (as a fasta file)";
  }

  @Override
  public Description getDesc() {
    return new Description("This function can be used when ref/alt alleles might be inverted (for example when the vcf file has been converted from a plink file)")
            .addLine("At each position, the given reference genome is checked to see which allele matches the reference")
            .addLine("If none of the allele matches the reference, the line is dropped, and a warning is displayed")
            .addLine("AC/AN/AF are updated")
            .addWarning("Annotation (INFO/AD/PL/...) are not updated");
  }

  @Override
  public boolean needVEP() {
    return false;
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FORBIDDEN;
  }

  @Override
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  private String revert(String geno) {
    if (geno.length() != 3) {
      fatalAndDie("Genotypes length != 3 : [" + geno + "]");
    }
    if (geno.charAt(1) == '|') {
      char f = geno.charAt(0);
      char l = geno.charAt(2);
      if (f == '0')
        f = '1';
      else if (f == '1')
        f = '0';
      if (l == '0')
        l = '1';
      else if (l == '1')
        l = '0';
      return f + "|" + l;
    }
    if (geno.equals("0/0"))
      return "1/1";
    if (geno.equals("1/1"))
      return "0/0";
    return geno;
  }

  @Override
  public String[] processInputLine(String line) {
    try {
      String[] f = line.split(T);

      String vcfref = f[VCF.IDX_REF];
      String vcfalt = f[VCF.IDX_ALT];
      
      if(vcfref.length() != 1 || vcfalt.length() != 1){
        Message.warning("Can't process multiallelic/nonSNV variant ["+Variant.shortString(" ", f)+"], in the ref.");
        return NO_OUTPUT;
      }
      
      String fastaRef = fasta.getStringFor(f[VCF.IDX_CHROM], new Integer(f[VCF.IDX_POS]), 1);
      if (f[VCF.IDX_REF].equals(fastaRef)) {
        this.pushAnalysis(Boolean.TRUE);//kept++;
        return new String[]{line};
      }
      
      if (!f[VCF.IDX_ALT].equals(fastaRef)){ 
        Message.warning("Can't process variant ["+Variant.shortString(" ", f)+"], reference from fasta not found ["+fastaRef+"]");
        return NO_OUTPUT;
      }
      
      this.pushAnalysis(Boolean.FALSE);//reverted++;
      //Swap ref/alt
      f[VCF.IDX_REF] = vcfalt;
      f[VCF.IDX_ALT] = vcfref;
      //Revert all genotypes
      //here we assume only biallelic snps
      for (int i = VCF.IDX_SAMPLE; i < f.length; i++) {
        String[] g = f[i].split(":");
        g[0] = revert(g[0]);
        f[i] = String.join(":", g);
      }
      //TODO what about info (fields are flipped)  ex AD, AC, ....
      Variant v = this.getVCF().createVariant(String.join(T, f));
      v.recomputeACAN();
      
      return asOutput(v);
    } catch (FastaException | VCFException ex) {
      this.fatalAndDie("Unable to process Line\n"+line, ex);
      return NO_OUTPUT;
    }
  }

  @Override
  public void begin() {
    super.begin();
    try {
      fasta = this.reffile.getFasta();
    } catch (FastaException e) {
      this.fatalAndDie("Could not load fasta file", e);
    }    
  }

  @Override
  public void end() {
    Message.info("Done. " + kept + " variants kept, " + reverted + " variants reverted");
  }
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    if(Boolean.TRUE.equals(analysis)){
      kept++;
      return true;
    }
    else if(Boolean.FALSE.equals(analysis)){
      reverted++;
      return true;
    }
    return false;
  }

  
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript ts = TestingScript.newFileTransform();
    ts.addAnonymousFilename("vcf", "vcf");
    ts.addAnonymousFilename("ref", "ref");
    return new TestingScript[]{ts};
  }
}
