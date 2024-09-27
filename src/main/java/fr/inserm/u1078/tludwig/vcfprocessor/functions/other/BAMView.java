package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.BAM;
import fr.inserm.u1078.tludwig.vcfprocessor.files.BAMRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BAMFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

public class BAMView extends Function {
  BAMFileParameter bamFile = new BAMFileParameter(OPT_BAM, "sample1.bam", "The bam file to process");

  @Override
  public String getSummary() {
    return "Print the content of a BAM file";
  }

  @Override
  public Description getDescription() {
    return new Description("analog the samtools view");
  }

  @Override
  public String getOutputExtension() {
    return OUT_SAM;
  }

  @Override
  public void executeFunction() throws Exception {
    BAM bam = bamFile.getBAM();
    for(String header :  bam.getHeader().getSamHeader())
      println(header);

    BAMRecord record;
    int i = 0;
    while((record = bam.readNext()) != null) {
      println(record);
      if(i++ == 10)
        break;
    }
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }
}
