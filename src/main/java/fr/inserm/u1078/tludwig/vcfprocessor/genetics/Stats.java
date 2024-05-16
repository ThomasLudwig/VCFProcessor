package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import java.util.ArrayList;
import java.util.Collection;

/**
 * BioStatistics Functions
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 */
public class Stats {

  public static double pValueExactHWE(int oHom1, int oHet, int oHom2) {
    int oRef = Math.max(oHom1, oHom2);
    int oAlt = Math.min(oHom1, oHom2);

    int na = 2 * oAlt + oHet;

    int n = oHet + oRef + oAlt;
    double[] probRA = new double[na + 1];

    for (int i = 0; i <= na; i++)
      probRA[i] = 0.0;

    /* start at midpoint */
    int mid = na * (2 * n - na) / (2 * n);

    /* check to ensure that midpoint and rare alleles have same parity */
    if ((na % 2) != (mid % 2))
      mid++;

    int curr_hets = mid;
    int curr_homR = (na - mid) / 2;
    int curr_homC = n - curr_hets - curr_homR;

    probRA[mid] = 1.0;
    double sum = probRA[mid];
    for (curr_hets = mid; curr_hets > 1; curr_hets -= 2) {
      probRA[curr_hets - 2] = probRA[curr_hets] * curr_hets * (curr_hets - 1.0)
              / (4.0 * (curr_homR + 1.0) * (curr_homC + 1.0));
      sum += probRA[curr_hets - 2];

      /* 2 fewer heterozygotes for next iteration -> add one rare, one common homozygote */
      curr_homR++;
      curr_homC++;
    }

    curr_hets = mid;
    curr_homR = (na - mid) / 2;
    curr_homC = n - curr_hets - curr_homR;
    for (curr_hets = mid; curr_hets <= na - 2; curr_hets += 2) {
      probRA[curr_hets + 2] = probRA[curr_hets] * 4.0 * curr_homR * curr_homC
              / ((curr_hets + 2.0) * (curr_hets + 1.0));
      sum += probRA[curr_hets + 2];

      /* add 2 heterozygotes for next iteration -> subtract one rare, one common homozygote */
      curr_homR--;
      curr_homC--;
    }

    for (int i = 0; i <= na; i++)
      probRA[i] /= sum;

    double p_hwe = 0.0;
    /*  p-value calculation for p_hwe  */
    for (int i = 0; i <= na; i++)
      if (probRA[i] <= probRA[oHet])
        p_hwe += probRA[i];


    p_hwe = Math.min(p_hwe, 1.0);

    return p_hwe;
  }

  public static double HWE_chiSquare(double oRef, double oHet, double oAlt) {
    double n = oRef + oHet + oAlt;
    double p = ((2 * oRef) + oHet) / (2 * n);
    double q = 1 - p;
    double eRef = p * p * n;
    double eHet = 2 * p * q * n;
    double eAlt = q * q * n;

    return chiSquare(new double[]{eRef, eHet, eAlt}, new double[]{oRef, oHet, oAlt});
  }

  public static double test(double chr, int dof) {
    return Math.pow(-1.0, chr);
  }

  public static double pValue(double chiSquare, int degreeOfFreedom) {
    if (chiSquare < 0 || degreeOfFreedom < 1)
      return 1.0;

    double x = chiSquare * 0.5;
    if (degreeOfFreedom == 2)
      return Math.exp(-1.0 * x);

    double k = degreeOfFreedom * 0.5;
    double pValue = igf(k, x);
    if (Double.isNaN(pValue) || Double.isInfinite(pValue))
      //println("pValue="+pValue);
      return 1 - 1e-14;

    pValue /= approxGamma(k);

    return Math.abs(1 - pValue);
  }

  private static final double CUT = 10E-10;

  /**
   * Incomplete Gamma Function
   *
   * @param s
   * @param z
   * @return
   */
  static double igf(double s, double z) {
    if (z < 0.0)
      return 0.0;

    double sum = 1.0;
    double nom = 1.0;
    double denom = 1.0;

    double sC = (Math.pow(z, s) * Math.exp(-z)) / s;
    //println("SC = "+sC);

    double prev = Math.PI;
    for (int i = 0; i < 200; i++) {
      nom *= z;
      s++;
      denom *= s;
      sum += (nom / denom);
      if (Double.isInfinite(sum) || Double.isNaN(sum)) {
        sum = prev;
        //println("sum="+sum+" ("+i+" iteration)");
        break;
      }
      if (Math.abs(prev - sum) < CUT)
        //println("sum="+sum+" ("+i+" iteration)");
        break;
      prev = sum;
    }

    return sum * sC;
  }

  public static double approxGamma(double z) {
    double d = 1.0 / (10.0 * z);
    d = 1.0 / ((12 * z) - d);
    d = (d + z) / Math.E;
    d = Math.pow(d, z);
    d *= Math.sqrt(2 * Math.PI / z);

    return d;
  }

  public static double chiSquare(double[] expected, double[] observed) {
    double chiSquare = 0;
    for (int i = 0; i < expected.length; i++) {
      double sqrt = expected[i] - observed[i];
      chiSquare += (sqrt * sqrt) / expected[i];
    }
    return chiSquare;
  }

  public static double HWE_exact(int nrr, int nra, int naa) {
    int n = nrr + nra + naa;
    int nr = 2 * nrr + nra;
    int na = 2 * naa + nra;

    ArrayList<Integer> fn = factorial(n);
    ArrayList<Integer> fnRR = factorial(nrr);

    ArrayList<Integer> fnr = factorial(nr);
    ArrayList<Integer> fnRA = factorial(nra);

    ArrayList<Integer> fna = factorial(na);
    ArrayList<Integer> fnAA = factorial(naa);

    ArrayList<Integer> pow = power(2, nra);
    ArrayList<Integer> f2n = factorial(2 * n);

    ArrayList<Integer> num = combine(combine(fn, fnr), combine(fna, pow));
    ArrayList<Integer> denom = combine(combine(fnRR, fnRA), combine(fnAA, f2n));

    return divide(num, denom);
  }

  private static ArrayList<Integer> factorial(int f) {
    ArrayList<Integer> ret = new ArrayList<>();
    for (int i = 1; i <= f; i++)
      ret.add(i);
    return ret;
  }

  private static ArrayList<Integer> combine(Collection<Integer> a, Collection<Integer> b) {
    ArrayList<Integer> ret = new ArrayList<>(a);
    ret.addAll(b);
    return ret;
  }

  private static ArrayList<Integer> power(int num, int pow) {
    ArrayList<Integer> ret = new ArrayList<>();
    for (int i = 0; i < pow; i++)
      ret.add(num);
    return ret;
  }
  
  /**
   * Avoids using integer larger than MAX_VALUE
   * @param n
   * @param d
   * @return 
   */
  private static double divide(ArrayList<Integer> n, ArrayList<Integer> d) {
    double ret = 1d;
    int max = Math.max(n.size(), d.size());
    for (int i = 0; i < max; i++) {
      if (i < n.size())
        ret *= n.get(i);
      if (i < d.size())
        ret /= d.get(i);
    }
    return ret;
  }
}
