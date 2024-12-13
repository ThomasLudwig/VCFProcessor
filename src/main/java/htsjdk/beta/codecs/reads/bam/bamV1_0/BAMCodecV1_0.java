package htsjdk.beta.codecs.reads.bam.bamV1_0;

import htsjdk.beta.codecs.reads.bam.BAMCodec;
import htsjdk.beta.codecs.reads.bam.BAMDecoder;
import htsjdk.beta.codecs.reads.bam.BAMEncoder;
import htsjdk.beta.exception.HtsjdkIOException;
import htsjdk.beta.io.bundle.Bundle;
import htsjdk.beta.io.bundle.SignatureStream;
import htsjdk.beta.plugin.reads.ReadsDecoderOptions;
import htsjdk.beta.plugin.reads.ReadsEncoderOptions;
import htsjdk.samtools.SamStreams;
import htsjdk.samtools.util.BlockCompressedStreamConstants;
import htsjdk.utils.ValidationUtils;

import java.io.IOException;

/**
 * BAM v1.0 codec.
 */
public class BAMCodecV1_0 extends BAMCodec {

    public int getSignatureProbeLength() { return BlockCompressedStreamConstants.MAX_COMPRESSED_BLOCK_SIZE; }

    public int getSignatureLength() {
        return BlockCompressedStreamConstants.DEFAULT_UNCOMPRESSED_BLOCK_SIZE;
    }

    public boolean canDecodeSignature(final SignatureStream probingInputStream, final String sourceName) {
        ValidationUtils.nonNull(probingInputStream);
        ValidationUtils.nonNull(sourceName);

        try {
            // technically this should check the version, but its BAM so there isn't one...
            return SamStreams.isBAMFile(probingInputStream);
        } catch (IOException e) {
            throw new HtsjdkIOException(String.format("Failure reading signature from stream for %s", sourceName), e);
        }
    }

    public BAMDecoder getDecoder(final Bundle inputBundle, final ReadsDecoderOptions decoderOptions) {
        return new BAMDecoderV1_0(inputBundle, decoderOptions);
    }

    public BAMEncoder getEncoder(final Bundle outputBundle, final ReadsEncoderOptions encoderOptions) {
        return null;
    }


}
