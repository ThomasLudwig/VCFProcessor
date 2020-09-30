package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Filters the variants to keep only those affect one of the given genes with one of the given consequences.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2016-04-25
 * Checked for release on 2020-08-06
 * Unit Test defined on   2020-08-06
 */
public class FilterGeneCsqList extends ParallelVCFVariantFilterFunction {

  private ArrayList<String> genes;
  private final FileParameter genefile = new FileParameter(OPT_GENES, "genes.txt", "List of the genes to keep");
  private final StringParameter effects = new StringParameter(OPT_CSQ, "csq1,csq2,...,csqN", "List (comma separated) of VEP consequences to keep");

  @Override
  public String getSummary() {
    return "Filters the variants to keep only those affect one of the given genes with one of the given consequences.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("If the variants has at least one of the effect from " + Description.code(genefile.getKey()) + " on one of the genes in the file from " + Description.code(this.effects.getKey()) + ", then the variants is kept.")
            .addLine("The list of effects can be empty : " + Description.code(this.effects.getKey()+ " null"))
            .addLine("VEP consequence must be selected from : ["+String.join(" | ", VEPConsequence.getAllConsequences())+"]");
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
    try {
      this.genes = new ArrayList<>();
      UniversalReader in = this.genefile.getReader();
      String gene;
      while ((gene = in.readLine()) != null)
        this.genes.add(gene);
    } catch (IOException e) {
      this.fatalAndDie("Could not read gene file : "+this.genefile.getFilename());
    }
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    for (VEPAnnotation annot : variant.getInfo().getAllVEPAnnotations())
      for (String gene : genes)
        if (gene.equalsIgnoreCase(annot.getSYMBOL())) {
          String efs = this.effects.getStringValue();
          if (efs == null || efs.length() < 1 || efs.equalsIgnoreCase("null"))
            return asOutput(variant);
          for (String effect : efs.split(",", -1))
            if (annot.getConsequence().contains(effect))
              return asOutput(variant);
        }
    return NO_OUTPUT;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addNamingFilename("genes", "ATPgenes");
    scr.addNamingValue("csq", "5_prime_UTR_variant,3_prime_UTR_variant");
    return new TestingScript[]{scr};
  }
}
