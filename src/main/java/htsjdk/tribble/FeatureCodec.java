/*
 * Copyright (c) 2007-2010 by The Broad Institute, Inc. and the Massachusetts Institute of Technology.
 * All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL), Version 2.1 which
 * is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR WARRANTIES OF
 * ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT
 * OR OTHER DEFECTS, WHETHER OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR
 * RESPECTIVE TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES OF
 * ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES, ECONOMIC
 * DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER THE BROAD OR MIT SHALL
 * BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT SHALL KNOW OF THE POSSIBILITY OF THE
 * FOREGOING.
 */

package htsjdk.tribble;

import htsjdk.samtools.util.LocationAware;

import java.io.IOException;
import java.io.InputStream;

/**
 * The base interface for classes that read in features.
 * <p/>
 * FeatureCodecs must implement several key methods:
 * <p/>
 * <ul>
 * <li>{@link #makeSourceFromStream} Return a {@link SOURCE} for this {@link FeatureCodec} given an input stream that is buffered.
 * <li>{@link #makeIndexableSourceFromStream} Return a {@link SOURCE} for this {@link FeatureCodec} that implements {@link LocationAware},
 * and is thus suitable for use during indexing. During the indexing process, the indexer passes the {@link SOURCE} to the codec
 * to consume Features from the underlying {@link SOURCE}, one at a time, recording the Feature location via the {@link SOURCE}'s
 * {@link LocationAware} interface. Therefore, it is essential that the {@link SOURCE} implementation, the {@link #readHeader}
 * method, and the {@link #decodeLoc} method, not introduce any buffering that would that would advance the {@link SOURCE}
 * more than a single feature (or the more than the size of the header, in the case of {@link #readHeader}). Otherwise the
 * index will be corrupt.
 * <li>{@link #readHeader} - Reads the header, provided a {@link SOURCE} pointing at the beginning of the source input.
 * The implementation of this method must not consume any input from the underlying SOURCE beyond the end of the header.
 * <li>{@link #decode} - Reads a {@link Feature} record, provided a {@link SOURCE} pointing at the beginning of a
 * record within the source input.
 * <li>{@link #decodeLoc} - Reads a {@link Feature} record, provided a {@link SOURCE} pointing at the beginning of a
 * record within the source input. The implementation of this method must not consume any input from the underlying stream
 * beyond the end of the {@link Feature} returned.
 * </ul>
 * <p/>
 * Note that it's not safe to carry state about the {@link SOURCE} within the codec.  There's no guarantee about its
 * state between calls.
 *
 * @param <FEATURE_TYPE> The type of {@link Feature} this codec generates
 * @param <SOURCE> The type of the data source this codec reads from
 */
public interface FeatureCodec<FEATURE_TYPE extends Feature, SOURCE> {
    /**
     * Decode a line to obtain just its FeatureLoc for indexing -- contig, start, and stop.
     *
     * @param source the input stream from which to decode the next record
     * @return Return the FeatureLoc encoded by the line, or null if the line does not represent a feature (e.g. is
     *         a comment)
     */
    public Feature decodeLoc(final SOURCE source) throws IOException;

    /**
     * Decode a single {@link Feature} from the {@link SOURCE}, reading no further in the underlying source than beyond that feature.
     *
     * @param source the input stream from which to decode the next record
     * @return Return the Feature encoded by the line,  or null if the line does not represent a feature (e.g. is
     *         a comment)
     */
    public FEATURE_TYPE decode(final SOURCE source) throws IOException;

    /**
     * Read and return the header, or null if there is no header.
     * 
     * Note: Implementers of this method must be careful to read exactly as much from {@link SOURCE} as needed to parse the header, and no 
     * more. Otherwise, data that might otherwise be fed into parsing a {@link Feature} may be lost.
     *
     * @param source the source from which to decode the header
     * @return header object
     */
    public FeatureCodecHeader readHeader(final SOURCE source) throws IOException;

    /**
     * <p>
     * This function returns the object the codec generates.  This is allowed to be Feature in the case where
     * conditionally different types are generated.  Be as specific as you can though.
     * </p>
     * <p>
     * This function is used by reflections based tools, so we can know the underlying type
     * </p>
     * 
     * @return the feature type this codec generates.
     */
    public Class<FEATURE_TYPE> getFeatureType();

    /**
     * Generates a reader of type {@link SOURCE} appropriate for use by this codec from the generic input stream.  Implementers should
     * assume the stream is buffered.
     */
    public SOURCE makeSourceFromStream(final InputStream bufferedInputStream);

    /**
     * Return a {@link SOURCE} for this {@link FeatureCodec} that implements {@link LocationAware},
     * and is thus suitable for use during indexing. Like {@link #makeSourceFromStream(java.io.InputStream)}, except
     * the {@link LocationAware} compatibility is required for creating indexes.
     * </p>
     * Implementers of this method must return a type that is both {@link LocationAware} as well as {@link SOURCE}.  Note that this 
     * requirement cannot be enforced via the method signature due to limitations in Java's generic typing system.  Instead, consumers
     * should cast the call result into a {@link SOURCE} when applicable.
     *</p>
     * NOTE: During the indexing process, the indexer passes the {@link SOURCE} to the codec
     * to consume Features from the underlying {@link SOURCE}, one at a time, recording the Feature location via the {@link SOURCE}'s
     * {@link LocationAware} interface. Therefore, it is essential that the {@link SOURCE} implementation, the {@link #readHeader}
     * method, and the {@link #decodeLoc} method, which are used during indexing, not introduce any buffering that would that
     * would advance the {@link SOURCE} more than a single feature (or the more than the size of the header, in the case of
     * {@link #readHeader}).
     */
    public LocationAware makeIndexableSourceFromStream(final InputStream inputStream);

    /** Adapter method that assesses whether the provided {@link SOURCE} has more data. True if it does, false otherwise. */
    public boolean isDone(final SOURCE source);

    /** Adapter method that closes the provided {@link SOURCE}. */
    public void close(final SOURCE source);

    /**
     * <p>
     * This function returns true iff the File potentialInput can be parsed by this
     * codec. Note that checking the file's extension is a perfectly acceptable implementation of this method
     * and file contents only rarely need to be checked.
     * </p>
     * <p>
     * There is an assumption that there's never a situation where two different Codecs
     * return true for the same file.  If this occurs, the recommendation would be to error out.
     * </p>
     * Note this function must never throw an error.  All errors should be trapped
     * and false returned.
     *
     * @param path the file to test for parsability with this codec
     * @return true if potentialInput can be parsed, false otherwise
     */
    public boolean canDecode(final String path);

    /**
     * Define the tabix format for the feature, used for indexing. Default implementation throws an exception.
     *
     * Note that only {@link AsciiFeatureCodec} could read tabix files as defined in
     * {@link AbstractFeatureReader#getFeatureReader(String, String, FeatureCodec, boolean, java.util.function.Function, java.util.function.Function)}
     *
     * @return the format to use with tabix
     * @throws TribbleException if the format is not defined
     */


    /**
     * Codecs may override this method if the file that they recognize with {@link #canDecode(String)} is different than
     * the file that contains the data they parse.
     *
     * This enables a class of codecs where the input file is a configuration that defines how to locate and handle the
     * datafile.
     *
     * The default implementation returns the same path which was passed in.
     *
     * @param path the path to a file that this codec {@link #canDecode}
     * @return the path to the data file that should be parsed by this codec to produce Features.
     * @throws TribbleException codecs may throw if they cannot decode the path.
     */
    default String getPathToDataFile(String path){
        return path;
    }
}
