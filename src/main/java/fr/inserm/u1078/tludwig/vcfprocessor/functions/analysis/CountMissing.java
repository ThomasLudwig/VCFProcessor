package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.RatioParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * For each samples in the PED file, print a summary of missingness
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-04-04
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-13
 */
public class CountMissing extends ParallelVCFVariantPedFunction {
  
  ArrayList<Sample> samples;
  int total;
  int kept;
  int[] ref;
  int[] alt;

  public static final String HEADER = "#SAMPLE" + T + "TOTAL" + T + "GENOTYPED" + T + "NB_MISSING" + T + "%_MISSING" + T + "REF" + T + "ALT";

  private final RatioParameter maxInd = new RatioParameter(OPT_THRESHOLD, "Maximum ratio of Missing Individuals per position");

  @Override
  public String getSummary() {
    return "For each samples in the PED file, print a summary of missingness";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("for each samples in the PED file, prints a summary, in the format")
            .addColumns((HEADER + T + "Total_Variants" + T + "Kept_Variants").split(T))
            .addLine("where")
            .addItemize(Description.code("SAMPLE")+" the sample name",
                Description.code("TOTAL")+" total variants kept",
                Description.code("GENOTYPED")+" variants with non missing genotypes for this sample",
                Description.code("NB_MISSING")+" variants with missing genotypes for this sample",
                Description.code("%_MISSING")+" percent of genotypes missing for this sample",
                Description.code("REF")+" number of variants homozygous to the ref for this sample",
                Description.code("ALT")+" number of variants not homozygous to the ref for this sample")
            .addLine("The header of the output also contains the total number of variants present in the input file and the number of variants that are kept")
            .addWarning("Kept variants are those with less than " + Description.code(maxInd.getKey()) + " genotypes missing");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    samples = getPed().getSamples();
    total = 0;
    kept = 0;
    ref = new int[samples.size()];
    alt = new int[samples.size()];
  }
  
  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{HEADER + T + "Total_variants:" + total + T + "Kept_variants:" + kept};
  }
  
  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    for (int s = 0; s < samples.size(); s++) {
      String name = samples.get(s).getId();
      int genotyped = ref[s] + alt[s];
      int missing = kept - genotyped;
      double percent = (missing * 1.0) / kept;
      out.add(name + T + kept + T + genotyped + T + missing + T + percent + T + ref[s] + T + alt[s]);
    }
    return out.toArray(new String[0]);
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    //total++;
    boolean[] sRef = new boolean[samples.size()];
    boolean[] sAlt = new boolean[samples.size()];
    if (variant.getPercentMissing() <= maxInd.getFloatValue()) {
      //kept++;
      for (int s = 0; s < samples.size(); s++) {
        Genotype g = variant.getGenotype(samples.get(s));
        if (!g.isMissing())
          if (!g.hasAlternate())
            sRef[s] = true;
          else
            sAlt[s] = true;
      }
    }
    this.pushAnalysis(new Object[]{sRef, sAlt});
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    try {
      boolean[] sRef = (boolean[])((Object[])analysis)[0];
      boolean[] sAlt = (boolean[])((Object[])analysis)[1];
      boolean keep = false;
      for(int s = 0; s < sRef.length; s++){
        if(sRef[s]){
          ref[s]++;
          keep = true;
        }
        if(sAlt[s]){
          alt[s]++;
          keep = true;
        }
      }
      if(keep)
        kept++;
      total++;
      return true;
    } catch (Exception ignore) { }
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript def = TestingScript.newFileAnalysis();
    def.addAnonymousFilename("vcf", "vcf");
    def.addAnonymousFilename("ped", "ped");
    def.addNamingValue("threshold", "0.05");
    return new TestingScript[]{def};
  }
}
