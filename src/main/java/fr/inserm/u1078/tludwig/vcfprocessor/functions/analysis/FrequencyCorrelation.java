package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.io.PrintWriter;
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
public class FrequencyCorrelation extends ParallelVCFVariantFunction { //TODO really similar to CompareToGnomAD, but uses annotation instead of second VCF, GnomadAD should appears in title

  String[] HEADER = {"CHR", "POS", "REF", "ALT", "Local", "GnomAD"};

  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();
  HashMap<VEPConsequence, PrintWriter> out;

  @Override
  public String getSummary() {
    return "Prints the frequency correlation of variants between local samples and GnomAD";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("For each variants prints :")
            .addColumns(HEADER)
            .addLine("Outputs one line per VEP Consequence");
  }

  @Override
  public boolean needVEP() {
    return true;
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
  public String[] getHeaders() {
    return null;
  }

  @Override
  public void begin() {
    out = new HashMap<>();
    String basename = this.vcffile.getBasename();
    for (VEPConsequence csq : VEPConsequence.values()) {
      String name = dir.getDirectory() + "freq." + basename + "." + csq.getLevel() + "." + csq.getName() + ".tsv";
      try {
        PrintWriter tmp = getPrintWriter(name);
        tmp.println(String.join(T, HEADER));
        out.put(csq, tmp);
      } catch (IOException e) {
        this.fatalAndDie("Unable to write to output file " + name);
      }
    }
  }

  @Override
  public void end() {
    for (PrintWriter pw : out.values())
      pw.close();
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    ArrayList<String> ret = new ArrayList<>();
    if (variant.getPercentMissing() <= 0.01)
      for (int a = 1; a < variant.getAlleles().length; a++) {
        String chr = variant.getChrom();
        int pos = variant.getPos();
        String ref = variant.getRef();
        String alt = variant.getAlleles()[a];
        double local = variant.getAlleleFrequencyTotal(a);
        if (local != 0) {
          double gnomad = variant.getInfo().getFreqGnomadVEP(a);
          String line = chr + T + pos + T + ref + T + alt + T + local + T + gnomad;
          for (int level : variant.getInfo().getConsequenceLevels(a))
            ret.add(level+"¤"+line);
        }
      }
    return ret.toArray(new String[ret.size()]);
    //return NO_OUTPUT;
  }

  @Override
  public void processOutput(String line) {
    String[] ol = line.split(("¤"));
    for (String level : ol[0].split(","))
      out.get(VEPConsequence.getConsequence(new Integer(level))).println(ol[1]);
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newDirectoryAnalysis();
    scr.addAnonymousFilename("vcf", "vcf");
    return new TestingScript[]{scr};
  }
}
