package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF.Reader;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.Collection;

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

  private Collection<Sample> samples = null;

  @Override
  public String getSummary() {
    return "Creates a bed file of the positions where at least one sample has 2 SNVs that could be in the same triplet (regardless of the reading frame)";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Scans the whole VCF file, for each successive variants V1 and V2")
            .addLine("if at least one sample has the variants V1 and V2 then a bed regions if printed. The region is defined as :")
            .addColumns("chr", "V1_pos", "V2_pos")
            .addLine("V1 and V2 must be on the same chromosome and V2_pos-V1_pos = 1 or V2_pos-V1_pos = 2");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return "chr V1_pos V2_pos is printed if, at least one alternate allele of V1_pos and V2_pos is a SNP, and if one sample has a variant of both side (not necessarily the SNP one)."; //TODO change implementation
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    VCF vcf = this.vcfFile.getVCF(VCF.STEP10000);
    Reader reader = vcf.getReaderAndStart();
    samples = vcf.getSortedSamples();
    VariantRecord previous = reader.nextRecord();
    VariantRecord current;
    if (previous != null)
      while ((current = reader.nextRecord()) != null) {
        compare(previous, current, vcf);
        previous = current;
      }
  }

  private void compare(VariantRecord previousW, VariantRecord currentW, VCF vcf) {
    VariantRecord p = previousW;
    VariantRecord c = currentW;
    if (!p.getChrom().equals(c.getChrom()))
      return;
    int posP = p.getPos();
    int posC = c.getPos();
    int dist = posC - posP;
    if (dist == 1 || dist == 2)
      try {
        Variant previous = previousW.createVariant(vcf);
        Variant current = currentW.createVariant(vcf);
        if (previous.isSNP() && current.isSNP())
          for (Sample sample : samples)
            if (previous.getGenotype(sample).hasAlternate() && current.getGenotype(sample).hasAlternate()) {
              println(previous.getChrom() + T + previous.getPos() + T + current.getPos());
              return;
            }
      } catch (Exception ignore) { }
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
