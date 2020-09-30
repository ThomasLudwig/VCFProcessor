package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.PrintWriter;

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
  private PrintWriter outIn;
  private PrintWriter outNot;

  @Override
  public String getSummary() {
    return "Generates two new VCF files with variants present/absent in 1kG/GnomAD.";
  }
//TODO this code needs to be checked
  
  @Override
  public Description getDesc() {
    return new Description("Generates two new VCF files : ")
            .addItemize(new String[]{
              Description.bold("inDB.MYVCF.vcf")+" (with variants present in 1kG/GnomAD)",
              Description.bold("notInDB.MYVCF.vcf")+" (with variant absent from 1kG/GnomAD)"
            });    
  }

  @Override
  public boolean needVEP() {
    return true;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return "If at least one alternate allele satisfy all the conditions, the whole variant line is kept in \"inDB.MYVCF.vcf\".";
  }

  @Override
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @Override
  public void executeFunction() throws Exception {
    String basename = this.vcffile.getBasename();
    this.outIn = getPrintWriter(dir.getDirectory() + "inDB." + basename);
    this.outNot = getPrintWriter(dir.getDirectory() + "notInDB." + basename);

    VCF vcf = this.vcffile.getVCF();
    vcf.getReaderAndStart();
    
    vcf.printHeaders(outIn);
    vcf.printHeaders(outNot);

    Variant variant;
    while ((variant = vcf.getNextVariant()) != null) {
      boolean in = false;

      for (int a = 1; a < variant.getAlleles().length; a++)
        if (variant.getInfo().isIn1KgVEP(a) || variant.getInfo().isInGnomADVEP(a)) {
          in = true;
          break;
        }

      if (in)
        outIn.println(variant);
      else
        outNot.println(variant);
    }

    this.outIn.close();
    this.outNot.close();
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newDirectoryTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    return new TestingScript[]{scr};
  }
}
