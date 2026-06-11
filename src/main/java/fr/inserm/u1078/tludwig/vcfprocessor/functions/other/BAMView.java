package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.AlignmentRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.BAMRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.SAMFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

public class BAMView extends SAMFunction<Object> {

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
  public Println[] processInputRecord(AlignmentRecord record) {
    BAMRecord bamRecord = (BAMRecord)record;
    return new Println[]{new Println(bamRecord.getPointer(), T, bamRecord.getRefId(), T, bamRecord.getPos(), T, "bin[", bamRecord.getBin(), "]")};
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }
}
