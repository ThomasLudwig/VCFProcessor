package fr.inserm.u1078.tludwig.vcfprocessor.files;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Heng Li <hengli@broadinstitute.org>
 */
public class TabixReader {

  private final String mFilePath;
  private final String mIndexPath;
  private final BlockCompressedInputStream mFp;

  private int mPreset;
  private int mSc;
  private int mBc;
  private int mEc;
  private int mMeta;

  //private int mSkip; (not used)
  private String[] mSeq;

  private Map<String, Integer> mChr2tid;

  private static int MAX_BIN = 37450;
  //private static int TAD_MIN_CHUNK_GAP = 32768; (not used)
  private static int TAD_LIDX_SHIFT = 14;
  /**
   * default buffer size for <code>readLine()</code>
   */
  private static final int DEFAULT_BUFFER_SIZE = 1000;

  /**
   * Gets the number of variant lines for the given chromosome
   *
   * @param chr - the chromosome
   * @return - the number of variant lines for this chromosome
   */
  public int getVariantCount(String chr) {
    return (int) this.mIndex[this.chr2tid(chr)].b.get(MAX_BIN)[1].u;
  }

  private static class TPair64 implements Comparable<TPair64> {

    long u, v;

    TPair64(final long _u, final long _v) {
      u = _u;
      v = _v;
    }

    TPair64(final TPair64 p) {
      u = p.u;
      v = p.v;
    }

    @Override
    public int compareTo(final TPair64 p) {
      return u == p.u ? 0 : ((u < p.u) ^ (u < 0) ^ (p.u < 0)) ? -1 : 1; // unsigned 64-bit comparison
    }
  }

  private static class TIndex {

    HashMap<Integer, TPair64[]> b; // binning index
    long[] l; // linear index
  }

  private TIndex[] mIndex;

  private static class TIntv {

    int tid, beg, end;
  }

  private static boolean less64(final long u, final long v) { // unsigned 64-bit comparison
    return (u < v) ^ (u < 0) ^ (v < 0);
  }

  /**
   * @param filePath path to the data file/uri
   * @throws java.io.IOException
   */
  public TabixReader(final String filePath) throws IOException {
    this(filePath, null);
  }

  /**
   * @param filePath  path to the of the data file/uri
   * @param indexPath Full path to the index file. Auto-generated if null
   * @throws java.io.IOException
   */
  public TabixReader(final String filePath, final String indexPath) throws IOException {
    mFilePath = filePath;
    mFp = new BlockCompressedInputStream(filePath);
    if (indexPath == null)
      mIndexPath = filePath + ".tbi";
    else
      mIndexPath = indexPath;
    readIndex();
  }

  private static int reg2bins(final int beg, final int _end, final int[] list) {
    int i = 0, k, end = _end;
    if (beg >= end)
      return 0;
    if (end >= 1 << 29)
      end = 1 << 29;
    --end;
    list[i++] = 0;
    for (k = 1 + (beg >> 26); k <= 1 + (end >> 26); ++k)
      list[i++] = k;
    for (k = 9 + (beg >> 23); k <= 9 + (end >> 23); ++k)
      list[i++] = k;
    for (k = 73 + (beg >> 20); k <= 73 + (end >> 20); ++k)
      list[i++] = k;
    for (k = 585 + (beg >> 17); k <= 585 + (end >> 17); ++k)
      list[i++] = k;
    for (k = 4681 + (beg >> 14); k <= 4681 + (end >> 14); ++k)
      list[i++] = k;
    return i;
  }

  private static int readInt(final InputStream is) throws IOException {
    byte[] buf = new byte[4];
    is.read(buf);
    return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getInt();
  }

  private static long readLong(final InputStream is) throws IOException {
    final byte[] buf = new byte[8];
    is.read(buf);
    return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
  }

  private static String readLine(final InputStream is) throws IOException {
    return readLine(is, DEFAULT_BUFFER_SIZE);
  }

  /**
   * reads a line with a defined buffer-size
   *
   * @param is             the input stream
   * @param bufferCapacity the buffer size, must be greater than 0
   * @return the line or null if there is no more input
   * @throws IOException
   */
  private static String readLine(final InputStream is, final int bufferCapacity) throws IOException {
    final StringBuffer buf = new StringBuffer(bufferCapacity);
    int c;
    while ((c = is.read()) >= 0 && c != '\n')
      buf.append((char) c);
    if (c < 0)
      return null;
    return buf.toString();
  }

  /**
   * Read the Tabix index from a file
   */
  private void readIndex(final String filename) throws IOException {

    final BlockCompressedInputStream is = new BlockCompressedInputStream(filename, 128000);
    byte[] buf = new byte[4];

    is.read(buf, 0, 4); // read "TBI\1"
    mSeq = new String[readInt(is)]; // # sequences
    mChr2tid = new HashMap<>(this.mSeq.length);
    mPreset = readInt(is);
    mSc = readInt(is);
    mBc = readInt(is);
    mEc = readInt(is);
    mMeta = readInt(is);
    readInt(is);//unused
    // read sequence dictionary
    int i, j, k, l = readInt(is);
    buf = new byte[l];
    is.read(buf);
    for (i = j = k = 0; i < buf.length; ++i)
      if (buf[i] == 0) {
        byte[] b = new byte[i - j];
        System.arraycopy(buf, j, b, 0, b.length);
        final String contig = new String(b);
        mChr2tid.put(contig, k);
        mSeq[k++] = contig;
        j = i + 1;
      }
    // read the index
    mIndex = new TIndex[mSeq.length];
    for (i = 0; i < mSeq.length; ++i) {
      // the binning index
      int n_bin = readInt(is);
      mIndex[i] = new TIndex();
      mIndex[i].b = new HashMap<>(n_bin);
      for (j = 0; j < n_bin; ++j) {
        int bin = readInt(is);
        TPair64[] chunks = new TPair64[readInt(is)];
        for (k = 0; k < chunks.length; ++k) {
          long u = readLong(is);
          long v = readLong(is);
          chunks[k] = new TPair64(u, v); // in C, this is inefficient
        }
        mIndex[i].b.put(bin, chunks);
      }
      // the linear index
      mIndex[i].l = new long[readInt(is)];
      for (k = 0; k < mIndex[i].l.length; ++k)
        mIndex[i].l[k] = readLong(is);
    }
    // close
    is.close();
    //this.mFp.close();
  }

