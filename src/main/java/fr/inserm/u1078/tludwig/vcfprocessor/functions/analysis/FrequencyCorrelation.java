package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.FileOutputer;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.Println;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Prints the frequency correlation of variants between local samples and GnomAD
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-01-26
 * Checked for release on 2020-05-12
 * Unit Test defined on 2020-07-10 
 */
public class FrequencyCorrelation extends ParallelVCFVariantFunction<FrequencyCorrelation.Analysis> { //TODO really similar to CompareToGnomAD, but uses annotation instead of second VCF, GnomadAD should appears in title

  final String[] HEADER = {"CHR", "POS", "REF", "ALT", "Local", "GnomAD"};

  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();
  HashMap<VEPConsequence, FileOutputer> out;

  @Override
  public String getSummary() {
    return "Prints the frequency correlation of variants between local samples and GnomAD";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("For each variants prints :")
            .addColumns(HEADER)
            .addLine("Outputs one line per VEP Consequence");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.IGNORE_STAR_ALLELE_AS_LINE); }

  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @SuppressWarnings("unused")
  @Override
  public Println[] getHeaders() {
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    out = new HashMap<>();
    String basename = this.vcfFile.getBasename();
    for (VEPConsequence csq : VEPConsequence.values()) {
      String name = dir.getDirectory() + "freq." + basename + "." + csq.getLevel() + "." + csq.getName() + ".tsv";
      try {
        FileOutputer tmp = getFileOutputer(name);
        tmp.println(Println.join(T, HEADER));
        out.put(csq, tmp);
      } catch (IOException e) {
        Message.die("Unable to write to output file " + name);
      }
    }
  }

  @SuppressWarnings("unused")
  @Override
  public void end() {
    for (FileOutputer pw : out.values())
      try { pw.close(); }
      catch (Exception e) { Message.error("Error while closing ["+pw.getFilename()+"] "+e.getMessage()); }
  }

  @Override
  public Println[] processInputVariant(Variant variant) {
    if (variant.getPercentMissing() <= 0.01)
      for (int a : variant.getNonStarAltAllelesAsArray()) {
        String chr = variant.getChrom();
        int pos = variant.getPos();
        String ref = variant.getRef();
        String alt = variant.getAlleles()[a];
        double local = variant.getAlleleFrequencyTotal(a);
        if (local != 0) {
          double gnomad = variant.getInfo().getVEPInfo().getgnomAD_AF(a);
          Println line = Println.join(T, chr, pos, ref, alt, local, gnomad);
          for (VEPConsequence csq : variant.getInfo().getVEPInfo().getAllVEPConsequences(a)){
            pushAnalysis(new Analysis(csq.getLevel(), line));
          }
        }
      }
    return NO_OUTPUT;
  }

  @Override
  public void processAnalysis(Analysis analysis) {
    super.processAnalysis(analysis);
    out.get(analysis.level).println(analysis.println);
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newDirectoryAnalysis();
    scr.addAnonymousFilename("vcf", "vcf");
    return new TestingScript[]{scr};
  }

  public static class Analysis {
    private final int level;
    private final Println println;

    public Analysis(int level, Println println) {
      this.level = level;
      this.println = println;
    }
  }
}
