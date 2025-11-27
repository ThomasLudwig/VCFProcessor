package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Outputs the same VCF same but randomly reassigns the genotypes among the samples
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2017-04-28
 * Checked for release on 2020-08-04
 * Unit Test defined on   2020-08-04
 */
public class Scramble extends ParallelVCFFunction {

  @Override
  public String getSummary() {
    return "Outputs the same VCF same but randomly reassigns the genotypes among the samples";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("This function can be used to anonymize a VCF file. The AC/AN/AF of each variants will stay consistent, but the haplotypes will be broken.")
            .addLine("For each line, the genotypes are randomly reassigned among the samples.")
            .addLine("The random reassignment is different for each line");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public String[] processInputRecord(VariantRecord record) {
    LineBuilder out = new LineBuilder(record.getChrom());
    out.addColumn(record.getPos());
    out.addColumn(record.getID());
    out.addColumn(record.getRef());
    out.addColumn(record.getAltString());
    out.addColumn(record.getQual());
    out.addColumn(record.getFiltersString());
    out.addColumn(record.getInfoString());
    out.addColumn(record.getFormatString());

    ArrayList<String> genos = new ArrayList<>();
    Collections.addAll(genos, record.getGenotypeStrings());

    while (!genos.isEmpty())
      out.addColumn(genos.remove((int) (genos.size() * Math.random())));
    return new String[]{out.toString()};
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[]{new CustomScript()};
  }
  
  private static class CustomScript extends TestingScript {
    
    CustomScript() {
      super(TestingScript.FILE, 0);
      this.addAnonymousFilename("vcf", "vcf");
    }

    @Override
    public LineBuilder testSingleFile() {
      LineBuilder out = new LineBuilder(TAB2);
      out.newLine("dif=`diff $exp $out | wc -l`;");
      out.newLine("countout=`cat $out | grep -v \"^#\" | wc -c`");
      out.newLine("countexp=`cat $exp | grep -v \"^#\" | wc -c`");
      out.newLine();
      out.newLine("if [ \"$dif\" -gt \"4\" ] && [ \"$countexp\" -eq \"$countout\" ] #check if there are differences after the header, but the number of characters is the same");
      out.newLine("then");
      out.newLine(TAB, "echo \"${BASH_SOURCE[0]} OK\";");
      out.newLine(TAB, "rm -rf $DIR/results.*.OK;");
      out.newLine(TAB, "rm $log;");
      out.newLine(TAB, "mv $out $out.OK;");
      out.newLine("else");
      out.newLine(TAB, ">&2 echo \"${BASH_SOURCE[0]} KO\";");
      out.newLine(TAB, "mv $out $out.KO;");
      out.newLine("fi");
      return out;
    }
  }
}
