package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import fr.inserm.u1078.tludwig.maok.tools.ArrayTools;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 26 juil. 2016
 */
public class ESPAnnotation {

  private final String alleles[];
  private final double espFreq[];
  private final double eaFreq[];
  private final double aaFreq[];

  ESPAnnotation() {
    this.alleles = new String[]{" "};
    this.espFreq = null;
    this.eaFreq = null;
    this.aaFreq = null;
  }

  public ESPAnnotation(String annot) {
    String[] blocks = annot.split(",");
    int N = blocks.length;
    this.alleles = new String[N];
    this.espFreq = new double[N];
    this.eaFreq = new double[N];
    this.aaFreq = new double[N];

    for (int i = 0; i < N; i++) {
      String[] f = blocks[i].split("\\|", -1);
      this.alleles[i] = f[0];
      this.espFreq[i] = convertFrequency(f[1]);
      this.eaFreq[i] = convertFrequency(f[2]);
      this.aaFreq[i] = convertFrequency(f[3]);
    }
  }

  private double convertFrequency(String string) {
    try {
      return Double.parseDouble(string);
    } catch (NumberFormatException e) {

    }
    return 0;
  }

  public double getESPFreq(String allele) {
    int i = ArrayTools.indexOf(alleles, allele);
    if (i == -1)
      return 0;
    return espFreq[i];
  }

  public double getESP_EAFreq(String allele) {
    int i = ArrayTools.indexOf(alleles, allele);
    if (i == -1)
      return 0;
    return eaFreq[i];
  }

  public double getESP_AAFreq(String allele) {
    int i = ArrayTools.indexOf(alleles, allele);
    if (i == -1)
      return 0;
    return aaFreq[i];
  }
}
