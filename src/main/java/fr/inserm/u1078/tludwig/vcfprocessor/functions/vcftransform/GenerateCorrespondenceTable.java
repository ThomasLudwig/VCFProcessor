package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.PGPBouncyCastle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

//TODO encrypt the table

public class GenerateCorrespondenceTable extends VCFFunction {
  private final StringParameter study = new StringParameter("--study", "PG", "2-3 letters symboizing the target study");
  private final StringParameter ref = new StringParameter(OPT_REF, "1kG", "2-3 letters symboizing the reference panel to anonymize");
  private final TSVFileParameter table = new TSVFileParameter(OPT_TABLE, "mytable.PGP.tsv", "The encrypted file containing the correspondence table");

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

    //BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(encryptedTable)));
    StringBuilder out  = new StringBuilder();
    for(int i = 0; i < samples.size(); i++)
      out.append("\n").append(prefix+hashes.get(i)+"\t"+samples.get(i));

    PGPBouncyCastle.writeEncryptedFile(table.getFilename(), out.substring(1).getBytes());
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
