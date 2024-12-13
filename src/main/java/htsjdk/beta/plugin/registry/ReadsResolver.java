package htsjdk.beta.plugin.registry;

import htsjdk.beta.exception.HtsjdkException;
import htsjdk.beta.exception.HtsjdkPluginException;
import htsjdk.beta.io.bundle.Bundle;
import htsjdk.beta.plugin.reads.ReadsDecoder;
import htsjdk.beta.plugin.reads.ReadsDecoderOptions;
import htsjdk.utils.ValidationUtils;

/**
 * Class with methods for resolving inputs and outputs to reads encoders and decoders.
 * <p>
 * {@link htsjdk.beta.plugin.reads.ReadsDecoderOptions}.
 */
public class ReadsResolver {

    /**
     * Create a ReadsResolver.
     */
    public ReadsResolver() {

    }

    /**
     * Get a {@link ReadsDecoder} suitable for decoding {@code inputBundle}. The {@code inputBundle} is
     * inspected to determine the appropriate file format/version.
     *
     * @param inputBundle the bundle to be decoded
     * @return a {@link ReadsDecoder} suitable for decoding {@code inputBundle}
     * @throws HtsjdkException if no registered codecs can handle the resource
     * @throws HtsjdkPluginException if more than one codec claims to handle the resource. this usually indicates
     * that the registry contains an incorrectly written codec.
     */
    public ReadsDecoder getReadsDecoder(final Bundle inputBundle) {
        ValidationUtils.nonNull(inputBundle, "Input bundle");

        return getReadsDecoder(inputBundle, new ReadsDecoderOptions());
    }

    /**
     * Get a {@link ReadsDecoder} suitable for decoding {@code inputBundle} using options in
     * {@code readsDecoderOptions}. The {@code inputBundle} is inspected to determine the appropriate
     * file format/version.
     *
     * @param inputBundle the bundle to be decoded
     * @param readsDecoderOptions {@link ReadsDecoderOptions} options to be used by the decoder
     * @return a {@link ReadsDecoder} suitable for decoding {@code inputBundle}
     * @throws HtsjdkException if no registered codecs can handle the resource
     * @throws HtsjdkPluginException if more than one codec claims to handle the resource. this usually indicates
     * that the registry contains an incorrectly written codec.
     */
    @SuppressWarnings("unchecked")
    public ReadsDecoder getReadsDecoder(
            final Bundle inputBundle,
            final ReadsDecoderOptions readsDecoderOptions) {
        ValidationUtils.nonNull(inputBundle, "Input bundle");
        ValidationUtils.nonNull(readsDecoderOptions, "Decoder options");

        return null;
    }



}


