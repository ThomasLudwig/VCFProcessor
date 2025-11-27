package fr.inserm.u1078.tludwig.vcfprocessor.functions.format;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.ConsequenceParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPConsequence;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Creates a TSV file, readable in Excel, keeps only annotations for given genes and consequences
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-06-01
 * Checked for release on 2020-08-20
 * Unit Test defined on 2020-20-20
 */
public class VCF2TSVGeneCsq extends VCF2TSV { //TODO should not exist, use genes/consequence filters for that

  private final FileParameter geneFile = new FileParameter(OPT_GENES, "genes.txt", "Filename of gene list");
  private final ConsequenceParameter leastCsq = new ConsequenceParameter();
  
  
  ArrayList<String> genes;
  int symbolCol;
  int csqCol;

  @Override
  public String getSummary() {
    return "Creates a TSV file, readable in Excel, keeps only annotations for given genes and consequences";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("Creates a TSV file, that can be opened in Excel.")
            .addLine("For each variants, all the VCF fields are displayed.")
            .addLine("All vep annotation are formatted and shown.")
            .addLine("Only the variants impacting a gene within the given list are displayed.")
            .addLine("Only the variants with consequence at least as severe as the one given are displayed.");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.NA); }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    genes = new ArrayList<>();
    try (UniversalReader in = this.geneFile.getReader()){
      String line;
      while ((line = in.readLine()) != null)
        genes.add(line.toLowerCase());
    } catch (IOException e) {
      Message.fatal("Could not read gene list from file "+this.geneFile.getFilename(), e, true);
    }    

    symbolCol = -1;
    csqCol = -1;

    for (int i = 0; i < vepHeaders.length; i++) {
      if (vepHeaders[i].equalsIgnoreCase("consequence"))
        csqCol = i;
      if (vepHeaders[i].equalsIgnoreCase("symbol"))
        symbolCol = i;
    }
  }

  @Override
  public ArrayList<String[]> getVEPs(String[][] info) {
    ArrayList<String[]> veps = super.getVEPs(info);
    int i = 0;
    while (i < veps.size())
      if (valid(veps.get(i)[symbolCol], veps.get(i)[csqCol]))
        i++;
      else
        veps.remove(i);
    return veps;
  }

  @Override
  public boolean keep(VariantRecord ignore, ArrayList<String[]> veps) {
    return !veps.isEmpty();
  }

  private boolean valid(String symbol, String csq) {
    return genes.contains(symbol.toLowerCase()) && VEPConsequence.getWorstConsequence(csq.split("&")).getLevel() >= leastCsq.getConsequenceLevel();
  }

  @Override
  public TestingScript[] getScripts() {
    TestingScript scr = TestingScript.newFileAnalysis();
    scr.addAnonymousFilename("vcf", "vcf");
    scr.addNamingFilename("genes", "genes");
    scr.addNamingValue("csq", "missense_variant");
    
    return new TestingScript[]{scr};
  }  
}