  /**
   * Read the Tabix index from the default file.
   */
  private void readIndex() throws IOException {
    readIndex(mIndexPath);
  }

  /**
   * return chromosome ID or -1 if it is unknown
   */
  private int chr2tid(final String chr) {
    final Integer tid = this.mChr2tid.get(chr);
    return tid == null ? -1 : tid;
  }

  /**
   * return the chromosomes in that tabix file
   *
   * @return
   */
  public List<String> getChromosomes() {
    ArrayList<String> chromosomes = new ArrayList(Arrays.asList(mSeq));
    return chromosomes;
  }

  /**
   * Parse a region in the format of "chr1", "chr1:100" or "chr1:100-1000"
   *
   * @param reg Region string
   * @return An array where the three elements are sequence_id, region_begin
   *         and region_end. On failure, sequence_id==-1.
   */
  private int[] parseReg(final String reg) { // FIXME: NOT working when the sequence name contains : or -.
    String chr;
    int colon, hyphen;
    int[] ret = new int[3];
    colon = reg.indexOf(':');
    hyphen = reg.indexOf('-');
    chr = colon >= 0 ? reg.substring(0, colon) : reg;
    ret[1] = colon >= 0 ? Integer.parseInt(reg.substring(colon + 1, hyphen >= 0 ? hyphen : reg.length())) - 1 : 0;
    ret[2] = hyphen >= 0 ? Integer.parseInt(reg.substring(hyphen + 1)) : 0x7fffffff;
    ret[0] = this.chr2tid(chr);
    return ret;
  }

  private TIntv getIntv(final String s) {
    TIntv intv = new TIntv();
    int col = 0, end, beg = 0;
    while ((end = s.indexOf('\t', beg)) >= 0 || end == -1) {
      ++col;
      if (col == mSc)
        intv.tid = chr2tid(end != -1 ? s.substring(beg, end) : s.substring(beg));
      else if (col == mBc) {
        intv.beg = intv.end = Integer.parseInt(end != -1 ? s.substring(beg, end) : s.substring(beg));
        if ((mPreset & 0x10000) != 0)
          ++intv.end;
        else
          --intv.beg;
        if (intv.beg < 0)
          intv.beg = 0;
        if (intv.end < 1)
          intv.end = 1;
      } else // FIXME: SAM supports are not tested yet
        switch (mPreset & 0xffff) {
          case 0:
            // generic
            if (col == mEc)
              intv.end = Integer.parseInt(end != -1 ? s.substring(beg, end) : s.substring(beg));
            break;
          case 1:
            // SAM
            if (col == 6)
              break;
          case 2:
            // VCF
            String alt;
            alt = end >= 0 ? s.substring(beg, end) : s.substring(beg);
            if (col == 4) { // REF
              if (!alt.isEmpty())
                intv.end = intv.beg + alt.length();
            } else if (col == 8)
              break;
          default:
            break;
        }
      if (end == -1)
        break;
      beg = end + 1;
    }
    return intv;
  }

  public interface Iterator {

    /**
     * return null when there is no more data to read
     *
     * @return
     * @throws java.io.IOException
     */
    String next() throws IOException;
  }

  /**
   * iterator returned instead of null when there is no more data
   */
  private static final Iterator EOF_ITERATOR = () -> null;

  /**
   * default implementation of Iterator
   */
  private class IteratorImpl implements Iterator {

    private int i;
    //private int n_seeks;
    private final int tid;
    private final int beg;
    private final int end;
    private final TPair64[] off;
    private long curr_off;
    private boolean iseof;

    private IteratorImpl(final int _tid, final int _beg, final int _end, final TPair64[] _off) {
      i = -1;
      //n_seeks = 0;
      curr_off = 0;
      iseof = false;
      off = _off;
      tid = _tid;
      beg = _beg;
      end = _end;
    }

    @Override
    public String next() throws IOException {
      if (iseof)
        return null;
      for (;;) {
        if (curr_off == 0 || !less64(curr_off, off[i].v)) { // then jump to the next chunk
          if (i == off.length - 1)
            break; // no more chunks
          if (i >= 0)
            assert (curr_off == off[i].v); // otherwise bug
          if (i < 0 || off[i].v != off[i + 1].u) { // not adjacent chunks; then seek
            mFp.seek(off[i + 1].u);
            curr_off = mFp.getFilePointer();
            //++n_seeks;
          }
          ++i;
        }
        String s;
        if ((s = readLine(mFp, DEFAULT_BUFFER_SIZE)) != null) {
          TIntv intv;
          curr_off = mFp.getFilePointer();
          if (s.isEmpty() || s.charAt(0) == mMeta)
            continue;
          intv = getIntv(s);
          if (intv.tid != tid || intv.beg >= end)
            break; // no need to proceed
          else if (intv.end > beg && intv.beg < end)
            return s; // overlap; return
        } else
          break; // end of file
      }
      iseof = true;
      return null;
    }
  }

  /**
   * Return
   *
   * @param tid Sequence id
   * @param beg beginning of interval, genomic coords
   * @param end end of interval, genomic coords
   * @return an iterator over the lines within the specified interval
   */
  private Iterator query(final int tid, final int beg, final int end) {
    TPair64[] off, chunks;
    long min_off;
    if (tid < 0 || tid >= this.mIndex.length)
      return EOF_ITERATOR;
    TIndex idx = mIndex[tid];
    int[] bins = new int[MAX_BIN];
    int i, l, n_off, n_bins = reg2bins(beg, end, bins);
    if (idx.l.length > 0)
      min_off = (beg >> TAD_LIDX_SHIFT >= idx.l.length) ? idx.l[idx.l.length - 1] : idx.l[beg >> TAD_LIDX_SHIFT];
    else
      min_off = 0;
    for (i = n_off = 0; i < n_bins; ++i)
      if ((chunks = idx.b.get(bins[i])) != null)
        n_off += chunks.length;
    if (n_off == 0)
      return EOF_ITERATOR;
    off = new TPair64[n_off];
    for (i = n_off = 0; i < n_bins; ++i)
      if ((chunks = idx.b.get(bins[i])) != null)
        for (int j = 0; j < chunks.length; ++j)
          if (less64(min_off, chunks[j].v))
            off[n_off++] = new TPair64(chunks[j]);
    Arrays.sort(off, 0, n_off);
    // resolve completely contained adjacent blocks
    for (i = 1, l = 0; i < n_off; ++i)
      if (less64(off[l].v, off[i].v)) {
        ++l;
        off[l].u = off[i].u;
        off[l].v = off[i].v;
      }
    n_off = l + 1;
    // resolve overlaps between adjacent blocks; this may happen due to the merge in indexing
    for (i = 1; i < n_off; ++i)
      if (!less64(off[i - 1].v, off[i].u))
        off[i - 1].v = off[i].u;
    // merge adjacent blocks
    for (i = 1, l = 0; i < n_off; ++i)
      if (off[l].v >> 16 == off[i].u >> 16)
        off[l].v = off[i].v;
      else {
        ++l;
        off[l].u = off[i].u;
        off[l].v = off[i].v;
      }
    n_off = l + 1;
    // return
    TPair64[] ret = new TPair64[n_off];
    for (i = 0; i < n_off; ++i)
      if (off[i] != null)
        ret[i] = new TPair64(off[i].u, off[i].v); // in C, this is inefficient
    if (ret.length == 0 || (ret.length == 1 && ret[0] == null))
      return EOF_ITERATOR;
    return new TabixReader.IteratorImpl(tid, beg, end, ret);
  }

