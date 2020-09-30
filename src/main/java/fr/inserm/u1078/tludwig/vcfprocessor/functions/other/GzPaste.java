package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ListParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2018-03-19
 */
public class GzPaste extends Function {

  private final ListParameter filenames = new ListParameter(OPT_FILES, "file1.gz,file2.gz,...,fileN.gz", "list (comma separated) of gzipped files to paste");

  @Override
  public String getSummary() {
    return "Unix paste command for gzipped files";
  }

  @Override
  public Description getDescription() {
    return new Description("Equivalent to the unix paste command without any special option.")
            .addLine("Each input file can be either gzipped or not (mix are possible)")
            .addLine("Use "+Description.code("--gz")+" to gzip the output");
  }

  @Override
  public String getOutputExtension() {
    return OUT_TXT;
  }

  @Override
  public void executeFunction() throws Exception {
    String[] fns = filenames.getList();
    UniversalReader[] ins = new UniversalReader[fns.length];
    for (int i = 0; i < fns.length; i++)
      ins[i] = new UniversalReader(fns[i]);

    String line;
    int read = 0;
    while ((line = ins[0].readLine()) != null) {
      LineBuilder out = new LineBuilder(line);
      for (int i = 1; i < fns.length; i++)
        out.addColumn(ins[i].readLine());
      if ((read++ % 100000) == 0)
        Message.progressInfo("Read : " + read);
      println(out.toString());
    }

    for (int i = 0; i < fns.length; i++)
      ins[i].close();
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty(); //TODO implement
  }
}
