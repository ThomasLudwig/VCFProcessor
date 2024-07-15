package fr.inserm.u1078.tludwig.vcfprocessor.files;

import fr.inserm.u1078.tludwig.vcfprocessor.commandline.BCFArguments;
import fr.inserm.u1078.tludwig.vcfprocessor.commandline.CommandParser;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Representation of the BCF File Header
 */
public class BCFHeader {
  private final ArrayList<String> values;
  private final ArrayList<String> contigs;
  private final String[] headerLines;

  private int gtIndex = 0;

  boolean[] keepFormat;
  boolean[] keepInfo;

  private final VCF vcf;
  private final CommandParser commandParser;
  private final ArrayList<LineFilter> lineFilters;


  private String[] rawSampleNames;
  private Sample[] rawSamples;


  /**
   * Reads a BCF Header from an InputStream
   * @param in - the InputStream on the BCF File
   * @throws IOException if the file can't be read
   */
  public BCFHeader(InputStream in, VCF vcf) throws IOException {
    this.vcf = vcf;
    this.commandParser = vcf.getCommandParser();
    values = new ArrayList<>();
    contigs = new ArrayList<>();
    // Read the header length
    int headerLength = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    byte[] header = in.readNBytes(headerLength);
    String hString = new String(header);

    this.headerLines = hString.split("\n", -1);
    // Decode the header as needed
    for(String line : this.headerLines) {
      if(line.startsWith("##")){
        String[] f = line.substring(2).split(",")[0].split("=");
        switch(f[0]){
          case "FILTER":
          case "FORMAT":
          case "INFO":
            if(!f[1].equals("<ID"))
              throw new IOException("Line seems malformed ["+line+"]");
            if(!this.values.contains(f[2]))
              this.values.add(f[2]);
            break;
          case "contig":
            if(!f[1].equals("<ID"))
              throw new IOException("Line seems malformed ["+line+"]");
            this.contigs.add(f[2]);
            break;
          default:
            break;
        }
      } else if(line.startsWith("#CHROM")) {
        String[] f = line.split("\t");
        rawSampleNames = new String[f.length - VCF.IDX_SAMPLE];
        System.arraycopy(f, 9, rawSampleNames, 0, rawSampleNames.length);
      }
    }
    gtIndex = values.indexOf("GT");
    this.keepInfo = new boolean[values.size()];
    this.keepFormat = new boolean[values.size()];
    Arrays.fill(this.keepInfo, true);
    Arrays.fill(this.keepFormat, true);
    this.lineFilters = new ArrayList<>();
    this.parseCommandLine();
  }

  public VCF getVCF() {
    return vcf;
  }

  public synchronized Sample[] getRawSamples() {
    if(rawSamples == null) {
      rawSamples = new Sample[rawSampleNames.length];
      for(int i = 0 ; i < rawSampleNames.length; i++)
        rawSamples[i] = vcf.getSample(rawSampleNames[i]);
    }
    return rawSamples;
  }

  private void parseCommandLine(){
    parseLineArgument();
    parseBCFArguments();
  }

  private void parseLineArgument(){
    lineFilters.clear();
    for(LineFilter lineFilter : commandParser.getLineFilters())
      if(lineFilter.leftColumnsOnly())
        lineFilters.add(lineFilter);
  }

  private void parseBCFArguments(){
    HashMap<String, String[]> bcfArguments = this.commandParser.getBCFArguments();
    for(String key : bcfArguments.keySet()) {
      String[] values = bcfArguments.get(key);
      if(key.equalsIgnoreCase(BCFArguments.CONSERVE_FORMAT.getKey()))
        keepFormat(true, values);
      if(key.equalsIgnoreCase(BCFArguments.IGNORE_FORMAT.getKey()))
        keepFormat(false, values);
      if(key.equalsIgnoreCase(BCFArguments.CONSERVE_INFO.getKey()))
        keepInfo(true, values);
      if(key.equalsIgnoreCase(BCFArguments.IGNORE_INFO.getKey()))
        keepInfo(false, values);
    }
  }

  /**
   * The FORMAT fields we want to KEEP/REJECT (the fewer, the faster)
   * @param keep - true:conserve, false:ignore
   * @param ss - the list of field names ("ALL" to keep all fields)
   */
  private void keepFormat(boolean keep, String... ss){
    if("all".equalsIgnoreCase(ss[0]))
      Arrays.fill(this.keepFormat, keep);
    else {
      Arrays.fill(this.keepFormat, !keep);
      for(String s : ss) {
        int idx = this.values.indexOf(s);
        if(idx > -1)
          keepFormat[idx] = keep;
      }
    }
    keepFormat[getGTIndex()] = true;
  }

  /**
   * The INFO fields we want to KEEP/REJECT (the fewer, the faster)
   * @param keep - true:conserve, false:ignore
   * @param ss - the list of field names ("ALL" to keep all fields)
   */
  private void keepInfo(boolean keep, String... ss){
    if("all".equalsIgnoreCase(ss[0]))
      Arrays.fill(this.keepInfo, keep);
    else {
      Arrays.fill(this.keepInfo, !keep);
      for(String s : ss) {
        int idx = this.values.indexOf(s);
        if(idx > -1)
          keepInfo[idx] = keep;
      }
    }
  }

  /**
   * Checks if the given FORMAT is kept
   * @param idx - the index of the FORMAT in the header
   * @return true if it is kept
   */
  public boolean isFormatKept(int idx){
    return this.keepFormat[idx];
  }

  /**
   * Checks if the given FORMAT is kept
   * @param format - the name of the FORMAT in the header
   * @return true if it is kept
   */
  public boolean isFormatKept(String format) throws BCFException {
    int idx = values.indexOf(format);
    if(idx < 0)
      throw new BCFException("The format ["+format+"] was not declared in the header file");
    return isFormatKept(idx);
  }

  /**
   * Checks if the given INFO is kept
   * @param idx - the index of the INFO in the header
   * @return true if it is kept
   */
  public boolean isInfoKept(int idx){
    return this.keepInfo[idx];
  }

  /**
   * Gets the name of the field with the given index
   * @param key - the index
   * @return the field name
   */
  public String getKeyName(int key) {
    return this.values.get(key);
  }

  /**
   * Gets the name of the Contig with the given index
   * @param key - the index
   * @return the Contig name
   */
  public String getContig(int key) {
    return this.contigs.get(key);
  }

  /**
   * Get the index of the GT field
   * @return the index of the GT field
   */
  public int getGTIndex(){
    return this.gtIndex;
  }

  /**
   * Gets the Header Lines
   * @return the header Lines (array of Strings, 1 per line) in the VCF Format
   */
  public String[] getHeaderLines() {
    return headerLines;
  }

  private int next = 0;

  /**
   * Reads the next header line
   * @return the next header line, or null if none is available
   */
  public String getNextHeaderLine() {
    if(next >= headerLines.length)
      return null;
    return headerLines[next++];
  }

  public boolean pass(BCFRecord r){
    for(LineFilter filter: lineFilters)
      if(!filter.pass(r))
        return false;
    return true;
  }
}
