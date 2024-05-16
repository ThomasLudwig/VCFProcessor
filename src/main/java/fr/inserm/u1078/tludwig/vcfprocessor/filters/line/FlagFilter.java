package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class FlagFilter extends LineFilter {
  private final boolean matchesAll;
  private final boolean strict;

  private final ArrayList<String> flags;

  /*
    A,B   | keepAll  | keepAny  | removeAll | removeAny | keepStrictAll | keepStrictAny | removeStrictAll | removeStrictAny
    ------+----------+----------+-----------+-----------+---------------+---------------+-----------------+-----------------
    A     | pass     | pass     | filtered  | filtered  | filtered      | filtered      | pass            | pass                
    B     | pass     | pass     | filtered  | filtered  | filtered      | filtered      | pass            | pass                
    C     | filtered | filtered | pass      | pass      | filtered      | filtered      | pass            | pass
    A;B   | pass     | pass     | filtered  | filtered  | pass          | pass          | filtered        | filtered        
    A;C   | filtered | pass     | pass      | filtered  | filtered      | filtered      | pass            | pass                
    B;C   | filtered | pass     | pass      | filtered  | filtered      | filtered      | pass            | pass                
    A;B;C | filtered | pass     | pass      | filtered  | filtered      | pass          | pass            | filtered        
				
    OPT_KEEP_FILTERED_ALL           to pass, all encountered flag must be listed
    OPT_KEEP_FILTERED_ANY           to pass, any encountered flag must be listed
    OPT_STRICT_KEEP_FILTERED_ALL    to pass, all listed flag must be present, and no other
    OPT_STRICT_KEEP_FILTERED_ANY    to pass, all listed flag must be present
    OPT_REMOVE_FILTERED_ALL         to pass, there must be unlisted flags (sites kept by OPT_KEEP_FILTERED_ALL are removed and vice versa)
    OPT_REMOVE_FILTERED_ANY         to pass, no encountered flag must be listed  (sites kept by OPT_KEEP_FILTERED_ANY are removed and vice versa)
    OPT_STRICT_REMOVE_FILTERED_ALL  to pass, all listed flag must not be present, and no other (sites kept by OPT_STRICT_KEEP_FILTERED_ALL are removed and vice versa)
    OPT_STRICT_REMOVE_FILTERED_ANY  to pass, all listed flag must not be present (sites kept by OPT_STRICT_KEEP_FILTERED_ANY are removed and vice versa)
  */
  
  /**
   * 
   * @param keep true if we keep the variant, false if we drop them
   * @param matchesAll true -> keep/reject only when all flag are in the list; false -> keep/reject when at least one flag is in the list
   * @param strict strict mode
   */
  public FlagFilter(boolean keep, boolean matchesAll, boolean strict) {
    super(keep);
    this.matchesAll = matchesAll;
    this.strict = strict;
    this.flags = new ArrayList<>();
  }
  
  public FlagFilter(String key){
    super(key.toLowerCase().contains("keep"));
    this.matchesAll = key.toLowerCase().contains("all");
    this.strict = key.toLowerCase().contains("strict");
    this.flags = new ArrayList<>();
  }

  public void add(String flag) {
    this.flags.add(flag);
  }
  

  @Override
  public boolean pass(String[] f) {
    ArrayList<String> presents = new ArrayList<>(Arrays.asList(f[6].split(";")));
    
    if(strict){
      //if not all are found --> isFilter()
      for(String flag : flags)
        if(!presents.contains(flag))
          return isFilter();
      //if others are found --> isKept() for any, isFilter() for all
      for(String present : presents)
        if(!flags.contains(present))
          if(matchesAll)
            return isFilter();
      return isKeep();
    } else {
      if(matchesAll){
        for(String present : presents)
          if(!flags.contains(present))  
            return isFilter();
        return isKeep();
      } else {
        for(String present : presents)
          if(flags.contains(present))
            return isKeep();
        return isFilter();
      }  
    }
  }
  
  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove") +(strict ? "strict" : "")+(matchesAll? "all":"any")+" "+StringTools.startOf(5, flags);
  }
}
