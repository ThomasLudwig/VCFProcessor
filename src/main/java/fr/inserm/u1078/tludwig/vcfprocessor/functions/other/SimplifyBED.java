package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-05-02
 */
public class SimplifyBED extends Function {

  private final BedFileParameter bedFile = new BedFileParameter(OPT_BED, "region.bed", "the Bed File to process");

  @Override
  public String getSummary() {
    return "Returns a simplified bed (with the smallest number of regions covering all the positions in the input bed file).";
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary())
            .addLine("This is useful when the input bed file contains several overlapping regions.");
  }

  @Override
  public String getOutputExtension() {
    return OUT_BED;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    ArrayList<Region> regions = new ArrayList<>();
    try(UniversalReader in = this.bedFile.getReader()) {
      //Bed bed = this.bedFile.getBed();

      String line;
      while ((line = in.readLine()) != null)
        if (!line.isEmpty())
          if (line.charAt(0) != '#') {
            Region r = new Region(line, Region.FORMAT_BED);
            boolean added = false;
            for (int i = 0; i < regions.size(); i++)
              if (r.compareTo(regions.get(i)) < 0) {
                regions.add(i, r);
                added = true;
                break;
              }
            if (!added)
              regions.add(r);
          }

      Message.info("Originally there are " + regions.size() + " regions in the file");
      ArrayList<Region> merged = new ArrayList<>();
      if (!regions.isEmpty()) {

        merged.add(regions.remove(0));
        while (!regions.isEmpty()) {
          Region r = regions.remove(0);
          Region c = merged.remove(merged.size() - 1);
          if (r.overlap(c))
            merged.add(Region.combine(r, c));
          else {
            merged.add(c);
            merged.add(r);
          }
        }
      }

      Message.info("After simplification there are " + merged.size() + " regions in the file");
      for (Region m : merged)
        println(m.getChrom() + T + m.getStart() + T + m.getEnd());
    }
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty(); //TODO implement
  }
}
