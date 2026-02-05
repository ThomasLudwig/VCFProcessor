package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

//TODO encrypt the table

public class GenerateCorrespondenceTable extends VCFFunction {
  private final StringParameter study = new StringParameter("--study", "PG", "2-3 letters symboizing the target study");
  private final StringParameter ref = new StringParameter("--ref", "1kG", "2-3 letters symboizing the reference panel to anonymize");

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @Override
  public String getSummary() {
    return "Generate a Correspondence Table between a reference panel and an anonymized version";
  }

  @Override
  public Description getDesc() {
    return new Description(getSummary());
  }

  @Override
  public VCFPolicies getVCFPolicies() {
    return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA);
  }

  @Override
  public void executeFunction() throws Exception {
    String prefix = study.getStringValue()+"_"+ref.getStringValue()+"_";
    Random random = new Random();
    VCF vcf = this.vcfFile.getVCF();
    ArrayList<String> samples = new ArrayList<>();
    ArrayList<String> hashes = new ArrayList<>();
    for(Sample sample : vcf.getPed().getSamples()) {
      int i = random.nextInt(samples.size() + 1);
      samples.add(i, sample.getId());
      hashes.add(i, getHash(hashes));
    }

    for(int i = 0; i < samples.size(); i++)
      System.out.println(prefix+hashes.get(i)+"\t"+samples.get(i));
  }

  public static String getHash(Collection<String> hashes) {
    String hash;
    do
      hash = getHash();
    while(hashes.contains(hash));

    return hash;
  }

  public static String getHash() {
    Random random = new Random();
    final String source = "0123456789"
        + "abcdefghijklmnopqrstuvwxyz"
        + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    int l = source.length();
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < 4; i++)
      sb.append(source.charAt(random.nextInt(l)));
    return sb.toString();
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }
}
