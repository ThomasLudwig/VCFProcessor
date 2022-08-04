package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF.Reader;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF.Wrapper;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;
import java.util.NavigableSet;

/**
 * Creates a bed file of the positions where at least one sample has 2 SNVs that could be in the same triplet (regardless of the reading frame)
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-01-31
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-15
 * Last Tested on         2020-08-14
 */
public class ExtractNeighbours extends VCFFunction {

  private NavigableSet<Sample> samples = null;

  @Override
  public String getSummary() {
    return "Creates a bed file of the positions where at least one sample has 2 SNVs that could be in the same triplet (regardless of the reading frame)";
  }

  @Override
  public Description getDesc() {
    return new Description("Scans the whole VCF file, for each successive variants V1 and V2")
            .addLine("if at least one sample has the variants V1 and V2 then a bed regions if printed. The region is defined as :")
            .addColumns(new String[]{"chr", "V1_pos", "V2_pos"})
            .addLine("V1 and V2 must be on the same chromosome and V2_pos-V1_pos = 1 or V2_pos-V1_pos = 2");
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return "chr V1_pos V2_pos is printed if, at least one alternate allele of V1_pos and V2_pos is a SNP, and if one sample has a variant of both side (not necessarily the SNP one)."; //TODO change implementation
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public void executeFunction() throws Exception {
    VCF vcf = this.vcffile.getVCF(VCF.STEP10000);
    Reader reader = vcf.getReaderAndStart();
    samples = vcf.getSamples();
    Wrapper previous = reader.nextLine();
    Wrapper current;
    if (previous != null && previous.line != null)
      while ((current = reader.nextLine()) != null && current.line != null) {
        compare(previous, current, vcf);
        previous = current;
      }
  }

  private void compare(Wrapper previousW, Wrapper currentW, VCF vcf) {

    String p[] = previousW.line.split(T);
    String c[] = currentW.line.split(T);
    if (!p[0].equals(c[0]))
      return;
    int posP = new Integer(p[1]);
    int posC = new Integer(c[1]);
    int dist = posC - posP;
    if (dist == 1 || dist == 2)
      try {
        Variant previous = vcf.createVariant(previousW.line);
        Variant current = vcf.createVariant(currentW.line);
        if (previous.isSNP() && current.isSNP())
          for (Sample sample : samples)
            if (previous.getGenotype(sample).hasAlternate() && current.getGenotype(sample).hasAlternate()) {
              println(previous.getChrom() + T + previous.getPos() + T + current.getPos());
              return;
            }
      } catch (Exception e) {
      }
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