  /**
   *
   * @see #parseReg(String)
   * @param reg A region string of the form acceptable by
   *            {@link #parseReg(String)}
   * @return
   */
  public Iterator query(final String reg) {
    int[] x = parseReg(reg);
    if (x[0] < 0)
      return EOF_ITERATOR;
    return query(x[0], x[1], x[2]);
  }

  /**
   *
   * @see #parseReg(String)
   * @param reg   a chromosome
   * @param start start interval
   * @param end   end interval
   * @return a tabix iterator
   */
  public Iterator query(final String reg, int start, int end) {
    int tid = this.chr2tid(reg);
    if (tid == -1)
      return EOF_ITERATOR;
    return query(tid, start, end);
  }

  @Override
  public String toString() {
    return "TabixReader: filename:" + this.mFilePath;
  }

  public final static String INCORRECT_HEADER_SIZE_MSG = "Incorrect header size for file: ";
  public final static String UNEXPECTED_BLOCK_LENGTH_MSG = "Unexpected compressed block length: ";
  public final static String PREMATURE_END_MSG = "Premature end of file: ";
  public final static String CANNOT_SEEK_STREAM_MSG = "Cannot seek a position for a non-file stream";
  public final static String CANNOT_SEEK_CLOSED_STREAM_MSG = "Cannot seek a position for a closed stream";
  public final static String INVALID_FILE_PTR_MSG = "Invalid file pointer: ";

  // Number of bytes in the gzip block before the deflated data.
  // This is not the standard header size, because we include one optional subfield,
  // but it is the standard for us.
  public static final int BLOCK_HEADER_LENGTH = 18;

  // Location in the gzip block of the total block size (actually total block size - 1)
  public static final int BLOCK_LENGTH_OFFSET = 16;

  // Number of bytes that follow the deflated data
  public static final int BLOCK_FOOTER_LENGTH = 8;

  // We require that a compressed block (including header and footer, be <= this)
  public static final int MAX_COMPRESSED_BLOCK_SIZE = 64 * 1024;

  // Gzip overhead is the header, the footer, and the block size (encoded as a short).
  public static final int GZIP_OVERHEAD = BLOCK_HEADER_LENGTH + BLOCK_FOOTER_LENGTH + 2;

  // If Deflater has compression level == NO_COMPRESSION, 10 bytes of overhead (determined experimentally).
  public static final int NO_COMPRESSION_OVERHEAD = 10;

  // Push out a gzip block when this many uncompressed bytes have been accumulated.
  // This size is selected so that if data is not compressible,  if Deflater is given
  // compression level == NO_COMPRESSION, compressed size is guaranteed to be <= MAX_COMPRESSED_BLOCK_SIZE.
  public static final int DEFAULT_UNCOMPRESSED_BLOCK_SIZE = 64 * 1024 - (GZIP_OVERHEAD + NO_COMPRESSION_OVERHEAD);

  // Magic numbers
  public static final byte GZIP_ID1 = 31;
  public static final int GZIP_ID2 = 139;

  // FEXTRA flag means there are optional fields
  public static final int GZIP_FLG = 4;

  // extra flags
  public static final int GZIP_XFL = 0;

  // length of extra subfield
  public static final short GZIP_XLEN = 6;

  // The deflate compression, which is customarily used by gzip
  public static final byte GZIP_CM_DEFLATE = 8;

  public static final int DEFAULT_COMPRESSION_LEVEL = 5;

  // We don't care about OS because we're not doing line terminator translation
  public static final int GZIP_OS_UNKNOWN = 255;

  // The subfield ID
  public static final byte BGZF_ID1 = 66;
  public static final byte BGZF_ID2 = 67;

  // subfield length in bytes
  public static final byte BGZF_LEN = 2;

  public static final byte[] EMPTY_GZIP_BLOCK = {
    GZIP_ID1,
    (byte) GZIP_ID2,
    GZIP_CM_DEFLATE,
    GZIP_FLG,
    0, 0, 0, 0, // Modification time
    GZIP_XFL,
    (byte) GZIP_OS_UNKNOWN,
    GZIP_XLEN, 0, // Little-endian short
    BGZF_ID1,
    BGZF_ID2,
    BGZF_LEN, 0, // Little-endian short
    // Total block size - 1
    BLOCK_HEADER_LENGTH + BLOCK_FOOTER_LENGTH - 1 + 2, 0, // Little-endian short
    // Dummy payload?
    3, 0,
    0, 0, 0, 0, // crc
    0, 0, 0, 0, // uncompressedSize
  };
  public static final byte[] GZIP_BLOCK_PREAMBLE = {
    GZIP_ID1,
    (byte) GZIP_ID2,
    GZIP_CM_DEFLATE,
    GZIP_FLG,
    0, 0, 0, 0, // Modification time
    GZIP_XFL,
    (byte) GZIP_OS_UNKNOWN,
    GZIP_XLEN, 0, // Little-endian short
    BGZF_ID1,
    BGZF_ID2,
    BGZF_LEN, 0, // Little-endian short
  };

