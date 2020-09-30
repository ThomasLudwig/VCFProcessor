package fr.inserm.u1078.tludwig.vcfprocessor.files;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Fasta file
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 21 mai 2015
 */
public class Fasta {

  /**
   * the filename of the fasq file
   */
  private final String filename;
  /**
   * the FAI for the associated index file
   */
  private final FAI fai;
  /**
   * A unique RandomAccessFile to do the job
   */
  private RandomAccessFile raf;

  /**
   * Creates a Fasta, from a fasta file with the given name
   * <b>/!\ a fasta index file with the name filename.fai must exist in the same directory</b>
   *
   * @param filename - name of the fasta file
   * @throws fr.inserm.u1078.tludwig.vcfprocessor.files.FastaException
   */
  public Fasta(String filename) throws FastaException {
    this(filename, filename + ".fai");
  }

  /**
   * Creates a Fasta from a fasta file and a fastq index file
   *
   * @param filename  - name of the fasta file
   * @param indexName - name of the fasta index file
   * @throws fr.inserm.u1078.tludwig.vcfprocessor.files.FastaException
   */
  public Fasta(String filename, String indexName) throws FastaException {
    this.filename = filename;
    try {
      fai = new FAI(indexName);
      raf = new RandomAccessFile(this.filename, "r");
    } catch (FAIException e) {
      throw new FastaException("Index not found for fastq file " + this.filename + " :", e);
    } catch (FileNotFoundException e) {
      throw new FastaException("Could not find file " + this.filename, e);
    }
  }

  /**
   * Gets the allele (as a character) for the given position on the given chromosome
   *
   * @param chromosome
   * @param position
   * @return
   * @throws fr.inserm.u1078.tludwig.vcfprocessor.files.FastaException
   */
  public char getCharacterFor(String chromosome, long position) throws FastaException {
    return this.getCharactersAt(chromosome, position, 1)[0];
  }

  /**
   * Gets the sequence (as a String) of given length, start at the given position on the given chromosome
   *
   * @param chromosome
   * @param position
   * @param length
   * @return
   * @throws fr.inserm.u1078.tludwig.vcfprocessor.files.FastaException
   */
  public String getStringFor(String chromosome, long position, int length) throws FastaException {
    return new String(this.getCharactersAt(chromosome, position, length));
  }

  /**
   * Gets the sequence (as a char[]) of given length, start at the given position on the given chromosome
   *
   * @param chromosome
   * @param position
   * @param length
   * @return
   */
  synchronized
  private char[] getCharactersAt(String chromosome, long position, int length) throws FastaException {
    long index = fai.getIndexForPosition(chromosome, position);

    try {
      raf.seek(index);
      char[] ret = new char[length];
      /*raf.seek(index-skipped);*/
      for (int i = 0; i < length; i++)
        ret[i] = (char) raf.readByte();
      return ret;
    } catch (IOException e) {
      throw new FastaException("Error while trying to read " + index + "th character (chromosome " + chromosome + " position " + position + ") from file " + this.filename, e);
    }
  }

  /**
   * closes the fastq file (closes it's associate RandomAccesFile)
   */
  public void close() {
    try {
      raf.close();
    } catch (IOException e) {
      Message.error("Could not close file " + this.filename);
    }
  }
}
