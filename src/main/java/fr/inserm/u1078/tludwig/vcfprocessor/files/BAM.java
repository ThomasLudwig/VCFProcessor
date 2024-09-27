package fr.inserm.u1078.tludwig.vcfprocessor.files;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

public class BAM implements FileFormat {
  private static final String BAM_MAGIC_STRING = "BAM\1";
  private final BAMHeader header;
  private final BufferedInputStream in;
  private final Bed bed;

  public BAM(String filename, Bed bed) throws IOException, BAMException {
    this.in = this.checkValid(filename);
    this.header = new BAMHeader(in);
    this.bed = bed;
  }

  public BAM(String filename) throws IOException, BAMException {
    this(filename, null);
  }

  public BAMRecord readNext() throws IOException {
    try {
      final long blockSize = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt()  - (2*4);
      final int refId = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
      final int pos = 1+ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
      //TODO OK for the start, but what about reads that start before and cross regions ?
      if(refId != -1 && (bed ==null || bed.contains(this.header.getReferences()[refId].getName(), pos+1))) {
        byte[] array = in.readNBytes((int)blockSize); //TODO long ?
        return new BAMRecord(this, refId, pos, blockSize, new BAMByteArray(array));
      }
      return null;
    } catch(BufferUnderflowException bue){
      return null;
    }
  }

  private BufferedInputStream checkValid(String filename) throws IOException, BAMException {
    try {
      BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(filename)));
      byte[] magic = in.readNBytes(4);
      String magicString = new String(magic);
      if (!magicString.equals(BAM_MAGIC_STRING))
        throw new BAMException("Not a valid BAM file, no BAM Magic String ("+magicString+")");
      return in;
    } catch(ZipException e) {
      throw new BAMException("Not a valid BAM file (not GZIP format)", e);
    }
  }

  public BAMHeader getHeader() {
    return header;
  }


  public Bed getBed() {
    return bed;
  }

  @Override
  public String[] knownExtensions() {
    return new String[]{"bam"};
  }

  @Override
  public String fileFormatDescription() {
    return "Binary Alignment Map";
  }
}
