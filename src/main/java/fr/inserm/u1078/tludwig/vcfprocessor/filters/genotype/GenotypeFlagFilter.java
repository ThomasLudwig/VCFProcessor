package fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype;

import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.GenotypeFilter;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-23
 */
public class GenotypeFlagFilter extends GenotypeFilter {
  public static final String FT = "FT";

  private final ArrayList<String> flags;
  String[] format;
  int filterPos = -1;

  public GenotypeFlagFilter(boolean keep) {
    super(keep);
    this.flags = new ArrayList<>();
  }

  @Override
  public void setFormat(String[] format) {
    this.format = format;
    for (int i = 0; i < format.length; i++)
      if (FT.equalsIgnoreCase(this.format[i])){
        this.filterPos = i;
        break;
      }
  }

  public void add(String flag) {
    this.flags.add(flag);
  }

  @Override
  public boolean pass(String t) {
    String flag = ".";

    try {
      flag = t.split(":")[this.filterPos];
    } catch (Exception ignore) { }

    if(flag.isEmpty() || flag.equals("."))
      return true;
    
    for(String f : flag.split(",")){
      if(f.equalsIgnoreCase("PASS"))
        return true;
      if(this.flags.contains(f))
        return this.isKeep();
    }
    return !this.isKeep();
    //return this.flags.contains(flag) == this.isKeep();
  }
  
  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove")+" Genotype with FT in : "+StringTools.startOf(5, flags);
  }
}
