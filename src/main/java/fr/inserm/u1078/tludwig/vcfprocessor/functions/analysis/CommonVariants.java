package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF.Reader;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF.Wrapper;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.VCFFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * Displays the list of variants that are common to two VCF files
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2019-11-28
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-14
 */
public class CommonVariants extends ParallelVCFFunction {
  private final VCFFileParameter file = new VCFFileParameter(OPT_FILE, "smallest.file.vcf", "the smallest of the two input VCF files (can be gzipped)");
  TreeSet<Canonical> variants;

  @Override
  public String getSummary() {
    return "Displays the list of variants that are common to two VCF files";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output is given as a list of canonical variants.")
            .addWarning("For faster execution, use --vcf with the largest file and --file with the smallest one");
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
    return OUT_TXT;
  }

  @SuppressWarnings("unused")
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    this.variants = new TreeSet<>();
    Message.info(("Loading variants from "+this.file.getFilename()));
    try {
      VCF ref = this.file.getVCF(VCF.STEP10000);
      Reader reader = ref.getReaderAndStart();      
      Wrapper wrapper = reader.nextLine();
      while((wrapper.line) != null){
        this.variants.addAll(Arrays.asList(Canonical.getCanonicals(wrapper.line)));
        wrapper = reader.nextLine();
      }
    } catch (VCFException e) {
      this.fatalAndQuit("Unable to read variants from "+this.file.getFilename(), e);
    } catch (PedException ex) {    
      this.fatalAndQuit("Unable to read ped file", ex);
    }
    Message.info((this.file.getFilename()+" variants loaded"));
  }  

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{};
  }

  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }
  
  @Override
  public String[] processInputLine(String line) {
    ArrayList<String> ret = new ArrayList<>();
    for(Canonical c :Canonical.getCanonicals(line))
      if(this.variants.contains(c))
        ret.add(c.toString());
    if(ret.isEmpty())
      return NO_OUTPUT;
    return ret.toArray(new String[0]);
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("file", "file");
    return new TestingScript[]{def};
  }
}
