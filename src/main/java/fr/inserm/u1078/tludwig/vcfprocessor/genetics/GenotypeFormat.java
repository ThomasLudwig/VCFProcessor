package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Genotype Format from VCF File
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 23 juin 2015
 */
public class GenotypeFormat {

  public static final String GT = "GT";
  public static final String GQ = "GQ";
  public static final String DP = "DP";
  public static final String PL = "PL";
  public static final String AD = "AD";

  private final ArrayList<String> keys;
  private String format;

  /**
   * Creates a Genotype Format
   *
   * @param format - the format described in the VCF File Line
   */
  public GenotypeFormat(String format) {
    this.format = format;
    keys = new ArrayList<>(Arrays.asList(format.split(":")));
  }

  public int getDP(String genotype) {
    try {
      return Integer.parseInt(this.getValue(genotype, DP));
    } catch (NumberFormatException e) {

    }
    return 0;
  }

  public int getGQ(String genotype) {
    try {
      return Integer.parseInt(this.getValue(genotype, DP));
    } catch (NumberFormatException e) {

    }
    return 0;
  }

  public String getValue(String genotype, String key) {
    String value = "";
    int index = getIndex(key);
    try {
      value = genotype.split(":")[index];
    } catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
    return value;
  }

  protected int getIndex(String key) {
    return this.keys.indexOf(key);
  }

  public int size() {
    return keys.size();
  }

  @Override
  public String toString() {
    return this.format;
  }

  public void addField(String key) {
    keys.add(key);
    format += ":"+key;
  }
}
