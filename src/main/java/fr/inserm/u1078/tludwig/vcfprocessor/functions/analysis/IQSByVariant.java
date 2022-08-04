package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.DateTools;
import fr.inserm.u1078.tludwig.maok.tools.MathTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCFException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.MultiVCFReader;
import fr.inserm.u1078.tludwig.vcfprocessor.files.MultiVCFReader.LinesPair;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction.Output;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.IntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.VCFFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Computes the IQS score for each variant between sequences data and data imputed from genotyping.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2019-09-04
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-09
 */
public class IQSByVariant extends VCFFunction {//TODO check why ID field is always empty in test

  public static final int STEP = 5000;
  public static final int DELAY = 10;
  public static final String END_MESSAGE = "XXX_NO_MORE_LINES_XXX";
  public static final String EMPTY = "ZZZ_EMPTY_ZZZ";

  public static final String[] HEADERS = {
    "chr",
    "pos",
    "rs",
    "ref",
    "alt",
    "gene",
    "consequence",
    "Freq_VCF",
    "Freq_GnomAD_NFE",
    "Freq_MaxPop",
    "Max_Pop",
    //"Imput",
    "IQS",
    "Info"
  };

  private final VCFFileParameter imputedFilename = new VCFFileParameter(OPT_FILE, "imputed.vcf(.gz)", "VCF File Containing imputed data (can be gzipped)");
  public final IntegerParameter cpu = new IntegerParameter(OPT_CPU, "Integer", "number of cores", 1, Integer.MAX_VALUE);
  //private final ConcurrentHashMap<Integer, String[]> outputLines = new ConcurrentHashMap<>();
  private final LinkedBlockingQueue<Output> outputLines = new LinkedBlockingQueue<>(1000);

  private VCF act;
  private VCF imputedVCF;
  private ArrayList<String> samples;

  @Override
  public String getSummary() {
    return "Computes the IQS score for each variant between sequences data and data imputed from genotyping.";
  }

