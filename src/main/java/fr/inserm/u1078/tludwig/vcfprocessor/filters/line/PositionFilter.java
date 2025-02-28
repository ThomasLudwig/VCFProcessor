package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Bed;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;

import java.io.IOException;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class PositionFilter extends LineFilter {

  private final boolean overlap;
  private final Bed bed;

  public PositionFilter(boolean keep, boolean overlap) {
    super(keep);
    this.overlap = overlap;
    this.bed = new Bed();
  }

  /**
   * This method is called from the pos and position command line, that handles commandline arguments and file
   *
   * @param position the position to add
   * @throws java.lang.Exception if something went wrong
   */
  public void addPosition(String position) throws Exception {
    //check if line is empty, trim removes #
    //check if line is multi-column
    //check if multi position
    
    String trimmed = position.trim().split("#")[0].trim();
    if (trimmed.isEmpty())
      return;
    String[] f = trimmed.split("\\s+");
    String chr = f[0];
    int start = 1;
    int end = Integer.MAX_VALUE;
    
    switch (f.length) {
      default:
      case 3:
        start = Integer.parseInt(f[1]);
        end = Integer.parseInt(f[2]);
        break;
      case 2:
        start = Integer.parseInt(f[1]);
        end = Integer.parseInt(f[1]);
        break;
      case 1:
        String[] g = f[0].split(":");
        chr = g[0];
        if (g.length > 1) {
          String[] p = g[1].split("-", -1);
          switch (p.length) {
            case 2:
              start = Integer.parseInt(p[0]);
              end = Integer.parseInt(p[1]);
              break;
            case 1:
              start = Integer.parseInt(p[0]);
              end = Integer.parseInt(p[0]);
              break;
            default:
              throw new Exception("Invalid for [" + trimmed + "]");
          }
        }
        break;
    }
    
    this.add(new Region(chr, start, end, Region.Format.FULL_1_BASED));
  }
  
  /**
   * This method is called from the bed command line, that handles bed file arguments
   *
   * @param position the Bed position to add
   * @throws java.lang.Exception if something went wrong
   */
  public void addBedPosition(String position) throws Exception {
    //check if line is empty, trim removes #
    //line is multi-column
    
    String trimmed = position.trim().split("#")[0].trim();
    if (trimmed.isEmpty())
      return;
    String[] f = trimmed.split("\\s+");
    String chr = f[0];
    int start;
    int end;
    
    if(f.length > 2) {
      start = Integer.parseInt(f[1]);
      end = Integer.parseInt(f[2]);
    } else {
      throw new Exception("Invalid for [" + trimmed + "]");
    }
    
    this.add(new Region(chr, start, end, Region.Format.BED_FILE));
  }

  
  private String unrecognizedPosition(String position){
    return "Unrecognized position format [" + position + "] expected [chr:start-end] or [chr<tab>start<tab>end] (only chr is mandatory)";
  }
  
  public void addPositions(String... positions) {
    for (String position : positions)       
      try {
        this.addPosition(position);
      } catch (Exception e) {
        Message.warning(unrecognizedPosition(position));
      }      
  }

  //accepted format for position lines is
  //chr #comment
  //chr:pos #comment
  //chr:start-end #comment
  //chr<TAB>pos #comment
  //chr<TAB>start<TAB>end #comment
  public static final String[] POSITION_FILE_FORMATS = {
    "chr #comment",
    "chr:pos #comment",
    "chr:start-end #comment",
    "chr[tab]pos #comment",
    "chr[tab]start[tab]end #comment"};

  public void addPositionFilenames(String... filenames) {
    for (String filename : filenames) {
      Message.info("Loading positions from "+filename);
      String error = null;
      try (UniversalReader in = new UniversalReader(filename)){
        String line;
        while ((line = in.readLine()) != null)
          try {
            this.addPosition(line);
          } catch (Exception e) {
            if(error == null)
              error = e.getMessage();
          }
        if (error != null)
          Message.warning("Some lines could not be read in file [" + filename + "]. Valid line formats are " + String.join("|", POSITION_FILE_FORMATS)+"\nFirst Error :"+error);
      } catch (IOException ioe) {
        Message.warning("Problem while reading file [" + filename + "] : " + ioe.getMessage());
      }
    }
  }

  public void addBedFilename(String... beds) {
    for (String filename : beds) {
      String error = null;
      try (UniversalReader in = new UniversalReader(filename)){
        String line;
        while ((line = in.readLine()) != null)
          try {
            this.addBedPosition(line);
          } catch (Exception e) {
            if(error == null)
              error = e.getMessage();
          }
        if (error != null)
          Message.warning("Some lines could not be read in bed file [" + filename + "]. Line format is chr[tab]start[tab]end"+"\nFirst Error :"+error);
      } catch (IOException ioe) {
        Message.warning("Problem while reading bed file [" + filename + "] : " + ioe.getMessage());
      }
    }
  }

  public void add(Region r) { this.bed.addRegion(r); }

  @Override
  public boolean pass(VariantRecord record) {
    if (overlap) {
      int start = record.getPos();
      int length = record.getRef().replace("-", "").length();
      for (String alt : record.getAlts())
        length = Math.max(length, alt.replace("-", "").length());
      Region target = new Region(record.getChrom(), start, start + length - 1, Region.Format.FULL_1_BASED);

      return isKeep() == bed.overlaps(target);
      /*
      if (isKeep()) {
        for (Region r : regions)
          if (target.overlap(r))
            return true;
        return false;
      } else {
        for (Region r : regions)
          if (target.overlap(r))
            return false;
        return true;
      }*/
    } else {
      return isKeep() == bed.contains(record.getChrom(), record.getPos());
      /*if (isKeep()) {
        for (Region r : regions)
          if (r.contains(chr, pos))
            return true;
        return false;
      } else {
        for (Region r : regions)
          if (r.contains(chr, pos))
            return false;
        return true;
      }*/
    }
  }

  @Override
  public boolean leftColumnsOnly() {
    return true;
  }

  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove")+" positions"+(this.overlap ? " Overlapping" : "")+" : "+bed.getRegionSize()+" regions provided";
  }
}
