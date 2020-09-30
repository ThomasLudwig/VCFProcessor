package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ListParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Filters the variants to match the given genotype filter.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2015-09-17
 * Checked for release on 2020-08-07
 * Unit Test defined on   2020-08-07
 */
public class FilterGenotype extends ParallelVCFFilterFunction { //TODO add support for phased/unphased

  private final ListParameter filter = new ListParameter(OPT_FILTER, "\"SAMPLE1:geno1:keep1,SAMPLE2:geno2:keep2,...,SAMPLEN,genoN:keepN\"", "List (comma separated) for samples, their associated genotypes and is they are to be kept");
  private String[] samples;
  private String[] genotypes;
  private boolean[] keeps;
  private int[] indices;

  @Override
  public String getSummary() {
    return "Filters the variants to match the given genotype filter.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("If the genotype of at least one sample mismatches, the variant is "+Description.bold("Excluded")+".")
            .addLine("Filter format : "+Description.code("SAMPLE1:geno1:keep1,SAMPLE2:geno2:keep2,...,SAMPLEN:genoN:keepN"))
            .addLine(Description.code("Keep=true|false")+" tells if we want to keep(true) or exclude(false) matching genotype for this sample")
            .addLine("Example "+Description.code("SA:0/0:false,SB:0/1:true,SC:0/1:true,SD:1/1:false")+" will keep variants that are 0/1 for "+Description.italic("SB")+" and "+Description.italic("SC")+", and that aren't 0/0 for "+Description.italic("SA")+" or 1/1 for "+Description.italic("SD"));
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
  }

  @Override
  public String getCustomRequierment() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public void begin() {
    super.begin();
    String[] filters = this.filter.getList();
    samples = new String[filters.length];
    genotypes = new String[filters.length];
    keeps = new boolean[filters.length];
    indices = new int[filters.length];
    
    for (int i = 0; i < filters.length; i++) {
      String[] split = filters[i].split(":");
      samples[i] = split[0];
      genotypes[i] = split[1];
      keeps[i] = Boolean.parseBoolean(split[2]);
      indices[i] = getVCF().getSamples().indexOf(getVCF().getPed().getSample(samples[i]));

      Message.info((keeps[i] ? "KEEP  " : "REMOVE")+" "+genotypes[i]+" for "+samples[i]+"(#"+indices[i]+", column["+(VCF.IDX_SAMPLE+indices[i]+1)+"])");
      if (this.indices[i] == -1)
        this.fatalAndDie("Sample " + samples[i] + " not found in VCF File " + vcffile.getFilename());
    }          
  }

  @Override
  public String[] processInputLineForFilter(String line) {
    String[] fields = line.split(T);
    for (int i = 0; i < this.indices.length; i++) {
      String geno = fields[VCF.IDX_SAMPLE+this.indices[i]].split(":")[0];
      if (geno.equals(this.genotypes[i]) != keeps[i]) //reject if "kept" genotypes mismatch, or "remove" genotypes match
        return NO_OUTPUT;
    }
    return new String[]{line};
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addAnonymousValue("filter", "B00GWAN:0/0:true,B00GWBB:0/1:true,B00GWBU:1/1:true,B00GWBV:0/0:false");
    return new TestingScript[]{scr};
  }
}
