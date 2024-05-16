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
public class SamplePhenotypeFilter extends SampleFilter {

  private final ArrayList<Integer> phenotypes;

  public SamplePhenotypeFilter(boolean keep) {
    super(keep);
    this.phenotypes = new ArrayList<>();
  }

  public SamplePhenotypeFilter(boolean keep, String... phenotypes) {
    this(keep);

    for (String pheno : phenotypes)
      try {
        this.add(new Integer(pheno));
      } catch (Exception e) {
        Message.warning("Unrecognized phenotype [" + pheno + "], must be an integer");
      }
  }

  private void add(int phenotype) {
    this.phenotypes.add(phenotype);
  }

  @Override
  public boolean pass(Sample t) {
    return this.isKeep() == phenotypes.contains(t.getPhenotype());
  }
  
  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove")+" samples with Phenotypes : "+StringTools.startOf(5, phenotypes);
  }
}