  private static final int SHIFT_AMOUNT = 16;
  private static final int OFFSET_MASK = 0xffff;
  private static final long ADDRESS_MASK = 0xFFFFFFFFFFFFL;

  public static final long MAX_BLOCK_ADDRESS = ADDRESS_MASK;
  public static final int MAX_OFFSET = OFFSET_MASK;

  /**
   * @param blockAddress File offset of start of BGZF block.
   * @param blockOffset  Offset into uncompressed block.
   * @return Virtual file pointer that embodies the input parameters.
   */
  public static long makeFilePointer(final long blockAddress, final int blockOffset) {
    if (blockOffset < 0)
      throw new IllegalArgumentException("Negative blockOffset " + blockOffset + " not allowed.");
    if (blockAddress < 0)
      throw new IllegalArgumentException("Negative blockAddress " + blockAddress + " not allowed.");
    if (blockOffset > MAX_OFFSET)
      throw new IllegalArgumentException("blockOffset " + blockOffset + " too large.");
    if (blockAddress > MAX_BLOCK_ADDRESS)
      throw new IllegalArgumentException("blockAddress " + blockAddress + " too large.");
    return blockAddress << SHIFT_AMOUNT | blockOffset;
  }

  /**
   * @param virtualFilePointer
   * @return File offset of start of BGZF block for this virtual file pointer.
   */
  public static long getBlockAddress(final long virtualFilePointer) {
    return (virtualFilePointer >> SHIFT_AMOUNT) & ADDRESS_MASK;
  }

  /**
   * @param virtualFilePointer
   * @return Offset into uncompressed block for this virtual file pointer.
   */
  public static int getBlockOffset(final long virtualFilePointer) {
    return (int) (virtualFilePointer & OFFSET_MASK);
  }

  public static long getFileBlock(final long bgzfOffset) {
    return getBlockAddress(bgzfOffset);
  }

  /**
   * @param stream Must be at start of file. Throws RuntimeException if
   *               !stream.markSupported().
   * @return true if the given file looks like a valid BGZF file.
   * @throws java.io.IOException
   */
  public static boolean isValidFile(final InputStream stream) throws IOException {
    if (!stream.markSupported())
      throw new RuntimeException("Cannot test non-buffered stream");
    stream.mark(BLOCK_HEADER_LENGTH);
    final byte[] buffer = new byte[BLOCK_HEADER_LENGTH];
    final int count = readBytes(stream, buffer, 0, BLOCK_HEADER_LENGTH);
    stream.reset();
    return count == BLOCK_HEADER_LENGTH && isValidBlockHeader(buffer);
  }

  private static boolean isValidBlockHeader(final byte[] buffer) {
    return (buffer[0] == GZIP_ID1
            && (buffer[1] & 0xFF) == GZIP_ID2
            && (buffer[3] & GZIP_FLG) != 0
            && buffer[10] == GZIP_XLEN
            && buffer[12] == BGZF_ID1
            && buffer[13] == BGZF_ID2);
  }

  private static int readBytes(final BlockCompressedInputStream.SeekableBufferedStream file, final byte[] buffer, final int offset, final int length) throws IOException {
    int bytesRead = 0;
    while (bytesRead < length) {
      final int count = file.read(buffer, offset + bytesRead, length - bytesRead);
      if (count <= 0)
        break;
      bytesRead += count;
    }
    return bytesRead;
  }

  private static int readBytes(final InputStream stream, final byte[] buffer, final int offset, final int length) throws IOException {
    int bytesRead = 0;
    while (bytesRead < length) {
      final int count = stream.read(buffer, offset + bytesRead, length - bytesRead);
      if (count <= 0)
        break;
      bytesRead += count;
    }
    return bytesRead;
  }

  public enum FileTermination {
    HAS_TERMINATOR_BLOCK, HAS_HEALTHY_LAST_BLOCK, DEFECTIVE
  }

  /*
    public static FileTermination checkTermination(final File file) throws IOException {
        return checkTermination(IOUtil.toPath(file));
    }*/
  /**
   *
   * @param path to the file to check
   * @return status of the last compressed block
   * @throws IOException
   */
  public static FileTermination checkTermination(final Path path) throws IOException {
    try (final SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
      return checkTermination(channel);
    }
  }

  /**
   * check the status of the final bzgipped block for the given bgzipped
   * resource
   *
   * @param channel an open channel to read from, the channel will remain open
   *                and the initial position will be restored when the operation completes
   *                this makes no guarantee about the state of the channel if an exception is
   *                thrown during reading
   *
   * @return the status of the last compressed black
   * @throws IOException
   */
  public static FileTermination checkTermination(SeekableByteChannel channel) throws IOException {
    final long fileSize = channel.size();
    if (fileSize < EMPTY_GZIP_BLOCK.length)
      return FileTermination.DEFECTIVE;
    final long initialPosition = channel.position();
    boolean exceptionThrown = false;
    try {
      channel.position(fileSize - EMPTY_GZIP_BLOCK.length);

      //Check if the end of the file is an empty gzip block which is used as the terminator for a bgzipped file
      final ByteBuffer lastBlockBuffer = ByteBuffer.allocate(EMPTY_GZIP_BLOCK.length);
      readFully(channel, lastBlockBuffer);
      if (Arrays.equals(lastBlockBuffer.array(), EMPTY_GZIP_BLOCK))
        return FileTermination.HAS_TERMINATOR_BLOCK;

      //if the last block isn't an empty gzip block, check to see if it is a healthy compressed block or if it's corrupted
      final int bufsize = (int) Math.min(fileSize, MAX_COMPRESSED_BLOCK_SIZE);
      final byte[] bufferArray = new byte[bufsize];
      channel.position(fileSize - bufsize);
      readFully(channel, ByteBuffer.wrap(bufferArray));
      for (int i = bufferArray.length - EMPTY_GZIP_BLOCK.length;
              i >= 0; --i) {
        if (!preambleEqual(GZIP_BLOCK_PREAMBLE,
                bufferArray, i, GZIP_BLOCK_PREAMBLE.length))
          continue;
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bufferArray,
                i + GZIP_BLOCK_PREAMBLE.length,
                4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        final int totalBlockSizeMinusOne = byteBuffer.getShort() & 0xFFFF;
        if (bufferArray.length - i == totalBlockSizeMinusOne + 1)
          return FileTermination.HAS_HEALTHY_LAST_BLOCK;
        else
          return FileTermination.DEFECTIVE;
      }
      return FileTermination.DEFECTIVE;
    } catch (final Throwable e) {
      exceptionThrown = true;
      throw e;
    } finally {
      //if an exception was thrown we don't want to reset the position because that would be likely to throw again
      //and suppress the initial exception
      if (!exceptionThrown)
        channel.position(initialPosition);
    }
  }

