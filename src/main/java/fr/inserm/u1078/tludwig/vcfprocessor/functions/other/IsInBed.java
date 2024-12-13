package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PositiveIntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 17 juin 2016
 */
public class IsInBed extends Function {

  private final StringParameter chromosome = new StringParameter(OPT_CHROM, "chromosome", "chromosome name : (chr)[1-25]/X/Y/M/MT");
  private final PositiveIntegerParameter position = new PositiveIntegerParameter(OPT_POS, "Position");
  private final BedFileParameter bedFile = new BedFileParameter(OPT_BED, "region.bed", "the Bed File to process");

  @Override
  public String getSummary() {
    return "Check if a given chromosome:position is contained in a bedFile";
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary())
            .addLine("If it is : gives the region's limits")
            .addLine("Otherwise : gives the regions before and after the position");
  }

  @Override
  public String getOutputExtension() {
    return OUT_TXT;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() {
    Region previousRegion = null;
    Region nextRegion = null;

    Bed bed = this.bedFile.getBed();
    String chr = this.chromosome.getStringValue();
    int pos = this.position.getIntegerValue();

    for (Region r : bed.getRegions(this.chromosome.getStringValue())) {
      if (r.contains(chr, pos)) {
        println("The position " + this.chromosome + ":" + this.position + " has been found in bed file " + this.bedFile.getFilename());
        println(r.toString());
        return;
      }

      int low = r.getStart1Based();
      int high = r.getEnd1Based();

      if (high < pos && (previousRegion == null || high > previousRegion.getEnd1Based()))
        previousRegion = r;
      if (low > pos) {
        nextRegion = r;
        break;
      }
    }

    println("Position " + this.chromosome + ":" + this.position + " is not covered bed file " + this.bedFile);
    if (previousRegion != null) {
      println("Previous Interval");
      println(previousRegion.toString());
    }
    if (nextRegion != null) {
      println("Next Interval");
      println(nextRegion.toString());
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty(); //TODO implement
  }
}
