package fr.inserm.u1078.tludwig.vcfprocessor.functions;

import fr.inserm.u1078.tludwig.maok.tools.DateTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF.Reader;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
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
public abstract class ParallelVCFFunction extends VCFFunction {

  public static final byte[] EOL = "\n".getBytes();

  public static final int QUEUE_DEPTH = 200;
  public static final int STEP = 10000;
  public static final int DELAY = 10;
  public static final String END_MESSAGE = "XXX_NO_MORE_LINES_XXX";
  public static final String EMPTY = "ZZZ_EMPTY_ZZZ";
  public static final String[] NO_OUTPUT = new String[]{};
  public static final int WORKERS = Math.max(1, Math.min(8, Runtime.getRuntime().availableProcessors() - 3));//Number of workers, there must be one consumer and one reader

  private VCF vcf;
  private LinkedBlockingQueue<Output> outputLines;

  private Analyzer analyzer;

  public void processOutput(String line) {
    println(line);
  }

  public void begin() {
    //Nothing
  }

  public String[] getExtraHeaders() {
    return null;
  }

  public String[] getHeaders() {
    getVCF().addExtraHeaders(getExtraHeaders());
    return getVCF().getFullHeaders().toArray(new String[0]);
  }

  public final void printHeaders() {
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

  public String[] getFooters() {
    return null;
  }

  public void end() {
    //No default
  }

  public final VCF getVCF() {
    return this.vcf;
  }

  public void openVCF() throws VCFException, PedException {
    this.setVCF(this.vcffile.getVCF(VCF.STEP_OFF));
  }

  public final void setVCF(VCF vcf) {
    this.vcf = vcf;
  }
  
  public static String[] asOutput(Variant variant){
    return new String[]{variant.toString()};
  }

  @Override
  public final void executeFunction() throws Exception {
    this.openVCF();

    this.begin();
    this.printHeaders();
    this.outputLines = new LinkedBlockingQueue<>(20 * WORKERS);

    ExecutorService threadPool = Executors.newFixedThreadPool(WORKERS + 2);

    analyzer = new Analyzer();
    analyzer.start();

    try {
      Reader reader = getVCF().getReaderWithoutStarting();
      threadPool.submit(reader);

      threadPool.submit(new Consumer());

      for (int i = 0; i < WORKERS; i++)
        threadPool.submit(new Worker(reader));

      threadPool.shutdown();

      threadPool.awaitTermination(100, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      Message.error("Thread was interrupted", e);
    }

    analyzer.willEnd();
    //Wait for analyzer to finish consuming
    while(this.isStillConsuming())
      try{
        Thread.sleep(10);
      } catch(Exception e){
        //Nothing
      }
    this.vcf.printVariantKept();
    end();
    this.printFooters();
  }

  public void putOutput(int n, String[] lines) {
    try {
      this.outputLines.put(new Output(n, lines));
    } catch (InterruptedException e) {
      this.fatalAndDie("Synchronisation Exception", e);
    }
  }

  public void pushOutput(int n, String line) {
    if (END_MESSAGE.equals(line))
      this.putOutput(n, new String[]{END_MESSAGE});
    else if (VCF.FILTERED_LINE.equals(line))
      this.putOutput(n, new String[]{});
    else
      try {
        this.putOutput(n, this.processInputLine(line));
      } catch (Exception e) {
        this.fatalAndDie("Unable to process line \n" + line, e);
      }
  }

  public abstract String[] processInputLine(String line);

  public class Worker implements Runnable {

    private final VCF.Reader reader;

    public Worker(VCF.Reader lr) {
      this.reader = lr;
    }

    @Override
    public void run() {
      try {
        VCF.Wrapper wrapper = reader.nextLine();
        while (wrapper.line != null) {
          pushOutput(wrapper.index, wrapper.line);
          wrapper = getVCF().getNextLineWrapper();
        }
        pushOutput(wrapper.index, END_MESSAGE);
      } catch (VCFException e) {
        fatalAndDie("Unable to read next line from VCF file", e);
      }
    }
  }

  /**
   * Process the analysis
   * @param analysis
   * @return false if the analysis object is on an unexpected type
   */
  public abstract boolean checkAndProcessAnalysis(Object analysis);

  public final void pushAnalysis(Object analysis) {
    this.analyzer.push(analysis);
  }

  /**
   * When collection statistics and other data during the function,
   * concurrent non-atomic modification (such as value++) can collide.
   * One workaround would be to use AtomicInteger, but it is not applicable to Double and other datatypes
   * So the modification, are pushed into a Queue, and Collected be this Thread
   */
  public class Analyzer extends Thread {

    private final LinkedBlockingQueue analyzes;
    private boolean stillRunning = true;

    public Analyzer() {
      this.analyzes = new LinkedBlockingQueue(QUEUE_DEPTH);
    }

    @Override
    public void run() {
      while (stillRunning)
        try {
          Object analysis = analyzes.take();
          if (END_MESSAGE.equals(analysis))
            stillRunning = false;
          else if (!checkAndProcessAnalysis(analysis))
            Message.warning("Unexpected Analysis [" + analysis + "]");
        } catch (InterruptedException ex) {
          //Ignore ?
        }
    }

    public void push(Object analysis) {
      try {
        analyzes.put(analysis);
      } catch (InterruptedException ex) {
        //Ignores ?
      }
    }

    private void willEnd() {
      this.push(END_MESSAGE);
    }
  }

  public static class Output {

    public final int n;
    public final String[] lines;

    public Output(int n, String[] lines) {
      this.n = n;
      this.lines = lines;
    }
  }

  public class Consumer extends Thread {

    private final ArrayList<Output> unqueuedOutput;
    private long start;

    public Consumer() {
      this.unqueuedOutput = new ArrayList<>();
    }

    private boolean process(Output out) {
      boolean run = true;
      if (out.n % STEP == 0) {
        double dur = DateTools.duration(start);
        int rate = (int)(out.n / dur);
        Message.info(out.n + " variants processed from " + vcffile.getFilename() + " in " + dur + "s (" + rate + " variants/s)");
      }

      //Process output
      for (String line : out.lines)
        switch (line) {
          case END_MESSAGE:
            double dur = DateTools.duration(start);
            int rate = (int)(out.n / dur);
            Message.info(out.n - 1 + " variants processed from " + vcffile.getFilename() + " in " + dur + "s (" + rate + " variants/s)");
            run = false;
            break;
          case EMPTY:
            //Ignore
            break;
          default:
            processOutput(line);
        }
      return run;
    }

    private Output remove(int nb) {
      for (int i = 0; i < this.unqueuedOutput.size(); i++)
        if (this.unqueuedOutput.get(i).n == nb)
          return this.unqueuedOutput.remove(i);

      return null;
    }

    @Override
    public void run() {
      start = new Date().getTime();
      boolean running = true;

      int nb = 1;
      while (running)
        try {
          Output out = outputLines.take();
          if (out.n == nb) {
            if (!process(out))
              running = false;
            nb++;
          } else { //out.n > nb 
            this.unqueuedOutput.add(out);

            Output lines;// = this.unqueuedOutput.remove(nb);
            while ((lines = remove(nb)) != null) {
              if (!process(lines))
                running = false;
              nb++;
            }
          }
        } catch (InterruptedException e) {
          fatalAndDie("Consumer interrupted", e);
        }
    }
  }
  
  public boolean isStillConsuming(){
    return this.analyzer.stillRunning;
  }
}
