package fr.inserm.u1078.tludwig.vcfprocessor.files.alignments;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.maok.tools.DateTools;
import fr.inserm.u1078.tludwig.vcfprocessor.Main;
import fr.inserm.u1078.tludwig.vcfprocessor.commandline.CommandParser;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThread;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SAM implements AlignmentProducer {
  private SAMHeader header;
  public static final int QUEUE_DEPTH = 200;
  //public static final int STEP1000000 = 1000000;
  //private final int step;
  private final Bed bed;
  private final String filename;
  private final Lock readLock;
  private Reader uniqLineReader = null;
  private final CommandParser commandParser;

  private final UniversalReader in;
  private final BAM bam;
  private final CRAM cram;

  private final AtomicInteger nbAlignmentsRead = new AtomicInteger(0);
  private final AtomicInteger nbAlignmentsFiltered = new AtomicInteger(0);

  public SAM(String filename, Bed bed/*, int step*/) throws IOException, SAMException {
    this.filename = filename;
    this.bed = bed;
    //this.step = step;
    this.readLock = new ReentrantLock();
    this.commandParser = Main.getCommandParser();

    BAM tmpBAM=null;
    CRAM tmpCRAM=null;

    try {
      tmpBAM = new BAM(this.filename, this);
    } catch(BAMException ignore){
      //nothing
    }

    try {
      tmpCRAM = new CRAM(this.filename, this);
    } catch(CRAMException ignore){
      //nothing
    }

    this.bam = tmpBAM;
    this.cram = tmpCRAM;

    if(this.bam == null && this.cram == null) {
      in = new UniversalReader(this.filename);
      this.loadHeaders();
    } else
      in = null;
  }

  /*public SAM(String filename, Bed bed) throws IOException, SAMException {
    this(filename, bed, STEP1000000);
  }*/

  public SAM(String filename) throws IOException, SAMException {
    this(filename, null);
  }

  @Override
  public long loadHeaders() throws IOException {
    setHeaders(new SAMHeader( this.filename));
    return -1;
  }

  @Override
  public String getFilename() {
    return filename;
  }

  public Bed getBed() {
    return bed;
  }

  public CommandParser getCommandParser() {
    return commandParser;
  }

  @Override
  public SAMHeader getHeaders() {
    return header;
  }

  @Override
  public void setHeaders(SAMHeader header){
    this.header = header;
  }

  public BAM getBAM() {
    return this.bam;
  }

  private RawAlignmentRecordData readRecordFromUnderlyingStructure() throws SAMException, IOException {
    if(in != null) {
      String line = in.readLine();
      if(line == null)
        return null;
      return new RawAlignmentRecordData(line);
    }
    if(bam != null)
      return bam.readNext();
    return cram.readNext();
  }

  private RawAlignmentRecordData readNextPhysicalRecord() throws SAMException {
    RawAlignmentRecordData record;
    try {
      if ((record = readRecordFromUnderlyingStructure()) != null)
        this.nbAlignmentsRead.incrementAndGet();
    } catch (IOException e) {
      throw new SAMException(this, "Could not read next line in SAM/BAM/CRAM file", e);
    }
    return record;//only filters until EOF
  }

  @Override
  public RawAlignmentRecordData readNext() throws IOException {
    String line = in.readLine();
    if(line == null)
      return null;
    return new RawAlignmentRecordData(line);
  }

  @Override
  public AlignmentRecord build(RawAlignmentRecordData record) throws SAMException {
    return new SAMRecord(record.getLine(), SAM.this);
  }

  public void filter() {
    nbAlignmentsFiltered.incrementAndGet();
  }

  @Override
  public String[] knownExtensions() {
    return new String[]{"sam"};
  }

  @Override
  public String fileFormatDescription() {
    return "Sequence Alignment Map";
  }

  public void printAlignmentsKept() {
    Message.info("Alignments Kept : " + (this.nbAlignmentsRead.get() - this.nbAlignmentsFiltered.get()) + "/" + this.nbAlignmentsRead.get() + " (" + this.nbAlignmentsFiltered.get() + " filtered)");
  }

  public Reader getReaderAndStart() {
    Reader reader = this.getReaderWithoutStarting();
    reader.start();
    return reader;
  }

  public Reader getReaderWithoutStarting() {
    if (this.uniqLineReader == null) {
      readLock.lock();
      try {
        if (this.uniqLineReader == null)
          this.uniqLineReader = new Reader();
      } finally {
        readLock.unlock();
      }
    }
    return this.uniqLineReader;
  }

  public class IndexedRecord {
    public final int index;
    public final RawAlignmentRecordData raw;
    public AlignmentRecord record = null;

    public IndexedRecord(int index, RawAlignmentRecordData raw) {
      this.index = index;
      this.raw = raw;
    }

    public AlignmentRecord getRecord() {
      if(raw == null)
        return null;
      if(record == null){
        record = buildRecord();
      }
      return record;
    }

    public AlignmentRecord buildRecord() {
      if (raw == null)
        return null;
      try {
        if (SAM.this.bam != null)
          return SAM.this.bam.build(raw);
        else if (SAM.this.cram != null)
          return SAM.this.cram.build(raw);
        else
          return SAM.this.build(raw);
      } catch(Exception e) {
        Message.fatal("Unable to create "+index+"th Record", e, true);
      }
      return null;
    }

    public boolean isEOF() {
      return null == raw;
    }

    @Override
    public String toString() {
      return "IndexreadRecord["+index+"]";
    }
  }

  public class Reader extends WellBehavedThread {
    private final LinkedBlockingQueue<SAM.IndexedRecord> queue;
    private int read = 0;
    private int consumed = 0;
    private long start = -1;
    private boolean stop = false;

    public Reader() {
      queue = new LinkedBlockingQueue<>(QUEUE_DEPTH);
    }

    @Override
    public String toString() {
      return this.getClass().getSimpleName() + "[" + SAM.this.getFilename() + "]";
    }

    @Override
    public void doRun() {
      RawAlignmentRecordData record;
      try {
        for (read = 1; (record = SAM.this.readNextPhysicalRecord()) != null; read++)
          this.queue.put(new SAM.IndexedRecord(read, record));
      } catch (SAMException e) {
        Message.fatal("Unable to read from file [" + getFilename() + "]", e, true);
      } catch (InterruptedException ignore) { }

      read--;//overshot by 1 in the for loop
      for (int i = 0; i < QUEUE_DEPTH / 2; i++)
        try {
          this.queue.put(new SAM.IndexedRecord(read + 1, null)); //Pack with trailing null to given one to each Worker
        } catch (InterruptedException ignore) { }
    }

    public AlignmentRecord nextRecord() { //Possibly called by a future Function
      SAM.IndexedRecord n = nextIndexedRecord();
      if(n == null)
        return null;
      return n.getRecord();
    }

    public SAM.IndexedRecord nextIndexedRecord() {
      if (start < 0)
        start = new Date().getTime();
      SAM.IndexedRecord next = null;
     /* if(start > -2)
        throw new RuntimeException("Vas te faire enculer bien comme il faut !");*/
      try {
        next = queue.take();
        if (next.raw != null) {
          this.consumed++;
          AlignmentRecord record = next.getRecord();
          /*if (step > 0 && this.consumed % step == 0) {
            double dur = DateTools.duration(start);
            int speed = (int) (consumed / dur);
            Message.info(consumed + "/" + read + " alignments read from " + getFilename() + " in " + dur + "s (" + speed + " v/s)");
          }*/
          record.applyNonAlignmentFilters(SAM.this);
        } else {
          try {
            readLock.lock();
            if (!stop) {
              stop = true;
              double dur = DateTools.duration(start);
              int speed = (int) (consumed / dur);
              Message.info(consumed + "/" + read + " alignments read from " + getFilename() + " in " + dur + "s (" + speed + " v/s)");
            }
          } finally {
            readLock.unlock();
          }
        }
      } catch(SAMException ve) {
        Message.fatal("Could not apply filter to "+AlignmentRecord.class.getSimpleName()+"\n"+next.raw, ve, true);
      } catch (InterruptedException ignore) { }

      return next;
    }
  }
}
