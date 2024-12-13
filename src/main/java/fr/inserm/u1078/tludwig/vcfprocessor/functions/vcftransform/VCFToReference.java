package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.*;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FastaFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Outputs the given VCF File and reverts genotypes when ref/alt alleles are inverted according to given reference (as a fasta file)
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-09-09
 * Checked for release on 2020-05-26
 * Unit Test defined on   2020-09-22
 */
public class VCFToReference extends ParallelVCFFunction<Boolean> {

  private final FastaFileParameter refFile = new FastaFileParameter();

  private int kept = 0;
  private int reverted = 0;
  private Fasta fasta;
  
  @Override
  public String getSummary() {
    return "Outputs the given VCF File and reverts genotypes when ref/alt alleles are inverted according to given reference (as a fasta file)";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("This function can be used when ref/alt alleles might be inverted (for example when the vcf file has been converted from a plink file)")
            .addLine("At each position, the given reference genome is checked to see which allele matches the reference")
            .addLine("If none of the allele matches the reference, the line is dropped, and a warning is displayed")
            .addLine("AC/AN/AF are updated")
            .addWarning("Annotation (INFO/AD/PL/...) are not updated");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
  }

  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FORBIDDEN;
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

  private String revert(String geno) {
    if (geno.length() != 3) {
      Message.die("Genotypes length != 3 : [" + geno + "]");
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
  public String[] processInputRecord(VariantRecord record) {
    try {
      String vcfRef = record.getRef();
      String vcfAlt = record.getAltString();
      
      if(vcfRef.length() != 1 || vcfAlt.length() != 1){
        Message.warning("Can't process multiallelic/nonSNV variant ["+record+"], in the ref.");
        return NO_OUTPUT;
      }
      
      String fastaRef = fasta.getStringFor(record.getChrom(), record.getPos(), 1);
      if (vcfRef.equals(fastaRef)) {
        this.pushAnalysis(true);//kept++;
        return new String[]{record.toString()};
      }
      
      if (!vcfAlt.equals(fastaRef)){
        Message.warning("Can't process variant ["+record+"], reference from fasta not found ["+fastaRef+"]");
        return NO_OUTPUT;
      }
      
      this.pushAnalysis(false);//reverted++;
      //Swap ref/alt
      record.setRef(vcfAlt);
      record.setAlt(vcfRef);
      //Revert all genotypes
      //here we assume only biallelic snps
      for (int i = 0; i < record.getNumberOfSamples(); i++)
        record.updateGT(i, revert(record.getGT(i)));

      //TODO what about info (fields are flipped)  ex AD, AC, ....
      Variant v = record.createVariant(getVCF());
      v.recomputeACAN();
      
      return asOutput(v);
    } catch (FastaException | VCFException ex) {
      Message.fatal("Unable to process Line\n"+record, ex, true);
      return NO_OUTPUT;
    }
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    try {
      fasta = this.refFile.getFasta();
    } catch (FastaException e) {
      Message.fatal("Could not load fasta file", e, true);
    }    
  }

  @SuppressWarnings("unused")
  @Override
  public void end() {
    Message.info("Done. " + kept + " variants kept, " + reverted + " variants reverted");
  }
  
  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Boolean isKept) {
    if(isKept)
      kept++;
    else
      reverted++;
  }

  
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript ts = TestingScript.newFileTransform();
    ts.addAnonymousFilename("vcf", "vcf");
    ts.addAnonymousFilename("ref", "ref");
    return new TestingScript[]{ts};
  }
}
