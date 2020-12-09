package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Removes Variants that are found in controls
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-10-03
 * Checked for release on 2020-08-07
 * Unit Test defined on   2020-08-07
 */
public class NotFoundInAnyControl extends ParallelVCFVariantFilterFunction {
  public final PedFileParameter pedfile = new PedFileParameter();
  private ArrayList<Sample> controls;

  @Override
  public String getSummary() {
    return "Removes Variants that are found in controls.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Control samples are defined by a \"2\" in the 6th field of the " + Description.code(this.pedfile.getKey()) + " file.")
            .addLine("In case of a multiallelic variant, if any variant allele isn't found, the whole variant is kept.");
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FILTER_ONE;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }
  
  @Override
  public void begin() {
    super.begin();
    this.controls = this.getVCF().getPed().getControls();
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    boolean keep = false;
    for (int a = 1; a < variant.getAlleleCount(); a++) {
      boolean present = false;
      for (Sample control : controls)
        if (variant.getGenotype(control).hasAllele(a)) {
          present = true;
          break;
        }

      if (!present) {
        keep = true;
        break;
      }
    }

    return keep ? asOutput(variant) : NO_OUTPUT;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedTransformScript();
  }
}
