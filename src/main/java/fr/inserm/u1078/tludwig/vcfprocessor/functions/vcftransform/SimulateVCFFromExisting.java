package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulate a VCF File from an existing VCF File be mixing genotypes of samples from different ancestries
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2023-07-10
 * Checked for release on XXXX-XX-XX
 * Unit Test defined on   XXXX-XX-XX
 */
public class SimulateVCFFromExisting extends ParallelVCFPedFunction<Boolean> {

  //Parameters
  final TSVFileParameter variantFile = new TSVFileParameter(OPT_TSV, "variantlist.tsv", "File containing the list of biallelic variants with affected gene");
  //Global data
  private final HashMap<String, String> variants2Genes = new HashMap<>();
  private final HashMap<String, Integer> geneSizes = new HashMap<>();
  private final HashMap<String, Integer> replacementsPerGenes = new HashMap<>();
  private final HashMap<Sample, Sample> replacements = new HashMap<>();
  private final ArrayList<Sample> replacementOrder = new ArrayList<>();

  final AtomicInteger nbKept = new AtomicInteger(0);
  final AtomicInteger nbDropped = new AtomicInteger(0);

  @Override
  public String getSummary() {
    return "Simulate a VCF File from an existing VCF File be mixing genotypes of samples from different ancestries";
  }
  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(getSummary());
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.DROP); }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }
  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    loadReplacements();
    loadGenes();
    computeReplacementsPerGenes();
    Message.info("End of Preparation");
  }
  private void computeReplacementsPerGenes() {
    Message.info("Computing number of replacement Samples for each gene");
    final ArrayList<String> sortedGenes = new ArrayList<>();
    for(String gene : this.geneSizes.keySet()){
      final int size = this.geneSizes.get(gene);
      boolean added = false;
      for(int i = 0; i < sortedGenes.size(); i++){
        final int currentSize = this.geneSizes.get(sortedGenes.get(i));
        if(size < currentSize) {
          sortedGenes.add(i, gene);
          added = true;
          break;
        }
      }
      if(!added)
        sortedGenes.add(gene);
    }
    int i = -1;
    final int R = replacements.size()+1;
    for(String gene : sortedGenes){
      i++;
      this.replacementsPerGenes.put(gene,  i%R);
    }
  }

  /**
   * Load the Replacement Sample for each Case Sample
   */
  private void loadReplacements() {
    Message.info("Loading samples.");
    final HashMap<String, Sample> cases = new HashMap<>();
    final HashMap<String, Sample> replace = new HashMap<>();

    for(Sample sample : this.getVCF().getSortedSamples()) {
      final String status = sample.getFid().toLowerCase();
      final String index = status.replace("control", "").replace("case", "").replace("replacement", "");
      if(status.startsWith("case"))
        cases.put(index, sample);
      else if(status.startsWith("replacement"))
        replace.put(index, sample);
    }
    if(cases.size() != replace.size()){
      Message.die("Number of case samples ["+cases.size()+"] and replacement samples ["+replace.size()+"] mismatch");
    }

    for(String index : cases.keySet()){
      final Sample c = cases.get(index);
      final Sample r = replace.get(index);
      if(r == null)
        Message.die("Cannot find a replacement Samples for case ["+c.toString()+"]");
      replacements.put(c, r);

      //Sort Replacements
      boolean added = false;
      final int fid = Integer.parseInt(c.getFid().replace("case",""));
      for(int i = 0; i < replacementOrder.size(); i++){
        final int currentFID = Integer.parseInt(replacementOrder.get(i).getFid().replace("case",""));
        if(fid < currentFID) {
          replacementOrder.add(i, c);
          added = true;
          break;
        }
      }
      if(!added)
        replacementOrder.add(c);
    }
    Message.info("Loaded "+replacements.size()+" replacement samples");
    for(Sample s : replacementOrder){
      final Sample re = replacements.get(s);
      Message.verbose("\t"+s.getFid()+"["+s.getId()+"] ==> "+ re.getFid()+"["+re.getId()+"]");
    }
  }

  /**
   * Loads each Variant and affect its associated gene. Update the number of a variants per gene.
   */
  private void loadGenes() {
    Message.info("Loading variants/genes.");
    try(final UniversalReader in = variantFile.getReader()) {
      String line;
      while ((line = in.readLine()) != null) {
        final String[] f = line.split("\t");
        if (f.length < 5)
          Message.warning("Problem with line [" + line + "]");
        else {
          final String key = new Canonical(f[0], Integer.parseInt(f[1]), f[2], f[3]).toString();
          final String gene = f[4];
          this.variants2Genes.put(key, gene);
          geneSizes.put(gene, geneSizes.getOrDefault(gene, 0) + 1);
        }
      }
    } catch(IOException e){
      Message.die("Could not load variant list from ["+variantFile.getFilename()+"]");
    }
    Message.info("Loaded "+variants2Genes.size()+" variants for "+geneSizes.size()+" genes.");
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    if(variant.isBiallelic() && variant.isSNP(1)) {
      //Get Gene
      final Canonical canonical = variant.getCanonical(1);
      final String gene = variants2Genes.get(canonical.toString());
      final int nbReplace = replacementsPerGenes.get(gene);
      //Get List of original/replacement samples
      for (int i = 0; i < nbReplace; i++) {
        //Replace genotypes
        final Sample original = replacementOrder.get(i);
        final Sample replacement = replacements.get(original);
        variant.getGenotype(original).setTo(variant.getGenotype(replacement));
      }
      //Remove replacement samples
      for (Sample repl : replacements.values())
        variant.getGenotype(repl).setToMissing();

      //Check it is still a variant
      variant.recomputeACAN();
      //push KEPT/DROPPED
      if (variant.getAC()[1] > 0) {
        this.pushAnalysis(true);
        return new String[]{variant.toString()};
      }
    }
    this.pushAnalysis(false);
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Boolean kept) {
    if(kept)
      this.nbKept.incrementAndGet();
    else
      this.nbDropped.incrementAndGet();
  }

  @SuppressWarnings("unused")
  @Override
  public void end() {
    super.end();
    final int total = this.nbKept.get() + this.nbDropped.get();
    Message.info("Kept variants "+this.nbKept.get()+"/"+total);
    Message.info("Dropped variants "+this.nbDropped.get()+"/"+total);
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }
}
