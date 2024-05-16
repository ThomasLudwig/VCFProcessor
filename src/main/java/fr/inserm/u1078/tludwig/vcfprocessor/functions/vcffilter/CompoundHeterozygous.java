package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BooleanParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Keeps only variants that respect the Compound Heterozygous pattern of inheritance.
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2016-09-09
 * Checked for release on 2020-08-07
 * Unit Test defined on 2020-08-14
 */
public class CompoundHeterozygous extends AbstractCompoundFunction {

  private final BooleanParameter missing = new BooleanParameter(OPT_MISSING, "Missing genotypes allowed ?"); //TODO Missing should always be allowed, but rejected from the commandline filters
  private int[] cases;
  private int[] controls;

  @Override
  public String getSummary() {
    return "Keeps only variants that respect the Compound Heterozygous pattern of inheritance.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("In the Compound Heterozygous pattern of inheritance, two variants V1 and V2 from the same gene are valid if")
            .addItemize("All cases have V1 and V2",
                "No control have V1 and V2")
            .addLine("Thus, a variant are rejected if")
            .addItemize("one case doesn't have V1 and V2",
                "one control has V1 and V2")
            .addLine("With " + Description.code(missing.getKey() + " true")+", missing genotypes are considered compatible with the transmission pattern.")
            .addLine("The " + Description.code(noHomo.getKey()) + " options allows to reject alternate alleles if a case is homozygous to an alternate allele or if at least one control is not heterozygous to an alternate allele of V1/V2. (If all the controls are supposed to be parents of cases)")
            .addDescription(WARNING);
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    Ped ped = this.getVCF().getPed();
    this.cases = new int[ped.getCases().size()];
    this.controls = new int[ped.getControls().size()];
    if (this.cases.length == 0)
      this.fatalAndQuit("No case sample present");
    if (this.controls.length == 0)
      this.fatalAndQuit("No control sample present");
    int i = 0;
    ArrayList<Sample> samples = new ArrayList<>(this.getVCF().getSamples());
    for (Sample cas : ped.getCases())
      cases[i++] = samples.indexOf(cas);
    i = 0;
    for (Sample control : ped.getControls())
      controls[i++] = samples.indexOf(control);
  }

  /**
   * A variant is NOT a valid candidate for a giving allele, if :
   * - A genotype is missing while it is not allowed
   * - One of the case samples doesn't have this allele
   *
   * @param genos the genotypes
   * @param a the allele number
   * @return true if the variant is a valid candidate
   */
  @Override
  public boolean isValidCandidate(Genotype[] genos, int a) {
    for (int cas : cases) {
      if (genos[cas].isMissing() && !this.missing.getBooleanValue())
        return false;
      if (!genos[cas].hasAllele(a))
        return false;
      if (noHomo.getBooleanValue() && genos[cas].getCount(a) == 2) //reject case homozygous
        return false;
    }
    for(int control : controls){
      if (genos[control].isMissing() && !this.missing.getBooleanValue())
        return false;
    }
    return true;
  }
  
  @Override
   public boolean areCompound(Genotype[] g1, int a, Genotype[] g2, int b) {
    for (int control : controls) {
      int c1 = g1[control].getCount(a);
      int c2 = g2[control].getCount(b);

      if (c1 > 0 && c2 > 0)
        return false; //reject if a control has both alleles
      else if (this.noHomo.getBooleanValue() && (c1 + c2) != 1){
        //if we are here, one of the alleles is 0 and this other is 0, 1 or 2
        //0 -> reject if a control doesn't have one of the two alleles
        //2 -> reject if one of the individuals is homozygous to one of the two alleles
        //thus only keep 1
          return false;
      }
    }
    return true;
  }

  @SuppressWarnings("SpellCheckingInspection")
  @Override
  public TestingScript[] getScripts() {
    ArrayList<TestingScript> scripts = new ArrayList<>();
    for(String setName : new String[]{"2trios", "trio"})
      for(String missingP : new String[]{"false", "true"})
        for(String noHomoP : new String[]{"false", "true"}){
          TestingScript scr = TestingScript.newFileTransform();
          scr.addNamingFilename("vcf", setName+".vcf");
          scr.addAnonymousFilename("ped", setName+".ped");
          scr.addNamingValue("missing", missingP);
          scr.addNamingValue("nohomo", noHomoP);
          scripts.add(scr);
        }
    
    return scripts.toArray(new TestingScript[0]);
  }
}
