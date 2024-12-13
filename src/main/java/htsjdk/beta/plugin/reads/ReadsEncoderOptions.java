package htsjdk.beta.plugin.reads;

import htsjdk.beta.codecs.reads.bam.BAMEncoderOptions;
import htsjdk.utils.ValidationUtils;

/**
 * General reads encoder options.
 */
public class ReadsEncoderOptions {
    private boolean preSorted = false;
    private BAMEncoderOptions bamEncoderOptions = new BAMEncoderOptions();

    /**
     * Get the preSorted setting for these options. Default value is false. If isPresorted is false, output
     * will be sorted as needed on write.
     *
     * @return true if preSorted is true, otherwise false
     */
    public boolean isPreSorted() {
        return preSorted;
    }

    /**
     * Set the preSorted setting for these options. Default value is false. If preSorted is false, output
     * will be sorted as needed on write.
     *
     * @param preSorted the preSorted state for these options
     * @return updated ReadsEncoderOptions
     */
    public ReadsEncoderOptions setPreSorted(boolean preSorted) {
        this.preSorted = preSorted;
        return this;
    }

    /**
     * Get the {@link BAMEncoderOptions} for these ReadsEncoderOptions. Defaults to default {@link BAMEncoderOptions}.
     *
     * @return the {@link BAMEncoderOptions} for these ReadsEncoderOptions
     */
    public BAMEncoderOptions getBAMEncoderOptions() { return bamEncoderOptions; }

    /**
     * Set the {@link BAMEncoderOptions} for these ReadsEncoderOptions. Defaults values are default
     * {@link BAMEncoderOptions}.
     *
     * @param bamEncoderOptions {@link BAMEncoderOptions} to use for these ReadsEncoderOptions
     * @return updated ReadsEncoderOptions
     */
    public ReadsEncoderOptions setBAMEncoderOptions(final BAMEncoderOptions bamEncoderOptions) {
        ValidationUtils.nonNull(bamEncoderOptions, "bamDecoderOptions");
        this.bamEncoderOptions = bamEncoderOptions;
        return this;
    }
}
