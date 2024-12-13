package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class InfoFilter extends LineFilter {

  private final ArrayList<String> fields;
  private final boolean and;

  public InfoFilter(boolean keep, boolean and) {
    super(keep);
    this.and = and;
    this.fields = new ArrayList<>();
  }

  public void add(String infoField) {
    this.fields.add(infoField);
  }

  @Override
  public boolean pass(VariantRecord record) {
    String[][] f = record.getInfo();
    ArrayList<String> keys = new ArrayList<>();
    for (String[] info : f)
      keys.add(info[0]);

    if (isKeep())
      if (and) { //All field must be present to keep
        for (String field : fields)
          if (!keys.contains(field))
            return false;
        return true;
      } else { //Any field must be present to exclude
        for (String field : fields)
          if (keys.contains(field))
            return true;
        return false;
      }
    else if (and) { //All field must be present to exclude
      for (String field : fields)
        if (!keys.contains(field))
          return true;
      return false;
    } else { //Any field must be present to exclude
      for (String field : fields)
        if (keys.contains(field))
          return false;
      return true;
    }
  }

  @Override
  public boolean leftColumnsOnly() {
    return true;
  }

  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove") + " variants with "+(and ? "ALL" : "ANY")+" of those INFO tags : "+StringTools.startOf(5, fields);
  }
}
