package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

public class Variant2Canonical extends Function {
  FileParameter file = new FileParameter(OPT_FILE, "my.variants.txt", "List of variants, without headers, as chr:pos:ref:alt");

  @Override
  public String getSummary() {
    return "Convert a file with variants as chr:pos:ref:alt to a canonical file";
  }

  @Override
  public Description getDescription() {
    return new Description(getSummary());
  }

  @Override
  public String getOutputExtension() {
    return OUT_TXT;
  }

  @Override
  public void executeFunction() throws Exception {
    UniversalReader in = file.getReader();
    String line;
    while ((line = in.readLine()) != null) {
      String[] f = line.split(":");
      String chr = f[0];
      int pos = Integer.parseInt(f[1]);
      String ref = f[2];
      String alt = f[3];
      println(new Canonical(chr, pos, ref, alt));
    }
    in.close();
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }
}