  /**
   * read as many bytes as dst's capacity into dst or throw if that's not
   * possible
   *
   * @throws EOFException if channel has fewer bytes available than dst's
   *                      capacity
   */
  static void readFully(SeekableByteChannel channel, ByteBuffer dst) throws IOException {
    final int bytesRead = channel.read(dst);
    if (bytesRead < dst.capacity())
      throw new EOFException();
  }

  /*
    public static void assertNonDefectiveFile(final File file) throws IOException {
        if (checkTermination(file) == FileTermination.DEFECTIVE) {
            throw new SAMException(file.getAbsolutePath() + " does not have a valid GZIP block at the end of the file.");
        }
    }
   */
  private static boolean preambleEqual(final byte[] preamble, final byte[] buf, final int startOffset, final int length) {
    for (int i = 0; i < length; ++i)
      if (preamble[i] != buf[i + startOffset])
        return false;
    return true;
  }

  protected static class DecompressedBlock {

    /**
     * Decompressed block
     */
    private final byte[] mBlock;
    /**
     * Compressed size of block (the uncompressed size can be found using
     * mBlock.length)
     */
    private final int mBlockCompressedSize;
    /**
     * Stream offset of start of block
     */
    private final long mBlockAddress;
    /**
     * Exception thrown (if any) when attempting to decompress block
     */
    private final Exception mException;

    DecompressedBlock(long blockAddress, byte[] block, int compressedSize) {
      mBlock = block;
      mBlockAddress = blockAddress;
      mBlockCompressedSize = compressedSize;
      mException = null;
    }

    DecompressedBlock(long blockAddress, int compressedSize, Exception exception) {
      mBlock = new byte[0];
      mBlockAddress = blockAddress;
      mBlockCompressedSize = compressedSize;
      mException = exception;
    }
  }

  public class BlockCompressedInputStream extends InputStream {

    private InputStream mStream = null;
    private boolean mIsClosed = false;
    private SeekableBufferedStream mFile = null;
    private byte[] mFileBuffer = null;
    private DecompressedBlock mCurrentBlock = null;
    private int mCurrentOffset = 0;
    private long mStreamOffset = 0;
    private final BlockGunzipper blockGunzipper;

    public BlockCompressedInputStream(final String filePath, final int bs) throws IOException {
      mFile = new SeekableBufferedStream(filePath, bs);
      mStream = null;
      blockGunzipper = new BlockGunzipper();
    }

    public BlockCompressedInputStream(final String filePath) throws IOException {
      mFile = new SeekableBufferedStream(filePath);
      mStream = null;
      blockGunzipper = new BlockGunzipper();
    }

    /**
     * Determines whether or not the inflater will re-calculated the CRC on
     * the decompressed data and check it against the value stored in the
     * GZIP header. CRC checking is an expensive operation and should be
     * used accordingly.
     *
     * @param check
     */
    public void setCheckCrcs(final boolean check) {
      this.blockGunzipper.setCheckCrcs(check);
    }

    /**
     * @return the number of bytes that can be read (or skipped over) from
     *         this input stream without blocking by the next caller of a method for
     *         this input stream. The next caller might be the same thread or
     *         another thread. Note that although the next caller can read this many
     *         bytes without blocking, the available() method call itself may block
     *         in order to fill an internal buffer if it has been exhausted.
     * @throws java.io.IOException
     */
    @Override
    public int available() throws IOException {
      if (mCurrentBlock == null || mCurrentOffset == mCurrentBlock.mBlock.length)
        readBlock();
      if (mCurrentBlock == null)
        return 0;
      return mCurrentBlock.mBlock.length - mCurrentOffset;
    }

    /**
     * @return <code>true</code> if the stream is at the end of a BGZF
     *         block, <code>false</code> otherwise.
     */
    public boolean endOfBlock() {
      return (mCurrentBlock != null && mCurrentOffset == mCurrentBlock.mBlock.length);
    }

    /**
     * Closes the underlying InputStream or RandomAccessFile
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
      if (mFile != null) {
        mFile.close();
        mFile = null;
      } else if (mStream != null) {
        mStream.close();
        mStream = null;
      }
      // Encourage garbage collection
      mFileBuffer = null;
      mCurrentBlock = null;

      // Mark as closed
      mIsClosed = true;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an int in the range 0 to 255. If no byte is available
     * because the end of the stream has been reached, the value -1 is
     * returned. This method blocks until input data is available, the end
     * of the stream is detected, or an exception is thrown.
     *
     * @return the next byte of data, or -1 if the end of the stream is
     *         reached.
     * @throws java.io.IOException
     */
    @Override
    public int read() throws IOException {
      return (available() > 0) ? (mCurrentBlock.mBlock[mCurrentOffset++] & 0xFF) : -1;
    }

    /**
     * Reads some number of bytes from the input stream and stores them into
     * the buffer array b. The number of bytes actually read is returned as
     * an integer. This method blocks until input data is available, end of
     * file is detected, or an exception is thrown.
     * <p>
     * read(buf) has the same effect as read(buf, 0, buf.length).
     *
     * @param buffer the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or -1 is
     *         there is no more data because the end of the stream has been reached.
     * @throws java.io.IOException
     */
    @Override
    public int read(final byte[] buffer) throws IOException {
      return read(buffer, 0, buffer.length);
    }

    private volatile ByteArrayOutputStream buf = null;
    private static final byte EOL = '\n';
    private static final byte EOLCR = '\r';

