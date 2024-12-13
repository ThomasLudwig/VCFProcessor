package htsjdk.beta.plugin.reads;

import htsjdk.beta.io.bundle.Bundle;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.util.CloseableIterator;

import java.util.Optional;

/**
 * Base clanes the type parameters instantiated for
 * reads decoders.
 */
public interface ReadsDecoder  {

    /**
     * {@inheritDoc}
     *
     * Requires an index resource to be included in the input {@link Bundle}.
     */

    CloseableIterator<SAMRecord> queryUnmapped();

    /**
     * {@inheritDoc}
     *
     * Requires an index resource to be included in the input {@link Bundle}.
     */

    Optional<SAMRecord> queryMate(SAMRecord rec);
}

