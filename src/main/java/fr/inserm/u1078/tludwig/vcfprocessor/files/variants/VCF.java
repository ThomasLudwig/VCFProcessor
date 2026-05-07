package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.DateTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.Main;
import fr.inserm.u1078.tludwig.vcfprocessor.commandline.CommandParser;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.VariantFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPFormat;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThread;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 23 juin 2015
 */
public class VCF implements VariantProducer {
  public static final int IDX_CHROM = 0;
  public static final int IDX_POS = 1;
  public static final int IDX_ID = 2;
  public static final int IDX_REF = 3;
  public static final int IDX_ALT = 4;
  public static final int IDX_QUAL = 5;
  public static final int IDX_FILTER = 6;
  public static final int IDX_INFO = 7;
  public static final int IDX_FORMAT = 8;
  public static final int IDX_SAMPLE = 9;

  private static final String VEP_HEADER = "##INFO=<ID=CSQ,";
  private static final String INFO_HEADER = "##INFO=";
  private static final String FORMAT_HEADER = "##FORMAT=";
  private static final String CHROM_HEADER = "#CHROM";

  public static final int QUEUE_DEPTH = 200;

  private final ArrayList<String> headers;

  private String originalChromToSampleHeader;

  private final String filename;
  private final CommandParser commandParser;
  private final UniversalReader in;
  private final BCF bcf;
  private VEPFormat vepFormat;
  /*private final TreeMap<Sample, Integer> sampleIndices;
  private final TreeMap<String, Sample> samplesByID;*/
  private final SampleSet sampleSet;
  private final AtomicInteger nbVariantsRead = new AtomicInteger(0);
  private final AtomicInteger nbVariantsFiltered = new AtomicInteger(0);

  //private VariantReader uniqVCFReader = null;
  private Reader uniqLineReader = null;

  private static final String T = "\t";
  public static final int STEP_OFF = -1;
  public static final int STEP10000 = 10000;
  private final int step;

  private final int mode;

  public static final int MODE_NORMAL = 0;
  public static final int MODE_QUICK_GENOTYPING = 1; // next 2,4,8,16... to mix modes

  private final Lock readLock;

  private final HashMap<String, InfoFormatHeader> infoHeaders;
  private final HashMap<String, InfoFormatHeader> formatHeaders;

  public VCF(String filename, int step) throws VCFException, PedException {
    this(filename, VCF.MODE_NORMAL, step);
  }

  public VCF(String filename) throws VCFException, PedException {
    this(filename, VCF.MODE_NORMAL, VCF.STEP10000);
  }

  public VCF(String filename, int mode, int step) throws VCFException, PedException {
    //Message.printDebuggingTrace("New VCF ["+filename+"]");
    this.readLock = new ReentrantLock();
    this.infoHeaders = new HashMap<>();
    this.formatHeaders = new HashMap<>();
    this.filename = filename;
    this.mode = mode;
    this.step = step;
    this.headers = new ArrayList<>();
    /*this.sampleIndices = new TreeMap<>();
    this.samplesByID = new TreeMap<>();*/

    //Process command line arguments
    this.commandParser = Main.getCommandParser();//TODO, a new commandParser is returned for each VCF files, see how it al plays out when there are filters and multiple VCF
    this.commandParser.processSampleArguments();
    this.commandParser.processPositionArguments();
    this.commandParser.processGenotypeArguments();
    this.commandParser.processPropertyArguments();

    BCF tmpBCF=null;
    try {
      tmpBCF = new BCF(this.filename, this);
    } catch(BCFException ignore){
    } catch (IOException e) {
      throw new VCFException(this, "Could not Read BCF/VCF File", e);
    }
    bcf = tmpBCF;

    if(bcf == null) {
      try {
        in = new UniversalReader(this.filename);
      } catch (IOException e) {
        throw new VCFException(this, "Could not Read VCF File", e);
      }
    } else
      in = null;
    readHeaders();

    this.sampleSet = new SampleSet(this);
    this.commandParser.processSampleDependantArguments(this, this.getSampleSet().getPed());
    this.commandParser.printSummary();
  }

  public InfoFormatHeader getInfoHeader(String name) {
    return this.infoHeaders.get(name);
  }

  public InfoFormatHeader getFormatHeader(String name) {
    return this.formatHeaders.get(name);
  }

  public String getFilename() {
    return this.filename;
  }



  private String getNextHeaderLine() throws IOException {
    return bcf == null
        ? in.readLine()
        : bcf.getNextHeaderLine();
  }

