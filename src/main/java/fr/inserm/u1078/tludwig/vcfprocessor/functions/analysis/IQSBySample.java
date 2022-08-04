package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.DateTools;
import fr.inserm.u1078.tludwig.maok.tools.MathTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.MultiVCFReader;
import fr.inserm.u1078.tludwig.vcfprocessor.files.MultiVCFReader.LinesPair;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.IntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.VCFFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * Computes the IQS score for each sample between sequences data and data imputed from genotyping.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2019-09-04
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 */

public class IQSBySample extends VCFPedFunction {

  public static final int STEP = 5000;
  public static final int DELAY = 10;
  
  private static final String[] HEADERS = {"#SAMPLE","GROUP","IQS","NB_VARIANTS","TOTAL_VARIANTS"};

  private final VCFFileParameter imputedFilename = new VCFFileParameter(OPT_FILE, "imputed.vcf(.gz)", "VCF File Containing imputed data (can be gzipped)");
  public final IntegerParameter cpu = new IntegerParameter(OPT_CPU, "Integer", "number of cores", 1, Integer.MAX_VALUE);
  
  //private ArrayList<String> samples;
  private TreeMap<String, SampleData> sampleData;

  private final AtomicInteger totalVariants = new AtomicInteger(0);

  private VCF act;
  private VCF imputedVCF;
  
  private long start;
  private int done = 0;

  @Override
  public String getSummary() {
    return "Computes the IQS score for each sample between sequences data and data imputed from genotyping.";
  }

  @Override
  public Description getDesc() {
    return new Description("Computes the IQS score between sequences data and data imputed from genotyping.")
            .addLine("Ref PMID26458263, See http://lysine.univ-brest.fr/redmine/issues/84")//TODO update URL to public doc
            .addLine("Here the IQS score is computed for each sample.")
            .addLine("Output format is :")
            .addColumns(HEADERS);
  }

