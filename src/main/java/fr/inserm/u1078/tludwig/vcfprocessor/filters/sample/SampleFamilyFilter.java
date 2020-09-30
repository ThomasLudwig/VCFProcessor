package fr.inserm.u1078.tludwig.vcfprocessor.filters.sample;

import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.SampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-23
 */
public class SampleFamilyFilter extends SampleFilter {

  private final ArrayList<String> families;

  public SampleFamilyFilter(boolean keep) {
    super(keep);
    this.families = new ArrayList<>();
  }

  public SampleFamilyFilter(boolean keep, String... families) {
    this(keep);
    for (String family : families)
      this.add(family);
  }

  public void add(String family) {
    this.families.add(family);
  }

  @Override
  public boolean pass(Sample t) {
    return this.isKeep() == families.contains(t.getFid());
  }
  
  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove")+" samples from Families : "+StringTools.startOf(5, families);
  }
}
