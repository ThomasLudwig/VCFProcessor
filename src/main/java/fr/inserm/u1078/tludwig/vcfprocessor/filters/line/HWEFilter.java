package fr.inserm.u1078.tludwig.vcfprocessor.filters.line;

import fr.inserm.u1078.tludwig.vcfprocessor.filters.LineFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-11-22
 */
public class HWEFilter extends LineFilter {
//TODO completly different from vcftools
  private final double threshold;

  public HWEFilter(double threshold, boolean keep) {
    super(keep);
    this.threshold = threshold;
  }

  @Override
  public boolean pass(String[] t) {
    //TODO what about multiallelic variants ?
    if(t[4].contains(","))
      return !this.isKeep();
    
    int aa = 0;
    int ab = 0;
    int bb = 0;

    for (int i = 9; i < t.length; i++) {
      String geno = t[i].split(":")[0].replace("|", "/");
      switch (geno) {
        case "0/0":
          aa++;
          break;
        case "1/1":
          bb++;
          break;
        case "0/1":
        case "1/0":
          ab++;
          break;
      }
    }

    double pvalue = 0;
    int taa = aa;
    int tbb = bb;

    for (int tab = ab; tab >= 0; tab -= 2, taa++, tbb++)
      pvalue += probaHWE(taa, tab, tbb);

    return (pvalue >= threshold) == this.isKeep();
  }

  public static double probaHWE(int aa, int ab, int bb) {
    int a = 2 * aa + ab;
    int b = 2 * bb + ab;
    int n = aa + ab + bb;

    int max = Math.max(a, b);

    double p = 1;
    for (int i = 1; i <= max; i++) {
      double p2ab = i <= ab ? 2 : 1;
      double fa = i <= a ? i : 1;
      double fb = i <= b ? i : 1;
      double faa = i <= aa ? i : 1;
      double fab = i <= ab ? i : 1;
      double fbb = i <= bb ? i : 1;
      double f2n = i <= n ? n + i : 1;

      p *= (p2ab * fa * fb) / (faa * fab * fbb * f2n);
    }
    return p;
  }
  
  @Override
  public String getDetails() {
    return (this.isKeep() ? "Keep" : "Remove")+ "variants with HWE p-value >= "+threshold;
  }
}
