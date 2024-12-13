package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.vcfprocessor.files.AbstractRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.SAMLineFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Alignment;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Cigar;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Tag;

public abstract class AlignmentRecord extends AbstractRecord {

  //TODO it is blocks, function getEOF(){ return SAMRecord.getEOF() }
  public static final AlignmentRecord EOF = new SAMRecord("This\tis\tan\tend\tof\tfile\tmarker", null);
  public final Alignment createAlignment(SAM sam) throws SAMException {
    return new Alignment(
        this.getQueryName(),
        this.getFlag(),
        this.getRefId(),
        this.getPos(),
        this.getMappingQuality(),
        this.getCigar(),
        this.getNextRefId(),
        this.getNextPos(),
        this.getTemplateLength(),
        this.getSequence(),
        this.getSeqQuality(),
        this.getTags()
    );
  }

  public boolean applySAMLineFilters(SAM sam) {
    //applying line filters
    if (!sam.getCommandParser().getSAMLineFilters().isEmpty()) {
      /*if (f == null)
        f = filteredLine.split(T);*/
      for (SAMLineFilter filter : sam.getCommandParser().getSAMLineFilters())
        if (!filter.pass(this)) {
          this.filter(sam);
          return true;
        }
    }
    return false;
  }

  public boolean applyNonAlignmentFilters(SAM sam) throws SAMException {
    //Already filtered
    if(isFiltered())
      return true;

    return this.applySAMLineFilters(sam);
  }

  public abstract String getQueryName();
  public abstract int getFlag();
  public abstract String getRefId();
  public abstract int getPos();
  public abstract int getMappingQuality();
  public abstract Cigar getCigar() throws SAMException.InvalidCigarException;
  public abstract String getNextRefId();
  public abstract int getNextPos();
  public abstract int getTemplateLength();
  public abstract String getSequence();
  public abstract String getSeqQuality();
  public abstract Tag[] getTags();
  public final int getEndPos() throws SAMException.InvalidCigarException {
    return this.getPos() + this.getSeqLengthOnRef() - 1;
  }
  public final int getSeqLengthOnRef() throws SAMException.InvalidCigarException {
    return this.getCigar().getReferenceLength();
  }


}
