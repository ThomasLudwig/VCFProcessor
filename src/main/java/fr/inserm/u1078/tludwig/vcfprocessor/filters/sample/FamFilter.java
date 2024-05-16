package fr.inserm.u1078.tludwig.vcfprocessor.filters.sample;

import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.files.PedException;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.SampleFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;

import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-30
 */
public class FamFilter extends SampleFilter {

  private final Ped fam;
  private final ArrayList<String> ids;

  public FamFilter(String filename) throws PedException {
    super(true);
    fam = new Ped(filename);
    ids = fam.getIDs();
  }

  @Override
  public boolean pass(Sample t) {
    return ids.contains(t.getId());
  }

  public Ped getFam() {
    return fam;
  }
  
  @Override
  public String getDetails() {
    return "Only samples listed in "+fam.getFilename();
  }
}
