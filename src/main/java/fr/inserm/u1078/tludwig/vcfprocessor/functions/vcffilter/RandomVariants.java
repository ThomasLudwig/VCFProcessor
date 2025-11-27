package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.RatioParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.util.ArrayList;

/**
 * kept only a portion of the variants from a VCF file.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2017-11-17
 * Checked for release on 2020-08-05
 * Unit Test defined on   xxxx-xx-xx
 */
public class RandomVariants extends ParallelVCFFilterFunction {

  private final RatioParameter probability = new RatioParameter(OPT_RATIO, "Probability of keeping each variant");
  private final FileParameter keepPosition = new FileParameter(OPT_FILE, "positions.txt", "File listing Positions to keep regardless of given probability in format chr:position");

  ArrayList<String> positions;

  @Override
  public String getSummary() {
    return "kept only a portion of the variants from a VCF file.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Each line has a "+Description.code(this.probability.getKey()) + " chance of being kept.")
            .addLine("Position listed in the file " + Description.code(this.keepPosition.getKey()) + " are always kept");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA); }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    positions = new ArrayList<>();
    try (UniversalReader in = this.keepPosition.getReader()){
      String line;
      while ((line = in.readLine()) != null)
        if (!line.trim().isEmpty())
          positions.add(line);
    } catch (IOException e) {
      Message.error("Could not read positions from "+this.keepPosition.getFilename());
    }
  }

  @Override
  public String[] processInputRecordForFilter(VariantRecord record) {
    if (positions.contains(record.getID()) || positions.contains(record.getChrom() + ":" + record.getPos()))
      return new String[]{record.toString()};
    return Math.random() < this.probability.getFloatValue() ? new String[]{record.toString()} : NO_OUTPUT;
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getEmpty(); //Impossible to test, has it is random
  }
}
