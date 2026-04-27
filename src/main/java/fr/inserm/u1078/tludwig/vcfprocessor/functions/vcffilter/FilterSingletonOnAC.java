package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

public class FilterSingletonOnAC extends ParallelVCFFilterFunction {

  @Override
  public String getSummary() { return "Keeps only variants where at least 1 allele is a singleton, based on the AC annotation."; }

  @Override
  public Description getDesc() { return new Description(getSummary()).addLine("Should be relatively fast, as genotypes will not be analysed."); }

  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.KEEP_IF_ONE_SATISFY); }

  @Override
  public String[] processInputRecordForFilter(VariantRecord record) {
    String AC = record.getInfo("AC");
    for(String ac : AC.split(",")){
      try{
        if(Integer.parseInt(ac) == 1)
          return asOutput(record);
      } catch(NumberFormatException ignore){}
    }
    return NO_OUTPUT;
  }

  @Override
  public TestingScript[] getScripts() { return new TestingScript[0]; }
}
