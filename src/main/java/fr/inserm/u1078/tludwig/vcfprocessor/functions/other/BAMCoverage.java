package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.AlignmentRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.SAM;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.SAMException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.alignments.SAMHeader;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.SAMFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThread;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.WellBehavedThreadFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BAMCoverage  extends Function {
  SAMFileParameter samFile = new SAMFileParameter(OPT_BAM, "sample1.bam", "The bam file to process");
  BedFileParameter bedFile = new BedFileParameter(OPT_BED, "regions.bed", "Regions");

  private SAM sam;
  LinkedBlockingQueue<AlignmentRecord> records = new LinkedBlockingQueue<>(50);

  @Override
  public String getSummary() {
    return "Print the info from of a BAM file to compute coverage";
  }

  @Override
  public Description getDescription() {
    return new Description("TODO set");
  }

  @Override
  public String getOutputExtension() {
    return OUT_SAM;
  }

  @Override
  public void executeFunction() throws Exception {
    sam = samFile.getSAM();

    ExecutorService threadPool = Executors.newFixedThreadPool(2, new WellBehavedThreadFactory());

    try {
      threadPool.submit(new Printer());
      threadPool.submit(new Producer());

      threadPool.shutdown();

      if(!threadPool.awaitTermination(300, TimeUnit.DAYS))
        Message.error("Thread reached its timeout");
    } catch (InterruptedException ignore) { }
  }


  public class Printer extends WellBehavedThread {
    @Override
    public void doRun() {
      AlignmentRecord record;
      for(SAMHeader.HeaderRecord header :  sam.getHeaders().getHeaderRecords())
        println(header);
      try {
        while (!AlignmentRecord.EOF.equals(record = records.take())) {
          try {
            println(record.createAlignment(sam));
          } catch (SAMException e) {
            Message.fatal("Can't Create Alignment from record \n" + record, e, true);
          }
        }
      } catch(InterruptedException ignore) {Message.fatal("Interrupted", true);}
    }
  }

  public class Producer extends WellBehavedThread {
    @Override
    public void doRun() {
      try{
        Bed bed = bedFile.getBed();
        bed.simplify();
        for(int chr : bed.getChromosomes()) {
          int ref = sam.getBAM().getHeaders().getRefFor("" + chr);
          if (ref != -1) {
            Message.debug("Looking for regions on ref[" + ref + "]");
            for(Region region : bed.getRegions(chr))
              sam.getBAM().populateRecords(ref, region, records);
            records.put(AlignmentRecord.EOF);
          }
        }
      } catch(InterruptedException e) {
        Message.fatal("Thread was interrupted", e, true);
      } catch(SAMException | IOException ioe) {
        Message.fatal("Can't get alignments", ioe, true);
      }
    }
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }
}
