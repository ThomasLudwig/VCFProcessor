package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import fr.inserm.u1078.tludwig.vcfprocessor.utils.PGPBouncyCastle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ApplyCorrespondenceTable extends ParallelVCFFilterFunction {
  private final TSVFileParameter tableFile = new TSVFileParameter(OPT_TABLE, "MyCorrespondenceTable", "Encrypted file containing the correspondence table");
  private final FileParameter keyFile = new TSVFileParameter("--key", "secret.OIE2S1PK.pgp", "The file containing the secret key");
  private Map<String, String> correspondence;
  private int[] fromTo;

  @Override
  public String getSummary() { return "Applies the Correspondence table to a VCF File"; }

  @Override
  public Description getDesc() { return new  Description(getSummary()); }

  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public void begin() {
    super.begin();
    correspondence = new LinkedHashMap<>();
    try {
      byte[] encryptedTable = PGPBouncyCastle.readEncryptedFile(keyFile.getFilename(), tableFile.getFilename());

      BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(encryptedTable)));

      String line;
      while ((line = in.readLine()) != null) {
        String[] f = line.split("\t");
        correspondence.put(f[1], f[0]);
      }
      in.close();
    } catch(IOException e){
      Message.die("Could not read Correspondence Table");
    }
  }

  @Override
  public String[] getHeaders() {
    String[] ret = super.getHeaders();
    String[] last = ret[ret.length - 1].split("\t");
    ArrayList<String> samples = new ArrayList<>();
    for(int i = 9; i < last.length; i++) {
      if(!correspondence.containsKey(last[i]))
        Message.die("Correspondence Table contains no correspondence for sample ["+last[i]+"]");
      samples.add(last[i]);
    }

    fromTo = new int[samples.size()];

    ArrayList<String> remove =  new ArrayList<>();
    for(String sample : correspondence.keySet())
      if(!samples.contains(sample))
        remove.add(sample);

    for(String r : remove)
      correspondence.remove(r);

    ArrayList<String> newOrder = new ArrayList<>();

    int j = 0;
    for(String sample : correspondence.keySet()) {
      last[9 + j++] = correspondence.get(sample);
      newOrder.add(sample);
    }

    for(int i = 0; i < samples.size(); i++)
      fromTo[i] = newOrder.indexOf(samples.get(i));

    ret[ret.length - 1] = String.join("\t", last);
    return ret;
  }

  @Override
  public String[] processInputRecordForFilter(VariantRecord record) {
    String[] f = record.asFields();
    String[] ret = new String[f.length];
    System.arraycopy(f, 0, ret, 0, VCF.IDX_INFO);
    ret[VCF.IDX_INFO] = processInfo(f[VCF.IDX_INFO]);
    ret[VCF.IDX_FORMAT] = f[VCF.IDX_FORMAT];

    for(int i = 0 ; i < fromTo.length; i++)
      ret[VCF.IDX_SAMPLE + fromTo[i]] = f[VCF.IDX_SAMPLE + i];

    return new String[]{String.join("\t", ret)};
  }

  public String processInfo(String info) {
    String ret = info;
    for(String from : correspondence.keySet()) {
      String to = correspondence.get(from);
      info = info.replaceAll(
          "(?<![A-Za-z0-9])" + Pattern.quote(from) + "(?![A-Za-z0-9])",
          to
      );
    }
    return ret;
  }

  @Override
  public TestingScript[] getScripts() { return new TestingScript[0]; }
}
