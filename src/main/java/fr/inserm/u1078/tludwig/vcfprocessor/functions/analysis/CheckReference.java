package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.FastaException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Fasta;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FastaFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * For every position in the vcf file, compares the reference from the VCF to the one in the fasta
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-06-22
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-09-22 
 */
public class CheckReference extends ParallelVCFFunction<Object> {
  private Fasta fasta;

  private final FastaFileParameter refFile = new FastaFileParameter();
  private static final String[] HEADER = {"CHROM","POS","VCF_REF","FASTA_REF"};

  @Override
  public String getSummary() {
    return "For every position in the vcf file, compares the reference from the VCF to the one in the fasta";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("For every position in the vcf file, gets the reference from the given fasta and prints :")
            .addColumns(HEADER)
            .addWarning("Lines containing indels are ignored");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.DROP); }

  @SuppressWarnings("unused")
  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    try {
      fasta = this.refFile.getFasta();
    } catch (FastaException e) {
      Message.fatal("Unable to open fasta file " + this.refFile.getFilename(), e, true);
    }
  }

  @SuppressWarnings("unused")
  @Override
  public void end() {
    fasta.close();
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T, HEADER)};
  }

  @Override
  public String[] processInputRecord(VariantRecord record) {
    String chrom = record.getChrom();
    int pos = record.getPos();
    String ref = record.getRef();
    boolean onlySNVs = ref.length() == 1;
    if(onlySNVs){
      for(String alt : record.getAlts())
        if(alt.length() != 1){
          onlySNVs = false;
          break;
        }            
    }

    if (onlySNVs) {
      try {
        char vcfRef = ref.charAt(0);
        if(!isValid(vcfRef))
          Message.warning("Unexpected vcf allele ["+vcfRef+"]");
        char fastaRef = fasta.getCharacterFor(chrom, pos);
        if(!isValid(fastaRef))
          Message.warning("Unexpected fasta allele ["+fastaRef+"]");
        if (vcfRef != fastaRef)
          return new String[]{chrom + T + pos + T + vcfRef + T + fastaRef};
      } catch (FastaException ex) {
        Message.fatal("Unable to process line\n"+record, ex, true);
      }
    }
    return NO_OUTPUT;
  }
  
  private static boolean isValid(char a){
    switch(a){
      case 'A' :
      case 'a' :
      case 'C' :
      case 'c' :
      case 'G' :
      case 'g' :
      case 'T' :
      case 't' :      
        return true;
      default :
        return false;
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("ref", "ref");
    return new TestingScript[]{def};
  }
}
