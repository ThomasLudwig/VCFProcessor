package fr.inserm.u1078.tludwig.vcfprocessor.filters.sample;

import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.SampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-23
 */
public class SampleGroupFilter extends SampleFilter {

  private final ArrayList<String> groups;

  public SampleGroupFilter(boolean keep) {
    super(keep);
    this.groups = new ArrayList<>();
  }

  public SampleGroupFilter(boolean keep, String... groups) {
    this(keep);
    for (String group : groups)
      this.add(group);
  }

  public void add(String group) {
    this.groups.add(group);
  }

  @Override
  public boolean pass(Sample t) {
    return this.isKeep() == groups.contains(t.getGroup());
  }
  
  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove")+" samples from Groups : "+StringTools.startOf(5, groups);
  }
}