    /**
     * Reads a whole line. A line is considered to be terminated by either a
     * line feed ('\n'), carriage return ('\r') or carriage return followed
     * by a line feed ("\r\n").
     *
     * @return A String containing the contents of the line, excluding the
     *         line terminating character, or null if the end of the stream has been
     *         reached
     *
     * @exception IOException If an I/O error occurs
     */
    public String readLine() throws IOException {
      int available = available();
      if (available == 0)
        return null;
      if (null == buf) // lazy initialisation 
        buf = new ByteArrayOutputStream(8192);
      buf.reset();
      boolean done = false;
      boolean foundCr = false; // \r found flag
      while (!done) {
        int linetmpPos = mCurrentOffset;
        int bCnt = 0;
        while ((available-- > 0)) {
          final byte c = mCurrentBlock.mBlock[linetmpPos++];
          if (c == EOL) { // found \n
            done = true;
            break;
          } else if (foundCr) {  // previous char was \r
            --linetmpPos; // current char is not \n so put it back
            done = true;
            break;
          } else if (c == EOLCR) { // found \r
            foundCr = true;
            continue; // no ++bCnt
          }
          ++bCnt;
        }
        if (mCurrentOffset < linetmpPos) {
          buf.write(mCurrentBlock.mBlock, mCurrentOffset, bCnt);
          mCurrentOffset = linetmpPos;
        }
        available = available();
        if (available == 0)
          // EOF
          done = true;
      }
      return buf.toString();
    }

    /**
     * Reads up to len bytes of data from the input stream into an array of
     * bytes. An attempt is made to read as many as len bytes, but a smaller
     * number may be read. The number of bytes actually read is returned as
     * an integer.
     * <p>
     * This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * @param buffer buffer into which data is read.
     * @param offset the start offset in array b at which the data is
     *               written.
     * @param length the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or -1 if
     *         there is no more data because the end of the stream has been reached.
     * @throws java.io.IOException
     */
    @Override
    public int read(final byte[] buffer, int offset, int length) throws IOException {
      final int originalLength = length;
      while (length > 0) {
        final int available = available();
        if (available == 0) {
          // Signal EOF to caller
          if (originalLength == length)
            return -1;
          break;
        }
        final int copyLength = Math.min(length, available);
        System.arraycopy(mCurrentBlock.mBlock, mCurrentOffset, buffer, offset, copyLength);
        mCurrentOffset += copyLength;
        offset += copyLength;
        length -= copyLength;
      }
      return originalLength - length;
    }

    /**
     * Seek to the given position in the file. Note that pos is a special
     * virtual file pointer, not an actual byte offset.
     *
     * @param pos virtual file pointer position
     * @throws IOException if stream is closed or not a file based stream
     */
    public void seek(final long pos) throws IOException {
      // Must be before the mFile == null check because mFile == null for closed files and streams
      if (mIsClosed)
        throw new IOException(CANNOT_SEEK_CLOSED_STREAM_MSG);

      // Cannot seek on streams that are not file based
      if (mFile == null)
        throw new IOException(CANNOT_SEEK_STREAM_MSG);

      // Decode virtual file pointer
      // Upper 48 bits is the byte offset into the compressed stream of a
      // block.
      // Lower 16 bits is the byte offset into the uncompressed stream inside
      // the block.
      final long compressedOffset = getBlockAddress(pos);
      final int uncompressedOffset = getBlockOffset(pos);
      final int available;
      if (mCurrentBlock != null && mCurrentBlock.mBlockAddress == compressedOffset)
        available = mCurrentBlock.mBlock.length;
      else {
        prepareForSeek();
        mFile.seek(compressedOffset);
        mStreamOffset = compressedOffset;
        mCurrentBlock = nextBlock(getBufferForReuse(mCurrentBlock));
        mCurrentOffset = 0;
        available = available();
      }
      if (uncompressedOffset > available || (uncompressedOffset == available && !eof()))
        throw new IOException(INVALID_FILE_PTR_MSG + pos + " for " + getSource());
      mCurrentOffset = uncompressedOffset;
    }

    /**
     * Performs cleanup required before seek is called on the underlying
     * stream
     */
    protected void prepareForSeek() {
    }

    private boolean eof() throws IOException {
      if (mFile.eof())
        return true;
      // If the last remaining block is the size of the EMPTY_GZIP_BLOCK, this is the same as being at EOF.
      return (mFile.length() - (mCurrentBlock.mBlockAddress
              + mCurrentBlock.mBlockCompressedSize) == EMPTY_GZIP_BLOCK.length);
    }

    /**
     * @return virtual file pointer that can be passed to seek() to return
     *         to the current position. This is not an actual byte offset, so
     *         arithmetic on file pointers cannot be done to determine the distance
     *         between the two.
     */
    public long getFilePointer() {
      if (mCurrentBlock == null)
        // Haven't read anything yet = at start of stream
        return makeFilePointer(0, 0);
      if (mCurrentOffset > 0 && mCurrentOffset == mCurrentBlock.mBlock.length)
        // If current offset is at the end of the current block, file
        // pointer should point
        // to the beginning of the next block.
        return makeFilePointer(mCurrentBlock.mBlockAddress + mCurrentBlock.mBlockCompressedSize, 0);
      return makeFilePointer(mCurrentBlock.mBlockAddress, mCurrentOffset);
    }

    private void readBlock() throws IOException {
      mCurrentBlock = nextBlock(getBufferForReuse(mCurrentBlock));
      mCurrentOffset = 0;
      checkAndRethrowDecompressionException();
    }

    /**
     * Reads and decompresses the next block
     *
     * @param bufferAvailableForReuse decompression buffer available for
     *                                reuse
     * @return next block in the decompressed stream
     */
    protected DecompressedBlock nextBlock(byte[] bufferAvailableForReuse) {
      return processNextBlock(bufferAvailableForReuse);
    }

    /**
     * Rethrows an exception encountered during decompression
     *
     * @throws IOException
     */
    private void checkAndRethrowDecompressionException() throws IOException {
      if (mCurrentBlock.mException != null)
        if (mCurrentBlock.mException instanceof IOException)
          throw (IOException) mCurrentBlock.mException;
        else if (mCurrentBlock.mException instanceof RuntimeException)
          throw (RuntimeException) mCurrentBlock.mException;
        else
          throw new RuntimeException(mCurrentBlock.mException);
    }

    /**
     * Attempt to reuse the buffer of the given block
     *
     * @param block owning block
     * @return null decompressing buffer to reuse, null if no buffer is
     *         available
     */
    private byte[] getBufferForReuse(DecompressedBlock block) {
      if (block == null)
        return null;
      return block.mBlock;
    }

