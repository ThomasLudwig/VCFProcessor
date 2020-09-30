package fr.inserm.u1078.tludwig.vcfprocessor.files;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.DateTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.commandline.CommandParser;
import fr.inserm.u1078.tludwig.vcfprocessor.Main;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VariantException;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.FamFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.GenotypeFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.sample.MaxSampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.SampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.VariantFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.GenotypeFormat;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Info;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPFormat;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 23 juin 2015
 */
public class VCF {

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

  public static final String FILTERED_LINE = "***FILTERED VCF LINE***";
  public static final int QUEUE_DEPTH = 200;

  private final ArrayList<String> headers;
  private String originalSampleHeader;
  private final ArrayList<Sample> samples;
  private final String filename;
  private final CommandParser commandParser;
  private final UniversalReader in;
  private VEPFormat vepFormat;
  private final HashMap<String, Integer> sampleIndices;
  private final ArrayList<Integer> keptIndices;
  private int nbVariantsRead = 0;
  private int nbVariantsFiltered = 0;

  //private VariantReader uniqVCFReader = null;
  private Reader uniqLineReader = null;

  private static final String T = "\t";
  public static final int STEP_OFF = -1;
  public static final int STEP10000 = 10000;
  private final int step;

  private Ped ped;

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
    this.readLock = new ReentrantLock();
    this.infoHeaders = new HashMap<>();
    this.formatHeaders = new HashMap<>();
    this.filename = filename;
    this.mode = mode;
    this.step = step;

    this.headers = new ArrayList<>();
    this.samples = new ArrayList<>();
    this.sampleIndices = new HashMap<>();
    this.keptIndices = new ArrayList<>();
    try {
      in = new UniversalReader(this.filename);
    } catch (IOException e) {
      throw new VCFException("Could not Read VCF File " + this.filename, e);
    }

