package fr.inserm.u1078.tludwig.vcfprocessor.files;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

/**
 * Representation of a BCF File.<br/>
 * The header is store and the record lines can be fetched on demand.
 */
public class BCF {
  private final BCFHeader header;
  private final BufferedInputStream in;

  /**
   * Constructor
   * @param filename - the name of the file
   * @param vcf - the calling VCF object
   * @throws IOException if the file can't be read
   * @throws BCFException if the file is not a gzipped BCF file
   */
  public BCF(String filename, VCF vcf) throws IOException, BCFException {
    this.in = this.checkValid(filename);
    this.header = new BCFHeader(in, vcf);
  }

  /**
   * Checks that the file is indeed a valid BCF file
   * @param filename - the path of the file
   * @return a BufferedInputStream open on the file if the file is valid
   * @throws IOException if the file can't be read
   * @throws BCFException if the file is not a gzipped BCF file
   */
  private BufferedInputStream checkValid(String filename) throws IOException, BCFException {
    try {
      BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(filename)));
      byte[] magic = in.readNBytes(5);
      String magicString = new String(magic);
      if (!magicString.equals("BCF\2\2"))
        throw new BCFException("Not a valid BCF2 file (no BCF Magic String))");
      return in;
    } catch(ZipException e) {
      throw new BCFException("Not a valid BCF2 file (not GZIP format)", e);
    }
  }

  /**
   * Reads the next BCF Record Line from the file
   * @return the BCF Record
   * @throws IOException if the file can't be read
   */
  public RawRecordData readNext() throws IOException {
    try {
      int leftSize = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
      int rightSize = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
      byte[] chromToInfo = in.readNBytes(leftSize);
      byte[] formatGenotypes = in.readNBytes(rightSize);
      return new RawRecordData(new BCFByteArray(chromToInfo), new BCFByteArray(formatGenotypes));
    } catch(BufferUnderflowException bue){
      return null;
    }
  }

  public VariantRecord build(RawRecordData raw) throws BCFException {
    return new BCFRecord(header, raw.getInCommon(), raw.getInFormatGeno());
  }

  public String getNextHeaderLine(){
    return header.getNextHeaderLine();
  }
}