    /**
     * Decompress the next block from the input stream. When using
     * asynchronous IO, this will be called by the background thread.
     *
     * @param bufferAvailableForReuse buffer in which to place decompressed
     *                                block. A null or incorrectly sized buffer will result in the buffer
     *                                being ignored and a new buffer allocated for decompression.
     * @return next block in input stream
     */
    protected DecompressedBlock processNextBlock(byte[] bufferAvailableForReuse) {
      if (mFileBuffer == null)
        mFileBuffer = new byte[MAX_COMPRESSED_BLOCK_SIZE];
      long blockAddress = mStreamOffset;
      try {
        final int headerByteCount = readBytes(mFileBuffer, 0, BLOCK_HEADER_LENGTH);
        mStreamOffset += headerByteCount;
        if (headerByteCount == 0)
          // Handle case where there is no empty gzip block at end.
          return new DecompressedBlock(blockAddress, new byte[0], 0);
        if (headerByteCount != BLOCK_HEADER_LENGTH)
          return new DecompressedBlock(blockAddress, headerByteCount, new IOException(INCORRECT_HEADER_SIZE_MSG + getSource()));
        final int blockLength = unpackInt16(mFileBuffer, BLOCK_LENGTH_OFFSET) + 1;
        if (blockLength < BLOCK_HEADER_LENGTH || blockLength > mFileBuffer.length)
          return new DecompressedBlock(blockAddress, blockLength,
                  new IOException(UNEXPECTED_BLOCK_LENGTH_MSG + blockLength + " for " + getSource()));
        final int remaining = blockLength - BLOCK_HEADER_LENGTH;
        final int dataByteCount = readBytes(mFileBuffer, BLOCK_HEADER_LENGTH,
                remaining);
        mStreamOffset += dataByteCount;
        if (dataByteCount != remaining)
          return new DecompressedBlock(blockAddress, blockLength,
                  new Exception(PREMATURE_END_MSG + getSource()));
        final byte[] decompressed = inflateBlock(mFileBuffer, blockLength, bufferAvailableForReuse);
        return new DecompressedBlock(blockAddress, decompressed, blockLength);
      } catch (IOException e) {
        return new DecompressedBlock(blockAddress, 0, e);
      }
    }

    private byte[] inflateBlock(final byte[] compressedBlock, final int compressedLength,
            final byte[] bufferAvailableForReuse) throws IOException {
      final int uncompressedLength = unpackInt32(compressedBlock, compressedLength - 4);
      if (uncompressedLength < 0)
        throw new RuntimeException(getSource() + " has invalid uncompressedLength: " + uncompressedLength);
      byte[] buffer = bufferAvailableForReuse;
      if (buffer == null || uncompressedLength != buffer.length)
        // can't reuse the buffer since the size is incorrect
        buffer = new byte[uncompressedLength];
      blockGunzipper.unzipBlock(buffer, compressedBlock, compressedLength);
      return buffer;
    }

    private String getSource() {
      return mFile == null ? "data stream" : mFile.getSource();
    }

    private int readBytes(final byte[] buffer, final int offset, final int length) throws IOException {
      if (mFile != null)
        return TabixReader.readBytes(mFile, buffer, offset, length);
      else if (mStream != null)
        return TabixReader.readBytes(mStream, buffer, offset, length);
      else
        return 0;
    }

    private int unpackInt16(final byte[] buffer, final int offset) {
      return ((buffer[offset] & 0xFF)
              | ((buffer[offset + 1] & 0xFF) << 8));
    }

    private int unpackInt32(final byte[] buffer, final int offset) {
      return ((buffer[offset] & 0xFF)
              | ((buffer[offset + 1] & 0xFF) << 8)
              | ((buffer[offset + 2] & 0xFF) << 16)
              | ((buffer[offset + 3] & 0xFF) << 24));
    }

    private class BlockGunzipper {
      //private static InflaterFactory defaultInflaterFactory = new InflaterFactory();

      private final Inflater inflater;
      private final CRC32 crc32 = new CRC32();
      private boolean checkCrcs = false;

      /**
       * Create a BlockGunzipper using the default inflaterFactory
       */
      BlockGunzipper() {
        inflater = new Inflater(true); // GZIP mode
      }

      /**
       * Allows the caller to decide whether or not to check CRCs on when
       * uncompressing blocks.
       */
      public void setCheckCrcs(final boolean check) {
        this.checkCrcs = check;
      }

      /**
       * Decompress GZIP-compressed data
       *
       * @param uncompressedBlock must be big enough to hold decompressed
       *                          output.
       * @param compressedBlock   compressed data starting at offset 0
       * @param compressedLength  size of compressed data, possibly less
       *                          than the size of the buffer.
       * @return the uncompressed data size.
       */
      public int unzipBlock(byte[] uncompressedBlock, byte[] compressedBlock, int compressedLength) {
        return unzipBlock(uncompressedBlock, 0, compressedBlock, 0, compressedLength);
      }

      /**
       * Decompress GZIP-compressed data
       *
       * @param uncompressedBlock       must be big enough to hold decompressed
       *                                output.
       * @param uncompressedBlockOffset the offset into uncompressedBlock.
       * @param compressedBlock         compressed data starting at offset 0.
       * @param compressedBlock         the offset into the compressed data.
       * @param compressedLength        size of compressed data, possibly less
       *                                than the size of the buffer.
       * @return the uncompressed data size.
       */
      public int unzipBlock(byte[] uncompressedBlock, int uncompressedBlockOffset,
              byte[] compressedBlock, int compressedBlockOffset, int compressedLength) {
        int uncompressedSize;
        try {
          ByteBuffer byteBuffer = ByteBuffer.wrap(compressedBlock, compressedBlockOffset, compressedLength);
          byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

          // Validate GZIP header
          if (byteBuffer.get() != GZIP_ID1
                  || byteBuffer.get() != (byte) GZIP_ID2
                  || byteBuffer.get() != GZIP_CM_DEFLATE
                  || byteBuffer.get() != GZIP_FLG)
            throw new RuntimeException("Invalid GZIP header");
          // Skip MTIME, XFL, OS fields
          byteBuffer.position(byteBuffer.position() + 6);
          if (byteBuffer.getShort() != GZIP_XLEN)
            throw new RuntimeException("Invalid GZIP header");
          // Skip blocksize subfield intro
          byteBuffer.position(byteBuffer.position() + 4);
          // Read ushort
          final int totalBlockSize = (byteBuffer.getShort() & 0xffff) + 1;
          if (totalBlockSize != compressedLength)
            throw new RuntimeException("GZIP blocksize disagreement");

          // Read expected size and CRD from end of GZIP block
          final int deflatedSize = compressedLength - BLOCK_HEADER_LENGTH - BLOCK_FOOTER_LENGTH;
          byteBuffer.position(byteBuffer.position() + deflatedSize);
          int expectedCrc = byteBuffer.getInt();
          uncompressedSize = byteBuffer.getInt();
          inflater.reset();

          // Decompress
          inflater.setInput(compressedBlock, compressedBlockOffset + BLOCK_HEADER_LENGTH, deflatedSize);
          final int inflatedBytes = inflater.inflate(uncompressedBlock, uncompressedBlockOffset, uncompressedSize);
          if (inflatedBytes != uncompressedSize)
            throw new RuntimeException("Did not inflate expected amount");

          // Validate CRC if so desired
          if (this.checkCrcs) {
            crc32.reset();
            crc32.update(uncompressedBlock, uncompressedBlockOffset, uncompressedSize);
            final long crc = crc32.getValue();
            if ((int) crc != expectedCrc)
              throw new RuntimeException("CRC mismatch");
          }
        } catch (DataFormatException e) {
          throw new RuntimeException(e);
        }
        return uncompressedSize;
      }
    }

