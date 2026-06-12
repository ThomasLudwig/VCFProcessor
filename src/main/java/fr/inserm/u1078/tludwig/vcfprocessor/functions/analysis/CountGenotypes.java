package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

/**
 * Counts the genotypes 0/1 1/1 for each variant
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-11-20
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-13
 */
public class CountGenotypes extends ParallelVCFVariantPedFunction<Override> {

  private int GRP;

  public static final String[] HEADER = {"CHROM","POS","REF","ALT","CONSEQUENCE","TOTAL_HETEROZYGOUS","TOTAL_HOMOZYGOUS_ALT"};

  @Override
  public String getSummary() {
    return "Counts the genotypes "+Description.code("0/1")+" and "+Description.code("1/1")+" for each variants";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("The output format is:")
            .addColumns(HEADER)
            .addLine("Followed by the number of heterozygous and homozygous for each group defined in the ped file.");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.IGNORE_STAR_ALLELE_AS_LINE); }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    GRP = getPed().getGroups().size();
  }

  @SuppressWarnings("unused")
  @Override
  public Println[] getHeaders() {
    StringBuilder header1 = new StringBuilder();
    for (String group : getPed().getGroups())
      header1.append(T).append(group).append(T).append(getPed().getGroupSize(group));

    StringBuilder header2 = new StringBuilder(String.join(T, HEADER));

    for (String group : getPed().getGroups())
      header2.append(T).append(group).append("_HETEROZYGOUS").append(T).append(group).append("_HOMOZYGOUS_ALT");
    return new Println[]{new Println(header1.substring(1)), new Println(header2.toString())};
  }

  @Override
  public Println[] processInputVariant(Variant variant) {
    int[] nonStars = variant.getNonStarAltAllelesAsArray();
    Println[] outs = new Println[nonStars.length - 1];
    for (int a = 1; a < nonStars.length; a++) {
      int[][] counts = new int[GRP + 1][3];

      for (Genotype g : variant.getGenotypes()) {
        int d = g.getCount(a);
        int i = getPed().getGroups().indexOf(g.getSample().getGroup());
        counts[i + 1][d]++;
        counts[0][d]++;
      }
      VEPAnnotation worst = VEPConsequence.getWorstVEPAnnotation(variant.getInfo().getVEPInfo().getVEPAnnotations(a));
      outs[a-1] = Println.join(T,
          variant.getChrom(),
          variant.getPos(),
          variant.getAlleles()[a],
          variant.getRef(),
          worst.getConsequences());
      for (int[] count : counts)
        outs[a-1].append(T, count[1], T, count[2]);
      

    }
    return outs;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }
}