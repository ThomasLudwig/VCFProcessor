package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcfannotate;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.AnnotationException;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Info;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * For each variant, add the most severe consequence from vep and add the consequence from vep for the annotation marked as Canonical.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-03-13
 * Checked for release on 2020-05-25
 * Unit Test defined on   2020-08-05
 */
public class AddWorstAndCanonicalConsequence extends ParallelVCFFunction {

  public static final String KEY_WORST_CSQ = "WORSTCSQ";
  public static final String KEY_WORST_GENE = "WORSTGENE";
  public static final String KEY_CANONICAL_CSQ = "CANONICALCSQ";
  public static final String KEY_CANONICAL_GENE = "CANONICALGENE";

  public static final String HEADER_WORST_CSQ = "##INFO=<ID=" + KEY_WORST_CSQ + ",Number=A,Type=String,Description=\"Most Severe vep Consequence for the variant\">";
  public static final String HEADER_WORST_GENE = "##INFO=<ID=" + KEY_WORST_GENE + ",Number=A,Type=String,Description=\"Gene affected by Most Severe vep Consequence for the variant\">";
  public static final String HEADER_CANONICAL_CSQ = "##INFO=<ID=" + KEY_CANONICAL_CSQ + ",Number=A,Type=String,Description=\"Canonical vep Consequence for the variant\">";
  public static final String HEADER_CANONICAL_GENE = "##INFO=<ID=" + KEY_CANONICAL_GENE + ",Number=A,Type=String,Description=\"Gene affected by Canonical vep Consequence for the variant\">";

  @Override
  public String getSummary() {
    return "For each variant, add the most severe consequence from vep and add the consequence from vep for the annotation marked as Canonical.";
  }

  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("The worst consequence is annotated with the keyword " + Description.code(KEY_WORST_CSQ))
            .addLine("The gene for the worst consequence is annotated with the keyword " + Description.code(KEY_WORST_GENE))
            .addLine("The canonical consequence is annotated with the keyword " + Description.code(KEY_CANONICAL_CSQ))
            .addLine("The gene for the canonical consequence is annotated with the keyword " + Description.code(KEY_CANONICAL_GENE))
            .addLine("If more than one annotation is marked as canonical, the most severe of them is kept")
            .addLine("If no annotation is marked as canonical, the most severe consequence is kept");
  }

  @Override
  public boolean needVEP() {
    return true;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ANNOTATION_FOR_ALL;
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
  public String[] processInputLine(String line) {
    String[] f = line.split("\t");
    int nbAllele = 1 + f[VCF.IDX_ALT].split(",").length;
    try {
      Info info = new Info(f[VCF.IDX_INFO], getVCF());
      String worstCsq = "";
      String canonicalCsq = "";
      String worstGene = "";
      String canonicalGene = "";
      for (int a = 1; a < nbAllele; a++) {
        VEPAnnotation worst = info.getWorstVEPAnnotation(a);
        VEPAnnotation canon = info.getCanonicalVEPAnnotation(a);
        worstCsq += "," + worst.getConsequence();
        worstGene += "," + worst.getSYMBOL();
        canonicalCsq += "," + canon.getConsequence();
        canonicalGene += "," + canon.getSYMBOL();
      }
      if (worstCsq.length() == 0)
        worstCsq = ",";
      if (worstGene.length() == 0)
        worstGene = ",";
      if (canonicalCsq.length() == 0)
        canonicalCsq = ",";
      if (canonicalGene.length() == 0)
        canonicalGene = ",";
      return new String[]{VCF.addInfo(line, new String[]{KEY_WORST_CSQ + "=" + worstCsq.substring(1), KEY_WORST_GENE + "=" + worstGene.substring(1), KEY_CANONICAL_CSQ + "=" + canonicalCsq.substring(1), KEY_CANONICAL_GENE + "=" + canonicalGene.substring(1)})};
    } catch (AnnotationException e) {
      e.printStackTrace();
    }
    return NO_OUTPUT;
  }
  
  @Override
  public String[] getExtraHeaders(){
    return new String[]{HEADER_WORST_CSQ, HEADER_WORST_GENE, HEADER_CANONICAL_CSQ, HEADER_CANONICAL_GENE};
  } 
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
