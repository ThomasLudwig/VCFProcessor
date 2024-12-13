package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.BAI;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

public class BAITest extends Function {
  public FileParameter baiFile = new FileParameter("--bai", "myfile.bai", "a bam index file");

  @Override
  public String getSummary() {
    return "Test BAI";
  }

  @Override
  public Description getDescription() {
    return new Description("Test BAI");
  }

  @Override
  public String getOutputExtension() {
    return OUT_TXT;
  }



  @Override
  public void executeFunction() throws Exception {
    BAI bai = new BAI(baiFile.getFilename());
    //System.out.println(bai);
    //bai.testOrder();
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }
}
