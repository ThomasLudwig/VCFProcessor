package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PositiveIntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-11-07
 */
public class ExtendBed extends Function {
  private final BedFileParameter bedFile = new BedFileParameter(OPT_BED, "regions.bed", "the Bed file to pad");
  private final PositiveIntegerParameter padding = new PositiveIntegerParameter(OPT_PAD, "number of bases to add left and right of each region");

  @Override
  public String getSummary() {
    return "Adds a padding to the left and right of each regions in the bed, and merges overlapping regions";
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary());
  }

  @Override
  public String getOutputExtension() {
    return OUT_BED;
  }
  
  @SuppressWarnings("unused")
  @Override
  public void executeFunction() {
    Bed bed = bedFile.getBed();
    bed.addPadding(this.padding.getIntegerValue());
    bed.print();
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty(); //TODO implement
  }
}
