package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PositiveIntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started : 20 oct. 2015
 */
public class RandomPed extends Function {

  private final PedFileParameter pedFile = new PedFileParameter(OPT_PED, "samples.ped", "The input PED file to process");
  private final PositiveIntegerParameter number = new PositiveIntegerParameter(OPT_THRESHOLD, "Number Of Samples");

  @Override
  public String getSummary() {
    return "Keeps N random samples from a Ped File";
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary());
  }

  @Override
  public String getOutputExtension() {
    return OUT_PED;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    int nbLine = 0;
    UniversalReader in = this.pedFile.getReader();
    String line;
    ArrayList<Integer> total = new ArrayList<>();
    while (in.readLine() != null) {
      nbLine++;
      total.add(nbLine);
    }
    in.close();
    ArrayList<Integer> keep = new ArrayList<>();
    for (int i = 0; i < this.number.getIntegerValue(); i++) {
      int index = (int) (Math.random() * total.size());
      keep.add(total.remove(index));
    }
    nbLine = 0;
    in = this.pedFile.getReader();
    while ((line = in.readLine()) != null) {
      nbLine++;
      if (keep.contains(nbLine))
        println(line);
    }
    in.close();
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty(); //TODO implement
  }
}
