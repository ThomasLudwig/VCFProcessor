package fr.inserm.u1078.tludwig.vcfprocessor.files;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.Main;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF.Reader;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
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
  
  private final LinkedBlockingQueue<LinesPair> queue = new LinkedBlockingQueue<>(1000);

  public MultiVCFReader(VCF vcf1, VCF vcf2) {
    this.vcf1 = vcf1;
    this.vcf2 = vcf2;
    this.commonsSamples = Sample.getCommonIDs(vcf1.getSamples(), vcf2.getSamples());

    this.reader1 = vcf1.getReaderWithoutStarting();
    new ReaderWrapper(reader1, vcf1.getFilename()).start();
    this.reader2 = vcf2.getReaderWithoutStarting();
    new ReaderWrapper(reader2, vcf2.getFilename()).start();
    new Synchronizer(new SyncReader()).start();
  }

  public ArrayList<String> getCommonsSamples() {
    return commonsSamples;
  }
  
  public LinesPair getNextLines(){
    if(!finished)
      try {
        LinesPair ret = queue.take();
        if(ret.getFirst() == null)
          finished = true;
        return ret;
      } catch (InterruptedException ex) {
        //Ignores
      }
    return new LinesPair();
  }
  
  private class SyncReader implements Runnable {
    
    @Override
    public void run() {
      try{
        Data data1 = nextLine(reader1);
        Data data2 = nextLine(reader2);
        while (data1.line != null && data2.line != null) {
          int compare = Variant.compare(data1.chrom, data1.pos, data2.chrom, data2.pos);
          if (compare < 0) {
            data1 = nextLine(reader1);
          } else if (compare > 0) {
            data2 = nextLine(reader2);
          } else {
            //Both files can have multiple lines for the same position
            ArrayList<String> lines1 = new ArrayList<>();
            int pos = data1.pos;
            //Message.debug("Match "+pos);
            while (pos == data1.pos) {
              lines1.add(data1.line);
              data1 = nextLine(reader1);
            }
            ArrayList<String> lines2 = new ArrayList<>();
            while (pos == data2.pos) {
              lines2.add(data2.line);
              data2 = nextLine(reader2);
            }
            try {
              queue.put(new LinesPair(lines1, lines2));
            } catch (InterruptedException ex) {
              //Ignore
            }
          }
        }
        if (data1.line == null)
          Message.info("Reached end of file " + vcf1.getFilename());
        if (data2.line == null)
          Message.info("Reached end of file " + vcf2.getFilename());
      } catch (VCFException e){
        Main.die("There was a problem while reading the VCF file", e);
      }

      reader1.close();
      reader2.close();
      
      try {
        queue.put(new LinesPair());
      } catch (InterruptedException ex) {
        //Ignore
      }
    }

    private Data nextLine(Reader reader) throws VCFException{
      String ret = reader.nextLine().line;
      while(VCF.FILTERED_LINE.equals(ret)){
        ret = reader.nextLine().line;
      }
      return new Data(ret);
    }    
  }
  
  public static class LinesPair {
    private final ArrayList<String> lines1;
    private final ArrayList<String> lines2;
    
    public LinesPair(){
      this.lines1 = null;
      this.lines2 = null;
    }

    public LinesPair(ArrayList<String> lines1, ArrayList<String> lines2) {
      this.lines1 = lines1;
      this.lines2 = lines2;
    }

    public ArrayList<String> getFirst() {
      return lines1;
    }

    public ArrayList<String> getSecond() {
      return lines2;
    }
  }
  
  private static class Data{
    private final String line;
    private String chrom = null;
    private int pos = -1;
    
    Data(String line){
      this.line = line;
      if(line != null){
        String[] f = line.split("\t");
        chrom = f[0];
        try {
          pos = new Integer(f[1]);
        } catch (NumberFormatException ignore) { }
      }
    }
  }
  
  private static class ReaderWrapper extends Thread {
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
  
  private static class Synchronizer extends Thread {

    public Synchronizer(Runnable target) {
      super(target);
    }
        
    @Override
    public String toString(){
      return "Synchronizer";
    }   
  }
}
