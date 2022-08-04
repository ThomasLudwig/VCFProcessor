package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-21
 */
public class AlleleNumberFilter extends LineFilter {

  private final int minAllele;
  private final int maxAllele;

  public AlleleNumberFilter(int minAllele, int maxAllele) {
    super(true);
    this.minAllele = minAllele;
    this.maxAllele = maxAllele;
  }

  @Override
  public boolean pass(String[] t) {
    int nb = t[4].split(",").length + 1;
    return (minAllele <= nb && nb <= maxAllele);
  }

  @Override
  public String getDetails() {
    return this.minAllele+" <= NbAlleles <= "+this.maxAllele;
  }
}
