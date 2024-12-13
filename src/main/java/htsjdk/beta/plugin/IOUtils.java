package htsjdk.beta.plugin;

import htsjdk.beta.exception.HtsjdkIOException;
import htsjdk.io.IOPath;
import htsjdk.samtools.util.BlockCompressedOutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Class for updated IOUtils methods that use either {@link htsjdk.io.IOPath} {@link java.nio.file.Path} in public
 * method argument lists.
 */
public class IOUtils {

    /**
     * Get the entire contents of an IOPath as a string.
     *
     * @param ioPath
     * @return a UTF-8 string representation of the file contents
     */
    public static String getStringFromPath(final IOPath ioPath) {
        try {
            final StringWriter stringWriter = new StringWriter();
            //TODO: the UTF-8 encoding of these should be codified somewhere else...
            Files.lines(ioPath.toPath(), StandardCharsets.UTF_8).forEach(
                    line -> {
                        stringWriter.write(line);
                        stringWriter.append("\n");
                    });
            return stringWriter.toString();
        } catch (final IOException e) {
            throw new HtsjdkIOException(
                    String.format("Failed to load reads bundle json from: %s", ioPath.getRawInputString()),
                    e);
        }
    }

    /**
     * Write a String to an IOPath.
     *
     * @param ioPath path where contents should be written
     * @param contents a UTF-8 string to be written
     * @param gzipOutput if true, gzip output
     */
    public static void writeStringToPath(final IOPath ioPath, final String contents, final boolean gzipOutput) {
         if (gzipOutput) {
             try (final BufferedOutputStream bos = new BufferedOutputStream(ioPath.getOutputStream());
                  final BlockCompressedOutputStream bcos = new BlockCompressedOutputStream(bos, ioPath.toPath())) {
                 bcos.write(contents.getBytes());
             } catch (final IOException e) {
                 throw new HtsjdkIOException(
                         String.format("Failed to load reads bundle json from: %s", ioPath.getRawInputString()), e);
             }
         } else {
             try (final BufferedOutputStream bos = new BufferedOutputStream(ioPath.getOutputStream())) {
                 bos.write(contents.getBytes());
             } catch (final IOException e) {
                 throw new HtsjdkIOException(
                         String.format("Failed to load reads bundle json from: %s", ioPath.getRawInputString()), e);
             }
         }
    }
}
