package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import fr.inserm.u1078.tludwig.maok.tools.ArrayTools;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 26 juil. 2016
 */
public class FrExAnnotation {

  private final String[] alleles;
  private final double[] frexFreq;
  private final double[] bordeauxFreq;
  private final double[] brestFreq;
  private final double[] dijonFreq;
  private final double[] lilleFreq;
  private final double[] nantesFreq;
  private final double[] rouenFreq;

  FrExAnnotation() {
    this.alleles = new String[]{" "};
    this.frexFreq = null;
    this.bordeauxFreq = null;
    this.brestFreq = null;
    this.dijonFreq = null;
    this.lilleFreq = null;
    this.nantesFreq = null;
    this.rouenFreq = null;
  }

  public FrExAnnotation(String annot) {
    String[] blocks = annot.split(",");
    int N = blocks.length;
    this.alleles = new String[N];
    this.frexFreq = new double[N];
    this.bordeauxFreq = new double[N];
    this.brestFreq = new double[N];
    this.dijonFreq = new double[N];
    this.lilleFreq = new double[N];
    this.nantesFreq = new double[N];
    this.rouenFreq = new double[N];

    for (int i = 0; i < N; i++) {
      String[] f = blocks[i].split("\\|", -1);
      this.alleles[i] = f[0];
      this.frexFreq[i] = convertFrequency(f[1]);
      this.bordeauxFreq[i] = convertFrequency(f[2]);
      this.brestFreq[i] = convertFrequency(f[3]);
      this.dijonFreq[i] = convertFrequency(f[4]);
      this.lilleFreq[i] = convertFrequency(f[5]);
      this.nantesFreq[i] = convertFrequency(f[6]);
      this.rouenFreq[i] = convertFrequency(f[7]);
    }
  }

  private double convertFrequency(String string) {
    try {
      return Double.parseDouble(string);
    } catch (NumberFormatException ignore) { }
    return 0;
  }

  public double getFrexFreq(String allele) {
    int i = ArrayTools.indexOf(alleles, allele);
    if (i == -1)
      return 0;
    return frexFreq[i];
  }

  public double getBordeauxFreq(String allele) {
    int i = ArrayTools.indexOf(alleles, allele);
    if (i == -1)
      return 0;
    return bordeauxFreq[i];
  }

  public double getBrestFreq(String allele) {
    int i = ArrayTools.indexOf(alleles, allele);
    if (i == -1)
      return 0;
    return brestFreq[i];
  }

  public double getDijonFreq(String allele) {
    int i = ArrayTools.indexOf(alleles, allele);
    if (i == -1)
      return 0;
    return dijonFreq[i];
  }

  public double getLilleFreq(String allele) {
    int i = ArrayTools.indexOf(alleles, allele);
    if (i == -1)
      return 0;
    return lilleFreq[i];
  }

  public double getNantesFreq(String allele) {
    int i = ArrayTools.indexOf(alleles, allele);
    if (i == -1)
      return 0;
    return nantesFreq[i];
  }

  public double getRouenFreq(String allele) {
    int i = ArrayTools.indexOf(alleles, allele);
    if (i == -1)
      return 0;
    return rouenFreq[i];
  }
}
