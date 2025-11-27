package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ConsequenceParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.HashMap;

/**
 * Filters Variants according to consequences. Replaces ID by gene_chr_pos.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-04-24
 * Checked for release on 2020-08-04
 * Unit Test defined on   2020-08-04
 */
public class FilterCsqExtractGene extends ParallelVCFVariantFunction {

  private final ConsequenceParameter leastCsq = new ConsequenceParameter();

  @Override
  public String getSummary() {
    return "Filters Variants according to consequences. Replaces ID by gene_chr_pos.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Filters Variants according to consequence.")
        .addLine("Replaces ID by gene_chr_pos.")
        .addLine("Only Ref and one alternate allele are Kept. The Kept alternate is (in this order) : 1. the most severe; 2. the most frequent in the file; 3. the first one.");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    HashMap<Integer, VEPAnnotation> annotations = variant.getInfo().getWorstAnnotationsByAllele();

    int worst = 1;
    int worstCsq = VEPConsequence.getWorstConsequence(annotations.get(1)).getLevel();

    if (variant.getAlleleCount() > 2) {
      int worstAC = variant.getAlleleCount(worst);

      for (int a = 2; a < variant.getAlleleCount(); a++) {
        int currentCsq = VEPConsequence.getWorstConsequence(annotations.get(a)).getLevel();
        if (currentCsq > worstCsq) {
          worst = a;
          worstCsq = currentCsq;
        } else {
          int currentAC = variant.getAlleleCount(a);
          if (currentAC < worstAC) {
            worst = a;
            worstAC = currentAC;
          }
        }
      }
    }

    if (worstCsq >= this.leastCsq.getConsequenceLevel()) {
      String[] f = variant.getFields();
      f[VCF.IDX_ID] = annotations.get(worst).getSYMBOL() + "_" + variant.getChrom() + "_" + variant.getPos();
      return new String[]{String.join(T, f)};
    }
    return NO_OUTPUT;
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addNamingValue("csq", "missense_variant");
    return new TestingScript[]{scr};    
  }
}