  @Override
  public Description getDesc() {
    return new Description("Computes the IQS score between sequences data and data imputed from genotyping.")
            .addLine("Ref PMID26458263, See http://lysine.univ-brest.fr/redmine/issues/84")//TODO update URL to public doc
            .addLine("Here the IQS score is computed for each variant.")
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
    return "Extra information are available if the input file was annotated with VEP";
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public void executeFunction() throws Exception {
    int nb = 0;
    act = this.vcffile.getVCF(VCF.STEP_OFF);
    imputedVCF = imputedFilename.getVCF(VCF.STEP_OFF);
    MultiVCFReader reader = new MultiVCFReader(act, imputedVCF);

    samples = reader.getCommonsSamples();

    Message.info("Found " + act.getSamples().size() + " samples in " + act.getFilename());
    Message.info("Found " + imputedVCF.getSamples().size() + " samples in " + imputedVCF.getFilename());
    Message.info("Found " + samples.size() + " in common");

    println(String.join(T, HEADERS));

    Consumer consumer = new Consumer();
    new Thread(consumer).start();

    Worker[] workers = new Worker[cpu.getIntegerValue()];
    for (int i = 0; i < workers.length; i++) {
      workers[i] = new Worker();
      new Thread(workers[i]).start();
    }

    LinesPair lines = reader.getNextLines();
    while (lines.getFirst() != null) {
      workers[nb % cpu.getIntegerValue()].put(new IQSData(lines, nb++)); //TODO check : objet must be build in thread ???
      lines = reader.getNextLines();
    }

    for (Worker worker : workers)
      worker.willEnd(nb);

    Message.info("All variants have been loaded");
    //Wait for consumers end
    while (consumer.isRunning())
      Thread.sleep(DELAY);
  }

  private class IQSData {
    LinesPair lines;
    private final int nb;

    IQSData(LinesPair lines, int nb) {
      this.lines = lines;
      this.nb = nb;
    }
  }

  private class Worker implements Runnable {

    LinkedBlockingQueue<IQSData> queue = new LinkedBlockingQueue<>(1000);

    public void willEnd(int nb) {
      put(new IQSData(null, nb));
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

          if (data.lines != null && data.lines.getFirst() != null) {
            ArrayList<String> outputs = new ArrayList<>();
            for (String actLine : data.lines.getFirst()) {
              Variant actual = null;
              try {
                actual = act.createVariant(actLine);
              } catch (VCFException e) {
                fatalAndDie("Unable to create variant from following line in " + act.getFilename() + "\n" + actLine, e);
              }
              if (actual != null) //Not filtered
                for (String impLine : data.lines.getSecond()) {
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
                        if (actAlt.equals(imputed.getAlt())) {
                          double iqs = iqs(actual, imputed, altA);
                          String suffix = /*1 + T + */ StringTools.formatDouble(iqs, 10) + T + getInfo(actual);

                          for (LineBuilder prefix : getPrefixes(actual, imputed.getAlt()))
                            outputs.add(prefix.addColumn(suffix).toString());
                        }
                      }
                    }
                }
            }
            pushOutput(data.nb, outputs.toArray(new String[outputs.size()]));
          } else {
            run = false;
            pushOutput(data.nb, new String[]{END_MESSAGE});
          }
        } catch (InterruptedException ex) {
          //Ignore
        }
      }
    }
  }

  private static ArrayList<LineBuilder> getPrefixes(Variant v, String allele) {
    ArrayList<LineBuilder> ret = new ArrayList<>();
    int a = -1;
    for (int i = 1; i < v.getAlleles().length; i++)
      if (v.getAllele(i).equals(allele))
        a = i;
/*
    HashMap<String, String> worstCSQ = new HashMap<>();
    String rs = ".";
    String gnomADNFE = "";
    String maxFreq = "";
    String maxPop = "";
    double frex = v.getAlleleFrequencyPresent(a);
    */
    Map<String, VEPAnnotation> worst = v.getInfo().getWorstVEPAnnotationsByGene(a);
    /*
    for (VEPAnnotation vep : v.getInfo().getVEPAnnotations(a)) {
      rs = vep.getValue("Existing_variation");
      gnomADNFE = vep.getValue("gnomAD_NFE_AF");
      maxFreq = vep.getValue("MAX_AF");
      maxPop = vep.getValue("MAX_AF_POPS");
      String gene = vep.getSYMBOL();
      String worst = worstCSQ.get(gene);
      if (worst == null)
        worst = "";

      worstCSQ.put(gene, VEPAnnotation.getWorstConsequence((vep.getConsequence() + "&" + worst).split("&")));
    }//TODO get the worst annotation for each gene and process
    */
    for (String gene : worst.keySet()) {
      VEPAnnotation annot = worst.get(gene);
      LineBuilder prefix = new LineBuilder(v.getChrom());
      prefix.addColumn(v.getPos())
              .addColumn(annot.getExisting_variation().replace("&", ","))
              .addColumn(v.getRef())
              .addColumn(allele)
              .addColumn(gene)
              .addColumn(annot.getConsequence())
              .addColumn(v.getAlleleFrequencyPresent(a))
              .addColumn(annot.getGNOMAD_NFE_AF())
              .addColumn(annot.getMAF_AF())
              .addColumn(annot.getMAX_AF_POPS());
      ret.add(prefix);
    }

    if (ret.isEmpty()) {
      ArrayList<VEPAnnotation> veps = v.getInfo().getVEPAnnotations(a);
      LineBuilder prefix = new LineBuilder(v.getChrom());
      
      String rs = "";
      String nfe = "";
      String maxaf ="";
      String maxpop = "";
      if(veps !=null && !veps.isEmpty()){
        VEPAnnotation first = veps.get(0);
        if(first != null){
          rs = first.getExisting_variation();
          nfe = first.getGNOMAD_NFE_AF();
          maxaf = first.getMAF_AF();
          maxpop = first.getMAX_AF_POPS();
        }
      }
      prefix.addColumn(v.getPos())
              .addColumn(rs)
              .addColumn(v.getRef())
              .addColumn(allele)
              .addColumn()
              .addColumn()
              .addColumn(v.getAlleleFrequencyPresent(a))
              .addColumn(nfe)
              .addColumn(maxaf)
              .addColumn(maxpop);
      ret.add(prefix);
    }

    return ret;

  }

  private static String getInfo(Variant variant) { //TODO why ?
    if (variant == null)
      return null;

    String info = null;
    for (String s : variant.getInfo().getFields()) {
      String[] kv = s.split("=");
      if (kv[0].equals("INFO"))
        info = kv[1];
    }
    return info;
  }

  private static double[] getGP(Genotype g/*, GenotypeFormat format*/) {
    String[] s = g.getValue("GP"/*, format*/).split(",");
    double[] d = new double[s.length];
    for (int i = 0; i < s.length; i++)
      d[i] = new Double(s[i]);
    return d;
  }

  public double iqs(Variant actual, Variant imputed, int altA) {//NOTE: imputed is ALWAYS mono-allelic, for GP to work
    double[][] p = new double[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
    for (String sample : samples) {
      Genotype g = actual.getGenotype(sample);
      if (!g.isMissing()) {
        double[] gp = getGP(imputed.getGenotype(sample)/*, imputed.getFormat()*/);
        double[][] m = new double[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        m[g.getCount(altA)] = gp;//AA = 0; AB=1; BB=2
        for (int i = 0; i < 3; i++)
          for (int j = 0; j < 3; j++)
            p[i][j] += m[i][j];
      }
    }
    return MathTools.iqs(p);
  }

  public void pushOutput(int n, String[] lines) {
    try {
      this.outputLines.put(new Output(n, lines));
    } catch (InterruptedException e) {
      this.fatalAndDie("Producer interrupted");
    }
  }

  public class Consumer implements Runnable {

    private final ArrayList<Output> unqueuedOutput;
    private long start;
    private boolean running = true;

    public Consumer() {
      this.unqueuedOutput = new ArrayList<>();
    }

    public boolean isRunning() {
      return running;
    }

    private Output remove(int nb) {
      for (int i = 0; i < this.unqueuedOutput.size(); i++)
        if (this.unqueuedOutput.get(i).n == nb)
          return this.unqueuedOutput.remove(i);

      return null;
    }

    private boolean process(Output out) {
      boolean run = true;
      if (out.n % STEP == 0) {
        double dur = DateTools.duration(start);
        int rate = (int) (out.n / dur);
        Message.info(out.n + " common variants processed from " + vcffile.getFilename() + " in " + dur + "s (" + rate + " variants/s)");
      }

      //Process output
      for (String line : out.lines)
        switch (line) {
          case END_MESSAGE:
            double dur = DateTools.duration(start);
            int rate = (int) (out.n / dur);
            Message.info(out.n - 1 + " common variants processed from " + vcffile.getFilename() + " in " + dur + "s (" + rate + " variants/s)");
            run = false;
            break;
          case EMPTY:
            //Ignore
            break;
          default:
            println(line);
        }
      return run;
    }

    @Override
    public void run() {
      start = new Date().getTime();

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
        } catch (Exception e) {
          fatalAndDie("Consumer interrupted", e);
        }
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript novep = TestingScript.newFileAnalysis();
    novep.addNamingFilename("vcf", "vcf.novep");
    novep.addAnonymousFilename("file", "file");
    novep.addAnonymousValue("cpu", "8");
    TestingScript vep = TestingScript.newFileAnalysis();
    vep.addNamingFilename("vcf", "vcf.vep");
    vep.addAnonymousFilename("file", "file");
    vep.addAnonymousValue("cpu", "8");
    return new TestingScript[]{novep, vep};
  }
}
