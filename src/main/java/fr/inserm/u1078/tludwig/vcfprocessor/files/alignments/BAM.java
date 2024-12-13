package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;
import htsjdk.samtools.util.BlockCompressedInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipException;

public class BAM implements AlignmentProducer {
  public static final byte[] BAM_MAGIC_STRING = "BAM\1".getBytes();
  private final BlockCompressedInputStream in;
  private final SAM sam;
  private final BAI bai;

  private long pointer = 0;

  public BAM(String filename, SAM sam) throws IOException, BAMException {
    this.sam = sam;
    this.in = this.checkValid(filename);

    pointer += loadHeaders();
    this.bai = new BAI(filename+".bai");
    //this.header = new SAMHeader(in);
  }

  public BAM(String filename) throws IOException, BAMException {
    this(filename, null);
  }

  public long getPointer(){
    return this.pointer;
  }

  @Override
  public AlignmentRecord build(RawAlignmentRecordData record) throws SAMException {
    try {
      return new BAMRecord(this, record);
    } catch(BAMException bamE){
      throw new SAMException(this, bamE.getMessage(), bamE);
    }
  }

  @Override
  public long loadHeaders() throws IOException {
    SAMHeader samHeader = new SAMHeader(in);
    this.sam.setHeaders(samHeader);
    return samHeader.getByteLength();
  }

  @Override
  public SAMHeader getHeaders() {
    return this.sam.getHeaders();
  }

  @Override
  public void setHeaders(SAMHeader header) {
    this.sam.setHeaders(header);
  }

  @Override
  public String getFilename() {
    return this.sam.getFilename();
  }

  public String getReferenceName(int id){
    if(id == -1 || id >= getHeaders().getReferences().length)
      return "*";
    return getHeaders().getReferences()[id].getName();
  }

  @Override
  public void filter() {
    this.sam.filter();
  }

  public RawAlignmentRecordData readNext() throws IOException {
    try {
      final int blockSize = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt(); //TODO long ?
      final BAMByteArray bba = new BAMByteArray(in.readNBytes(blockSize));
      RawAlignmentRecordData rawAlignmentRecordData = new RawAlignmentRecordData(bba, this.pointer);
      this.pointer += 4 + blockSize;
      return rawAlignmentRecordData;
      //TODO OK for the start, but what about reads that start before and cross regions ?
    } catch(BufferUnderflowException bue){
      return null;
    }
  }

  private BlockCompressedInputStream checkValid(String filename) throws IOException, BAMException {
    try {
      BlockCompressedInputStream in = new BlockCompressedInputStream(new File(filename));
      byte[] magic = in.readNBytes(4);
      this.pointer += 4 ;
      if (!Arrays.equals(magic, BAM_MAGIC_STRING))
        throw new BAMException(BAMException.BAM_NO_MAGIC);
      return in;
    } catch(ZipException e) {
      throw new BAMException(BAMException.BAM_NOT_GZIP, e);
    }
  }

  public Bed getBed() {
    return sam.getBed();
  }


  /**
   * @param ref
   * @param originalRegions Regions are expected to be sorted
   * @param queue
   * @throws IOException
   * @throws SAMException
   * @throws InterruptedException
   */
  public void populateRecords(int ref, List<Region> originalRegions, LinkedBlockingQueue<AlignmentRecord> queue) throws IOException, SAMException, InterruptedException {
    //TODO is it faster to work region-by-region ?
    if(originalRegions.isEmpty()) {
      Message.debug("Empty regions");
      return;
    }
    ArrayList<Region> regions = new ArrayList<>(originalRegions);
    Message.debug("Looking for chunks");
    ChunkList chunks = bai.getChunks(ref, regions);
    Message.debug("Found " + chunks.size() + " chunks for ref ["+ref+"]");

    int first = regions.get(0).getStart1Based();
    int last = regions.get(regions.size() - 1).getEnd1Based();
    for (Chunk chunk : chunks) {
      //Message.debug("Chunk " + chunk);
      pointer = chunk.getBeg();
      long start = chunk.getBeg();
      long end = chunk.getEnd();

      BlockCompressedInputStream in = new BlockCompressedInputStream(new File(this.getFilename()));

      //Message.debug("Skipping to " + pointer);

      in.seek(pointer);
      //Message.debug("Reading until " + chunk.getEnd());
      int curPos = -1;
      while (pointer < end && curPos <= last) {
        final int blockSize = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt(); //TODO long ?
        final BAMByteArray bba = new BAMByteArray(in.readNBytes(blockSize));
        RawAlignmentRecordData record = new RawAlignmentRecordData(bba, pointer);
        pointer += 4 + blockSize;
        BAMRecord bamRecord = (BAMRecord) this.build(record);
        if (bamRecord == null)
          Message.debug("AlignmentRecord is null");
        else {
          curPos = bamRecord.getPos();
          int curEnd = bamRecord.getEndPos();
          Set<Region> toRemove = new HashSet<>();
          for (Region r : regions) {
            //if (curPos == 65438) Message.debug("Pos[" + curPos + "] End[" + curEnd + "] region [" + r + "] overlap [" + r.overlap(curPos, curEnd) + "]");
            if (r.overlap(curPos, curEnd)) {
              queue.put(bamRecord);
              break;
            } else if (r.getEnd1Based() < curPos){
              toRemove.add(r);
            }
          }
          regions.removeAll(toRemove);
        }
      }
      in.close();
    }
  }

  public void populateRecords(int ref, Region region, LinkedBlockingQueue<AlignmentRecord> queue) throws IOException, SAMException, InterruptedException {
    //TODO is it faster to work region-by-region ?
    ChunkList chunks = bai.getChunks(ref, region);

    int last = region.getEnd1Based();
    for (Chunk chunk : chunks) {
      //Message.debug("Chunk " + chunk);
      pointer = chunk.getBeg();
      long start = chunk.getBeg();
      long end = chunk.getEnd();

      BlockCompressedInputStream in = new BlockCompressedInputStream(new File(this.getFilename()));

      //Message.debug("Skipping to " + pointer);

      in.seek(pointer);
      //Message.debug("Reading until " + chunk.getEnd());
      int curPos = -1;
      while (pointer < end && curPos <= last) {
        final int blockSize = ByteBuffer.wrap(in.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt(); //TODO long ?
        final BAMByteArray bba = new BAMByteArray(in.readNBytes(blockSize));
        RawAlignmentRecordData record = new RawAlignmentRecordData(bba, pointer);
        pointer += 4 + blockSize;
        BAMRecord bamRecord = (BAMRecord) this.build(record);
        if (bamRecord == null)
          Message.debug("AlignmentRecord is null");
        else {
          curPos = bamRecord.getPos();
          int curEnd = bamRecord.getEndPos();
          if (region.overlap(curPos, curEnd))
            queue.put(bamRecord);
        }
      }
      in.close();
    }
  }

  @Override
  public String[] knownExtensions() {
    return new String[]{"bam"};
  }

  @Override
  public String fileFormatDescription() {
    return "Binary Alignment Map";
  }


}