  private void readHeaders() throws VCFException {
    String line;
    try {
      while ((line = getNextHeaderLine()) != null) {
        if (line.charAt(0) != '#')
          throw new VCFException(this, "No sample list found in vcf file");

        if (line.startsWith(INFO_HEADER)) {
          InfoFormatHeader infoHeader = new InfoFormatHeader(line);
          this.infoHeaders.put(infoHeader.getName(), infoHeader);
        }

        if (line.startsWith(FORMAT_HEADER)) {
          InfoFormatHeader infoHeader = new InfoFormatHeader(line);
          this.formatHeaders.put(infoHeader.getName(), infoHeader);
        }

        if (line.startsWith(VEP_HEADER))
          if (VEPFormat.isValid(line))
            vepFormat = VEPFormat.createVepFormat(line);

        if (line.startsWith(CHROM_HEADER)) {
          this.headers.add(getStamp());
          this.originalChromToSampleHeader = line;
          break; //only read headers, break when reading the last line, which is always #CHROM
        }
        this.headers.add(line);
      }
      //this.nextLine = in.readLine();
    } catch (VCFException | IOException e) {
      throw new VCFException(this, "Could not read headers from BCF/VCF file", e);
    }
  }

  public static String getStamp() {
    String title = Main.TITLE;
    String version = Main.getVersion();
    String options = Main.getCommandParser().getCommandLine();
    long epoch = Main.getStart().getTime();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = format.format(Main.getStart());
    return "##" + title + "CommandLine=<ID=" + title + ",Version=" + version + ",Date=\"" + date + "\",Epoch=" + epoch + ",CommandLineOptions=\"" + options + "\">";
  }

  public void printVariantsKept() {
    Message.info("Variants Kept : " + (this.nbVariantsRead.get() - this.nbVariantsFiltered.get()) + "/" + this.nbVariantsRead.get() + " (" + this.nbVariantsFiltered.get() + " filtered)");
  }

  @Override
  public RawVariantRecordData readNext() throws IOException {
    String line = in.readLine();
    return line == null ? null : new RawVariantRecordData(line);
  }

  @Override
  public VariantRecord build(RawVariantRecordData record) throws VCFException {
    return new VCFRecord(record.getLine(), this);
  }

  private RawVariantRecordData readRecordFromUnderlyingStructure() throws BCFException, IOException {
    return bcf == null ? this.readNext(): bcf.readNext();
  }

  private RawVariantRecordData readNextPhysicalRecord() throws VCFException {
    try {
      RawVariantRecordData record  = readRecordFromUnderlyingStructure();
      if (record != null)
        this.nbVariantsRead.incrementAndGet();
      return record;//only filters until EOF
    } catch (IOException e) {
      throw new VCFException(this, "Could not read next line in BCF/VCF file", e);
    } catch (BCFException bcfe) {
      throw new VCFException(this, "Could not parse BCF file", bcfe);
    }
  }

  /**
   * should be accessed by multiple threads
   *
   * @return a indexedRecord for the next line
   */
  public IndexedRecord getNextNumberedRecord() {
    return this.getReaderWithoutStarting().nextIndexedRecord();
  }

  /**
   * Use using this method does not fully exploit the parallelism
   *
   * @return the next line
   */
  public VariantRecord getUnparallelizedNextRecord(){
    return this.getNextNumberedRecord().getRecord();
  }

  /**
   * Using this method does not fully exploit the parallelism
   * If not called through multiple worker threads, it might be slow
   * <p>
   * If this is stuck, make sure vcf.getReaderAndStart() has been called before
   *
   * @return the next variant
   * @throws VCFException if there was a problem while parsing the line
   */
  public Variant getUnparallelizedNextVariant() throws VCFException {
    VariantRecord record = this.getUnparallelizedNextRecord();
    while (record != null) {
      Variant variant = this.createVariant(record);
      if (variant != null)
        return variant;
      record = this.getUnparallelizedNextRecord();
    }
    return null;
  }

  public static boolean isSNP(String line) {
    String[] f = line.split("\t");
    if (f[3].length() != 1)
      return false;
    for (String s : f[4].split(","))
      if (s.length() != 1)
        return false;
    return true;
  }

  public static boolean isGE(String line, String chrom, int pos) {
    String[] f = line.split("\t");
    return Variant.compare(f[0], Integer.parseInt(f[1]), chrom, pos) >= 0;
  }

