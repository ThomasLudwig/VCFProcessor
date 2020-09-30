package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Counts the genotypes 0/1 1/1 for each variants
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-11-20
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-13
 */
public class CountGenotypes extends ParallelVCFVariantPedFunction {

  private int GRP;

  public static final String[] HEADER = {"CHROM","POS","REF","ALT","CONSEQUENCE","TOTAL_HETEROZYGOUS","TOTAL_HOMOZYGOUS_ALT"};

  @Override
  public String getSummary() {
    return "Counts the genotypes "+Description.code("0/1")+" and "+Description.code("1/1")+" for each variants";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("The output format is:")
            .addColumns(HEADER)
            .addLine("Followed by the number of heterozygous and homozygous for each group defined in the ped file.");
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
    return OUT_TSV;
  }

  @Override
  public void begin() {
    GRP = getPed().getGroups().size();
  }

  @Override
  public String[] getHeaders() {
    String line = "";
    for (String group : getPed().getGroups())
      line += T + group + T + getPed().getGroupSize(group);
    println(line.substring(1));

    line = String.join(T, HEADER);

    for (String group : getPed().getGroups())
      line += T + group + "_HETEROZYGOUS" + T + group + "_HOMOZYGOUS_ALT";
    return new String[]{line};
  }

  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ALLELE_AS_LINE;
  }
  
  @Override
  public String[] processInputVariant(Variant variant) {
    String[] outs = new String[variant.getAlleleCount() -1];
    for (int a = 1; a < variant.getAlleleCount(); a++) {
      int counts[][] = new int[GRP + 1][3];

      for (Genotype g : variant.getGenotypes()) {
        int d = g.getCount(a);
        int i = getPed().getGroups().indexOf(g.getSample().getGroup());
        counts[i + 1][d]++;
        counts[0][d]++;
      }
      VEPAnnotation worst = variant.getInfo().getWorstVEPAnnotation(a);
      LineBuilder line = new LineBuilder(variant.getChrom());
      line.addColumn(variant.getPos());
      line.addColumn(variant.getRef());
      line.addColumn(variant.getAlleles()[a]);
      line.addColumn(worst.getConsequence());
      for (int[] count : counts) 
        line.addColumn(count[1]).addColumn(count[2]);
      
      outs[a-1] = line.toString();
    }
    return outs;
  }

  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }
}