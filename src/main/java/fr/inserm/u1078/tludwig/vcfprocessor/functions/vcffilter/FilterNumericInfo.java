package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

public class FilterNumericInfo extends ParallelVCFFilterFunction {
  private final StringParameter query = new StringParameter(OPT_QUERY, "\"VALUE1>0|VALUE2>0\"", "A query describing the variants to filter out");
  //TODO implement

  @Override
  public String getSummary() {
    return "Filters variants according to the numerical values of info fieds";
  }

  @Override
  public Description getDesc() {
    return new Description("Provide a logical definition for variants to remove. Example:")
        .addItemize("\"VALUE=17.5\"","\"VALUE1>0|VALUE2>0\"","\"VALUE>20&VALUE<50\"");
  }

  @Override
  public String getMultiallelicPolicy() {
    return null;
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
  public void begin() {
    super.begin();
    throw new UnsupportedOperationException("Function not yet available");
  }

  @Override
  public String[] processInputRecordForFilter(VariantRecord record) {
    return new String[0];
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }

  private static class Condition {
    final String info;
    final double value;

    public Condition(String info, double value) {
      this.info = info;
      this.value = value;
    }
  }

}
