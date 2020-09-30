package fr.inserm.u1078.tludwig.vcfprocessor.filters.sample;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.SampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-30
 */
public class MaxSampleFilter extends SampleFilter {

  private final int maxSamples;
  private ArrayList<String> samples;
  private boolean first = true;

  public MaxSampleFilter(int maxSamples) {
    super(true);
    this.maxSamples = maxSamples;
    samples = null;
  }

  public void setSamples(ArrayList<String> samples) {
    this.samples = samples;
    if (samples != null)
      while (samples.size() > this.maxSamples)
        samples.remove((int) (samples.size() * Math.random()));
  }

  @Override
  public boolean pass(Sample t) {
    if (samples == null) {
      if (first) {
        Message.warning("This filter [" + this.getClass().getSimpleName() + "] has not been properly initialized");
        first = false;
      }
      return false;
    } else
      return samples.contains(t.getId());
  }
  
  @Override
  public String getDetails() {
    return "Keep at most "+this.maxSamples+" samples";
  }
}
