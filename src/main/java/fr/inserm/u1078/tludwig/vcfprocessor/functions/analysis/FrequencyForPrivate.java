package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

import java.util.ArrayList;

/**
 * Prints the Allele frequency in the file and each group, for variants not found in dbSNP, 1kG or GnomAD.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-11-16
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-07
 */
public class FrequencyForPrivate extends ParallelVCFVariantPedFunction<Object> {

  private int G;

  @Override
  public String getSummary() {
    return "Prints the Allele frequency in the file and each group, for variants not found in dbSNP, 1kG or GnomAD.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("For each variant in the file, if the variant is not found in dbSNP, 1KG or GnomAD :")
            .addLine("Prints the frequency in the file, and in each group, as well as its consequence");
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
    G = getPed().getGroups().size();
  }

  @SuppressWarnings("unused")
  @Override
  public Println[] getHeaders() {
    Println out = Println.join(T, "CHROM", "POS", "REF", "ALT", "Frq_Total");
    for (int i = 0; i < G; i++)
      out.append(T).append("Frq_").append(getPed().getGroups().get(i));
    out.append(T).append("Consequences");
    return new Println[]{out};
  }

  @Override
  public Println[] processInputVariant(Variant variant) {
    ArrayList<Println> outs = new ArrayList<>();
    for (int a : variant.getNonStarAltAllelesAsArray())
      if (!variant.getInfo().getVEPInfo().isIn1kg(a) && !variant.getInfo().getVEPInfo().hasExistingVariants(a) && !variant.getInfo().getVEPInfo().isInGnomAD(a)) {
        int[] countByGroup = new int[G];
        int[] totalInGroup = new int[G];
        int count = 0;
        int total = 0;

        for (Genotype genotype : variant.getGenotypes()) {
          if(!genotype.isMissing()){
            int idx = getPed().getGroups().indexOf(genotype.getSample().getGroup());
            total += genotype.getNbChrom();
            totalInGroup[idx] += genotype.getNbChrom();
            int t = genotype.getCount(a);
            if (t > 0) {
              count += t;              
              countByGroup[idx] += t;
            }
          }
        }

        Println out = Println.join(T,
            variant.getChrom(),
            variant.getPos(),
            variant.getRef(),
            variant.getAlleles()[a],
            total == 0 ? 0 : count / (1d * total));
        for (int g = 0; g < G; g++)
          out.append(T, totalInGroup[g] == 0 ? 0 : countByGroup[g] / (1d * totalInGroup[g]));
        out.append(T, String.join(",", variant.getInfo().getVEPInfo().getAllConsequences(a)));
        outs.add(out);
      }
    return outs.toArray(new Println[0]);
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }
}
