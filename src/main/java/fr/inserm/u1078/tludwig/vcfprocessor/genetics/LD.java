package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 */
public class LD {

  private static final int REF = 0;
  private static final int HET = 1;
  private static final int ALT = 2;
  private static final int TOT = 3;

  private final double[][] count;
  private final double n;
  private final double p;
  private final double q;

  private final double d;
  private final double dprime;
  private final double r2;
  private final double lod;
  private final double chiSquarePvalue;

  public LD(int[][] matrix33) {
    count = new double[4][4];
    for (int i = 0; i < 3; i++)
      for (int j = 0; j < 3; j++) {
        count[i][j] = matrix33[i][j];
        count[TOT][TOT] += count[i][j];
        count[i][TOT] += count[i][j];
        count[TOT][j] += count[i][j];
      }
    n = count[TOT][TOT];
    p = (2 * count[REF][TOT] + count[HET][TOT]) / (2 * n);
    q = (2 * count[TOT][REF] + count[TOT][HET]) / (2 * n);
    System.out.println("p=" + p);
    System.out.println("1-p=" + (1 - p));
    System.out.println("q=" + q);
    System.out.println("1-q=" + (1 - q));

    if (p == 0 || p == 1 || q == 0 || q == 1) {
      //LD between two loci measure when they vary at the same time
      //if one locus doesn't vary, LD cannot be computed      
      this.d = Double.NaN;
      this.dprime = Double.NaN;
      this.r2 = Double.NaN;
      this.lod = Double.NaN;
      this.chiSquarePvalue = Double.NaN;
    } else {
      double n11 = count[REF][REF];
      double n12 = count[REF][HET];
      double n21 = count[HET][REF];
      double n22 = count[HET][HET];
      double pa = 4 * n;
      double pb = 2 * n * (1 - 2 * p - 2 * q) - 2 * (2 * n11 + n12 + n21) - n22;
      double pc = 2 * n * p * q - (2 * n11 + n12 + n21) * (1 - 2 * p - 2 * q) - n22 * (1 - p - q);
      double pd = -(2 * n11 + n12 + n21) * p * q;
      double xn = -pb / (3 * pa);
      double deltasq = (pb * pb - 3 * pa * pc) / (9 * pa * pa);
      double hsq = 4 * pa * pa * deltasq * deltasq * deltasq;
      double yn = pa * xn * xn * xn + pb * xn * xn + pc * xn + pd;
      double det = (yn * yn) - hsq;

      System.out.println("Det=" + det);

      Root root;

      if (det > 0) {
        double rdet = Math.sqrt(det);
        double factor = 1 / (2 * pa);
        double alpha = xn + Math.cbrt(factor * (-yn + rdet)) + Math.cbrt(factor * (-yn - rdet));
        root = new Root(alpha);
      } else if (det == 0) {
        double mu = Math.cbrt(yn / (2 * pa));
        double alpha = xn + mu;
        double gamma = xn - 2 * mu;
        Root alphaLD = new Root(alpha);
        Root gammaLD = new Root(gamma);
        root = getValidLD(alphaLD, gammaLD);
      } else {
        double PI = Math.PI;
        double h = Math.sqrt(hsq);
        double delta = Math.sqrt(deltasq);
        double theta = Math.acos(-yn / h) / 3;
        double alpha = xn + 2 * delta * Math.cos(theta);
        double beta = xn + 2 * delta * Math.cos(theta + 2 * PI / 3);
        double gamma = xn + 2 * delta * Math.cos(theta + 4 * PI / 3);
        Root alphaLD = new Root(alpha);
        Root betaLD = new Root(beta);
        Root gammaLD = new Root(gamma);
        root = getValidLD(alphaLD, betaLD, gammaLD);
      }
      this.d = root.getD();
      this.dprime = root.getDprime();
      this.r2 = root.getR2();
      this.lod = root.getLOD();
      this.chiSquarePvalue = root.getChiSquarePvalue();
      System.out.println(root);
    }
  }

