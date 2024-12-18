package htsjdk.samtools;

import htsjdk.beta.exception.HtsjdkException;
import htsjdk.samtools.seekablestream.SeekablePathStream;
import htsjdk.samtools.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BamFileIoUtils {
    private static final Log LOG = Log.getInstance(BamFileIoUtils.class);

    /**
     * @deprecated since June 2019 Use {@link FileExtensions#BAM} instead.
     */
    @Deprecated
    public static final String BAM_FILE_EXTENSION = FileExtensions.BAM;

    public static boolean isBamFile(final File file) {
        return ((file != null) && SamReader.Type.BAM_TYPE.hasValidFileExtension(file.getName()));
    }


    public static void blockCopyBamFile(final File inputFile, final OutputStream outputStream, final boolean skipHeader, final boolean skipTerminator) {
        blockCopyBamFile(IOUtil.toPath(inputFile), outputStream, skipHeader, skipTerminator);
    }

    /**
     * Copy data from a BAM file to an OutputStream by directly copying the gzip blocks.
     *
     * @param inputFile      The BAM file to be copied
     * @param outputStream   The stream to write the copied data to
     * @param skipHeader     If true, the header of the input file will not be copied to the output stream
     * @param skipTerminator If true, the terminator block of the input file will not be written to the output stream
     */
    public static void blockCopyBamFile(final Path inputFile, final OutputStream outputStream, final boolean skipHeader, final boolean skipTerminator) {
        try (final SeekablePathStream in = new SeekablePathStream(inputFile)){
            // a) It's good to check that the end of the file is valid and b) we need to know if there's a terminator block and not copy it if skipTerminator is true
            final BlockCompressedInputStream.FileTermination term = BlockCompressedInputStream.checkTermination(inputFile);
            if (term == BlockCompressedInputStream.FileTermination.DEFECTIVE)
                throw new SAMException(inputFile.toUri() + " does not have a valid GZIP block at the end of the file.");

            if (skipHeader) {
                final long vOffsetOfFirstRecord = SAMUtils.findVirtualOffsetOfFirstRecordInBam(inputFile);

                // tsato: curious --- why do we need BlockCompressedInputStream at all here?
                try (final BlockCompressedInputStream blockIn = new BlockCompressedInputStream(inputFile)) {
                    blockIn.seek(vOffsetOfFirstRecord);
                    final long remainingInBlock = blockIn.available();

                    // If we found the end of the header then write the remainder of this block out as a
                    // new gzip block and then break out of the while loop (tsato: update this comment)
                    if (remainingInBlock >= 0) {
                        final BlockCompressedOutputStream blockOut = new BlockCompressedOutputStream(outputStream, (Path) null);
                        IOUtil.transferByStream(blockIn, blockOut, remainingInBlock);
                        blockOut.flush();
                        // Don't close blockOut because closing underlying stream would break everything
                    }

                    final long pos = BlockCompressedFilePointerUtil.getBlockAddress(blockIn.getFilePointer());
                    blockIn.close(); // tsato: why doesn't IntelliJ say this is unnecessary?

                    in.seek(pos);
                } catch (IOException e){
                    throw new HtsjdkException("Encountered an error.", e);
                }
            }

            // Copy remainder of input stream into output stream
            final long currentPos = in.position();
            final long length = Files.size(inputFile);
            final long skipLast = ((term == BlockCompressedInputStream.FileTermination.HAS_TERMINATOR_BLOCK) && skipTerminator) ?
                    BlockCompressedStreamConstants.EMPTY_GZIP_BLOCK.length : 0;
            final long bytesToWrite = length - skipLast - currentPos;

            IOUtil.transferByStream(in, outputStream, bytesToWrite);
        } catch (final IOException ioe) {
            throw new RuntimeIOException(ioe);
        }
    }

    /**
     * Assumes that all inputs and outputs are block compressed VCF files and copies them without decompressing and parsing
     * most of the gzip blocks. Will decompress and parse blocks up to the one containing the end of the header in each file
     * (often the first block) and re-compress any data remaining in that block into a new block in the output file. Subsequent
     * blocks (excluding a terminator block if present) are copied directly from input to output.
     */
    public static void gatherWithBlockCopying(final List<File> bams, final File output, final boolean createIndex, final boolean createMd5) {
        try {
            OutputStream out = new FileOutputStream(output);
            if (createMd5) out = new Md5CalculatingOutputStream(out, new File(output.getAbsolutePath() + ".md5"));
            File indexFile = null;
            if (createIndex) {
                indexFile = new File(output.getParentFile(), IOUtil.basename(output) + FileExtensions.BAI_INDEX);
                out = new StreamInflatingIndexingOutputStream(out, indexFile);
            }

            boolean isFirstFile = true;

            for (final File f : bams) {
                LOG.info(String.format("Block copying %s ...", f.getAbsolutePath()));
                blockCopyBamFile(IOUtil.toPath(f), out, !isFirstFile, true);
                isFirstFile = false;
            }

            // And lastly add the Terminator block and close up
            out.write(BlockCompressedStreamConstants.EMPTY_GZIP_BLOCK);
            out.close();

            // It is possible that the modified time on the index file is ever so slightly older than the original BAM file
            // and this makes ValidateSamFile unhappy.
            if (createIndex && (output.lastModified() > indexFile.lastModified())) {
                final boolean success = indexFile.setLastModified(System.currentTimeMillis());
                if (!success) {
                    System.err.print(String.format("Index file is older than BAM file for %s and unable to resolve this", output.getAbsolutePath()));
                }
            }
        } catch (final IOException ioe) {
            throw new RuntimeIOException(ioe);
        }
    }

    private static OutputStream buildOutputStream(final File outputFile, final boolean createMd5, final boolean createIndex) throws IOException {
        return buildOutputStream(IOUtil.toPath(outputFile), createMd5, createIndex);
    }

    private static OutputStream buildOutputStream(final Path outputFile, final boolean createMd5, final boolean createIndex) throws IOException {
        OutputStream outputStream = Files.newOutputStream(outputFile);
        if (createMd5) {
            outputStream = new Md5CalculatingOutputStream(outputStream, IOUtil.addExtension(outputFile, FileExtensions.MD5));
        }
        if (createIndex) {
            outputStream = new StreamInflatingIndexingOutputStream(outputStream, outputFile.resolveSibling(outputFile.getFileName() + FileExtensions.BAI_INDEX));
        }
        return outputStream;
    }


    @Deprecated
    private static void assertSortOrdersAreEqual(final SAMFileHeader newHeader, final File inputFile) throws IOException {
        assertSortOrdersAreEqual(newHeader, IOUtil.toPath(inputFile));
    }

    private static void assertSortOrdersAreEqual(final SAMFileHeader newHeader, final Path inputFile) throws IOException {
        final SamReader reader = SamReaderFactory.makeDefault().open(inputFile);
        final SAMFileHeader origHeader = reader.getFileHeader();
        final SAMFileHeader.SortOrder newSortOrder = newHeader.getSortOrder();
        if (newSortOrder != SAMFileHeader.SortOrder.unsorted && newSortOrder != origHeader.getSortOrder()) {
            throw new SAMException("Sort order of new header does not match the original file, needs to be " + origHeader.getSortOrder());
        }
        reader.close();
    }
}
