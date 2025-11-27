package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.ArrayList;

/**
 * Extracts All Variants that are private to a Group.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-01-04
 * Checked for release on 2020-05-06
 * Unit Test defined on   2020-05-15
 * Last Tested on         2020-08-14
 */
public class ExtractPrivateToGroup extends ParallelVCFVariantPedFunction<Object> {
  private static final String[] HEADER = {"#CHR","POS","GROUP","SAMPLES"};
  private int[] orderedSamples;

  @Override
  public String getSummary() {
    return "Extracts All Variants that are private to a Group.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Only the variants found in a single group of samples (as defined in the ped file) are extracted")
            .addLine("The list of the N samples in the group that have the variant is given")
            .addLine("Output Format :")
            .addColumns(HEADER);
  }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T,HEADER)};
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.IGNORE_STAR_ALLELE_AS_LINE); }

  @Override
  public void begin() {
    super.begin();
    ArrayList<Sample>[] samples = getVCF().getPed().getSamplesByGroup();
    final int N = getVCF().getPed().getSampleSize();
    this.orderedSamples = new int[N];
    int n = 0;
    while(n < N){
      for(int g = 0 ; g < samples.length; g++){
        if(!samples[g].isEmpty()) {
          Sample s = samples[g].remove(0);
          this.orderedSamples[n++] = getVCF().indexOfSample(s);
        }
      }
    }
  }

  //@Override
  public String[] oldprocessInputVariant(Variant variant) {
    ArrayList<String> outs = new ArrayList<>();
    for (int a : variant.getNonStarAltAllelesAsArray()) {
      ArrayList<String> groups = variant.getGroupsWithAllele(a);
      if (groups.size() == 1) {
        String out = variant.getChrom() + T + variant.getPos() + T + groups.get(0);
        StringBuilder samples = new StringBuilder();
        for (Genotype g : variant.getGenotypes())
          if (g.hasAllele(a))
            samples.append(",").append(g.getSample().getId());
        out += T + samples.substring(1);
        outs.add(out);
      }
    }
    return outs.toArray(new String[0]);
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    //Init
    ArrayList<String> outs = new ArrayList<>();
    ArrayList<Integer> alleles = variant.getNonStarAltAllelesAsList();
    int N = variant.getAlleleCount()+1;
    Allele[] all = new Allele[N];

    for(int i = 0; i < N; i++)
      all[i] = new Allele(alleles.contains(i));

    //scan for each samples
    for(int i : this.orderedSamples) {
      Genotype genotype = variant.getGenotypes()[i];
      ArrayList<Integer> seen = new ArrayList<>();
      int[] geno = genotype.getAlleles();
      if(geno != null) {
        for(int a : geno) {
          if(!seen.contains(a))
            test(genotype.getSample(), a, all);
          seen.add(a);
        }
      }
    }

    //Output results
    for (int a : alleles)
      if (all[a].isOutput())
        outs.add(variant.getChrom() + T + variant.getPos() + T + all[a].getOutput());

    return outs.toArray(new String[0]);
  }

  private void test(Sample sample, int a, Allele[] all) {
    if (all[a].isValid()) {
      String group = sample.getGroup();
      if (all[a].isNew()) {
        all[a].setGroup(group);
        all[a].add(sample);
      } else if (all[a].isGroup(group)) {
        all[a].add(sample);
      } else {
        all[a].ignore();
      }
    }
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedAnalysisScript();
  }

  private static class Allele {
    private String group;
    private final ArrayList<Sample> samples;
    private boolean valid;

    public Allele(boolean valid) {
      this.group = null;
      this.samples = new ArrayList<>();
      this.valid = valid;
    }

    public void ignore() {
      this.valid = false;
      this.samples.clear();
    }

    public boolean isValid() {
      return this.valid;
    }

    public void add(Sample sample){
      this.samples.add(sample);
    }

    public boolean isNew(){
      return this.group == null;
    }

    public void setGroup(String group){
      this.group = group;
    }

    public boolean isGroup(String group){
      return this.group.equals(group);
    }

    public boolean isOutput() {
      return !this.samples.isEmpty();
    }

    public String getOutput() {
      StringBuilder sb = new StringBuilder();
      for(Sample sample : samples)
        sb.append(",").append(sample.getId());
      return group + T + sb.substring(1);
    }
  }
}
