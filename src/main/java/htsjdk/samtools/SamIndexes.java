package htsjdk.samtools;

import htsjdk.samtools.seekablestream.SeekableBufferedStream;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.util.FileExtensions;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.RuntimeIOException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * A helper class to read BAI and CRAI indexes. Main goal is to provide BAI stream as a sort of common API for all index types.
 * <p/>
 * Created by vadim on 14/08/2015.
 */
public enum SamIndexes {
    BAI(FileExtensions.BAI_INDEX, "BAI\1".getBytes()),
    // CRAI is gzipped text, so it's magic is same as {@link java.util.zip.GZIPInputStream.GZIP_MAGIC}
    CRAI(FileExtensions.CRAM_INDEX, new byte[]{(byte) 0x1f, (byte) 0x8b}),
    CSI(FileExtensions.CSI, "CSI\1".getBytes());

    public final String fileNameSuffix;
    public final byte[] magic;

    SamIndexes(final String fileNameSuffix, final byte[] magic) {
        this.fileNameSuffix = fileNameSuffix;
        this.magic = magic;
    }

    public static InputStream openIndexFileAsBaiOrNull(final File file, final SAMSequenceDictionary dictionary) throws IOException {
        return openIndexUrlAsBaiOrNull(file.toURI().toURL(), dictionary);
    }

    public static InputStream openIndexUrlAsBaiOrNull(final URL url, final SAMSequenceDictionary dictionary) throws IOException {
        if (url.getFile().toLowerCase().endsWith(BAI.fileNameSuffix.toLowerCase())) {
            return url.openStream();
        }

        return null;
    }

    public static InputStream asBaiStreamOrNull(final InputStream inputStream, final SAMSequenceDictionary dictionary) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream(inputStream);
        bis.mark(BAI.magic.length);
        if (doesStreamStartWith(bis, BAI.magic)) {
            bis.reset();
            return bis;
        } else {
            bis.reset();
        }

         return null;
    }

    public static SeekableStream asBaiSeekableStreamOrNull(final SeekableStream inputStream, final SAMSequenceDictionary dictionary) throws IOException {
        final SeekableBufferedStream bis = new SeekableBufferedStream(inputStream);
        bis.seek(0);
        if (doesStreamStartWith(bis, BAI.magic)) {
            bis.seek(0);
            return bis;
        }

        bis.seek(0);

            bis.reset();

        bis.seek(0);
        if (doesStreamStartWith(bis, CSI.magic)) {
            bis.seek(0);
            return bis;
        }

        return null;
    }

    public static SamIndexes getSAMIndexTypeFromStream(final SeekableStream seekableStream) {
        SamIndexes indexType = null;
        try {
            seekableStream.seek(0);
            final SeekableBufferedStream bss = new SeekableBufferedStream(seekableStream);

            if (IOUtil.isGZIPInputStream(bss)) {
                bss.seek(0);
                GZIPInputStream gzipStream = new GZIPInputStream(bss);
                if (doesStreamStartWith(gzipStream, CSI.magic)) {
                    indexType = CSI;
                } else {
                    // the CRAI format has no signature bytes, so optimistically call it CRAI
                    // if its gzipped but not CSI
                    indexType = CRAI;
                }
            } else {
                bss.seek(0);
                if (doesStreamStartWith(bss, BAI.magic)) {
                    indexType = BAI;
                }
            }
            seekableStream.seek(0);
        } catch (final IOException e) {
            throw new RuntimeIOException("Error interrogating index input stream", e);
        }

        return indexType;
    }

    private static boolean doesStreamStartWith(final InputStream is, final byte[] bytes) throws IOException {
        for (final byte b : bytes) {
            if (is.read() != (0xFF & b)) {
                return false;
            }
        }
        return true;
    }
}
