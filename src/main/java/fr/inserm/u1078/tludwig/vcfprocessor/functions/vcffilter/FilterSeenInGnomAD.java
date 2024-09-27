package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

public class FilterSeenInGnomAD extends ParallelVCFFilterFunction {
  private final String EXOME = "gnomad_exome_AN";
  private final String GENOME = "gnomad_genome_AN";

  @Override
  public String getSummary() {
    return "Filters out variants that are seen in gnomAD";
  }

  @Override
  public Description getDesc() {
    return new Description("Variants that are seen the gnomAD for at least one allele are filtered out")
        .addLine("Variants are filtered if "+GENOME+" > 0 or "+EXOME+" > 0");
  }

  @Override
  public boolean needVEP() {
    return false;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @Override
  public String[] processInputRecordForFilter(VariantRecord record) {
    for(String[] info : record.getInfo()){
      if(GENOME.equals(info[0]) && getAN(info[1]) > 0)
        return NO_OUTPUT;
      if(EXOME.equals(info[0]) && getAN(info[1]) > 0)
        return NO_OUTPUT;
    }
    return new String[]{record.toString()};
  }

  private int getAN(String value){
    try{
      return Integer.parseInt(value);
    } catch(NumberFormatException e){
      return 0;
    }
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }
}
