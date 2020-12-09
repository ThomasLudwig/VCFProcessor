package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.TabixReader;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Gets the number of lines indexed by a tabix file
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2019-09-27
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 */
public class NumberOfLinesFromTabix extends VCFFunction {

  @Override
  public String getOutputExtension() {
    return OUT_TXT;
  }

  @Override
  public String getSummary() {
    return "Gets the number of lines indexed by a tabix file";
  }

    @Override
  public Description getDesc() {
    return new Description(this.getSummary());
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
    return "The bgzipped VCF file FILENAME.vcf.gz must have an associated tabix file FILENAME.vcf.gz.tbi";
  }

  @Override
  public void executeFunction() throws Exception {
    TabixReader tabix = new TabixReader(this.vcffile.getFilename());
    long total = 0;
    for(String chr : tabix.getChromosomes()){
      long nb = tabix.getVariantCount(chr);
      total += nb;
      println(chr + " : " + nb);
    }
    println("Total : " + total);
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