    public class SeekableBufferedStream extends InputStream {

      /**
       * Little extension to buffered input stream to give access to the
       * available bytes in the buffer.
       */
      private class ExtBufferedInputStream extends BufferedInputStream {

        private ExtBufferedInputStream(final InputStream inputStream, final int i) {
          super(inputStream, i);
        }

        /**
         * Returns the number of bytes that can be read from the buffer
         * without reading more into the buffer.
         */
        int getBytesInBufferAvailable() {
          return this.count - this.pos;
        }

        /**
         * Return true if the position can be changed by the given delta
         * and remain in the buffer.
         */
        boolean canChangePos(long delta) {
          long newPos = this.pos + delta;
          return newPos >= 0 && newPos < this.count;
        }

        /**
         * Changes the position in the buffer by a given delta.
         */
        void changePos(int delta) {
          int newPos = this.pos + delta;
          if (newPos < 0 || newPos >= this.count)
            throw new IllegalArgumentException("New position not in buffer pos=" + this.pos + ", delta=" + delta);
          this.pos = newPos;
        }
      }

      public static final int DEFAULT_BUFFER_SIZE = 512000;

      final private int bufferSize;
      final SeekableFileStream wrappedStream;
      ExtBufferedInputStream bufferedStream;
      long position;

      public SeekableBufferedStream(final String filePath, final int bufferSize) throws FileNotFoundException {
        this.bufferSize = bufferSize;
        this.wrappedStream = new SeekableFileStream(filePath);
        this.position = 0;
        bufferedStream = new ExtBufferedInputStream(wrappedStream, bufferSize);
      }

      public SeekableBufferedStream(final String filePath) throws FileNotFoundException {
        this(filePath, DEFAULT_BUFFER_SIZE);
      }

      public long length() {
        return wrappedStream.length();
      }

      @Override
      public long skip(final long skipLength) throws IOException {
        if (skipLength < this.bufferedStream.getBytesInBufferAvailable()) {
          final long retval = this.bufferedStream.skip(skipLength);
          this.position += retval;
          return retval;
        } else {
          seekInternal(this.position + skipLength);
          return skipLength;
        }
      }

      public void seek(final long position) throws IOException {
        if (this.position == position)
          return;
        // check if the seek is within the buffer
        long delta = position - this.position;
        if (this.bufferedStream.canChangePos(delta)) {
          // casting to an int is safe since the buffer is less than the size of an int
          this.bufferedStream.changePos((int) delta);
          this.position = position;
        } else
          seekInternal(position);
      }

      private void seekInternal(final long position) throws IOException {
        wrappedStream.seek(position);
        bufferedStream = new ExtBufferedInputStream(wrappedStream, bufferSize);
        this.position = position;
      }

      @Override
      public int read() throws IOException {
        int b = bufferedStream.read();
        position++;
        return b;
      }

      @Override
      public int read(final byte[] buffer, final int offset, final int length) throws IOException {
        int nBytesRead = bufferedStream.read(buffer, offset, length);
        if (nBytesRead > 0) {
          //if we can't read as many bytes as we are asking for then attempt another read to reset the buffer.
          if (nBytesRead < length) {
            final int additionalBytesRead = bufferedStream.read(buffer, nBytesRead + offset, length - nBytesRead);
            //if there were additional bytes read then update nBytesRead
            if (additionalBytesRead > 0)
              nBytesRead += additionalBytesRead;
          }
          position += nBytesRead;
        }
        return nBytesRead;
      }

      @Override
      public void close() throws IOException {
        wrappedStream.close();
      }

      public boolean eof() throws IOException {
        return position >= wrappedStream.length();
      }

      public String getSource() {
        return wrappedStream.getSource();
      }

      public long position() throws IOException {
        return position;
      }

      public class SeekableFileStream extends InputStream {

        File file;
        RandomAccessFile fis;

        public SeekableFileStream(final String filePath) throws FileNotFoundException {
          this.file = new File(filePath);
          fis = new RandomAccessFile(file, "r");
        }

        public long length() {
          return file.length();
        }

        public boolean eof() throws IOException {
          return fis.length() == fis.getFilePointer();
        }

        public void seek(final long position) throws IOException {
          fis.seek(position);
        }

        public long position() throws IOException {
          return fis.getChannel().position();
        }

        @Override
        public long skip(long n) throws IOException {
          long initPos = position();
          fis.getChannel().position(initPos + n);
          return position() - initPos;
        }

        @Override
        public int read(final byte[] buffer, final int offset, final int length) throws IOException {
          if (length < 0)
            throw new IndexOutOfBoundsException();
          int n = 0;
          while (n < length) {
            final int count = fis.read(buffer, offset + n, length - n);
            if (count < 0)
              if (n > 0)
                return n;
              else
                return count;
            n += count;
          }
          return n;

        }

        @Override
        public int read() throws IOException {
          return fis.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
          return fis.read(b);
        }

        public String getSource() {
          return file.getAbsolutePath();
        }

        @Override
        public void close() throws IOException {
          fis.close();
        }
      }
    }
  }
}
