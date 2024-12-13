package fr.inserm.u1078.tludwig.vcfprocessor.filters;

import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.AlignmentRecord;

public abstract class SAMLineFilter extends Filter<AlignmentRecord> {
  public SAMLineFilter(boolean keep) {
    super(keep);
  }
}