  @Override
  public boolean needVEP() {
    return false;
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public void executeFunction() throws Exception {
    this.loadData();
    this.computeIQS();
  }
  
  private void update(){
    int prog = ++done;
    if(prog % STEP == 0){
      double dur = DateTools.duration(start);
      Message.progressInfo(prog + " common variants processed in " +dur+ "s (" + (int)(prog / dur) + " v/s)");
    }
  }

  private void loadData() throws Exception {
    start = new Date().getTime();
    sampleData = new TreeMap<>();

    act = this.vcffile.getVCF(VCF.STEP_OFF);
    imputedVCF = this.imputedFilename.getVCF(VCF.STEP_OFF);
    MultiVCFReader reader = new MultiVCFReader(act, imputedVCF);
    //samples = reader.getCommonsSamples();
    for (String sample : reader.getCommonsSamples())
      sampleData.put(sample, new SampleData());

    Message.info("Found " + act.getSamples().size() + " samples in " + act.getFilename());
    Message.info("Found " + imputedVCF.getSamples().size() + " samples in " + imputedVCF.getFilename());
    Message.info("Found " + sampleData.size() + " in common");

    ExecutorService threadPool = Executors.newFixedThreadPool(cpu.getIntegerValue());
    Worker[] workers = new Worker[cpu.getIntegerValue()];
    for (int i = 0; i < workers.length; i++) {
      workers[i] = new Worker();
      threadPool.submit(workers[i]);
    }
    
    int nb = 0;
    MultiVCFReader.LinesPair lines = reader.getNextLines();
    while (lines.getFirst() != null) {
      workers[nb++ % cpu.getIntegerValue()].put(new IQSData(lines)); //TODO check : objet must be build in thread ???
      lines = reader.getNextLines();
    }

    for (Worker worker : workers)
      worker.willEnd();

    Message.info("All variants have been loaded");
    threadPool.shutdown();
    threadPool.awaitTermination(100, TimeUnit.DAYS);
    double dur = DateTools.duration(start);
    Message.info(done + " common variants processed in " +dur + "s (" + (int)(done / dur) + " v/s)");
  }

  private double[] getGP(Genotype g/*, GenotypeFormat format*/) {
    String[] s = g.getValue("GP"/*, format*/).split(",");
    double[] d = new double[s.length];
    for (int i = 0; i < s.length; i++)
      d[i] = new Double(s[i]);
    return d;
  }

  public void computeIQS() throws PedException {
    println(String.join(T, HEADERS));

    String group = "NOGROUP";
    Ped ped = null;
    if (!this.pedfile.getFilename().equals("null"))
      ped = this.pedfile.getPed();

    Message.info("Computing IQS for Sample");
    for (String sample : sampleData.navigableKeySet()) {
      if (ped != null)
        group = ped.getSample(sample).getGroup();
      SampleData data = sampleData.get(sample);
      double iqs = MathTools.iqs(data.matrix);
      int present = data.nbVariants;
      println(sample + T + group + T + StringTools.formatDouble(iqs, 10) + T + present + T + totalVariants);
    }
    Message.info("\nDone");
  }

  private static class SampleData {
    private int nbVariants;
    private final double[][] matrix;
    private final Lock lock = new ReentrantLock();

    SampleData() {
      nbVariants = 0;
      matrix = new double[][]{{0d,0d,0d}, {0d,0d,0d}, {0d,0d,0d}};
    }

    void increment(double[][] cur) {
      lock.lock();
      try {
        nbVariants++;
        for (int i = 0; i < 3; i++)
          for (int j = 0; j < 3; j++)
            matrix[i][j] += cur[i][j];
      } finally {
        lock.unlock();
      }
    }
  }

  private static class IQSData {
    static final IQSData LAST = new IQSData(new LinesPair());
    
    ArrayList<String> actLines;
    ArrayList<String> impLines;

    IQSData(LinesPair pair) {
      this.actLines = pair.getFirst();
      this.impLines = pair.getSecond();
    }
  }

  private class Worker implements Runnable {
    LinkedBlockingQueue<IQSData> queue = new LinkedBlockingQueue<>(1000);

    public void willEnd() {
      put(IQSData.LAST);
    }

    public void put(IQSData iqsdata) {
      try {
        this.queue.put(iqsdata);
      } catch (InterruptedException e) {
        //Ignore
      }
    }

    @Override
    public void run() {
      boolean run = true;
      while (run) {
        IQSData data;
        try {
          data = queue.take();

          if (data.actLines != null) {
            for (String actLine : data.actLines) {
              Variant actual = null;
              try {
                actual = act.createVariant(actLine);
              } catch (VCFException e) {
                fatalAndDie("Unable to create variant from following line in " + act.getFilename() + "\n" + actLine, e);
              }
              if (actual != null) //Not filtered 
                for (String impLine : data.impLines) {
                  Variant imputed = null;
                  try {
                    imputed = imputedVCF.createVariant(impLine);
                  } catch (VCFException e) {
                    fatalAndDie("Unable to create variant from following line in " + imputedVCF.getFilename() + "\n" + impLine, e);
                  }

                  if (imputed != null)//Not filtered
                    if (actual.getRef().equals(imputed.getRef())) {
                      int altA = 0;
                      for (String actAlt : actual.getAlt().split(",")) {
                        altA++;
                        if (actAlt.equals(imputed.getAlt())){
                          totalVariants.incrementAndGet();
                          for (String sample : sampleData.navigableKeySet()) {
                            Genotype g = actual.getGenotype(sample);
                            if (!g.isMissing()) {
                              double[][] mat = new double[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
                              mat[g.getCount(altA)] = getGP(imputed.getGenotype(sample)/*, imputed.getFormat()*/);
                              sampleData.get(sample).increment(mat);
                            }
                          }
                        }
                      }
                    }
                }
            }
            update();
          } else
            run = false;
        } catch (InterruptedException ex) {
          //Ignore
        }
      }
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("ped", "ped");
    def.addAnonymousFilename("file", "file");
    def.addAnonymousValue("cpu", "8");
    return new TestingScript[]{def};
  }
}
