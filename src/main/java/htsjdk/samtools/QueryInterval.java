package htsjdk.samtools;

import htsjdk.samtools.util.CoordMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interval relative to a reference, for querying a BAM file.
 */
public class QueryInterval implements Comparable<QueryInterval> {

    /** Index of reference sequence, based on the sequence dictionary of the BAM file being queried. */
    public final int referenceIndex;
    /** 1-based, inclusive */
    public final int start;
    /** 1-based, inclusive.  If <= 0, implies that the interval goes to the end of the reference sequence */
    public final int end;


    public QueryInterval(final int referenceIndex, final int start, final int end) {
        if (referenceIndex < 0) {
            throw new IllegalArgumentException("Invalid reference index " + referenceIndex);
        }
        this.referenceIndex = referenceIndex;
        this.start = start;
        this.end = end;
    }


    @Override
    public int compareTo(final QueryInterval other) {
        int comp = this.referenceIndex - other.referenceIndex;
        if (comp != 0) return comp;
        comp = this.start - other.start;
        if (comp != 0) return comp;
        else if (this.end == other.end) return 0;
        else if (this.end == 0) return 1;
        else if (other.end == 0) return -1;
        else return this.end - other.end;
    }

    /**
     * @return true if both are on same reference, and other starts exactly before this ends.
     */
    public boolean endsAtStartOf(final QueryInterval other) {
        return this.referenceIndex == other.referenceIndex && this.end + 1 == other.start;
    }

    /**
     * @return true if both are on same reference, and the overlap.
     */
    public boolean overlaps(final QueryInterval other) {
        if (this.referenceIndex != other.referenceIndex) {
            return false;
        }
        final int thisEnd = (this.end == 0 ? Integer.MAX_VALUE : this.end);
        final int otherEnd = (other.end == 0 ? Integer.MAX_VALUE : other.end);
        return CoordMath.overlaps(this.start, thisEnd, other.start, otherEnd);
    }

    @Override
    public String toString() {
        return String.format("%d:%d-%d", referenceIndex, start, end);
    }

    private static final QueryInterval[] EMPTY_QUERY_INTERVAL_ARRAY = new QueryInterval[0];

    /**
     * @param inputIntervals WARNING: This list is modified (sorted) by this method.
     * @return Ordered list of intervals in which abutting and overlapping intervals are merged.
     */
    public static QueryInterval[] optimizeIntervals(final QueryInterval[] inputIntervals) {
        if (inputIntervals.length == 0) return EMPTY_QUERY_INTERVAL_ARRAY;
        Arrays.sort(inputIntervals);

        final List<QueryInterval> unique = new ArrayList<QueryInterval>();
        QueryInterval previous = inputIntervals[0];


        for (int i = 1; i < inputIntervals.length; ++i) {
            final QueryInterval next = inputIntervals[i];
            if (previous.endsAtStartOf(next) || previous.overlaps(next)) {
                final int newEnd = ((previous.end == 0 || next.end == 0) ? 0 : Math.max(previous.end, next.end));
                previous = new QueryInterval(previous.referenceIndex, previous.start, newEnd);
            } else {
                unique.add(previous);
                previous = next;
            }
        }

        if (previous != null) unique.add(previous);

        return unique.toArray(EMPTY_QUERY_INTERVAL_ARRAY);
    }

    /**
     * @throws java.lang.IllegalArgumentException if the intervals are not optimized
     * @see QueryInterval#optimizeIntervals(QueryInterval[])
     */
    public static void assertIntervalsOptimized(final QueryInterval[] intervals) {
        if (intervals.length == 0) return;
        for (int i = 1; i < intervals.length; ++i) {
            final QueryInterval prev = intervals[i-1];
            final QueryInterval thisInterval = intervals[i];
            if (prev.compareTo(thisInterval) >= 0) {
                throw new IllegalArgumentException(String.format("List of intervals is not sorted: %s >= %s", prev, thisInterval));
            }
            if (prev.overlaps(thisInterval)) {
                throw new IllegalArgumentException(String.format("List of intervals is not optimized: %s intersects %s", prev, thisInterval));
            }
            if (prev.endsAtStartOf(thisInterval)) {
                throw new IllegalArgumentException(String.format("List of intervals is not optimized: %s abuts %s", prev, thisInterval));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryInterval that = (QueryInterval) o;

        return this.compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
        int result = referenceIndex;
        result = 31 * result + start;
        result = 31 * result + end;
        return result;
    }
}