    //Process command line arguments
    this.commandParser = Main.getCommandParser();//TODO, a new commandParser is return for each VCF files, see how it al plays out when there are filters and multiple VCF
    this.commandParser.processSampleArguments();
    this.commandParser.processPositionArguments();
    this.commandParser.processGenotypeArguments();
    this.commandParser.processPropertyArguments();
    readHeaders();
    this.initSamples();
    this.filterSamples();
    this.commandParser.processSampleDependantArguments(this, this.getPed());
    this.commandParser.printSummary();
  }

  public Ped getPed() {
    return ped;
  }

  public InfoFormatHeader getInfoHeader(String name) {
    return this.infoHeaders.get(name);
  }

  public InfoFormatHeader getFormatHeader(String name) {
    return this.formatHeaders.get(name);
  }

  private void filterSamples() throws VCFException {
    int originalSampleNb = this.getSamples().size();
    FamFilter famFilter = null;
    MaxSampleFilter maxSampleFilter = null;
    ArrayList<SampleFilter> sampleFilters = new ArrayList<>();

    for (SampleFilter filter : this.commandParser.getSampleFilters())
      if (filter instanceof FamFilter)
        famFilter = (FamFilter) filter;
      else if (filter instanceof MaxSampleFilter)
        maxSampleFilter = (MaxSampleFilter) filter;
      else
        sampleFilters.add((SampleFilter) filter);

    ArrayList<Sample> filtered = new ArrayList<>();

    //First apply famFilter
    if (famFilter != null) {
      for (Sample sample : samples)
        if (!famFilter.pass(sample)) {
          Message.verbose("Sample [" + sample.getId() + "] has been filtered out by " + famFilter.getClass().getSimpleName());
          filtered.add(sample);
        }
      //has to be last of the block
      this.bindToPed(famFilter.getFam());
    }
    //apply all filter        
    for (Sample sample : samples)
      if (!filtered.contains(sample))
        for (SampleFilter filter : sampleFilters)
          if (!(filter.pass(sample))) {
            Message.verbose("Sample [" + sample.getId() + "] has been filtered out by " + filter.getClass().getSimpleName());
            filtered.add(sample);
            break;
          }

    //Last apply maxSampleFilter
    if (maxSampleFilter != null) {
      ArrayList<String> keptSoFar = new ArrayList<>();
      for (Sample sample : this.samples)
        if (!filtered.contains(sample))
          keptSoFar.add(sample.getId());

      maxSampleFilter.setSamples(keptSoFar);
      for (Sample sample : samples)
        if (!filtered.contains(sample))
          if (!(maxSampleFilter.pass(sample))) {
            Message.verbose("Sample [" + sample.getId() + "] has been filtered out by " + maxSampleFilter.getClass().getSimpleName());
            filtered.add(sample);
          }
    }

    this.removeSamples(filtered);

    int kept = originalSampleNb - filtered.size();
    Message.info("Sample kept : " + kept + "/" + originalSampleNb);
    if (kept == 0)
      throw new VCFException("No sample remaining after filtering");

  }

  public String getFilename() {
    return this.filename;
  }

  private void initSamples() throws VCFException {
    this.ped = new Ped(this.originalSampleHeader.split("\t"));
    this.sampleIndices.clear();
    this.keptIndices.clear();

    for (int i = 0; i < this.ped.getSampleSize(); i++) {
      Sample s = this.ped.getSample(i);
      int idx = i + 9;
      this.samples.add(s);
      this.sampleIndices.put(s.getId(), idx);
      this.keptIndices.add(idx);
    }
  }

  /**
   * Doesn't remove the samples anymore, this will be done from the famFilter
   *
   * @param ped
   */
  public void bindToPed(Ped ped) {
    Message.verbose("Binding ped file [" + ped.getFilename() + "] to VCF file [" + this.getFilename() + "]");
    this.ped = ped;
    /*
    ArrayList<String> vcfNames = new ArrayList<>();

    //get all the current names
    for (Sample sample : this.samples)
      vcfNames.add(sample.getId());
    
    //clear the samples
    this.samples.clear();
    //adds vcf variants that are also in ped
    for (String name : vcfNames) {
      Sample sample = ped.getSample(name);
      if (sample != null)
        this.samples.add(sample);
    }

    //remove indices of samples not found in the ped file
    ArrayList<String> excluded = new ArrayList<>();
    for (String name : this.sampleIndices.keySet())
      if (!pedNames.contains(name))
        excluded.add(name);
    for (String name : excluded)
      this.sampleIndices.remove(name);

    this.keptIndices.clear();
    for (Sample sample : samples)
      this.keptIndices.add(this.sampleIndices.get(sample.getId()));
     */

    for (Sample pedSample : ped.getSamples())
      for (Sample vcfSample : this.getSamples())
        if (pedSample.getId().equals(vcfSample.getId()))
          vcfSample.apply(pedSample);

    ped.keepOnly(this.samples);
  }

  public void removeSamples(Collection<Sample> excluded) {
    ArrayList<Sample> original = new ArrayList<>();
    for (Sample sample : samples)
      original.add(sample);

    this.samples.clear();
    for (Sample sample : original)
      if (excluded.contains(sample))
        this.sampleIndices.remove(sample.getId());
      else
        this.samples.add(sample);
    this.keptIndices.clear();
    for (Sample sample : samples)
      this.keptIndices.add(this.sampleIndices.get(sample.getId()));
    ped.keepOnly(this.samples); //ped is never null
  }

  private void readHeaders() throws VCFException {
    String vep = "##INFO=<ID=CSQ,";
    String info = "##INFO=";
    String format = "##FORMAT=";
    String chrom = "#CHROM";
    String line;
    try {
      while ((line = in.readLine()) != null) {
        if (line.charAt(0) != '#')
          throw new VCFException("No sample list found in vcf file " + filename);

        if (line.startsWith(info)) {
          InfoFormatHeader infoHeader = new InfoFormatHeader(line);
          this.infoHeaders.put(infoHeader.getName(), infoHeader);
        }

        if (line.startsWith(format)) {
          InfoFormatHeader infoHeader = new InfoFormatHeader(line);
          this.formatHeaders.put(infoHeader.getName(), infoHeader);
        }

        if (line.startsWith(vep))
          if (VEPFormat.isValid(line))
            vepFormat = VEPFormat.createVepFormat(line);

        if (line.startsWith(chrom)) {
          this.headers.add(getStamp());
          this.originalSampleHeader = line;
          break; //only read headers, break when reading the last line, which is always #CHROM
        }
        this.headers.add(line);
      }
      //this.nextLine = in.readLine();
    } catch (VCFException | IOException e) {
      throw new VCFException("Could not read headers from vcf file " + filename, e);
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

  private String applySampleFilters(String line) {

    if ((this.commandParser.getSampleFilters().isEmpty()) || line == null)
      return line;

    LineBuilder out = new LineBuilder();
    String[] f = line.split(T);
    for (int i = 0; i < 9; i++)
      out.addColumn(f[i]);

    for (Integer i : keptIndices)
      out.addColumn(f[i]);

    return out.substring(1);
  }

  int nt = 0;

  private String applyNonVariantFilters(String rawLine) {
    //boolean debug = rawLine.startsWith("1\t866524");

    //removing unwanted individuals
    String filteredLine = this.applySampleFilters(rawLine);
    //Message.debug(debug, "SAMPLE : "+filteredLine);
    String[] f = null; //Trying to split only once, if there is a line filter and a genotype filter

    //apply genotype filters ++ Must be called before linefilter (max missing geno is part of line filters)
    boolean hasMissingGenotypes = false;
    if (!this.commandParser.getGenotypeFilters().isEmpty()) {
      nt++;
      f = filteredLine.split(T, -1);
      String format = f[VCF.IDX_FORMAT];
      String[] formatFields = format.split(":");
      LineBuilder missingPattern = new LineBuilder(".");
      for (int i = 1; i < format.split(":").length; i++)
        missingPattern.append(":.");
      for (GenotypeFilter filter : this.commandParser.getGenotypeFilters()) {
        filter.setFormat(formatFields);
        for (int i = VCF.IDX_SAMPLE; i < f.length; i++)
          if (!filter.pass(f[i])) {
            hasMissingGenotypes = true;
            f[i] = missingPattern.toString();
          }
      }
    }

    //update line (missing samples or genotypes impact AC,AF,AN
    //Message.debug("hasMissingGenotypes:"+hasMissingGenotypes+" hasSampleFileted:"+hasSampleFileted);
    if (hasMissingGenotypes || !this.commandParser.getSampleFilters().isEmpty()) {
      if (f == null)
        f = filteredLine.split(T, -1);
      if (updateACANAF(f)) {//if all ACs are 0, drop the line
        this.nbVariantsFiltered++;
        return null;
      }

      filteredLine = String.join(T, f);
    }

    //Message.debug(debug, "MISSING : "+filteredLine);
    //applying line filters
    if (!this.commandParser.getLineFilters().isEmpty()) {
      if (f == null)
        f = filteredLine.split(T);
      for (LineFilter filter : this.commandParser.getLineFilters())
        if (!filter.pass(f)) {
          this.nbVariantsFiltered++;
          return null;
        }
    }
    return filteredLine;
  }

  public void printVariantKept() {
    Message.info("Variant Kept : " + (this.nbVariantsRead - this.nbVariantsFiltered) + "/" + this.nbVariantsRead + " (" + this.nbVariantsFiltered + " filtered)");
  }

  private boolean updateACANAF(String[] f) {
    int an = 0;
    int[] ac = new int[1 + f[IDX_ALT].split(",").length];

    for (int i = IDX_SAMPLE; i < f.length; i++) {
      String geno = f[i].split(":")[0];
      if (geno == null || geno.isEmpty()) {
        //Nothing
      } else
        for (String g : geno.split("[/\\|]")) //split on '|' and '/'
          try {
            int a = new Integer(g);
            ac[a]++;
            an++;
          } catch (NumberFormatException e) {
            //Nothing : missing data
          } catch (ArrayIndexOutOfBoundsException ae) {
            Message.warning("genotype [" + f[i] + "] impossible with " + ac.length + " alleles (at " + f[0] + ":" + f[1] + " " + f[2] + " " + f[3] + "/" + f[4] + ")");
          }
    }

    //replace old values of AC/AN/AF if present
    String newAN = "AN=" + an;
    String newAC = "";
    String newAF = "";

    int sumAC = 0;
    for (int i = 1; i < ac.length; i++) {
      sumAC += ac[i];
      newAC += "," + ac[i];
      newAF += "," + (1d * ac[i]) / an;
    }

    newAC = "AC=" + newAC.substring(1);
    newAF = "AF=" + newAF.substring(1);

    String[] info = f[IDX_INFO].split(";");
    boolean replacedAC = false;
    boolean replacedAN = false;
    boolean replacedAF = false;
    for (int i = 0; i < info.length; i++) {
      String[] kv = info[i].split("=");
      switch (kv[0]) {
        case "AC":
          replacedAC = true;
          info[i] = newAC;
          break;
        case "AF":
          replacedAF = true;
          info[i] = newAF;
          break;
        case "AN":
          replacedAN = true;
          info[i] = newAN;
          break;
        default:
          break;
      }
      if (replacedAC && replacedAN && replacedAF)
        break;
    }

    f[IDX_INFO] = String.join(";", info);

    //return true if all ACs are null
    return sumAC == 0;
  }

  /**
   * Should only be called by getunfilteredline
   *
   * @return
   * @throws VCFException
   */
  private String readNextLine() throws VCFException {
    String rawLine = this.readNextPhysicalLine();
    return rawLine == null ? null : applyNonVariantFilters(rawLine);
  }

  private String readNextPhysicalLine() throws VCFException {
    String line;
    try {
      if ((line = in.readLine()) != null) {
        this.nbVariantsRead++;
        return line;
      }
      in.close();
    } catch (IOException e) {
      throw new VCFException("Could not read next line in vcf file " + this.filename, e);
    }
    return null;//only filters until EOF
  }

  /**
   * should be accessed by multiple threads
   *
   * @return
   * @throws VCFException
   */
  public Wrapper getNextLineWrapper() throws VCFException {
    return this.getReaderWithoutStarting().nextLine();
  }

  @Deprecated
  /**
   * Use using this method does not full exploit the paralelism
   * Here
   * this method should only be accessed privately in VCF.
   *
   * @return
   * @throws VCFException
   */
  public String getNextLine() throws VCFException {
    return this.getNextLineWrapper().line;
  }

  @Deprecated
  /**
   * Using this method does not fully exploit the paralelism
   * If not called through multiple worker threads, it might be slow
   * <p>
   * If this is stuck, make sure vcf.getReaderAndStart() has been called before
   *
   * @return
   * @throws VCFException
   */
  public Variant getNextVariant() throws VCFException {
    Variant variant = null;
    String line = this.getNextLine();
    while (line != null && variant == null) {
      variant = this.createVariant(line);
      if (variant != null)
        return variant;
      line = this.getNextLine();
    }
    return variant;
  }

  @Deprecated
  /**
   * This method reads the file and not the Queue, so it should only be accessed by the LineReader
   *
   * @return
   * @throws VCFException
   */
  private String getNextUnfilteredLine() throws VCFException {
    if (this.commandParser.getVariantFilters().isEmpty())
      return this.readNextLine();
    String line;
    while ((line = this.readNextLine()) != null) {
      Variant variant = this.createVariant(line);
      if (variant != null)
        return line;
    }
    return null;
  }

  /*
  public VariantWrapper getNextVariantWrapper() throws VCFException {
    return this.getVariantReader().nextVariant();
  }

  public Variant getNextVariant() throws VCFException {
    return this.getNextVariantWrapper().variant;
  }

  private Variant readNextVariant() throws VCFException {
    String line;
    while ((line = this.readNextLine()) != null) {
      Variant variant = this.createVariant(line);
      if (variant != null)
        return variant;
    }
    return null;
  }
   */
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
    String f[] = line.split("\t");
    return Variant.compare(f[0], Integer.parseInt(f[1]), chrom, pos) >= 0;
  }

  public String getSampleHeader() {
    String ret = "#CHROM" + T + "POS" + T + "ID" + T + "REF" + T + "ALT" + T + "QUAL" + T + "FILTER" + T + "INFO" + T + "FORMAT";
    for (Sample sample : this.samples)
      ret += T + sample.getId();
    return ret;
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
   * @param id
   * @param description
   */
  public void addFilter(String id, String description) {
    this.headers.add("##FILTER=<ID=" + id + ",Description=\"" + description + "\">");
  }

  public ArrayList<String> getFullHeaders() {
    ArrayList<String> ret = new ArrayList<>();
    for (String h : this.getHeadersWithoutSamples())
      ret.add(h);
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

  public Variant createVariant(String line) throws VCFException {
    if (line == null)
      throw new VCFException("Could not create variant from null line");
    int nbSamples = this.samples.size();
    if (line.charAt(0) == '#')
      throw new VCFException("In vcf file " + this.filename + " Could not create variant from the folowing line\n" + line);
    if (nbSamples == 0)
      throw new VCFException("In vcf file " + this.filename + " Could not create variant from the folowing line (list of selected sample is empty)\n" + line);

    String[] fields = line.split("\t");

    if (fields.length < 9 + nbSamples)
      throw new VCFException("(In vcf file" + this.filename + "). Could not create variant from the following line (not enough fields " + (fields.length - 9) + "/" + nbSamples + " samples)\n" + line);

    try {
      String chrom = fields[IDX_CHROM];
      int pos = Integer.parseInt(fields[IDX_POS]);
      String id = fields[IDX_ID];
      String ref = fields[IDX_REF];
      String alt = fields[IDX_ALT];
      String qual = fields[IDX_QUAL];
      String filter = fields[IDX_FILTER];
      Info info = new Info(fields[IDX_INFO], this);
      GenotypeFormat format = new GenotypeFormat(fields[IDX_FORMAT]);
      if (checkMode(MODE_QUICK_GENOTYPING))
        format = new GenotypeFormat("GT");

      //limit to selected samples : in fact, there is nothing to do because de input line has already been altered by SampleFilters
      Genotype[] genotypes = new Genotype[nbSamples];
      for (int i = 0; i < this.samples.size(); i++) {
        int index = IDX_SAMPLE + i;
        String geno = fields[index];//right index, because line has already be cut
        if (checkMode(MODE_QUICK_GENOTYPING))
          geno = geno.split(":")[0];
        genotypes[i] = new Genotype(geno, format, this.samples.get(i));//right index, because samples has already been reduces
      }

      Variant variant = new Variant(chrom, pos, id, ref, alt, qual, filter, info, format, genotypes);
      boolean pass = true;
      for (VariantFilter variantFilter : this.commandParser.getVariantFilters())
        if (!variantFilter.pass(variant)) {
          pass = false;
          this.nbVariantsFiltered++;
          break;
        }
      if (pass)
        return variant;
      return null;

    } catch (VariantException | NumberFormatException e) {
      throw new VCFException("In vcf file " + this.filename + " Could not create variant from the folowing line\n" + line + "\n" + e.getMessage(), e);
    }
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

  public static ArrayList<String> commonSamples(VCF file1, VCF file2) {
    ArrayList<String> ret = new ArrayList<>();
    ArrayList<Sample> samples2 = file2.getSamples();
    for (Sample sample : file1.getSamples())
      for (Sample s : samples2)
        if (sample.getId().equals(s.getId())) {
          ret.add(sample.getId());
          break;
        }
    return ret;
  }

  public ArrayList<String> getHeadersWithoutSamples() {
    return headers;
  }

  public ArrayList<Sample> getSamples() {
    return this.samples;
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

  /**
   * Gets the indices of samples for the given group
   *
   * @param group
   * @return
   */
  public ArrayList<Integer> getMatrixForGroup(String group) {
    Message.debug("Looking for " + group);
    ArrayList<Integer> members = new ArrayList<>();
    if (group == null || group.isEmpty())
      return members;
    for (Sample sample : this.samples)
      if (group.equals(sample.getGroup()))
        members.add(this.sampleIndices.get(sample.getId()));
    Message.debug(members.size() + " members found for " + group);
    return members;
  }

  /**
   * Return a Variant 'original' line, edited to add extra info fields
   *
   * @param line   the original line
   * @param extras the extra info fields
   * @return
   */
  public static String addInfo(String line, String[] extras) {
    if (line == null)
      return null;
    String[] f = line.split(T);
    if (extras != null && extras.length > 0)
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
   * @return
   */
  public static String addInfo(String line, String extra) {
    if (line == null)
      return null;
    String[] f = line.split("\t");
    if (extra != null && extra.length() > 0)
      f[IDX_INFO] += ";" + extra;
    return String.join("\t", f);
  }

  /*
  public VariantReader getVariantReader() throws VCFException {
    if (this.uniqLineReader != null)
      throw new VCFException("A " + VariantReader.class.getSimpleName() + " cannot be obtained for this VCF as a " + LineReader.class.getSimpleName() + " has already been provided");
    if (this.uniqVCFReader == null) {
      readLock.lock();
      try {
        if (this.uniqVCFReader == null)
          this.uniqVCFReader = new VariantReader();
      } finally {
        readLock.unlock();
      }
    }
    return this.uniqVCFReader;
  }
   */
  public Reader getReaderAndStart() throws VCFException {
    Reader reader = this.getReaderWithoutStarting();
    new Thread(reader).start();
    return reader;
  }

  public Reader getReaderWithoutStarting() throws VCFException {
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

  public static final long TIMEOUT = 10;
  public static final TimeUnit TIMEUNIT = TimeUnit.MILLISECONDS;

  public boolean hasSample(String id) {
    for (Sample sample : this.getSamples())
      if (sample.getId().equals(id))
        return true;
    return false;
  }

  public class Wrapper {

    public final int index;
    public String line;

    public Wrapper(int index, String line) {
      this.index = index;
      this.line = line;
    }
  }

  public class Reader implements Runnable {

    private final LinkedBlockingQueue<Wrapper> queue = new LinkedBlockingQueue<>(QUEUE_DEPTH);
    private int read = 0;
    private int consumed = 0;
    private long start = -1;
    private boolean stop = false;
    private boolean run = true;

    @Override
    public String toString() {
      return this.getClass().getSimpleName() + "[" + VCF.this.getFilename() + "]";
    }

    @Override
    public void run() {
      String line;
      try {
        while (run && (line = VCF.this.readNextPhysicalLine()) != null)
          this.queue.put(new Wrapper(++read, line));
      } catch (VCFException e) {
        Message.die("Unable to read from file [" + getFilename() + "]", e); //TODO change, the STDERR/STDOUT are not closed properly
      } catch (InterruptedException ex) {
        //ignore
      }
      for (int i = 0; i < QUEUE_DEPTH / 2; i++)
        try {
          this.queue.put(new Wrapper(read + 1, null)); //Pack with trailing null to given one to each Worker
        } catch (InterruptedException ex) {
          //ignore
        }
    }

    public void close() {
      run = false;
    }

    public Wrapper nextLine() {
      if (start < 0)
        start = new Date().getTime();
      Wrapper next = null;
      try {
        next = queue.take();
        if (next.line != null) {
          this.consumed++;
          if (step > 0 && this.consumed % step == 0) {
            double dur = DateTools.duration(start);
            int speed = (int) (consumed / dur);
            Message.info(consumed + "/" + read + " variants read from " + filename + " in " + dur + "s (" + speed + " v/s)");
          }
          next.line = applyNonVariantFilters(next.line);
          if (next.line == null)
            next.line = FILTERED_LINE;
        } else
          try {
            readLock.lock();
            if (!stop) {
              stop = true;
              double dur = DateTools.duration(start);
              int speed = (int) (consumed / dur);
              Message.info(consumed + "/" + read + " variants read from " + filename + " in " + dur + "s (" + speed + " v/s)");
              printVariantKept();
            }
          } finally {
            readLock.unlock();
          }
      } catch (InterruptedException ex) {
        //Ignores
      }

      return next;
    }

    public String getProgress() {
      return "[" + getFilename() + " : " + this.consumed + "/" + this.read + "]";
    }
  }

  public class InfoFormatHeader {

    private final String name;
    private final String description;
    private final String type;
    private final int number;

    public static final int NUMBER_ALLELES = -9;
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
              nb = new Integer(kv[1]);
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
}
