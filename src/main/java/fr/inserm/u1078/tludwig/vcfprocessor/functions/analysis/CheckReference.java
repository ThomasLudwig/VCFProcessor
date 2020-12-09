package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.FastaException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Fasta;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FastaFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * For every position in the vcf file, compares the reference from the VCF to the one in the fasta
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-06-22
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-09-22 
 */
public class CheckReference extends ParallelVCFFunction {  
  private Fasta fasta;

  private final FastaFileParameter reffile = new FastaFileParameter();
  private static final String[] HEADER = {"CHROM","POS","VCF_REF","FASTA_REF"};

  @Override
  public String getSummary() {
    return "For every position in the vcf file, compares the reference from the VCF to the one in the fasta";
  }

  @Override
  public Description getDesc() {
    return new Description("For every position in the vcf file, gets the reference from the given fasta and prints :")
            .addColumns(HEADER)
            .addWarning("Lines containing indels are ignored");
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
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public void begin() {
    try {
      fasta = this.reffile.getFasta();
    } catch (FastaException e) {
      this.fatalAndDie("Unable to open fasta file " + this.reffile.getFilename(), e);
    }
  }

  @Override
  public void end() {
    fasta.close();
  }

  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T, HEADER)};
  }

  @Override
  public String[] processInputLine(String line) {
    String[] fields = line.split(T);
    String chrom = fields[VCF.IDX_CHROM];
    int pos = Integer.parseInt(fields[VCF.IDX_POS]);
    String ref = fields[VCF.IDX_REF];
    boolean onlySNVs = ref.length() == 1;
    if(onlySNVs){
      for(String alt : fields[VCF.IDX_ALT].split(","))
        if(alt.length() != 1){
          onlySNVs = false;
          break;
        }            
    }

    if (onlySNVs) {
      try {
        char vcfref = ref.charAt(0);
        if(!isValid(vcfref))
          Message.warning("Unexception vcf allele ["+vcfref+"]");
        char fastaref = fasta.getCharacterFor(chrom, pos);
        if(!isValid(fastaref))
          Message.warning("Unexception fasta allele ["+fastaref+"]");
        if (vcfref != fastaref)
          return new String[]{chrom + T + pos + T + vcfref + T + fastaref};
      } catch (FastaException ex) {
        this.fatalAndDie("Unable to process line\n"+line, ex);
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
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("ref", "ref");
    return new TestingScript[]{def};
  }
}
