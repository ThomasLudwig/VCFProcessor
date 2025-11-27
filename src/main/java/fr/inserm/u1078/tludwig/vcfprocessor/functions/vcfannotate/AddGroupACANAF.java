package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcfannotate;

import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Add AN,AC,AF annotation for each group described in the ped file
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-03-13
 * Checked for release on 2020-05-25
 * Unit Test defined on   2020-08-05
 */
public class AddGroupACANAF extends ParallelVCFVariantPedFunction<Object> {

  @Override
  public String getSummary() {
    return "Add AN,AC,AF annotation for each group described in the ped file";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description("For each group "+Description.bold("G")+", the info field has new annotations")
            .addItemize(Description.code("G_AN")+" AlleleNumber for this group",
                Description.code("G_AC")+" AlleleCounts for this group",
                Description.code("G_AF")+" AlleleFrequencies for this group");
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.ANNOTATION_FOR_ALL); }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getExtraHeaders(){
    ArrayList<String> groups = getPed().getGroups();
    String[] headers = new String[groups.size() * 3];
    for (int i = 0; i < groups.size(); i++) {
      String group = groups.get(i);
      headers[3 * i + 0] = "##INFO=<ID=" + group + "_AC,Number=A,Type=Integer,Description=\"Allele count in genotypes, for each ALT allele for group " + group + ", in the same order as listed\">";
      headers[3 * i + 1] = "##INFO=<ID=" + group + "_AF,Number=A,Type=Float,Description=\"Allele Frequency, for each ALT allele for group " + group + ", in the same order as listed\">";
      headers[3 * i + 2] = "##INFO=<ID=" + group + "_AN,Number=1,Type=Integer,Description=\"Total number of alleles in called genotypes for group " + group + "\">";
    }
    return headers;
  }

  private String[] getAnnotation(ArrayList<String> groups, Variant v) {
    String[] ret = new String[3*groups.size()];
    int[][] ac = new int[groups.size()][v.getAlleleCount()];
    int[] an = new int[groups.size()];

    for (Genotype g : v.getGenotypes()){
      int i = groups.indexOf(g.getSample().getGroup());    
      int[] as = g.getAlleles();
      if (as != null) //null indicates missing
        for (int a : as)
          if(a > -1){
            an[i]++;
            ac[i][a]++;
          }
    }
    for (int i = 0; i < groups.size(); i++) {
      String group = groups.get(i);
      ret[3*i+0] = group + "_AC=" + ac[i][1];
      for (int a = 2; a < ac[i].length; a++)
        ret[3*i+0] += "," + ac[i][a];      
      ret[3*i+1] = group + "_AF=" + (ac[i][1] / (1.0d * an[i]));
      for (int a = 2; a < ac[i].length; a++)
        ret[3*i+1] += "," + (ac[i][a] / (1.0d * an[i]));      
      ret[3*i+2] = group + "_AN=" + an[i];
    }
    return ret;
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    variant.addInfo(getAnnotation(getPed().getGroups(), variant));
    return asOutput(variant);
  }

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFPedTransformScript();
  }
}