    public String getSampleHeader() {
    StringBuilder ret = new StringBuilder(String.join(T, "#CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO", "FORMAT"));
    //for (Sample sample : this.sampleIndices.navigableKeySet())
    for (Sample sample : this.sampleSet.getOutputSamples())
      ret.append(T).append(sample.getId());
    return ret.toString();
  }

  public boolean checkMode(int pattern) {
    return (this.mode & pattern) == pattern;
  }

  public void printHeaders(PrintWriter out) {
    for (String h : this.getFullHeaders())
      out.println(h);
  }

  public void addExtraHeaders(String[] extra) {
    if (extra != null)
      this.headers.addAll(Arrays.asList(extra));
  }

  /**
   * The Number entry is an Integer that describes the number of values that can be included with the INFO field.
   * If the INFO field contains a single number, then this value should be 1.
   * If the INFO field describes a pair of numbers, then this value should be 2 and so on.
   * If the field has one value per alternate allele then this value should be ‘A’.
   * If the field has one value for each possible genotype (more relevant to the FORMAT tags) then this value should be ‘G’.
   * If the number of possible values varies, is unknown, or is unbounded, then this value should be ‘.’.
   * <p>
   * Possible Types for INFO fields are: Integer, Float, Flag, Character, and String.
   * The ‘Flag’ type indicates that the INFO field does not contain a Value entry, and hence the Number should be 0 in this case.
   *
   * @param id the ID for the filter
   * @param description the description for the filter
   */
  public void addFilter(String id, String description) {
    this.headers.add("##FILTER=<ID=" + id + ",Description=\"" + description + "\">");
  }

  public ArrayList<String> getFullHeaders() {
    ArrayList<String> ret = new ArrayList<>(this.getHeadersWithoutSamples());
    ret.add(this.getSampleHeader());
    return ret;
  }

  public void printHeaders(PrintWriter out, String[] extraHeaders) {
    if (extraHeaders == null) {
      printHeaders(out);
      return;
    }

    for (String h : this.getHeadersWithoutSamples())
      out.println(h);
    for (String header : extraHeaders)
      out.println(header);
    out.println(this.getSampleHeader());
  }

  public Variant createVariant(VariantRecord record) throws VCFException {
    if (record == null)
      throw new VCFException(this, "Could not create variant from a null VariantRecord");
    boolean pass = true;
    //TODO here check if is filtered ? or before
    Variant variant = record.createVariant();
    for (VariantFilter variantFilter : commandParser.getVariantFilters())
      if (!variantFilter.pass(variant)) {
        pass = false;
        nbVariantsFiltered.incrementAndGet();
        break;
      }
    if (pass)
      return variant;
    return null;
  }

  public VEPFormat getVepFormat() {
    return vepFormat;
  }

  public void close() {
    try {
      in.close();
    } catch (IOException e) {
      //Nothing
    }
  }

  public SampleSet getSampleSet() { return sampleSet; }

  public static ArrayList<String> commonSamples(VCF file1, VCF file2) {
    if(file1 == null || file2 == null)
      return new ArrayList();
    return SampleSet.commonOutputSamples(file1.getSampleSet(), file2.getSampleSet());
  }

  public ArrayList<String> getHeadersWithoutSamples() {
    return headers;
  }

  public int getNumberOfSamples() {
    return sampleSet.getOutputSize();
  }

  /*
  public NavigableSet<Sample> getUnsortedSamples() {
    return sampleIndices.navigableKeySet();
  }*/
/*
  public List<Sample> getSortedSamples(){
    ArrayList<Sample> ret = new ArrayList<>();
    for(Sample sample : sampleIndices.navigableKeySet()){
      int index = sampleIndices.get(sample);
      boolean added = false;
      for(int i = 0 ; i < ret.size(); i++){
        Sample current = ret.get(i);
        if(index < sampleIndices.get(current)){
          added = true;
          ret.add(i, sample);
          break;
        }
      }
      if(!added)
        ret.add(sample);
    }
    return ret;
  }*/

  public Sample getSample(String id){
    //return samplesByID.get(id);
    return sampleSet.getSample(id);
  }

  public int indexOfSample(Sample sample){
    return indexOfSample(sample.getId());
  }

  public int indexOfSample(String sampleID){
    /*int i = 0;
    for(Sample sample : this.sampleIndices.navigableKeySet()){
      if(sample.getId().equals(sampleID))
        return i;
      i++;
    }
    return -1;*/
    return sampleSet.getOutputIndex(sampleID);
  }

