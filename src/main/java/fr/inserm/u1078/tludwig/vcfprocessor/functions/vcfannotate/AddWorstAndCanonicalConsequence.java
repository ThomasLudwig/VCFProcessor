package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcfannotate;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Info;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.VEPAnnotation;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * For each variant, add the most severe VEP consequence and add the VEP consequence for the annotation marked as Canonical.
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-03-13
 * Checked for release on 2020-05-25
 * Unit Test defined on   2020-08-05
 */
public class AddWorstAndCanonicalConsequence extends ParallelVCFFunction<Object> {

  public static final String KEY_WORST_CSQ = "WORST_CSQ";
  public static final String KEY_WORST_GENE = "WORST_GENE";
  public static final String KEY_CANONICAL_CSQ = "CANONICAL_CSQ";
  public static final String KEY_CANONICAL_GENE = "CANONICAL_GENE";

  public static final String HEADER_WORST_CSQ = "##INFO=<ID=" + KEY_WORST_CSQ + ",Number=A,Type=String,Description=\"Most Severe vep Consequence for the variant\">";
  public static final String HEADER_WORST_GENE = "##INFO=<ID=" + KEY_WORST_GENE + ",Number=A,Type=String,Description=\"Gene affected by Most Severe vep Consequence for the variant\">";
  public static final String HEADER_CANONICAL_CSQ = "##INFO=<ID=" + KEY_CANONICAL_CSQ + ",Number=A,Type=String,Description=\"Canonical vep Consequence for the variant\">";
  public static final String HEADER_CANONICAL_GENE = "##INFO=<ID=" + KEY_CANONICAL_GENE + ",Number=A,Type=String,Description=\"Gene affected by Canonical vep Consequence for the variant\">";

  @Override
  public String getSummary() {
    return "For each variant, add the most severe consequence from vep and add the consequence from vep for the annotation marked as Canonical.";
  }

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return true;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_ANNOTATION_FOR_ALL;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @Override
  public String[] processInputRecord(VariantRecord record) {
    int nbAllele = 1 + record.getAlts().length;
    Info info = new Info(record.getInfo(), getVCF());
    StringBuilder worstCsq = new StringBuilder();
    StringBuilder canonicalCsq = new StringBuilder();
    StringBuilder worstGene = new StringBuilder();
    StringBuilder canonicalGene = new StringBuilder();
    for (int a = 1; a < nbAllele; a++) {
      VEPAnnotation worst = info.getWorstVEPAnnotation(a);
      VEPAnnotation canon = info.getCanonicalVEPAnnotation(a);
      worstCsq.append(",").append(worst.getConsequence());
      worstGene.append(",").append(worst.getSYMBOL());
      canonicalCsq.append(",").append(canon.getConsequence());
      canonicalGene.append(",").append(canon.getSYMBOL());
    }
    if (worstCsq.length() == 0)
      worstCsq = new StringBuilder(",");
    if (worstGene.length() == 0)
      worstGene = new StringBuilder(",");
    if (canonicalCsq.length() == 0)
      canonicalCsq = new StringBuilder(",");
    if (canonicalGene.length() == 0)
      canonicalGene = new StringBuilder(",");

    record.addInfo(KEY_WORST_CSQ, worstCsq.substring(1));
    record.addInfo(KEY_WORST_GENE, worstGene.substring(1));
    record.addInfo(KEY_CANONICAL_CSQ, canonicalCsq.substring(1));
    record.addInfo(KEY_CANONICAL_GENE, canonicalGene.substring(1));
    return new String[]{record.toString()};
  }
  
  @SuppressWarnings("unused")
  @Override
  public String[] getExtraHeaders(){
    return new String[]{HEADER_WORST_CSQ, HEADER_WORST_GENE, HEADER_CANONICAL_CSQ, HEADER_CANONICAL_GENE};
  } 

  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
