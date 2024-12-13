package htsjdk.beta.codecs.reads.sam.samV1_0;

import htsjdk.beta.codecs.reads.sam.SAMCodec;
import htsjdk.beta.codecs.reads.sam.SAMDecoder;
import htsjdk.beta.io.bundle.Bundle;
import htsjdk.beta.plugin.reads.ReadsDecoderOptions;

/**
 * SAM v1.0 codec.
 */
public class SAMCodecV1_0 extends SAMCodec {

    public SAMDecoder getDecoder(final Bundle inputBundle, final ReadsDecoderOptions decoderOptions) {
        return new SAMDecoderV1_0(inputBundle, decoderOptions);
    }
}
