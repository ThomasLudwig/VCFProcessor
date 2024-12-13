package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.Main;
import fr.inserm.u1078.tludwig.vcfprocessor.files.*;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.AlignmentRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.SAM;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.SAMException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.SAMHeader;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.SAMFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThread;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThreadFactory;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class SAMFunction<T> extends Function {
  public SAMFileParameter samFile = new SAMFileParameter(OPT_BAM, "sample1.bam", "The bam file to process");
  private SAM sam;
  public static final int STEP1000000 = 1000000;
  public static final int QUEUE_DEPTH = 200;
  public static final int WORKERS = Math.max(1, Math.min(8, Runtime.getRuntime().availableProcessors() - 3));//Number of workers, there must be one consumer and one reader
  private LinkedBlockingQueue<SAMFunction.Output> outputLines;
  private Analyzer analyzer;

  public void processOutput(String line) {
    println(line);
  }

  public static String[] asOutput(Variant variant){
    return new String[]{variant.toString()};
  }

  @SuppressWarnings("unused")
  @Override
  public final void executeFunction() throws Exception {
    Bed bed = null; //TODO fetch
    this.openBAM(bed);

    this.begin();
    this.printHeaders();
    this.outputLines = new LinkedBlockingQueue<>(20 * WORKERS);

    ExecutorService threadPool = Executors.newFixedThreadPool(WORKERS + 2, new WellBehavedThreadFactory());


    analyzer = new Analyzer();
    analyzer.start();

    try {
      SAM.Reader reader = getSAM().getReaderAndStart();
      threadPool.submit(new Consumer());

      for (int i = 0; i < WORKERS; i++)
        threadPool.submit(new Worker(reader));

      threadPool.shutdown();

      if(!threadPool.awaitTermination(300, TimeUnit.DAYS))
        Message.error("Thread reached its timeout");
    } catch (InterruptedException ignore) { }

    analyzer.willEnd();
    //Wait for analyzer to finish consuming
    while(this.isStillConsuming())
      TimeUnit.MILLISECONDS.sleep(10);

    this.sam.printAlignmentsKept();
    this.end();
    this.printFooters();
  }

  public void putOutput(int n, String[] lines) {
    try {
      this.outputLines.put(new SAMFunction.Output(n, lines));
    } catch (InterruptedException ignore) { }
  }

  public void putEOFOutput(SAMFunction.Output output) {
    try {
      this.outputLines.put(output);
    } catch (InterruptedException ignore) { }
  }

  public boolean processInputAndPushOutput(SAM.IndexedRecord indexedRecord) {
    int index = indexedRecord.index;

    if (indexedRecord.isEOF()) {
      this.putEOFOutput(SAMFunction.Output.eofOutput(index));
      return false;
    }
    AlignmentRecord record = indexedRecord.getRecord();
    try {
      String[] output =
          record.isFiltered()
              ? new String[0]
              : this.processInputRecord(indexedRecord.getRecord());
      if(output == null)
        throw new RuntimeException("Trying to push an empty output for "+index+"th Record");
      this.putOutput(indexedRecord.index, output);
    } catch (Exception e) {
      Message.fatal("Unable to process record \n" + indexedRecord.getRecord(), e, true);
    }
    return true;
  }

  @SuppressWarnings("unused")
  public void processAnalysis(T analysis) {}

  public final void pushAnalysis(T analysis) {
    this.analyzer.push(new SAMFunction.AnalysisWrapper<>(analysis));
  }

  public void openBAM(Bed bed) throws SAMException, IOException {
    this.setSAM(this.samFile.getSAM(bed));
  }
  public void setSAM(SAM sam) { this.sam = sam; }

  public SAM getSAM() {
    return this.sam;
  }

  @SuppressWarnings("unused")
  public void begin() {
    Message.info("Starting at " + new Date());
  }

  @SuppressWarnings("unused")
  public String[] getExtraHeaders() {
    return new String[]{getFunctionHeader()};
  }

  public String getFunctionHeader() {
    String key = "PG";
    ArrayList<Map.Entry<String, String>> entries = new ArrayList<>();
    entries.add(new AbstractMap.SimpleEntry<>("ID", Main.TITLE));
    entries.add(new AbstractMap.SimpleEntry<>("PN", Main.TITLE));
    entries.add(new AbstractMap.SimpleEntry<>("PP", this.getClass().getSimpleName()));
    entries.add(new AbstractMap.SimpleEntry<>("VN", Main.getVersion()));
    entries.add(new AbstractMap.SimpleEntry<>("CL", Main.getCommandParser().getCommandLine()));
    return new SAMHeader.HeaderRecord(key, entries).toString();
  }

  @SuppressWarnings("unused")
  public SAMHeader.HeaderRecord[] getHeaders() {
    getSAM().getHeaders().addExtraHeaders(getExtraHeaders());
    return getSAM().getHeaders().getHeaderRecords();
  }

  public final void printHeaders() { //can't move begin/headers/execute/end/footers be moved to Function, getHeaders might need to production differents headers for different output
    SAMHeader.HeaderRecord[] headers = this.getHeaders();
    if (headers != null)
      for (SAMHeader.HeaderRecord header : headers)
        println(header);
  }

  @SuppressWarnings("unused")
  public String[] getFooters() {
    return null;
  }

  public final void printFooters() {
    String[] footers = this.getFooters();
    if (footers != null)
      for (String footer : footers)
        println(footer);
  }

  @SuppressWarnings("unused")
  public void end() {
    //No default
  }

  public abstract String[] processInputRecord(AlignmentRecord record);

  public class Worker extends WellBehavedThread {
    private final SAM.Reader reader;

    public Worker(SAM.Reader lr) {
      this.reader = lr;
    }

    @Override
    public void doRun() {
      while (processInputAndPushOutput(reader.nextIndexedRecord()));
    }
  }

  public class Analyzer extends WellBehavedThread {

    private final LinkedBlockingQueue<SAMFunction.AnalysisWrapper<T>> analyzes;
    private boolean stillRunning = true;

    public Analyzer() {
      this.analyzes = new LinkedBlockingQueue<>(QUEUE_DEPTH);
    }

    @Override
    public void doRun() {
      while (stillRunning)
        try {
          SAMFunction.AnalysisWrapper<T> analysis = analyzes.take();
          if (analysis.isEOF())
            stillRunning = false;
          else
            processAnalysis(analysis.value);
        } catch (InterruptedException ignore) { }
    }

    public void push(SAMFunction.AnalysisWrapper<T> analysis) {
      try {
        analyzes.put(analysis);
      } catch (InterruptedException ignore) { }
    }

    private void willEnd() {
      this.push(new SAMFunction.AnalysisWrapper<>(true));
    }
  }

  public static class AnalysisWrapper<T> {
    private final T value;
    private final boolean eof;

    public AnalysisWrapper(T value) {
      this.value = value;
      this.eof = false;
    }

    public AnalysisWrapper(boolean eof) {
      this.value = null;
      this.eof = eof;
    }

    public T getValue() {
      return value;
    }

    public boolean isEOF() {
      return eof;
    }
  }

  public static class Output {
    public final int n;
    public final String[] lines;

    /**
     * Ouput
     * @param n the order ?
     * @param lines the output lines
     */
    public Output(int n, String[] lines) {
      this.n = n;
      if(lines == null)
        throw new RuntimeException("Trying to create a null Output");
      this.lines = lines;
    }

    private Output(int n) {
      this.n = n;
      this.lines = null;
    }

    public static SAMFunction.Output eofOutput(int n){
      return new SAMFunction.Output(n);
    }

    public boolean isEOF() {
      return lines == null;
    }
  }

  public class Consumer extends WellBehavedThread {
    private final ArrayList<SAMFunction.Output> dequeuedOutput;
    private long start;

    public Consumer() {
      this.dequeuedOutput = new ArrayList<>();
    }

    private boolean process(SAMFunction.Output out) {
      if (out.n % STEP1000000 == 0)
        Message.info(progression("alignments", out.n, sam.getFilename(), start));

      if(out.isEOF()){
        Message.info(progression("alignments", out.n-1, sam.getFilename(), start));
        return false;
      }

      //Process output
      for (String line : out.lines)
        processOutput(line);

      return true;
    }

    private SAMFunction.Output remove(int nb) {
      for (int i = 0; i < this.dequeuedOutput.size(); i++)
        if (this.dequeuedOutput.get(i).n == nb)
          return this.dequeuedOutput.remove(i);

      return null;
    }

    @Override
    public void doRun() {
      start = new Date().getTime();
      boolean running = true;
      int nb = 1;
      try {
        while (running) {
          SAMFunction.Output out = outputLines.take();
          if (out.n == nb) {
            if (!process(out)) {
              running = false;//Don't simplify with running = !process(), reactivation
            }
            nb++;
          } else { //out.n > nb
            this.dequeuedOutput.add(out);
            SAMFunction.Output lines;// = this.dequeuedOutput.remove(nb);
            while ((lines = remove(nb)) != null) {
              if (!process(lines)) {
                running = false;//Don't simplify with running = !process(), reactivation
              }
              nb++;
            }
          }
        }
      } catch (InterruptedException ignore) { }
    }
  }

  public boolean isStillConsuming(){
    return this.analyzer.stillRunning;
  }
}
