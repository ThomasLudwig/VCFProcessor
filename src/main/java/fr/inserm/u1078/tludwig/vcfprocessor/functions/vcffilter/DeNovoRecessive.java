package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Keeps only variants that strictly respect this genotypes parent1 0/1 + parent2 0/0 + child 1/1
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-09-06
 * Checked for release on 2020-08-07
 * Unit Test defined on   2020-08-07
  */
public class DeNovoRecessive extends ParallelVCFVariantFilterPedFunction {
  private Sample p1;
  private Sample p2;
  private Sample child;

  @Override
  public String getSummary() {
    return "Keeps only variants that strictly respect this genotypes "+Description.italic("parent1")+" 0/1 + "+Description.italic("parent2")+" 0/0 + "+Description.italic("child")+" 1/1";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("parent1 and parent2 are interchangeable")
            .addWarning("Will only run if input file has a trio with 1 case an 2 controls");    
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.KEEP_IF_ONE_SATISFY); }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    Ped ped = this.getVCF().getPed();
    super.begin();
    if (ped.getCases().size() != 1)
      Message.die("There should be exactly 1 case (child of 2 controls)");
    if (ped.getControls().size() != 2)
      Message.die("There should be exactly 2 controls (parents of the case)");

    p1 = ped.getControls().get(0);
    p2 = ped.getControls().get(1);
    child =ped.getCases().get(0);
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    Genotype gc = variant.getGenotype(child);
    Genotype gp1 = variant.getGenotype(p1);
    Genotype gp2 = variant.getGenotype(p2);

    for (int a = 1; a < variant.getAlleles().length; a++)       
      if (gc.getCount(a) == 2) 
        if (gp1.getCount(a) + gp2.getCount(a) == 1)
          return  asOutput(variant);

    return NO_OUTPUT;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedTransformScript();
  }
}
