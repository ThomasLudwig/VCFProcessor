package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

  //private int mSkip; (not used)
  private String[] mSeq;

  private Map<String, Integer> mChr2tid;

  private static final int MAX_BIN = 37450;

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
    final long u;
    //long v;

    TPair64(final long _u/*, final long _v*/) {
      u = _u;
      //v = _v;
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

  /**
   * @param filePath path to the data file/uri
   * @throws IOException if something went wrong
   */
  public TabixReader(final String filePath) throws IOException {
    this(filePath, null);
  }

  /**
   * @param filePath  path to the of the data file/uri
   * @param indexPath Full path to the index file. Auto-generated if null
   * @throws IOException if something went wrong
   */
  public TabixReader(final String filePath, final String indexPath) throws IOException {
    mFilePath = filePath;
    try(BlockCompressedInputStream mFp = new BlockCompressedInputStream(filePath)){
    if (indexPath == null)
      mIndexPath = filePath + ".tbi";
    else
      mIndexPath = indexPath;
    readIndex();}
  }

  private static int readInt(final InputStream is) throws IOException {
    byte[] buf = new byte[4];
    @SuppressWarnings("unused")
    int skip = is.read(buf);
    return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getInt();
  }

  private static long readLong(final InputStream is) throws IOException {
    final byte[] buf = new byte[8];
    @SuppressWarnings("unused")
    int skip = is.read(buf);
    return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
  }

  /**
   * Read the Tabix index from a file
   */
  private void readIndex(final String filename) throws IOException {

    final BlockCompressedInputStream is = new BlockCompressedInputStream(filename, 128000);
    byte[] buf = new byte[4];

    @SuppressWarnings("unused")
    int skip = is.read(buf, 0, 4); // read "TBI\1"
    mSeq = new String[readInt(is)]; // # sequences
    mChr2tid = new HashMap<>(this.mSeq.length);
    /*int mPreset =*/ readInt(is);
    /*int mSc =*/ readInt(is);
    /*int mBc =*/ readInt(is);
    /*int mEc =*/ readInt(is);
    /*int mMeta =*/ readInt(is);
    readInt(is);//unused
    // read sequence dictionary
    int i, j, k, l = readInt(is);
    buf = new byte[l];

    skip = is.read(buf);
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
          /*long v = */readLong(is);
          chunks[k] = new TPair64(u/*, v*/); // in C, this is inefficient
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
   * @return the list of chromosome names
   */
  public List<String> getChromosomes() {
    return new ArrayList<>(Arrays.asList(mSeq));
  }

  @Override
  public String toString() {
    return "TabixReader: filename:" + this.mFilePath;
  }

  public final static String INCORRECT_HEADER_SIZE_MSG = "Incorrect header size for file: ";
  public final static String UNEXPECTED_BLOCK_LENGTH_MSG = "Unexpected compressed block length: ";
  public final static String PREMATURE_END_MSG = "Premature end of file: ";

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

  // Magic numbers
  public static final byte GZIP_ID1 = 31;
  public static final int GZIP_ID2 = 139;

  public static final int GZIP_FLG = 4;


  // length of extra subfield
  public static final short GZIP_XLEN = 6;

  // The deflate compression, which is customarily used by gzip
  public static final byte GZIP_CM_DEFLATE = 8;

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

  protected static class DecompressedBlock {

    /**
     * Decompressed block
     */
    private final byte[] mBlock;
    /**
     * Compressed size of block (the uncompressed size can be found using "mBlock.length")
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

  private static class BlockCompressedInputStream extends InputStream {

    private InputStream mStream;
    private SeekableBufferedStream mFile;
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
     * Determines whether the inflater will re-calculated the CRC on
     * the decompressed data and check it against the value stored in the
     * GZIP header. CRC checking is an expensive operation and should be
     * used accordingly.
     *
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
     * @throws IOException if something went wrong
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
     * Closes the underlying InputStream or RandomAccessFile
     *
     * @throws IOException if something went wrong
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
     * @throws IOException if something went wrong
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
     * read(buf) has the same effect as read(buffer, 0, length(buffer)).
     *
     * @param buffer the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or -1 is
     *         there is no more data because the end of the stream has been reached.
     * @throws IOException if something went wrong
     */
    @Override
    public int read(final byte[] buffer) throws IOException {
      return read(buffer, 0, buffer.length);
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
     * @throws IOException if something went wrong
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
     * @throws IOException if something went wrong
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
            final byte[] bufferAvailableForReuse) {
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

    private static class BlockGunzipper {
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
       * Allows the caller to decide whether to check CRCs on when decompressing blocks.
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
       * @param compressedBlockOffset         the offset into the compressed data.
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
          // Read unsigned short
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

    public static class SeekableBufferedStream extends InputStream {

      /**
       * Little extension to buffered input stream to give access to the
       * available bytes in the buffer.
       */
      private static class ExtBufferedInputStream extends BufferedInputStream {

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

      @Override
      public long skip(final long skipLength) throws IOException {
        if (skipLength < this.bufferedStream.getBytesInBufferAvailable()) {
          final long ret = this.bufferedStream.skip(skipLength);
          this.position += ret;
          return ret;
        } else {
          seekInternal(this.position + skipLength);
          return skipLength;
        }
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

      public String getSource() {
        return wrappedStream.getSource();
      }

      public static class SeekableFileStream extends InputStream {

        final File file;
        RandomAccessFile fis;

        public SeekableFileStream(final String filePath) throws FileNotFoundException {
          this.file = new File(filePath);
          fis = new RandomAccessFile(file, "r");
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
