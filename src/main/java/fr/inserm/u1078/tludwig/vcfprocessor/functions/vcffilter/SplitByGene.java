package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.OutputDirectoryParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Creates an output VCF file for each gene.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-10-08
 * Checked for release on 2020-08-07
 * Unit Test defined on   2020-08-07
 */
public class SplitByGene extends VCFFunction { //TODO parallelize

  private final OutputDirectoryParameter dir = new OutputDirectoryParameter();

  @Override
  public String getOutputExtension() {
    return OUT_NONE;
  }

  @Override
  public String getSummary() {
    return "Creates an output VCF file for each gene.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Some variants can be in several output files, if they impact several genes.");
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return true;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FILTER_ONE;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    VCF vcf = this.vcfFile.getVCF();
    vcf.getReaderAndStart();
    //ArrayList<String> allGenes = new ArrayList<>();
    TreeMap<String, ArrayList<String>> variantsByGenes = new TreeMap<>();

    Variant variant;
    while ((variant = vcf.getUnparallelizedNextVariant()) != null) {
      ArrayList<String> localGenes = new ArrayList<>();
      for (String gene : variant.getGeneList())
        if (!localGenes.contains(gene)) {
          if (variantsByGenes.containsKey(gene)) {
            variantsByGenes.get(gene).add(variant.toString());
          } else {
            ArrayList<String> lines = new ArrayList<>();
            lines.add(variant.toString());
            variantsByGenes.put(gene, lines);
          }
          localGenes.add(gene);
        }
    }

    for (String gene : variantsByGenes.navigableKeySet()) {
      PrintWriter out = getPrintWriter(this.dir.getDirectory() + File.separator + gene + ".vcf");
      vcf.printHeaders(out);
      for (String l : variantsByGenes.get(gene))
        out.println(l);
      out.close();
    }
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newDirectoryTransform();
    scr.addAnonymousFilename("vcf", "vcf");
    return new TestingScript[]{scr};
  }
}
