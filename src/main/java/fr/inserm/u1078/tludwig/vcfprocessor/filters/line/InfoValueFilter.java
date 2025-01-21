package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.BooleanParser;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-09-27
 */
public class InfoValueFilter extends LineFilter {


  public InfoValueFilter(boolean keep, BooleanParser parser) {
    super(keep);
  }

  @Override
  public boolean pass(VariantRecord record) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO implement : MYTAG=XXXYYY, MYTAG<=0.5, MYTAG>17 etc....
  }

  @Override
  public boolean leftColumnsOnly() {
    return true;
  }

  @Override
  public String getDetails() {
    throw new UnsupportedOperationException("Not supported yet."); //TODO
  }
}
