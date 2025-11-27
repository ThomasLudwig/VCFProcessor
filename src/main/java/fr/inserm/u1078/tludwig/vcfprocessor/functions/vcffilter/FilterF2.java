package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Filters variants to keep only those contributing to F2 data.
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-05-11
 * Checked for release on 2020-08-06
 * Unit Test defined on 2020-08-06
 */
public class FilterF2 extends ParallelVCFVariantFilterFunction {

  @Override
  public String getSummary() {
    return "Filters variants to keep only those contributing to F2 data.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("F2 data are described in PubMedId: 23128226, figure 3a");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.KEEP_IF_ONE_SATISFY); }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    final int A = variant.getAlleleCount();
    int[] count = new int[A];

    for(Genotype g : variant.getGenotypes()){
      if(!g.isMissing()) {
        int[] local = new int[A];
        for (int a : g.getAlleles())
          if (a > -1)
            local[a]++;
        for (int a = 0; a < A; a++) {
          count[a] += local[a];
          if (local[a] == 2) //Overload homozygous to avoid false F2 from a single homozygous
            count[a] += local[a];
        }
      }
    }

    for(int c : count)
      if(c == 2)
        return asOutput(variant);
    return NO_OUTPUT;
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
