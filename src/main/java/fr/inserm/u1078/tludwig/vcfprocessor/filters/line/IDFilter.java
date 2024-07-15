package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class IDFilter extends LineFilter {

  private final ArrayList<String> ids;

  public IDFilter(boolean keep) {
    super(keep);
    this.ids = new ArrayList<>();
  }

  public void addID(String id) {
    this.ids.add(id);
  }

  @Override
  public boolean pass(VariantRecord record) {
    return ids.contains(record.getID()) == isKeep();
  }

  @Override
  public boolean leftColumnsOnly() {
    return true;
  }

  public void addIDs(String... ids) {
    for (String id : ids)
      this.addID(id);
  }

  public void addFilenames(String... filenames) {
    for (String filename : filenames)
      try (UniversalReader in = new UniversalReader(filename)){
        String line;
        while ((line = in.readLine()) != null)
          if (!line.startsWith("#")) {
            String[] f = line.split("\\s+");
            this.addID(f[0]);
          }
      } catch (IOException ioe) {
        Message.warning("Problem while file [" + filename + "] : " + ioe.getMessage());
      }
  }
  
  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove")+" variant with ID : "+StringTools.startOf(5, ids);
  }
}
