package htsjdk.beta.codecs.reads.bam;

import htsjdk.beta.io.bundle.BundleResourceType;
import htsjdk.beta.plugin.reads.ReadsFormats;
import htsjdk.io.IOPath;
import htsjdk.samtools.util.FileExtensions;
import htsjdk.utils.ValidationUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * InternalAPI
 *
 * Base class for {@link BundleResourceType#FMT_READS_BAM} codecs.
 */
public abstract class BAMCodec{

    private static final Set<String> extensionMap = new HashSet<>(Arrays.asList(FileExtensions.BAM));


    public String getFileFormat() { return ReadsFormats.BAM; }


    public boolean canDecodeURI(final IOPath ioPath) {
        ValidationUtils.nonNull(ioPath, "ioPath");
        return extensionMap.stream().anyMatch(ext-> ioPath.hasExtension(ext));
    }

}
