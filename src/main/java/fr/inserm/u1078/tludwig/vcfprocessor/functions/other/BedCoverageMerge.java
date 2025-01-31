package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PositiveIntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThread;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThreadFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BedCoverageMerge extends Function {
  private final FileParameter files = new FileParameter(OPT_FILE, "mybedfiles.txt", "File containing a List of [input bed files containing the depth in the 4h column]");
  private final PositiveIntegerParameter depth = new PositiveIntegerParameter(OPT_DP, "Minimal depth for the regions");
  private final PositiveIntegerParameter threads = new PositiveIntegerParameter("--threads", "Thread pool size");

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
    int bSize = 2; //After bench this is the fastest parameter
    //check input
    List<Batch[]> batchesList = divide(checkInputFile(), bSize, depth.getIntegerValue());
    for(int s = 0 ; s < batchesList.size(); s++) {
      Batch[] batches = batchesList.get(s);
      int parallelBatches = Math.min(batches.length, threads.getIntegerValue());
      int parallelChromosomes = Math.max(1, threads.getIntegerValue() / parallelBatches);

      Message.verbose("Processing Step "+s);
      ExecutorService batchPool = Executors.newFixedThreadPool(parallelBatches, new WellBehavedThreadFactory());
      for(Batch batch : batches) {
        batch.setThreadNumber(parallelChromosomes);
        batchPool.submit(batch);
      }
      batchPool.shutdown();
      if(!batchPool.awaitTermination(300, TimeUnit.DAYS))
        Message.error("Thread reached its timeout");
      boolean ignore;
      if(s > 1)
        for(Batch batch : batchesList.get(s-1)) {
          for(File f : batch.getInputs()) {
            String name = f.getName();
            String not = "";
            if(!f.delete())
              not = " not";
            Message.verbose("TMP File "+name+" was"+not+" deleted");
          }
        }
    }
  }

  public static class Worker extends WellBehavedThread {
    final HashMap<Integer, List<RegionSamples>> output;
    final Bed bed;
    final int chr;
    final int minDepth;

    public Worker(HashMap<Integer, List<RegionSamples>> output, Bed bed, int chr, int minDepth) {
      this.output = output;
      this.bed = bed;
      this.chr = chr;
      this.minDepth = minDepth;
    }

    @Override
    public void doRun() {
      List<RegionSamples> loaded = load(bed.getRegions(chr), minDepth);
      List<RegionSamples> current = output.get(chr);
      List<RegionSamples> merged = merge(current, loaded);
      output.put(chr, merged);
    }
  }

  private List<File> checkInputFile() {
    Message.info("Checking input files:");
    List<File> fileList = new ArrayList<>();
    try (UniversalReader in = files.getReader()) {
      String filename;
      while ((filename = in.readLine()) != null) {
        //for(String filename : files.getList()) {
        File file = new File(filename);
        boolean die = false;
        if (!file.exists()) {
          Message.error("Missing input file : " + filename);
          die = true;
        }
        if (die)
          Message.die("Some input files are missing");
        fileList.add(file);
      }
    } catch(IOException e) {
      Message.die("Unable to read input file list ["+files.getFilename()+"]");
    }
    return fileList;
  }



  private static List<RegionSamples> load(List<Region> bedRegions, int minDepth) {
    ArrayList<RegionSamples> rS = new ArrayList<>();
    if(minDepth != -1) {
      for (Region r : bedRegions)
        if (Integer.parseInt(r.getAnnotation()) >= minDepth)
          addAndSimplify(rS, r);
    } else {
      for (Region r : bedRegions) //here no need to simplify
        rS.add(new RegionSamples(r.getChrom(), r.getStart1Based(), r.getEnd1Based(), Integer.parseInt(r.getAnnotation())));
    }
    return rS;
  }

  private static void addAndSimplify(ArrayList<RegionSamples> regions, Region r) {
    if(regions.isEmpty() || !regions.get(regions.size() - 1).overlapsOrTouches(r)) {
      RegionSamples regionSamples = new RegionSamples(r.getChrom(), r.getStart1Based(), r.getEnd1Based(), 1);
      regions.add(regionSamples);
    } else { //Overlap
      regions.get(regions.size() - 1).setEnd1Based(r.getEnd1Based());
    }
  }

  private static List<RegionSamples> merge(List<RegionSamples> rAs, List<RegionSamples> rBs) {
    int na=0,nb=0;
    if(rAs != null)
      na = rAs.size();
    if(rBs != null)
      nb = rBs.size();
    //Message.debug("Merging "+na+" with "+nb);
    if(rAs == null || rAs.isEmpty()) return rBs;
    if(rBs == null || rBs.isEmpty()) return rAs;

    List<RegionSamples> out = new ArrayList<>();

    //init
    RegionSamples a = next(rAs);
    RegionSamples b = next(rBs);

    int nbOp = 0;
    while(a != null || b != null) {
      if(a == null) {
        out.add(b);
        b = next(rBs);
      } else if(b == null) {
        out.add(a);
        a = next(rAs);
      } else if(a.getStart1Based() == b.getStart1Based() && a.getEnd1Based() == b.getEnd1Based()) {
        out.add(add(a, b));
        a = next(rAs);
        b = next(rBs);
      } else if(a.getEnd1Based() < b.getStart1Based()) {
        out.add(a);
        a = next(rAs);
      } else if(b.getEnd1Based() < a.getStart1Based()) {
        out.add(b);
        b = next(rBs);
      } else { //overlap
        ListPair newLists = new ListPair(a, b);
        rAs.addAll(0, newLists.rA);
        rBs.addAll(0, newLists.rB);
        a = next(rAs);
        b = next(rBs);
      }
      nbOp++;
    }
    //Message.debug("Nb Operation : "+nbOp);
    return out;
  }

  private static RegionSamples next(List<RegionSamples> list) {
    RegionSamples region = null;
    try {
      region = list.remove(0);
    } catch(Exception ignore) {}
    return region;
  }

  private static RegionSamples add(RegionSamples a, RegionSamples b) {
    String chrom = a.getChrom();
    int start = a.getStart1Based();
    int end = a.getEnd1Based();
    int samples = a.getSamples() + b.getSamples();
    return new RegionSamples(chrom, start, end, samples);
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  static class RegionSamples extends Region {
    private final int samples;
    public RegionSamples(String chr, int startBase1, int endBase1, int samples){
      super(chr, startBase1, endBase1, Format.FULL_1_BASED);
      this.samples = samples;
    }

    public int getSamples() {
      return samples;
    }

    @Override
    public String toString() {
      return super.asBed(false)+"\t"+samples;
    }
  }

  private static class ListPair {
    private final List<RegionSamples> rA;
    private final List<RegionSamples> rB;

    public ListPair(RegionSamples a, RegionSamples b) {
      rA = new ArrayList<>();
      rB = new ArrayList<>();
      int sA = a.getStart1Based();
      int sB = b.getStart1Based();
      int eA = a.getEnd1Based();
      int eB = b.getEnd1Based();

      if(sA == sB){
        if(eA < eB) { //3
          //AAABBB
          //   BBB
          rA.add(a);
          rB.addAll(split(b, a.getEnd1Based() + 1));
        } else if(eA > eB) { //7
          //AAABBB
          //AAA
          rB.add(b);
          rA.addAll(split(a, b.getEnd1Based() + 1));
        } else { //a == b should not be reached
          //AAABBB
          Message.warning("Regions "+a+" and "+b+" should not have the same start and end");
          rA.add(a);
          rB.add(b);
        }
      } else if(sA < sB) {
        if(eA < eB) { //1
          //AAA
          //AAABBB
          //   BBB
          rA.addAll(split(a, b.getStart1Based()));
          rB.addAll(split(b, a.getEnd1Based() + 1));
        } else if(eA > eB) { //2
          //AAA
          //AAABBB
          //AAA
          rB.add(b);
          rA.addAll(split(a, b.getStart1Based(), b.getEnd1Based() + 1));
        } else { // eA == eB //4
          //AAA
          //AAABBB
          rB.add(b);
          rA.addAll(split(a, b.getStart1Based()));
        }
      } else { //sA > sB
        if(eA > eB) { //5
          //   BBB
          //AAABBB
          //AAA
          rA.addAll(split(a, b.getEnd1Based() + 1));
          rB.addAll(split(b, a.getStart1Based()));
        } else if(eA < eB) { //6
          //   BBB
          //AAABBB
          //   BBB
          rA.add(a);
          rB.addAll(split(b, a.getStart1Based(), a.getEnd1Based() + 1));
        } else { // eA == eB //8
          //   BBB
          //AAABBB
          rA.add(a);
          rB.addAll(split(b, a.getStart1Based()));
        }
      }
    }

    static List<RegionSamples> split(RegionSamples r, int start2) {
      List<RegionSamples> ret = new ArrayList<>();
      ret.add(new RegionSamples(r.getChrom(), r.getStart1Based(), start2 - 1, r.getSamples()));
      ret.add(new RegionSamples(r.getChrom(), start2, r.getEnd1Based(), r.getSamples()));
      return ret;
    }

    List<RegionSamples> split(RegionSamples r, int start2, int start3) {
      List<RegionSamples> ret = new ArrayList<>();
      ret.add(new RegionSamples(r.getChrom(), r.getStart1Based(), start2 - 1, r.getSamples()));
      ret.addAll(split(new RegionSamples(r.getChrom(), start2, r.getEnd1Based(), r.getSamples()), start3));
      return ret;
    }
  }

  public static class Batch extends WellBehavedThread {
    final HashMap<Integer, List<RegionSamples>> output;
    List<File> inputs;
    File outputFile;
    int minDepth;

    int threadNumber = 1;

    public Batch(File outputFile, int minDepth) {
      output = new HashMap<>();
      this.inputs = new ArrayList<>();
      this.outputFile = outputFile;
      this.minDepth = minDepth;
    }

    public void setThreadNumber(int threadNumber) {
      this.threadNumber = threadNumber;
    }

    public void addInput(File input) {
      this.inputs.add(input);
    }

    public List<File> getInputs() {
      return inputs;
    }

    public File getOutputFile() {
      return this.outputFile;
    }

    public int getMinDepth() {
      return minDepth;
    }

    @Override
    public String toString() {
      if(inputs.isEmpty())
        return "[]>"+ outputFile;
      StringBuilder out = new StringBuilder("[" + inputs.get(0));
      for(int i = 1; i < inputs.size(); i++)
        out.append(",").append(inputs.get(i));
      return out + "]>"+ outputFile;
    }

    @Override
    public void doRun() {
      try {
        process();
      } catch (InterruptedException e) {
        Message.die("Thread died "+this);
      }
      print();
    }

    public void process() throws InterruptedException {
      for (File file : getInputs()) {
        Bed bed = new Bed(file);
        ExecutorService threadPool = Executors.newFixedThreadPool(this.threadNumber, new WellBehavedThreadFactory());
        for(int chr : bed.getChromosomes())
          threadPool.submit(new Worker(output, bed, chr, getMinDepth()));

        threadPool.shutdown();
        if(!threadPool.awaitTermination(300, TimeUnit.DAYS))
          Message.error("Thread reached its timeout");
      }
    }

    private void print() {
      //Sort chromosome in outputfile by number
      SortedList<Integer> chroms = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_FROM_END);
      chroms.addAll(output.keySet());

      if(outputFile == null)
        for(int chrom : chroms)
          for(RegionSamples region : output.get(chrom))
            System.out.println(region);
      else {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
          for(int chrom : chroms)
            for(RegionSamples region : output.get(chrom))
              out.println(region);
        } catch(IOException e) {
          Message.die("Unable to write to ["+outputFile+"]");
        }
      }
    }
  }

  private static ArrayList<Batch[]> divide(final List<File> files, final int width, final int minDepth) {
    ArrayList<Batch[]> batches = new ArrayList<>();
    int batchSize = files.size() / width;
    if(files.size() % width != 0)
      batchSize++;
    Batch[] previous = new Batch[batchSize];
    for(int batch = 0; batch < batchSize; batch++) {
      previous[batch] = new Batch(files.size() > width ? tmpFile(0, batch) : null, minDepth);
      for(int input = 0; input < width && batch * width + input < files.size(); input++)
        previous[batch].addInput(files.get(batch * width + input));
    }
    batches.add(previous);

    for(int step = 1 ; previous.length > 1; step++) {
      int nSize = previous.length / width;
      if(previous.length % width != 0)
        nSize++;
      Batch[] current = new Batch[nSize];

      for(int batch = 0 ; batch < nSize; batch++) {
        current[batch] = new Batch(previous.length > width ? tmpFile(step, batch) : null, -1);
        for(int input = 0; input < width && batch * width + input < previous.length; input++)
          current[batch].addInput(previous[batch * width + input].getOutputFile());
      }
      batches.add(current);
      previous = current;
    }
    return batches;
  }

  private static File tmpFile(int step, int batch) {
    String filename = "step"+step+"-"+batch+".";
    try {
      return File.createTempFile(filename, ".tsv");
    } catch (IOException e) {
      Message.die("Unable to create temporary file ["+filename+"].tsv");
    }
    return null;
  }
}
