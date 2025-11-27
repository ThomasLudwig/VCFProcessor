package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * Splits a given vcf file by chromosome.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-06-24
 * Checked for release on 2020-08-07
 * Unit Test defined on   2020-08-07
 */
public class SplitByChromosome extends VCFFunction { //TODO parallelize

  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();

  @Override
  public String getSummary() {
    return "Splits a given vcf file by chromosome";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Splits a given vcf file and produces one resulting vcf file by chromosome.");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    String basename = this.vcfFile.getBasename();
    VCF vcf = this.vcfFile.getVCF();
    vcf.getReaderAndStart();
    String current = "current";
    PrintWriter out = null;

    VariantRecord record;
    while ((record = vcf.getUnparallelizedNextRecord()) != null) {
      String chrom = record.getChrom();
      if (!current.equals(chrom)) {
        current = chrom;
        if (out != null)
          out.close();
        String filename = dir.getDirectory() + chrom + "." + basename + ".vcf";
        Message.info("Creating vcf file " + filename);
        out = getPrintWriter(filename);
        vcf.printHeaders(out);
      }
      Objects.requireNonNull(out,"PrintWriter is null");
      out.println(record.toString());
    }
    if (out != null)
      out.close();
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newDirectoryTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    return new TestingScript[]{scr};
  }
}
