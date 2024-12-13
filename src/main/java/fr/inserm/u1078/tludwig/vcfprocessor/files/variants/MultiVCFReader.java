package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF.Reader;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThread;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-09-25
 */
public class MultiVCFReader {
  private final VCF vcf1;
  private final VCF vcf2;
  private final ArrayList<String> commonsSamples;
  private final Reader reader1;
  private final Reader reader2;
  private boolean finished = false;
  
  private final LinkedBlockingQueue<RecordPair> queue = new LinkedBlockingQueue<>(1000);

  public MultiVCFReader(VCF vcf1, VCF vcf2) {
    this.vcf1 = vcf1;
    this.vcf2 = vcf2;
    this.commonsSamples = Sample.getCommonIDs(vcf1.getSortedSamples(), vcf2.getSortedSamples());

    this.reader1 = vcf1.getReaderWithoutStarting();
    new ReaderWrapper(reader1, vcf1.getFilename()).start();
    this.reader2 = vcf2.getReaderWithoutStarting();
    new ReaderWrapper(reader2, vcf2.getFilename()).start();
    new Synchronizer(new SyncReader()).start();
  }

  public ArrayList<String> getCommonsSamples() {
    return commonsSamples;
  }
  
  public RecordPair getNextLines(){
    if(!finished)
      try {
        RecordPair ret = queue.take();
        if(ret.getFirst() == null)
          finished = true;
        return ret;
      } catch (InterruptedException ignore) { }
    return new RecordPair();
  }
  
  private class SyncReader extends WellBehavedThread {
    
    @Override
    public void doRun() {
      try{
        Data data1 = nextRecord(reader1);
        Data data2 = nextRecord(reader2);
        while (data1.record != null && data2.record != null) {
          int compare = Variant.compare(data1.chrom, data1.pos, data2.chrom, data2.pos);
          if (compare < 0) {
            data1 = nextRecord(reader1);
          } else if (compare > 0) {
            data2 = nextRecord(reader2);
          } else {
            //Both files can have multiple lines for the same position
            ArrayList<VariantRecord> lines1 = new ArrayList<>();
            int pos = data1.pos;
            //Message.debug("Match "+pos);
            while (pos == data1.pos) {
              lines1.add(data1.record);
              data1 = nextRecord(reader1);
            }
            ArrayList<VariantRecord> lines2 = new ArrayList<>();
            while (pos == data2.pos) {
              lines2.add(data2.record);
              data2 = nextRecord(reader2);
            }
            try {
              queue.put(new RecordPair(lines1, lines2));
            } catch (InterruptedException ignore) { }
          }
        }
        if (data1.record == null)
          Message.info("Reached end of file " + vcf1.getFilename());
        if (data2.record == null)
          Message.info("Reached end of file " + vcf2.getFilename());
      } catch (VCFException e){
        Message.fatal("There was a problem while reading the VCF file", e, true);
      }
/*
      reader1.close();
      reader2.close();
   */
      try {
        queue.put(new RecordPair());
      } catch (InterruptedException ignore) { }
    }

    private Data nextRecord(Reader reader) throws VCFException{
      VariantRecord record = reader.nextIndexedRecord().getRecord();
      while(record.isFiltered())
        record = reader.nextIndexedRecord().getRecord();
      return new Data(record);
    }
  }
  
  public static class RecordPair {
    private final ArrayList<VariantRecord> lines1;
    private final ArrayList<VariantRecord> lines2;
    
    public RecordPair(){
      this.lines1 = null;
      this.lines2 = null;
    }

    public RecordPair(ArrayList<VariantRecord> lines1, ArrayList<VariantRecord> lines2) {
      this.lines1 = lines1;
      this.lines2 = lines2;
    }

    public ArrayList<VariantRecord> getFirst() {
      return lines1;
    }

    public ArrayList<VariantRecord> getSecond() {
      return lines2;
    }
  }
  
  private static class Data{
    private VariantRecord record = null;
    private String chrom = null;
    private int pos = -1;
    
    Data(VariantRecord record){
      this.record = record;
      if(record != null){
        chrom = record.getChrom();
        try {
          pos = record.getPos();
        } catch (NumberFormatException ignore) { }
      }
    }
  }
  
  private static class ReaderWrapper extends WellBehavedThread {
    private final String name;

    ReaderWrapper(Runnable target, String name) {
      super(target);
      this.name = name;
    }

    @Override
    public String toString(){
      return "Reader("+name+")";
    }
  }
  
  private static class Synchronizer extends WellBehavedThread {

    public Synchronizer(Runnable target) {
      super(target);
    }
        
    @Override
    public String toString(){
      return "Synchronizer";
    }
  }
}
