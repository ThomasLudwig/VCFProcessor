package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Ped;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Sample;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Keeps only variants that strictly respect the Compound Heterozygous pattern of inheritance.
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-08-11
 * Checked for release on 2020-08-11
 * Unit Test defined on 2020-08-13
 */
public class StrictCompoundHeterozygous extends AbstractCompoundFunction {

  private static final int MOTHER = 0;
  private static final int FATHER = 1;
  private int[] cases;
  private int[][] parents;

  @Override
  public String getSummary() {
    return "Keeps only variants that strictly respect the Compound Heterozygous pattern of inheritance.";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("In the Compound Heterozygous pattern of inheritance, two variants V1 and V2 from the same gene are valid if")
            .addItemize("All cases have V1 and V2 from their parents",
                "All controls (parents) have one of V1/V2 while the other parents have V2/V1")
            .addLine("A variant is kept if and only if :")
            .addItemize("All case have V1 and V2",
                "All cases have a parent (control) with V1 and not V2, and this other parent with V2 and not V1")
            .addLine("The " + Description.code(noHomo.getKey()) + " options allows to reject alternate alleles if a sample is homozygous to it.")
            .addDescription(WARNING);
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return "This function expects a complete definition of the sample, where all cases are affected children and both their parents are identified controls.";
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    Ped ped = this.getVCF().getPed();
    this.cases = new int[ped.getCases().size()];
    this.parents = new int[ped.getCases().size()][2];
    if (this.cases.length == 0)
      Message.die("No case sample present");
    int i = 0;
    for (Sample cas : ped.getCases()) {
      int c = this.getVCF().indexOfSample(cas);
      int mid = this.getVCF().indexOfSample(cas.getMid());
      int pid = this.getVCF().indexOfSample(cas.getPid());
      if (mid == -1)
        Message.die("No mother found for case [" + cas + "]");
      if (pid == -1)
        Message.die("No father found for case [" + cas + "]");
      cases[i] = c;
      parents[i] = new int[]{mid, pid};
      Message.verbose("Added trio : " + cas.getFid() + " " + cas.getId() + " [" + cas.getMid() + "|" + cas.getPid() + "]");
      i++;
    }
  }

  @Override
  public boolean isValidCandidate(Genotype[] genos, int a) {
    for (int c = 0; c < this.cases.length; c++) {
      if (genos[cases[c]].isMissing() || genos[parents[c][MOTHER]].isMissing() || genos[parents[c][FATHER]].isMissing()) //Removing missing
        return false;

      int cc = genos[cases[c]].getCount(a);
      int cm = genos[parents[c][MOTHER]].getCount(a);
      int cf = genos[parents[c][FATHER]].getCount(a);
      if (this.noHomo.getBooleanValue()) {
        if (cc != 1) //absence or homo
          return false;
        if (cm == 2 || cf == 2)  //homo
          return false;
      } else if (cc < 1) //absence
        return false;
      //present for one parent and not the other
      if (cm < 1 && cf < 1) //no parent with allele
        return false;
      if (cm > 0 && cf > 0) //both parents with allele
        return false;
    }

    return true;
  }

  @Override
  public boolean areCompound(Genotype[] g1, int a, Genotype[] g2, int b) {
    //No need to check that child has both alleles, if we enter this function is already true
    for (int c = 0; c < this.cases.length; c++) {
      int cm1 = g1[parents[c][MOTHER]].getCount(a);
      int cm2 = g2[parents[c][MOTHER]].getCount(b);
      int cf1 = g1[parents[c][FATHER]].getCount(a);
      int cf2 = g2[parents[c][FATHER]].getCount(b);

      if (cm1 > 0 && cm2 > 0)  //Mother has both alleles
        return false;
      if (cf1 > 0 && cf2 > 0) //Father has both alleles
        return false;
      if (cm1 < 1 && cm2 < 1) //Mother has no allele
        return false;
      if (cf1 < 1 && cf2 < 1) //Father has no allele
        return false;
    }
    return true;
  }

  @Override
  public TestingScript[] getScripts() {
    ArrayList<TestingScript> scripts = new ArrayList<>();
    for(String setName : new String[]{"2trios", "trio", "fake"})
      for(String noHomoP : new String[]{"false", "true"}){
         TestingScript scr = TestingScript.newFileTransform();
         scr.addNamingFilename("vcf", setName+".vcf");
         scr.addAnonymousFilename("ped", setName+".ped");
         scr.addNamingValue("nohomo", noHomoP);
         scripts.add(scr);
       }
    
    return scripts.toArray(new TestingScript[0]);
  }
}
