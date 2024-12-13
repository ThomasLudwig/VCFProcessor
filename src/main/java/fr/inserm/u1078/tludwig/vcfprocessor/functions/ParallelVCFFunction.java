package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF.Reader;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThread;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThreadFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-09-09
 */
public abstract class ParallelVCFFunction<T> extends VCFFunction {

  public static final int QUEUE_DEPTH = 200;
  public static final int STEP = 10000;

  public static final String[] NO_OUTPUT = new String[]{};
  public static final int WORKERS = Math.max(1, Math.min(8, Runtime.getRuntime().availableProcessors() - 3));//Number of workers, there must be one consumer and one reader

  private VCF vcf;
  private LinkedBlockingQueue<Output> outputLines;

  private Analyzer analyzer;

  public void processOutput(String line) {
    println(line);
  }

  @SuppressWarnings("unused")
  public void begin() {
    Message.info("Starting at " + new Date());
  }

  @SuppressWarnings("unused")
  public String[] getExtraHeaders() {
    return null;
  }

  @SuppressWarnings("unused")
  public String[] getHeaders() {
    getVCF().addExtraHeaders(getExtraHeaders());
    return getVCF().getFullHeaders().toArray(new String[0]);
  }

  public final void printHeaders() { //can't move begin/headers/execute/end/footers be moved to Function, getHeaders might need to production differents headers for different output
    String[] headers = this.getHeaders();
    if (headers != null)
      for (String header : headers)
        println(header);
  }

  public final void printFooters() {
    String[] footers = this.getFooters();
    if (footers != null)
      for (String footer : footers)
        println(footer);
  }

  @SuppressWarnings("unused")
  public String[] getFooters() {
    return null;
  }

  @SuppressWarnings("unused")
  public void end() {
    //No default
  }

  public final VCF getVCF() {
    return this.vcf;
  }

  public void openVCF() throws VCFException, PedException {
    this.setVCF(this.vcfFile.getVCF(VCF.STEP_OFF));
  }

  public final void setVCF(VCF vcf) {
    this.vcf = vcf;
  }
  
  public static String[] asOutput(Variant variant){
    return new String[]{variant.toString()};
  }

  @SuppressWarnings("unused")
  @Override
  public final void executeFunction() throws Exception {
    this.openVCF();

    this.begin();
    this.printHeaders();
    this.outputLines = new LinkedBlockingQueue<>(20 * WORKERS);

    ExecutorService threadPool = Executors.newFixedThreadPool(WORKERS + 2, new WellBehavedThreadFactory());


    analyzer = new Analyzer();
    analyzer.start();

    try {
      Reader reader = getVCF().getReaderAndStart();
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

    this.vcf.printVariantsKept();
    end();
    this.printFooters();
  }

  public void putOutput(int n, String[] lines) {
    try {
      this.outputLines.put(new Output(n, lines));
    } catch (InterruptedException ignore) { }
  }

  public void putEOFOutput(Output output) {
    try {
      this.outputLines.put(output);
    } catch (InterruptedException ignore) { }
  }


  /**
   * Gets an indexedRecord and return an indexOutput (the output is the result of the processedinput)
   * @param indexedRecord the input to process
   * return false if InputRecord is EOF
   */
  public boolean processInputAndPushOutput(VCF.IndexedRecord indexedRecord) {
    int index = indexedRecord.index;

    if (indexedRecord.isEOF()) {
      this.putEOFOutput(Output.eofOutput(index));
      return false;
    }
    VariantRecord record = indexedRecord.getRecord();
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

  public abstract String[] processInputRecord(VariantRecord record);

  public class Worker extends WellBehavedThread {

    private final VCF.Reader reader;

    public Worker(VCF.Reader lr) {
      this.reader = lr;
    }

    @Override
    public void doRun() {
        while (processInputAndPushOutput(reader.nextIndexedRecord()));
    }
  }

  /**
   * Process the analysis
   * @param analysis the analysis result to process
   */

  @SuppressWarnings("unused")
  public void processAnalysis(T analysis) {}

  public final void pushAnalysis(T analysis) {
    this.analyzer.push(new AnalysisWrapper<>(analysis));
  }

  /**
   * When collection statistics and other data during the function,
   * concurrent non-atomic modification (such as value++) can collide.
   * One workaround would be to use AtomicInteger, but it is not applicable to Double and other datatypes
   * So the modification, are pushed into a Queue, and Collected be this Thread
   */
  public class Analyzer extends WellBehavedThread {

    private final LinkedBlockingQueue<AnalysisWrapper<T>> analyzes;
    private boolean stillRunning = true;

    public Analyzer() {
      this.analyzes = new LinkedBlockingQueue<>(QUEUE_DEPTH);
    }

    @Override
    public void doRun() {
      while (stillRunning)
        try {
          AnalysisWrapper<T> analysis = analyzes.take();
          if (analysis.isEOF())
            stillRunning = false;
          else
            processAnalysis(analysis.value);
        } catch (InterruptedException ignore) { }
    }

    public void push(AnalysisWrapper<T> analysis) {
      try {
        analyzes.put(analysis);
      } catch (InterruptedException ignore) { }
    }

    private void willEnd() {
      this.push(new AnalysisWrapper<>(true));
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

    public static Output eofOutput(int n){
      return new Output(n);
    }

    public boolean isEOF() {
      return lines == null;
    }
  }

  public class Consumer extends WellBehavedThread {
    private final ArrayList<Output> dequeuedOutput;
    private long start;

    public Consumer() {
      this.dequeuedOutput = new ArrayList<>();
    }

    private boolean process(Output out) {
      if (out.n % STEP == 0)
        Message.info(progression("variants", out.n, vcf.getFilename(), start));

      if(out.isEOF()){
        Message.info(progression("variants", out.n-1, vcf.getFilename(), start));
        return false;
      }

      //Process output
      for (String line : out.lines)
        processOutput(line);

      return true;
    }

    private Output remove(int nb) {
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
          Output out = outputLines.take();
          if (out.n == nb) {
            if (!process(out)) {
              running = false;//Don't simplify with running = !process(), reactivation
            }
            nb++;
          } else { //out.n > nb
            this.dequeuedOutput.add(out);
            Output lines;// = this.dequeuedOutput.remove(nb);
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