  private Root getValidLD(Root... list) {
    Root min = null;
    Root max = null;

    for (Root root : list)
      if (root.check()) {
        if (min == null || min.f11 > root.f11)
          min = root;
        if (max == null || max.f11 < root.f11)
          max = root;
      }

    if (max == null || min == null) {
      System.err.println("There are not valid root between");
      for (Root root : list)
        System.err.println(root);
      return null;
    }

    if (max.f11 < 0.5)
      return min;
    if (min.f11 > 0.5)
      return max;
    return min;
  }

  private class Root {

    private final static double LIMIT = 10e-9;

    private final double f11;
    private final double f12;
    private final double f21;
    private final double f22;
    private final double d;
    private final double dprime;
    private final double rsq;
    private final double lod;
    private final double chiSquarePvalue;

    @Override
    public String toString() {
      return "Root{" + "\n"
              + "\tf11=" + f11 + "\n"
              + "\tf12=" + f12 + "\n"
              + "\tf21=" + f21 + "\n"
              + "\tf22=" + f22 + "\n"
              + "\td=" + d + "\n"
              + "\tdprime=" + dprime + "\n"
              + "\trsq=" + rsq + "\n"
              + "\tlod=" + lod + "\n"
              + "\tchiSquarePvalue=" + chiSquarePvalue + "\n"
              + "}";
    }

    private Root(double root) {
      f11 = round(root);
      f12 = round(p - f11);
      f21 = round(q - f11);
      f22 = round((1 - q) - f12);

      d = f11 * f22 - f12 * f21;

      if (f11 == 0 || f12 == 0 || f21 == 0 || f22 == 0)
        dprime = 1;
      else {
        double dmax = d > 0 ? Math.min(p * (1 - q), q * (1 - p)) : Math.min(p * q, (1 - p) * (1 - q));
        dprime = Math.abs(round(d / dmax));
      }
      rsq = round((d * d) / (p * q * (1 - p) * (1 - q)));

      double chiSquare = this.rsq * n;
      this.chiSquarePvalue = Stats.pValue(chiSquare, 1);

      double n11 = f11 * n;
      double n12 = f12 * n;
      double n21 = f21 * n;
      double n22 = f22 * n;
      double lodlike1
              = n11 * Math.log10(f11)
              + n12 * Math.log10(f12)
              + n21 * Math.log10(f21)
              + n22 * Math.log10(f22);

      double lodlike0
              = n11 * Math.log10(p * q)
              + n12 * Math.log10(p * (1 - q))
              + n21 * Math.log10((1 - p) * q)
              + n22 * Math.log10((1 - p) * (1 - q));

      this.lod = lodlike1 - lodlike0;
    }

    private double round(double f) {
      if (f < LIMIT && f > -LIMIT)
        return 0;
      if (f > 1 - LIMIT && f < 1 + LIMIT)
        return 1;
      if (f < -(1 - LIMIT) && f > -(1 + LIMIT))
        return -1;
      return f;
    }

    public boolean check() {
      return checkFrequency(f11)
              && checkFrequency(f12)
              && checkFrequency(f21)
              && checkFrequency(f22);
    }

    private boolean checkFrequency(double f) {
      return f >= 0 && f <= 1;
    }

    public double getD() {
      return d;
    }

    public double getDprime() {
      return dprime;
    }

    public double getR2() {
      return rsq;
    }

    public double getLOD() {
      return lod;
    }

    public double getChiSquarePvalue() {
      return this.chiSquarePvalue;
    }

  }

