package fr.inserm.u1078.tludwig.vcfprocessor.files;

import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.RegionException;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 5 avr. 2016
 */
public class Bed implements FileFormat {

  private final String filename;
  private final HashMap<Integer, SortedList<Region>> regions;

  public Bed(File file) {
    this(file.getAbsolutePath());
  }

  public Bed() {
    this.filename = null;
    this.regions = new HashMap<>();
  }

  public Bed(String filename) {
    Message.info("Start loading Bed file : " + filename);
    this.filename = filename;
    this.regions = new HashMap<>();
    this.parse();
    Message.info(filename + " loaded : " + this.getRegionNumber() + " regions found; total size : " + this.getRegionSize());
    /*for(Integer chr : regions.keySet())
      for(Region region : regions.get(chr))
        Message.debug("Loaded "+region);*/
  }

  private void parse() {
    regions.clear();
    try(UniversalReader in = new UniversalReader(this.filename)){
      String line;
      while ((line = in.readLine()) != null) {
        boolean process = true;
        if (line.isEmpty())
          process = false;
        if (line.charAt(0) == '#')
          process = false;
        if (line.toLowerCase().startsWith("browser"))
          process = false;
        if (line.toLowerCase().startsWith("track"))
          process = false;
        if(process)
          this.addRegion(new Region(line, Region.Format.BED_FILE));
      }
    } catch (IOException e) {
      Message.error("Could not parse BED file " + this.filename + "\n" + e.getMessage());
    }
  }

  public ArrayList<Integer> getChromosomes(){
    SortedList<Integer> ret = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_FROM_END);
    ret.addAll(this.regions.keySet());
    return ret;
  }

  public void addRegion(Region r) {
    int num = Variant.chromToNumber(r.getChrom());
    SortedList<Region> chrReg = this.regions.get(num);

    if (chrReg == null)
      chrReg = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_FROM_END);
    chrReg.add(r);
    this.regions.put(num, chrReg);
  }

  public void addPadding(int padding){
    for (int n : this.regions.keySet()){
      this.addPadding(n, padding);
      this.simplify(n);
    }
  }

  private void addPadding(int seq, int padding){
    for(Region region : this.regions.get(seq))
      region.addPadding(padding);
  }
  
  public void print(){
    for (int chr :this.getChromosomes())
      for(Region region : this.regions.get(chr))
        System.out.println(region.asBed());
  }

  public void simplify() {
    for (int n : this.regions.keySet())
      this.simplify(n);
  }

  private void simplify(int n) {
    //TODO how to manage Annotations ? Here everything is put to the first region
    SortedList<Region> tmp = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_FROM_END);
    SortedList<Region> reg = this.regions.get(n);

    if (!reg.isEmpty()) {
      tmp.add(reg.get(0));
      for (int i = 1; i < reg.size(); i++) {
        Region current = reg.get(i);
        Region last = tmp.get(tmp.size() - 1);
        if (last.overlap(current)) {
          last = tmp.remove(tmp.size() - 1);
          try {
            tmp.add(Region.merge(last, current));
          } catch (RegionException e) {
            Message.error(e.getMessage());
          }
        } else
          tmp.add(current);
      }
    }

    this.regions.put(n, tmp);
  }

  public final int getRegionSize() {
    int ret = 0;
    for (int n : this.regions.keySet())
      for (Region region : this.regions.get(n))
        ret += region.getSize();
    return ret;
  }

  public final int getRegionNumber() {
    int ret = 0;
    for (int n : this.regions.keySet())
      ret += this.regions.get(n).size();
    return ret;
  }

  public final String getFilename() {
    return filename;
  }

  public ArrayList<Region> getRegions(String chr) {
    return getRegions(Variant.chromToNumber(chr));
  }

  public ArrayList<Region> getRegions(int chr) {
    return regions.get(chr);
  }

  public ArrayList<Region> getAllRegions() {
    ArrayList<Region> ret = new ArrayList<>();
    for (int key : this.regions.keySet())
      ret.addAll(this.regions.get(key));
    return ret;
  }

  public boolean overlaps(Region target) {
    //true if
    //contains start
    //contains end
    //is after start && before end

    ArrayList<Region> reg = this.regions.get(Variant.chromToNumber(target.getChrom()));

    int low = 0;
    int high = reg.size() - 1;
    int previous = -1;
    int current = 0;
    while (low != high) {
      current = (low + high) / 2;
      if (current == previous) {
        if (current == low)
          low++;
        current++;
      }
      Region r = reg.get(current);
      //Message.debug("target "+pos+" value "+r.getStart()+";"+r.getEnd()+" current "+current+" low "+low+" high "+high);

      if (r.overlap(target))
        return true;
      int compare = r.compareTo(target);
      previous = current;
      if (compare > 0)
        high = current;
      else
        low = current;
    }
    return reg.get(current).overlap(target);
  }

  public boolean contains(String chr, int pos) {
    Region target = new Region(chr, pos, pos, Region.Format.FULL_1_BASED);
    ArrayList<Region> reg = this.regions.get(Variant.chromToNumber(chr));
    int low = 0;
    int high = reg.size() - 1;
    int previous = -1;
    int current = 0;
    while (low != high) {
      current = (low + high) / 2;
      if (current == previous) {
        if (current == low)
          low++;
        current++;
      }
      Region r = reg.get(current);
      //Message.debug("target "+pos+" value "+r.getStart()+";"+r.getEnd()+" current "+current+" low "+low+" high "+high);

      if (r.contains(chr, pos))
        return true;
      int compare = r.compareTo(target);
      previous = current;
      if (compare > 0)
        high = current;
      else
        low = current;
    }
    return reg.get(current).contains(chr, pos);
  }

  @Override
  public String[] knownExtensions() {
    return new String[]{"bed"};
  }

  @Override
  public String fileFormatDescription() {
    return "Browser Extensible Data";
  }
}