  public boolean has1kGAnnotation() {
    for (String header : this.headers)
      if (header.startsWith("##INFO=<ID=CSQ") && header.contains("GMAF"))
        return true;
    return false;
  }

  public boolean has1kGEurAnnotation() {
    for (String header : this.headers)
      if (header.startsWith("##INFO=<ID=CSQ") && header.contains("EUR_MAF"))
        return true;
    return false;
  }

  public boolean hasExACAnnotation() {
    for (String header : this.headers)
      if (header.startsWith("##INFO=<ID=CSQ") && header.contains("ExAC_AF"))
        return true;
    return false;
  }

  public boolean hasExACNFEAnnotation() {
    for (String header : this.headers)
      if (header.startsWith("##INFO=<ID=CSQ") && header.contains("ExAC_AF_NFE"))
        return true;
    return false;
  }

  public boolean hasESPAnnotation() {
    for (String header : this.headers)
      if (header.startsWith("##INFO=<ID=ESP") && header.contains("ESP_AF"))
        return true;
    return false;
  }

  public boolean hasESPEAAnnotation() {
    for (String header : this.headers)
      if (header.startsWith("##INFO=<ID=ESP") && header.contains("ESP_EA_AF"))
        return true;
    return false;
  }

  public boolean hasFREXAnnotation() {
    for (String header : this.headers)
      if (header.startsWith("##INFO=<ID=FREX") && header.contains("FrEx_AF"))
        return true;
    return false;
  }

  public CommandParser getCommandParser() { return commandParser; }

  public String getOriginalChromToSampleHeader() { return originalChromToSampleHeader; }

  public void setOriginalChromToSampleHeader(String header) { this.originalChromToSampleHeader = header; }

  /*
  public TreeMap<Sample, Integer> getSampleIndices() {
    return sampleIndices;
  }*/

  public AtomicInteger getNbVariantsRead() { return nbVariantsRead; }

  /*public AtomicInteger getNbVariantsFiltere() {
    return nbVariantsFiltered;
  }*/

  /*
   * Return a Variant 'original' line, edited to add extra info fields
   *
   * @param line   the original line
   * @param extras the extra info fields
   * @return the variant line with extra info fields

  public static String addInfo(String line, String[] extras) {
    if (line == null)
      return null;
    String[] f = line.split(T);
    if (extras != null)
      for (String extra : extras)
        if (extra != null && !extra.isEmpty())
          f[IDX_INFO] += ";" + extra;
    return String.join(T, f);
  }

  /**
   * Return a Variant 'original' line, edited to add an extra info field
   *
   * @param line  the original line
   * @param extra the extra info field
   * @return the variant line with extra info fields
   */
  /*public static String addInfo(String line, String extra) {
    if (line == null)
      return null;
    String[] f = line.split("\t");
    if (extra != null && !extra.isEmpty())
      f[IDX_INFO] += ";" + extra;
    return String.join("\t", f);
  }*/

  public Reader getReaderAndStart() {
    Reader reader = this.getReaderWithoutStarting();
    reader.start();
    return reader;
  }

  public Reader getReaderWithoutStarting() {
    if (this.uniqLineReader == null) {
      readLock.lock();
      try {
        if (this.uniqLineReader == null)
          this.uniqLineReader = new Reader();
      } finally {
        readLock.unlock();
      }
    }
    return this.uniqLineReader;
  }

  public void filter() { nbVariantsFiltered.incrementAndGet(); }

  public class IndexedRecord {
    public final int index;
    public final RawVariantRecordData raw;
    public VariantRecord record = null;

    public IndexedRecord(int index, RawVariantRecordData raw) {
      this.index = index;
      this.raw = raw;
    }

    public VariantRecord getRecord() {
      if(raw == null)
        return null;
      if(record == null){
        record = buildRecord();
      }
      return record;
    }

    public VariantRecord buildRecord() {
      if (raw == null)
        return null;
      try {
        if (VCF.this.bcf != null)
          return VCF.this.bcf.build(raw);
        else
          return VCF.this.build(raw);
      } catch(Exception e) {
        Message.fatal("Unable to create "+index+"th Record", e, true);
      }
      return null;
    }

    public boolean isEOF() {
      return null == raw;
    }

    @Override
    public String toString() {
      return "IndexreadRecord["+index+"]";
    }
  }

  public class Reader extends WellBehavedThread {

