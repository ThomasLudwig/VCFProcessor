package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.BAMByteArray;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.CRAMByteArray;

public class RawAlignmentRecordData {
  private final String line;
  private final BAMByteArray bamBytes;
  private final CRAMByteArray cramBytes;
  private final long pointer;

  public RawAlignmentRecordData(String line) {
    this.line = line;
    this.bamBytes = null;
    this.cramBytes = null;
    this.pointer = -1;
  }

  public RawAlignmentRecordData(BAMByteArray bytes, long pointer) {
    this.line = null;
    this.bamBytes = bytes;
    this.cramBytes = null;
    this.pointer = pointer;
  }

  public RawAlignmentRecordData(CRAMByteArray bytes) {
    this.line = null;
    this.bamBytes = null;
    this.cramBytes = bytes;
    this.pointer = -1;
  }

  public String getLine() {
    return line;
  }

  public BAMByteArray getBAMBytes() {
    return bamBytes;
  }

  public long getPointer() {
    return pointer;
  }

  public CRAMByteArray getCRAMBytes() {
    return cramBytes;
  }
}
