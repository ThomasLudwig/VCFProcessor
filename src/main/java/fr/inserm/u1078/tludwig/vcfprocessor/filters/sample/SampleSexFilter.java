package fr.inserm.u1078.tludwig.vcfprocessor.filters.sample;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.maok.tools.StringTools;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.SampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-23
 */
public class SampleSexFilter extends SampleFilter {

  private final ArrayList<Integer> sexes;

  public SampleSexFilter(boolean keep) {
    super(keep);
    this.sexes = new ArrayList<>();
  }

  public SampleSexFilter(boolean keep, String... sexes) {
    this(keep);
    for (String sex : sexes)
      try {
        this.add(Integer.parseInt(sex));
      } catch (Exception e) {
        Message.warning("Unrecognized sex [" + sex + "] must be an integer");
      }
  }

  public void add(int sex) {
    this.sexes.add(sex);
  }

  @Override
  public boolean pass(Sample t) {
    return this.isKeep() == sexes.contains(t.getSex());
  }
  
  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove")+" samples with  Sexes : "+StringTools.startOf(5, sexes);
  }
}
