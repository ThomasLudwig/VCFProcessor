package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.FileOutputer;

/**
 * Generates two new VCF files with variants present/absent in 1kG/GnomAD.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-01-21
 * Checked for release on 2020-08-07
 * Unit Test defined on   2020-08-07
 */
public class SplitFromDB extends VCFFunction { //TODO parallelize

  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();

  @Override
  public String getSummary() {
    return "Generates two new VCF files with variants present/absent in 1kG/GnomAD.";
  }
//TODO this code needs to be checked
  
  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Generates two new VCF files : ")
        .addItemize(Description.bold("inDB.MYVCF.vcf")+" (with variants present in 1kG/GnomAD)",
                Description.bold("notInDB.MYVCF.vcf")+" (with variant absent from 1kG/GnomAD)");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.KEEP_IF_ONE_SATISFY); }

  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    String basename = this.vcfFile.getBasename();
    FileOutputer outIn = getFileOutputer(dir.getDirectory() + "inDB." + basename);
    FileOutputer outNot = getFileOutputer(dir.getDirectory() + "notInDB." + basename);

    VCF vcf = this.vcfFile.getVCF();
    vcf.getReaderAndStart();
    
    vcf.printHeaders(outIn);
    vcf.printHeaders(outNot);

    Variant variant;
    while ((variant = vcf.getUnparallelizedNextVariant()) != null) {
      boolean in = false;

      for (int a = 1; a < variant.getAlleles().length; a++)
        if (variant.getInfo().getVEPInfo().isIn1kg(a) || variant.getInfo().getVEPInfo().isInGnomAD(a)) {
          in = true;
          break;
        }

      if (in)
        outIn.println(variant.println());
      else
        outNot.println(variant.println());
    }

    outIn.close();
    outNot.close();
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newDirectoryTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    return new TestingScript[]{scr};
  }
}
