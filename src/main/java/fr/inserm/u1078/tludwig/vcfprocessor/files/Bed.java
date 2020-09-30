package fr.inserm.u1078.tludwig.vcfprocessor.files;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.RegionException;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Region;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 5 avr. 2016
 */
public class Bed {

  private final String filename;
  private final HashMap<Integer, ArrayList<Region>> regions;

  public Bed(String filename) {
    Message.info("Start loading Bed file : " + filename);
    this.filename = filename;
    this.regions = new HashMap<>();
    this.parse();
    Message.info(filename + " loaded : " + this.getRegionNumber() + " regions found; total size : " + this.getRegionSize());
  }

  private void parse() {
    regions.clear();
    try {
      UniversalReader in = new UniversalReader(this.filename);
      String line;
      while ((line = in.readLine()) != null)
        if (line.charAt(0) != '#')
          this.addToSortedRegions(new Region(line, Region.FORMAT_BED));
      in.close();
    } catch (Exception e) {
      Message.error("Could not parse BED file " + this.filename + "\n" + e.getMessage());
    }
  }

  private void addToSortedRegions(Region r) {
    boolean added = false;
    int num = Variant.chromToNumber(r.getChrom());
    ArrayList<Region> chrReg = this.regions.get(num);

    if (chrReg == null)
      chrReg = new ArrayList<>();

    for (int i = chrReg.size() - 1; i > -1; i--) {
      Region c = chrReg.get(i);
      if (r.compareTo(c) > 0) {
        chrReg.add(i + 1, r);
        added = true;
        break;
      }
    }

    if (!added)
      chrReg.add(0, r);

    this.regions.put(num, chrReg);
  }
  
  public void addPadding(int padding){
    for (int n : this.regions.keySet()){
      this.addPadding(n, padding);
      this.simplify(n);
    }
  }
  
  private void addPadding(int seq, int padding){
    for(Region region : this.regions.get(seq)){
      region.addPadding(padding);
    }
  }
  
  public void print(){
    for (int n : this.regions.keySet())
      for(Region region : this.regions.get(n)){
        System.out.println(region.asBed());
      }
  }

  private void simplify() {
    for (int n : this.regions.keySet())
      this.simplify(n);
  }

  private void simplify(int n) {
    ArrayList<Region> tmp = new ArrayList<>();
    ArrayList<Region> reg = this.regions.get(n);

    if (reg.size() > 0) {
      tmp.add(reg.get(0));
      for (int i = 1; i < reg.size(); i++) {
        Region current = reg.get(i);
        Region last = tmp.get(tmp.size() - 1);
        if (last.overlap(current)) {
          last = tmp.remove(tmp.size() - 1);
          try {
            tmp.add(Region.combine(last, current));
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
    return regions.get(Variant.chromToNumber(chr));
  }

  public ArrayList<Region> getAllRegions() {
    ArrayList<Region> ret = new ArrayList<>();
    for (int key : this.regions.keySet())
      ret.addAll(this.regions.get(key));
    return ret;
  }

  public boolean contains(String chr, int pos) {
    Region target = new Region(chr, pos, pos, Region.FORMAT_BED);
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
}