    private final LinkedBlockingQueue<IndexedRecord> queue;
    private int read = 0;
    private int consumed = 0;
    private long start = -1;
    private boolean stop = false;

    public Reader() {
      queue = new LinkedBlockingQueue<>(QUEUE_DEPTH);
    }

    @Override
    public String toString() {
      return this.getClass().getSimpleName() + "[" + VCF.this.getFilename() + "]";
    }

    @Override
    public void doRun() {
      RawVariantRecordData record;
      try {
        for (read = 1; (record = VCF.this.readNextPhysicalRecord()) != null; read++)
          this.queue.put(new IndexedRecord(read, record));
      } catch (VCFException e) {
        Message.fatal("Unable to read from file [" + getFilename() + "]", e, true);
      } catch (InterruptedException ignore) { }

      read--;//overshot by 1 in the for loop
      for (int i = 0; i < QUEUE_DEPTH / 2; i++)
        try {
          this.queue.put(new IndexedRecord(read + 1, null)); //Pack with trailing null to given one to each Worker
        } catch (InterruptedException ignore) { }
    }

    public VariantRecord nextRecord() {
      IndexedRecord n = nextIndexedRecord();
      return n == null ? null : n.getRecord();
    }

    public IndexedRecord nextIndexedRecord() {
      if (start < 0)
        start = new Date().getTime();
      IndexedRecord next = null;
     /* if(start > -2)
        throw new RuntimeException("Vas te faire enculer bien comme il faut !");*/
      try {
        next = queue.take();
        if (next.raw != null) {
          this.consumed++;
          VariantRecord record = next.getRecord();
          if (step > 0 && this.consumed % step == 0) {
            double dur = DateTools.duration(start);
            int speed = (int) (consumed / dur);
            Message.info(consumed + "/" + read + " variants read from " + filename + " in " + dur + "s (" + speed + " v/s)");
          }
          record.applyNonVariantFilters();
        } else {
          try {
            readLock.lock();
            if (!stop) {
              stop = true;
              double dur = DateTools.duration(start);
              int speed = (int) (consumed / dur);
              Message.info(consumed + "/" + read + " variants read from " + filename + " in " + dur + "s (" + speed + " v/s)");
            }
          } finally {
            readLock.unlock();
          }
        }
      } catch(VCFException ve) {
        Message.fatal("Could not apply filter to VariantRecord\n"+next.raw, ve, true);
      } catch (InterruptedException ignore) { }

      return next;
    }
  }

  public static class InfoFormatHeader {
    private final String name;
    private final String description;
    private final String type;
    private final int number;

    public static final int NUMBER_ALLELES = -9; //TODO use enum ?
    public static final int NUMBER_ALTS = -8;
    public static final int NUMBER_GENOTYPES = -7;
    public static final int NUMBER_UNKNOWN = -6;
    public static final int NUMBER_NONE = 0;

    public InfoFormatHeader(String line) {
      String[] f = line.split("<")[1].split(">")[0].split(",");
      String id = "";
      String desc = "";
      int nb = 0;
      String typ = "Flag";

      for (String s : f) {
        String[] kv = s.split("=");
        switch (kv[0].toLowerCase()) {
          case "id":
            id = kv[1];
            break;
          case "type":
            typ = kv[1];
            break;
          case "description":
            desc = kv[1];
            break;
          case "number":
            try {
              nb = Integer.parseInt(kv[1]);
            } catch (NumberFormatException e) {
              switch (kv[1].toUpperCase()) {
                case "A":
                  nb = NUMBER_ALTS;
                  break;
                case "R":
                  nb = NUMBER_ALLELES;
                  break;
                case "G":
                  nb = NUMBER_GENOTYPES;
                  break;
                case ".":
                  nb = NUMBER_UNKNOWN;
                  break;
                default:
                  Message.warning("Unknown Number of values for INFO field : [" + line + "]");
                  nb = NUMBER_UNKNOWN;
              }
            }
            break;
        }
      }

      this.name = id;
      this.description = desc;
      this.type = typ;
      this.number = nb;
    }

    private InfoFormatHeader(String name, String description, String type, int number) {
      this.name = name;
      this.description = description;
      this.type = type;
      this.number = number;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public String getType() {
      return type;
    }

    public int getNumber() {
      return number;
    }
  }

  @Override
  public String[] knownExtensions() {
    return new String[]{"vcf", "vcf.gz"};
  }

  @Override
  public String fileFormatDescription() {
    return "Variant call format";
  }
}
