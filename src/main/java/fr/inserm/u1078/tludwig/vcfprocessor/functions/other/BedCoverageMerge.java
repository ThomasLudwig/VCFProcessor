package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ListParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PositiveIntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class BedCoverageMerge extends Function {
  private final ListParameter files = new ListParameter(OPT_FILES, "", "List of input bed files containing the depth in the 4h column");
  private final PositiveIntegerParameter depth = new PositiveIntegerParameter(OPT_DP, "Minimal depth for the regions");
  final HashMap<Integer, SortedList<Region>> output = new HashMap<>();

  @Override
  public String getSummary() {
    return "Merge n input BED files containing the depth in the 4th column, on the regions where the depth is above a given threshold. Outputs a single BED file, with the number of samples for each regions in the 4th column";
  }

  @Override
  public Description getDescription() {
    return new Description(getSummary())
        .addLine("Input:")
        .addLine("n input BED files (1 per sample), without header")
        .addTable(new String[][]{{"chr","start","end","depth"}},true)
        .addLine("Output:")
        .addLine("1 output BED file, without header")
        .addTable(new String[][]{{"chr","start","end","nb_samples"}},true);
  }

  @Override
  public String getOutputExtension() {
    return OUT_BED;
  }

  @Override
  public void executeFunction() throws Exception {
    //check input
    checkInputFile();

    //merge regions
    for(String file : files.getList())
      processFile(file);

    //Sort chromosome in outputfile by number
    SortedList<Integer> chroms = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_FROM_END);
    chroms.addAll(output.keySet());
    for(int chrom : chroms)
      for(Region region : output.get(chrom))
        System.out.println(region.asBed(true));
  }

  private void checkInputFile() {
    Message.info("Checking of input files:");
    for(String filename : files.getList()) {
      File file = new File(filename);
      boolean die = false;
      if (!file.exists()) {
        Message.error("Missing input file : " + filename);
        die = true;
      }
      if (die)
        Message.die("Some input files are missing");
    }
  }

  private void processFile(String filename) {
    Message.info("Merging with bed ["+filename+"]");
    Bed bed = new Bed(filename);
    for(int chr : bed.getChromosomes())
      merge(chr, bed.getRegions(chr));
  }

  private void merge(int chr, ArrayList<Region> regions) {
    SortedList<Region> rS = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_FROM_END);
    for(Region r : regions){
      if(Integer.parseInt(r.getAnnotation()) >= depth.getIntegerValue()) {
        r.setAnnotation("1");
        rS.add(r);
      }
    }
    output.put(chr, merge(output.remove(chr), rS));
  }

  private SortedList<Region> merge(SortedList<Region> rA, SortedList<Region> rB) {
    if(rA == null || rA.isEmpty())
      return rB;
    if(rB == null || rB.isEmpty())
      return rA;

    SortedList<Region> out = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_FROM_END);
    //TODO implement here
    return out;
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }
}
