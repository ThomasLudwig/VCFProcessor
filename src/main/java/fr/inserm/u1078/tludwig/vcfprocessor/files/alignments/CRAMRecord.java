package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.AlignmentRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Cigar;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Tag;

public class CRAMRecord extends AlignmentRecord {

  @Override
  public String getQueryName() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public int getFlag() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public String getRefId() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public int getPos() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public int getMappingQuality() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public Cigar getCigar() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public String getNextRefId() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public int getNextPos() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public int getTemplateLength() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public String getSequence() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public String getSeqQuality() {
    throw new UnsupportedOperationException(); //TODO implement
  }

  @Override
  public Tag[] getTags() {
    throw new UnsupportedOperationException(); //TODO implement
  }
}
