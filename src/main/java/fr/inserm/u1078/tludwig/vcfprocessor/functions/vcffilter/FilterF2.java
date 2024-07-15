package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

/**
 * Filters variants to keep only those contributing to F2 data.
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-05-11
 * Checked for release on 2020-08-06
 * Unit Test defined on 2020-08-06
 */
public class FilterF2 extends ParallelVCFPedFunction<FilterF2.Analysis> {

  private final StringParameter prefix = new StringParameter(OPT_PREFIX, "prefix", "Output filename prefix");
  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();
  PrintWriter f2all;
  PrintWriter f2old;
  PrintWriter f2new;
  PrintWriter f2snpAll;
  PrintWriter f2snpOld;
  PrintWriter f2snpNew;
  
  private static final int OLD_SNV = 0;
  private static final int OLD_IND = 1;
  private static final int NEW_SNV = 2;
  private static final int NEW_IND = 3;
  private static final int OVERALL = 4;
  private TreeMap<Variant, boolean[]> storeF2;

  @Override
  public String getSummary() {
    return "Filters variants to keep only those contributing to F2 data.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {

    return new Description(this.getSummary())
            .addLine("F2 data are described in PubMedId: 23128226, figure 3a")
            .addLine("Six sets of results are given, one for:")
            .addEnumerate(
                    "All variants",
                    "All SNVs",
                    "variants without rs",
                    "SNVs without rs",
                    "variants with rs",
                    "SNVs with rs"
            );
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return true;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FILTER_ONE;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{};
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    this.storeF2 = new TreeMap<>();
    String filename = this.dir.getDirectory() + this.prefix.getStringValue();

    try {
      this.f2all = getPrintWriter(filename + ".all.vcf");
      this.f2old = getPrintWriter(filename + ".old.vcf");
      this.f2new = getPrintWriter(filename + ".new.vcf");
      this.f2snpAll = getPrintWriter(filename + ".snp.all.vcf");
      this.f2snpOld = getPrintWriter(filename + ".snp.old.vcf");
      this.f2snpNew = getPrintWriter(filename + ".snp.new.vcf");
    } catch (IOException e) {
      Message.fatal("Unable to create result files", e, true);
    }
    this.getVCF().printHeaders(this.f2all);
    this.getVCF().printHeaders(this.f2old);
    this.getVCF().printHeaders(this.f2new);
    this.getVCF().printHeaders(this.f2snpAll);
    this.getVCF().printHeaders(this.f2snpOld);
    this.getVCF().printHeaders(this.f2snpNew);
  }

  @SuppressWarnings("unused")
  @Override
  public void end() {
    super.end();

    for (Variant variant : this.storeF2.navigableKeySet()) {
      boolean[] isF2 = this.storeF2.get(variant);
      this.f2all.println(variant);
      if (isF2[NEW_SNV])
        this.f2snpNew.println(variant);
      if (isF2[OLD_SNV])
        this.f2snpOld.println(variant);
      if (isF2[NEW_SNV] || isF2[OLD_SNV])
        this.f2snpAll.println(variant);
      if (isF2[OLD_IND] || isF2[OLD_SNV])
        this.f2old.println(variant);
      if (isF2[NEW_SNV] || isF2[NEW_IND])
        this.f2new.println(variant);
    }

    this.f2all.close();
    this.f2old.close();
    this.f2new.close();
    this.f2snpAll.close();
    this.f2snpOld.close();
    this.f2snpNew.close();
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    //The trick here is to add the line only ONCE everywhere that is needed :
    //For example is the allele 1 is a F2 SNV and the allele 2 is a F2 INDEL, breaking after the first allele skips the indel

    boolean[] isF2 = new boolean[5];

    for (int a = 1; a < variant.getAlleles().length; a++) {
      isF2[OVERALL] = process(variant, a);
      if (isF2[OVERALL])
        if (variant.isSNP(a))
          if (variant.getInfo().isInDBSNPVEP(a))
            isF2[OLD_SNV] = true;
          else
            isF2[NEW_SNV] = true;
        else if (variant.getInfo().isInDBSNPVEP(a))
          isF2[OLD_IND] = true;
        else
          isF2[NEW_IND] = true;
    }
    if (isF2[OVERALL])
      this.pushAnalysis(new Analysis(variant, isF2));

    return NO_OUTPUT;
  }

  private boolean process(Variant variant, int a) {
    //TODO here no need to check the group. a F2 variants is valid regardless of the groups it belongs to
    int found = 0;
    for (Genotype geno : variant.getGenotypes()) {
      int c = geno.getCount(a);
      if (c > 1)
        return false; //two allele in the same person -> not f2
      found += c;

      if (found > 2) //more than two allele -> not f2
        return false;
    }

    return found == 2; //0 or 1 allele, not f2
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Analysis analysis) {
    Variant variant = analysis.getVariant();
    boolean[] isF2 = analysis.getIsF2();
    storeF2.put(variant, isF2);
  }

  public static class Analysis {
    private final Variant variant;
    private final boolean[] isF2;

    public Analysis(Variant variant, boolean[] isF2) {
      this.variant = variant;
      this.isF2 = isF2;
    }

    public Variant getVariant() {
      return variant;
    }

    public boolean[] getIsF2() {
      return isF2;
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newDirectoryTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addAnonymousFilename("ped", "ped");
    scr.addAnonymousValue("prefix", this.getClass().getSimpleName());
    
    return new TestingScript[]{scr};
  }
}
