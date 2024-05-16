package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PositiveIntegerParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.PedFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 18 nov. 2015
 */
public class NormalizePed extends Function {

  private final PedFileParameter pedFile = new PedFileParameter(OPT_PED, "samples.ped", "The input PED file to process");
  private final PositiveIntegerParameter nbGroups = new PositiveIntegerParameter(OPT_NUMBER, "Number Of subgroups for each group");
  private final PositiveIntegerParameter groupSize = new PositiveIntegerParameter(OPT_SIZE, "Group Size");

  @Override
  public String getSummary() {
    return "Extract x subgroups of y samples for each group present in the Ped file";
  }

  @Override
  public Description getDescription() {
    return new Description(this.getSummary())
            .addLine("If the input ped file has three groups A,B,C of 50 individuals each. Using the command with " + Description.code(nbGroups.getKey() + " 3 " + groupSize.getKey() + " 10")+" will create 9 group :")
            .addLine("A A2 A3 B B2 B3 C C2 C3, with 10 individuals in each, randomly picked from groups A B and C.")
            .addLine("This function is useful to dived groups, for instance to have 1 learning set and several computing sets.");
  }

  @Override
  public String getOutputExtension() {
    return OUT_PED;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    Ped ped = this.pedFile.getPed();
    ArrayList<String> groups = ped.getGroups();

    for (String group : groups)
      process(group, ped);
  }

  private void process(String groupName, Ped ped) {
    int g = ped.getGroupIndex(groupName);
    ArrayList<Sample> samples = ped.getSamplesByGroup()[g];

    String newGroupName = groupName;
    for (int i = 1; i <= this.nbGroups.getIntegerValue(); i++) {
      if (i > 1)
        newGroupName = groupName + i;
      for (int s = 0; s < this.groupSize.getIntegerValue(); s++) {
        int r = (int) (Math.random() * samples.size());
        if (r == samples.size())
          r--;
        Sample sample = samples.remove(r);
        Sample out = new Sample(sample.getFid(), sample.getId(), sample.getPid(), sample.getMid(), sample.getSex(), sample.getPhenotype(), newGroupName);
        println(out.toString());
      }
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty(); //TODO implement
  }
}
