package htsjdk.beta.io;

import htsjdk.beta.exception.HtsjdkIOException;
import htsjdk.io.IOPath;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Function;

public class IOPathUtils {


    /**
     * Get the entire contents of an IOPath file as a string.
     *
     * @param ioPath ioPath to consume
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
                    String.format("Failed to read from: %s", ioPath.getRawInputString()),
                    e);
        }
    }

    /**
     * Write a String to an IOPath.
     *
     * @param ioPath path where contents should be written
     * @param contents a UTF-8 string to be written
     */
    public static void writeStringToPath(final IOPath ioPath, final String contents) {
        try (final BufferedOutputStream bos = new BufferedOutputStream(ioPath.getOutputStream())) {
            bos.write(contents.getBytes());
        } catch (final IOException e) {
            throw new HtsjdkIOException(
                    String.format("Failed to write to: %s", ioPath.getRawInputString()),
                    e);
        }
    }

    /**
     * Takes an IOPath and returns a new IOPath object that keeps the same basename as the original but with
     * a new extension. Only the last component of the extension will be replaced, e.g. ("my.fasta.gz", ".tmp") ->
     * "my.fasta.tmp". If the original path has no extension, an exception will be thrown.
     *
     * If the input IOPath was created from a rawInputString that specifies a relative local path, the new path will
     * have a rawInputString that specifies an absolute path.
     *
     * Examples
     *     - ("test_na12878.bam", ".bai") -> "test_na12878.bai"
     *     - ("test_na12878.bam", "bai") -> "test_na12878.bai"
     *     - ("test_na12878.ext.bam, ".bai") -> "test_na12878.ext.md5"
     *
     * @param path The original path
     * @param newExtension The new file extension. If no leading "." is provided as part of the new extension, one will be added.
     * @param ioPathConstructor a function that takes a string and returns an IOPath-derived class of type <T>
     * @return A new IOPath object with the new extension
     */
    public static <T extends IOPath> T replaceExtension(
            final IOPath path,
            final String newExtension,
            final Function<String, T> ioPathConstructor){
        final String extensionToUse = newExtension.startsWith(".") ?
                newExtension :
                "." + newExtension;
        final Optional<String> oldExtension = path.getExtension();
        if (oldExtension.isEmpty()){
            throw new RuntimeException("The original path has no extension to replace" + path.getURIString());
        }
        final String oldFileName = path.toPath().getFileName().toString();
        final String newFileName = oldFileName.replaceAll(oldExtension.get() + "$", extensionToUse);
        return ioPathConstructor.apply(path.toPath().resolveSibling(newFileName).toUri().toString());
    }

    /**
     * Takes an IOPath and returns a new IOPath object that keeps the same name as the original, but with
     * the new extension added.  If no leading "." is provided as part of the new extension, one will be added.
     *
     * If the input IOPath was created from a rawInputString that specifies a relative local path, the new path will
     * have a rawInputString that specifies an absolute path.
     *
     * Examples:
     *     - ("test_na12878.bam", ".bai") -> "test_na12878.bam.bai"
     *     - ("test_na12878.bam", "md5") -> "test_na12878.bam.md5"
     *
     * @param path The original path
     * @param extension The file extension to add. If no leading "." is provided as part of the extension, one will be added.
     * @param ioPathConstructor a function that takes a string and returns an IOPath-derived class of type <T>
     * @return A new IOPath object with the new extension
     */
    public static <T extends IOPath> T appendExtension(
            final IOPath path,
            final String extension,
            final Function<String, T> ioPathConstructor){
        final String oldFileName = path.toPath().getFileName().toString();
        final String newExtension = extension.startsWith(".") ?
                extension :
                "." + extension;
        return ioPathConstructor.apply(path.toPath().resolveSibling(oldFileName + newExtension).toUri().toString());
    }
}
