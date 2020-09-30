package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Filters variants to keep only those contributing to F2 data.
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-05-11
 * Checked for release on 2020-08-06
 * Unit Test defined on 2020-08-06
 */
public class FilterF2 extends ParallelVCFPedFunction {

  private final StringParameter prefix = new StringParameter(OPT_PREFIX, "prefix", "Output filename prefix");
  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();
  PrintWriter f2all;
  PrintWriter f2old;
  PrintWriter f2new;
  PrintWriter f2snpall;
  PrintWriter f2snpold;
  PrintWriter f2snpnew;
  
  private static final int OLDSNV = 0;
  private static final int OLDIND = 1;
  private static final int NEWSNV = 2;
  private static final int NEWIND = 3;
  private static final int OVERALL = 4;
  private HashMap<Variant, boolean[]> storeF2;
  private SortedList<Variant> output;

  @Override
  public String getSummary() {
    return "Filters variants to keep only those contributing to F2 data.";
  }

  @Override
  public Description getDesc() {

    return new Description(this.getSummary())
            .addLine("F2 data are described in PubMedId: 23128226, figure 3a")
            .addLine("Six sets of results are given, one for:")
            .addEnumerate(new String[]{
      "All variants",
      "All SNVs",
      "variants without rs",
      "SNVs without rs",
      "variants with rs",
      "SNVs with rs"
    });
  }

  @Override
  public boolean needVEP() {
    return true;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FILTER_ONE;
  }

  @Override
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @Override
  public String[] getHeaders() {
    return new String[]{};
  }

  @Override
  public void begin() {
    super.begin();
    this.output = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_FROM_END);
    this.storeF2 = new HashMap<>();
    String filename = this.dir.getDirectory() + this.prefix.getStringValue();

    try {
      this.f2all = getPrintWriter(filename + ".all.vcf");
      this.f2old = getPrintWriter(filename + ".old.vcf");
      this.f2new = getPrintWriter(filename + ".new.vcf");
      this.f2snpall = getPrintWriter(filename + ".snp.all.vcf");
      this.f2snpold = getPrintWriter(filename + ".snp.old.vcf");
      this.f2snpnew = getPrintWriter(filename + ".snp.new.vcf");
    } catch (IOException e) {
      this.fatalAndDie("Unable to create result files", e);
    }
    this.getVCF().printHeaders(this.f2all);
    this.getVCF().printHeaders(this.f2old);
    this.getVCF().printHeaders(this.f2new);
    this.getVCF().printHeaders(this.f2snpall);
    this.getVCF().printHeaders(this.f2snpold);
    this.getVCF().printHeaders(this.f2snpnew);
  }

  @Override
  public void end() {
    super.end();

    for (Variant variant : this.output) {
      boolean[] isF2 = this.storeF2.get(variant);
      this.f2all.println(variant);
      if (isF2[NEWSNV])
        this.f2snpnew.println(variant);
      if (isF2[OLDSNV])
        this.f2snpold.println(variant);
      if (isF2[NEWSNV] || isF2[OLDSNV])
        this.f2snpall.println(variant);
      if (isF2[OLDIND] || isF2[OLDSNV])
        this.f2old.println(variant);
      if (isF2[NEWSNV] || isF2[NEWIND])
        this.f2new.println(variant);
    }

    this.f2all.close();
    this.f2old.close();
    this.f2new.close();
    this.f2snpall.close();
    this.f2snpold.close();
    this.f2snpnew.close();
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
            isF2[OLDSNV] = true;
          else
            isF2[NEWSNV] = true;
        else if (variant.getInfo().isInDBSNPVEP(a))
          isF2[OLDIND] = true;
        else
          isF2[NEWIND] = true;
    }
    if (isF2[OVERALL])
      this.pushAnalysis(new Object[]{variant, isF2});

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

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    try {
      Object[] objects = (Object[]) analysis;
      Variant variant = (Variant) objects[0];
      boolean[] isF2 = (boolean[]) objects[1];
      output.add(variant);//Avoid lines being printed out of order
      storeF2.put(variant, isF2);
    } catch (Exception e) {
      return false;
    }
    return true;
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
