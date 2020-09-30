package fr.inserm.u1078.tludwig.vcfprocessor.files;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Fasta index file
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 20 mai 2015
 */
public class FAI {

  public final String filename;
  private final HashMap<String, FAILine> chromosomes;

  /**
   * Create a FAI for the fastq index file with the given name
   *
   * @param filename - name of the index file
   * @throws fr.inserm.u1078.tludwig.vcfprocessor.exceptions.FAIException
   */
  public FAI(String filename) throws FAIException {
    this.filename = filename;
    chromosomes = new HashMap<>();
    try {
      this.load();
    } catch (IOException e) {
      throw new FAIException("Could not create a FAI from the given fastq index file " + this.filename, e);
    }
  }

  /**
   * Return the index in the fastq file associated to a given position for a given chromosome
   *
   * @param chromosome - the chromosome to look for
   * @param position   - the position on the chromosome
   * @return
   */
  public long getIndexForPosition(String chromosome, long position) {
    FAILine faiLine = this.chromosomes.get(chromosome.toUpperCase());
    if (faiLine != null)
      return faiLine.getIndexForPosition(position);
    return -1;
  }

  /**
   * Loads the fastq index into the FAI object
   */
  private void load() throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(filename));
    String line;
    while ((line = in.readLine()) != null) {
      FAILine faiLine = new FAILine(line);
      this.chromosomes.put(faiLine.getChromosome(), faiLine);
    }
    in.close();
  }

  /**
   * One line from the fastq index file
   */
  private class FAILine {

    /**
     * chromosome name
     */
    private final String chromosome;
    /**
     * chromosome length
     */
    private final long chromosomeLength;
    /**
     * offset of the first base of the chromosome sequence in the file
     */
    private final long fileOffset;
    /**
     * length of the fastq lines
     */
    private final long lineBases;
    /**
     * some other length of the fastq lines called "line_blen" in the source code? Appears to typically (for me) be length of fastq line + 1
     */
    private final long lineCharacters;

    /**
     * Create a FAIline from a line from the fastq index file
     *
     * @param line
     */
    public FAILine(String line) {
      String[] fields = line.split("\t");
      chromosome = fields[0].toUpperCase();
      chromosomeLength = Long.parseLong(fields[1]);
      fileOffset = Long.parseLong(fields[2]);
      lineBases = Long.parseLong(fields[3]);
      lineCharacters = Long.parseLong(fields[4]);
    }

    /**
     * Return the index in the fastq file associated to a given position for the current chromosome
     *
     * @param position - the chromosomic position to look for
     * @return
     */
    public long getIndexForPosition(long position) {
      long numberOfLine = (position - 1) / this.lineBases;
      long characterInLine = (position - 1) % this.lineBases;
      long localOffset = numberOfLine * this.lineCharacters + characterInLine;
      long index = this.fileOffset + localOffset;
      return index;
    }

    /**
     * Gets the chromosome associated to this line
     *
     * @return
     */
    public String getChromosome() {
      return this.chromosome;
    }
  }
}
