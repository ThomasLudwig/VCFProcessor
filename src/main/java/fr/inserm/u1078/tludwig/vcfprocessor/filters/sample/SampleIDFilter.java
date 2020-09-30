package fr.inserm.u1078.tludwig.vcfprocessor.filters.sample;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.SampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-23
 */
public class SampleIDFilter extends SampleFilter {

  private final ArrayList<String> ids;

  public SampleIDFilter(boolean keep) {
    super(keep);
    this.ids = new ArrayList<>();
  }

  public void add(String id) {
    this.ids.add(id);
  }

  @Override
  public boolean pass(Sample t) {
    return this.isKeep() == ids.contains(t.getId());
  }

  public void addIDs(String... ids) {
    for (String id : ids)
      this.add(id);
  }

  public void addFilenames(String... filenames) {
    for (String filename : filenames)
      try {
        UniversalReader in = new UniversalReader(filename);
        String line;
        while ((line = in.readLine()) != null)
          if (!line.startsWith("#")) {
            String[] f = line.split("\\s+");
            this.add(f[0]);
          }
        in.close();
      } catch (IOException ioe) {
        Message.warning("Problem while file [" + filename + "] : " + ioe.getMessage());
      }
  }
  
  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove")+" samples : "+StringTools.startOf(5, ids);
  }
}
