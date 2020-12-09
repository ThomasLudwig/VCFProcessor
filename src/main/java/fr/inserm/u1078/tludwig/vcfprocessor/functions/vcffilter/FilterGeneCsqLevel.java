package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ConsequenceParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Filters the variants according to their consequences on a list of genes.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-03-12
 * Checked for release on 2020-08-06
 * Unit Test defined on   2020-08-06
 */
public class FilterGeneCsqLevel extends ParallelVCFVariantFilterFunction {

  private final FileParameter geneFile = new FileParameter(OPT_GENES, "genes.txt", "File listing genes to keep");
  private final ConsequenceParameter leastCsq = new ConsequenceParameter();

  private ArrayList<String> genes;

  @Override
  public String getSummary() {
    return "Filters the variants according to their consequences on a list of genes.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Only the variants impacting at least one of the genes in the list are kept.")
            .addLine("The consequence of the variant on the gene must be at least as severe as the one given");
  }

  @Override
  public boolean needVEP() {
    return true;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FILTER_ONE;
  }

  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public void begin() {
    super.begin();
    genes = new ArrayList<>();
    String line;
    try {
      UniversalReader in = this.geneFile.getReader();
      while ((line = in.readLine()) != null)
        genes.add(line.split(T)[0]);
      in.close();
    } catch (IOException e) {
      this.fatalAndDie("Unable to read gene file : " + this.geneFile.getFilename());
    }
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    for (VEPAnnotation annot : variant.getInfo().getAllVEPAnnotations())
      for (String gene : genes)
        if (gene.equalsIgnoreCase(annot.getSYMBOL()) && VEPConsequence.getWorstConsequence(annot).getLevel() >= this.leastCsq.getConsequenceLevel())
          return asOutput(variant);
    return NO_OUTPUT;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addNamingFilename("genes", "ATPgenes");
    scr.addNamingValue("csq", "missense_variant");
    return new TestingScript[]{scr};
  }
}
