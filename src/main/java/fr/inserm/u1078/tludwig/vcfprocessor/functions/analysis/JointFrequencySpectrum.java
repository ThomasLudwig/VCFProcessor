package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Creates a JointFrequencySpectrum result file for each group defined in the ped file.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-11-18
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-09-10 //TODO completly KO, rewrite everything !!
 */
public class JointFrequencySpectrum extends ParallelVCFVariantPedFunction {

  ArrayList<Sample>[] samples;
  ArrayList<String> groups;
  int nb;  
  int size;
  int[][][][] count;
  
  private final OutputDirectoryParameter outdir = new OutputDirectoryParameter();

  @Override
  public String getSummary() {
    return "Creates a JointFrequencySpectrum result file for each group defined in the ped file.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("See https://www.nature.com/articles/ejhg2013297")
            .addLine("Samples from the same group MUST be split into 2 subgroup, so as to be compared")
            .addLine("Example : GroupA1 GroupA2 GroupB1 GroupB2 GroupC1 GroupC2")
            .addLine("each group MUST HAVE the same number of samples.")
            .addLine("The format of the output is :")
            .addLine("G*G output files (where G is the number of groups). Each file is named VCF_INPUT_FILE.group1.group2.tsv")
            .addLine("Each of these files contains a (2n+1)x(2n+1) matrix, where n is the number of samples in each population. The number at matrix[A][B], is the number of variants for which the first group has A variant alleles and the second group has B variant alleles")
            .addLine("Output file must then be processed with the function " + JFSSummary.class.getSimpleName());
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    groups = new ArrayList<>();
    for (String group : getPed().getGroups())//from all group A1, A2, B1, B2, C1, C2
      if (isPrimaryGroup(group))
        groups.add(group);

    Message.debug("Final groups");
    for (String group : groups)
      Message.debug(group);

    nb = groups.size(); //real groups (A, B, C)
    samples = getPed().getSamplesByGroup();
    size = (samples[0].size() * 2) + 1;
    count = new int[nb][nb][size][size];
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return null;
  }

  @SuppressWarnings("unused")
  @Override
  public void end() {
    String suffix = this.vcfFile.getBasename();

    for (int ga = 0; ga < nb; ga++) {
      String groupA = groups.get(ga);
      for (int gb = ga; gb < nb; gb++) {
        String groupB = groups.get(gb);
        String outFilename = outdir.getDirectory() + groupA + "." + groupB + "." + suffix+ ".tsv";
        try {
          PrintWriter out = getPrintWriter(outFilename);
          for (int ca = 0; ca < size; ca++) {
            LineBuilder line = new LineBuilder();
            for (int cb = 0; cb < size; cb++)
              line.addColumn(count[ga][gb][ca][cb]);
            out.println(line.substring(1));
          }
          out.close();
        } catch (IOException e) {
          Message.error("Unable to write results to "+outFilename);
        }
      }
    }
  }

  private static boolean isPrimaryGroup(String name) {
    return name.charAt(name.length() - 1) != '2';
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    //count variants for each groups
    int alts = variant.getAlleleCount() - 1;
    int[][] tmpCount = new int[nb * 2][alts];
    for (Genotype genotype : variant.getGenotypes()) {
      String group = genotype.getSample().getGroup();
      int idx = getPed().getGroups().indexOf(group);
      int[] alleles = genotype.getAlleles();
      if(alleles != null)
        for(int a : alleles)
          if(a > 0)
            tmpCount[idx][a-1]++;
    }

    //add variants to global count
    ArrayList<String> pedGroups = getPed().getGroups();
    for (int ga = 0; ga < nb; ga++)
      for (int gb = ga; gb < nb; gb++) {
        String groupA = groups.get(ga);
        String groupB = groups.get(gb);

        if (gb == ga)
          groupB += 2;

        int ia = pedGroups.indexOf(groupA);
        int ib = pedGroups.indexOf(groupB);
        
        if(ia == -1)
          ia = pedGroups.indexOf(groupA.substring(0,groupA.length()-1));
        if(ib == -1)
          ib = pedGroups.indexOf(groupB.substring(0,groupB.length()-1));

        try {
          for (int a = 0; a < alts; a++) {
            int ca = tmpCount[ia][a];
            int cb = tmpCount[ib][a];
            this.pushAnalysis(new Integer[]{ga, gb, ca, cb});
          }
        } catch (Exception e) {
          Message.error("Group A " + groupA + " (" + ia + ")");
          Message.error("Group B " + groupB + " (" + ib + ")");
          this.fatalAndQuit("error", e);
        }
      }
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    try {
      Integer[] n = (Integer[])analysis;
      count[n[0]][n[1]][n[2]][n[3]]++;
      return true;
    } catch (Exception e) {
      Message.error("Error while checking analysis results", e);
    }
    return false;
  }  
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript s = TestingScript.newDirectoryAnalysis();
    s.addAnonymousFilename("vcf", "vcf");
    s.addAnonymousFilename("ped", "ped");
    return new TestingScript[]{s};
  }
}