  /*

    private final double chiSquarePvalue;
    private final double lod;

    private final int nA1B1;
    private final int nA1B2;
    private final int nA2B1;
    private final int nA2B2;

    public LD(int nA1B1, int nA1B2, int nA2B1, int nA2B2) {
        this.nA1B1 = nA1B1;
        this.nA1B2 = nA1B2;
        this.nA2B1 = nA2B1;
        this.nA2B2 = nA2B2;
        int nP1 = nA1B1 + nA1B2;
        int nP2 = nA2B1 + nA2B2;
        int nQ1 = nA1B1 + nA2B1;
        int nQ2 = nA1B2 + nA2B2;
        int nS1 = nA1B1 + nA2B2;
        int nS2 = nA2B1 + nA1B2;
        double n = nA1B1 + nA1B2 + nA2B1 + nA2B2;
        //Haplotype frequencies
        double x11 = nA1B1 / n; //p(A1B1)
        double x12 = nA1B2 / n; //p(A1B2)
        double x21 = nA2B1 / n; //p(A2B1)
        double x22 = nA2B2 / n; //p(A2B2)

        //Markers frequencies
        double p1 = nP1 / n; //p(A1)
        double p2 = nP2 / n; //p(A2)
        double q1 = nQ1 / n; //p(B1)
        double q2 = nQ2 / n; //p(B2)
        
        if (nA1B1 == 0 || nA1B2 == 0 || nA2B1 == 0 || nA2B2 == 0) {
            this.d = 1;
            this.dPrime = 1;
            this.rSquare = 1;
            this.chiSquarePvalue = Stats.pValue(n, 1);
        } else {
            //D
            this.d = x11 - (p1 * q1); //D = (x11*x22) - (x12*x21)

            //D'
            if (nS1 == 0 || nS2 == 0) {
                this.dPrime = 1;
            } else {

                double dMax = 1;
                if (d > 0) {
                    dMax = Math.min(p1 * q2, p2 * q1); //0 if (nA1B1 = nA2B2)
                }
                if (d < 0) {
                    dMax = -Math.min(p1 * q1, p2 * q2);//0 if (nA1B2 = nA2B1)
                }
                this.dPrime = d / dMax; //D' = D/DMax
            }

            //R²
            if (nP1 == 0 || nP2 == 0 || nQ1 == 0 || nQ2 == 0) { //0 si A ou B n'est pas hérétozygote
                this.rSquare = 1;
            } else {
                this.rSquare = (d * d) / (p1 * p2 * q1 * q2);
            }

            //X² P-Value
            double chiSquare = this.rSquare * n;
            this.chiSquarePvalue = Stats.pValue(chiSquare, 1);

            
        }
        //LOD
        double lodlike1 = nA1B1 * Math.log10(x11)
                + nA1B2 * Math.log10(x12)
                + nA2B1 * Math.log10(x21)
                + nA2B2 * Math.log10(x22);

        double lodlike0 = nA1B1 * Math.log10(p1 * q1)
                + nA1B2 * Math.log10(p1 * q2)
                + nA2B1 * Math.log10(p2 * q1)
                + nA2B2 * Math.log10(p2 * q2);

        this.lod = lodlike1 - lodlike0;
        //this.lod = nA1B1 * Math.log10(x11/(p1*q2)) + ....
    }

    public String toString() {
        String ret
                = "A1B1 = " + this.nA1B1
                + " A1B2 = " + this.nA1B2
                + " A2B1 = " + this.nA2B1
                + " A2B2 = " + this.nA2B2
                + " D = " + this.d
                + " D' = " + this.dPrime
                + " R² = " + this.rSquare
                + " X²pValue = " + this.chiSquarePvalue
                + " LOD = " + this.lod;
        return ret;
    }
   */
  public double getDPrime() {
    return this.dprime;
  }

  public double getD() {
    return this.d;
  }

  public double getRSquare() {
    return this.r2;
  }

  public double getLOD() {
    return lod;
  }

  public double getChiSquarePvalue() {
    return chiSquarePvalue;
  }

  @Override
  public String toString() {
    return "D=" + this.d + " D'=" + this.dprime + " R²=" + this.r2 + " LOD=" + this.lod + " chi² p-value=" + this.chiSquarePvalue;
  }
}
