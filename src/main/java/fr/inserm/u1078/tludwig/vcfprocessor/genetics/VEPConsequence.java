package fr.inserm.u1078.tludwig.vcfprocessor.genetics;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) Started : 2016/03/14
 */
public enum VEPConsequence {

  EMPTY(0, "empty", Impact.UNKNOWN),
  //modifier + moderate
  INTERGENIC_VARIANT(1, "intergenic_variant", Impact.MODIFIER),
  FEATURE_TRUNCATION(2, "feature_truncation", Impact.MODIFIER),
  REGULATORY_REGION_VARIANT(3, "regulatory_region_variant", Impact.MODIFIER),
  FEATURE_ELONGATION(4, "feature_elongation", Impact.MODIFIER),
  REGULATORY_REGION_AMPLIFICATION(5, "regulatory_region_amplification", Impact.MODIFIER),
  REGULATORY_REGION_ABLATION(6, "regulatory_region_ablation", Impact.MODERATE),
  TF_BINDING_SITE_VARIANT(7, "TF_binding_site_variant", Impact.MODIFIER),
  TFBS_AMPLIFICATION(8, "TFBS_amplification", Impact.MODIFIER),
  TFBS_ABLATION(9, "TFBS_ablation", Impact.MODIFIER),
  DOWNSTREAM_GENE_VARIANT(10, "downstream_gene_variant", Impact.MODIFIER),
  UPSTREAM_GENE_VARIANT(11, "upstream_gene_variant", Impact.MODIFIER),
  NON_CODING_TRANSCRIPT_VARIANT(12, "non_coding_transcript_variant", Impact.MODIFIER),
  NMD_TRANSCRIPT_VARIANT(13, "NMD_transcript_variant", Impact.MODIFIER),
  INTRON_VARIANT(14, "intron_variant", Impact.MODIFIER),
  NON_CODING_TRANSCRIPT_EXON_VARIANT(15, "non_coding_transcript_exon_variant", Impact.MODIFIER),
  PRIME_3_UTR_VARIANT(16, "3_prime_UTR_variant", Impact.MODIFIER),
  PRIME_5_UTR_VARIANT(17, "5_prime_UTR_variant", Impact.MODIFIER),
  MATURE_MIRNA_VARIANT(18, "mature_miRNA_variant", Impact.MODIFIER),
  CODING_SEQUENCE_VARIANT(19, "coding_sequence_variant", Impact.MODIFIER),
  //low
  SYNONYMOUS_VARIANT(20, "synonymous_variant", Impact.LOW),
  STOP_RETAINED_VARIANT(21, "stop_retained_variant", Impact.LOW),
  START_RETAINED_VARIANT(22, "start_retained_variant", Impact.LOW),
  INCOMPLETE_TERMINAL_CODON_VARIANT(23, "incomplete_terminal_codon_variant", Impact.LOW),
  SPLICE_REGION_VARIANT(24, "splice_region_variant", Impact.LOW),
  //moderate  
  PROTEIN_ALTERING_VARIANT(25, "protein_altering_variant", Impact.MODERATE),
  MISSENSE_VARIANT(26, "missense_variant", Impact.MODERATE),
  INFRAME_DELETION(27, "inframe_deletion", Impact.MODERATE),
  INFRAME_INSERTION(28, "inframe_insertion", Impact.MODERATE),
  //high
  TRANSCRIPT_AMPLIFICATION(29, "transcript_amplification", Impact.HIGH),
  START_LOST(30, "start_lost", Impact.HIGH),
  STOP_LOST(31, "stop_lost", Impact.HIGH),
  FRAMESHIFT_VARIANT(32, "frameshift_variant", Impact.HIGH),
  STOP_GAINED(33, "stop_gained", Impact.HIGH),
  SPLICE_DONOR_VARIANT(34, "splice_donor_variant", Impact.HIGH),
  SPLICE_ACCEPTOR_VARIANT(35, "splice_acceptor_variant", Impact.HIGH),
  TRANSCRIPT_ABLATION(36, "transcript_ablation", Impact.HIGH);

  private final int level;
  private final String name;
  private final Impact impact;

  VEPConsequence(int code, String name, Impact impact) {
    this.level = code;
    this.name = name;
    this.impact = impact;
  }

  public int getLevel() {
    return level;
  }

  public String getName() {
    return name;
  }

  public Impact getImpact() {
    return impact;
  }

  public static String[] getAllConsequences() {
    VEPConsequence[] all = VEPConsequence.values();
    String[] ret = new String[all.length];
    for (int i = 0; i < all.length; i++)
      ret[i] = all[i].getName();
    return ret;
  }

  @Override
  public String toString() {
    return this.getName();
  }

  public boolean matches(String csq) {
    return this.getName().equalsIgnoreCase(csq);
  }

  public static VEPConsequence getConsequence(String csq) {
    if(csq == null || csq.isEmpty())
      return VEPConsequence.EMPTY;
    for (VEPConsequence consequence : VEPConsequence.values())
      if (consequence.matches(csq))
        return consequence;
    return null;
  }

  public static VEPConsequence getConsequence(int l) {
    for (VEPConsequence consequence : VEPConsequence.values())
      if (consequence.getLevel() == l)
        return consequence;
    return null;
  }

  public static int getConsequenceLevel(String csq) {
    VEPConsequence c = getConsequence(csq);
    return c == null ? -1 : c.getLevel();
  }

  public static VEPConsequence getWorstConsequence(Collection<VEPAnnotation> veps, String symbol) {
    ArrayList<String> csqs = new ArrayList<>();
    for (VEPAnnotation vep : veps)
      if (vep.getSYMBOL().equalsIgnoreCase(symbol))
        csqs.add(vep.getConsequence());
    return getWorstConsequence(csqs.toArray(new String[0]));
  }

  public static VEPConsequence getWorst(Collection<VEPAnnotation> veps) {
    ArrayList<String> csqs = new ArrayList<>();
    for (VEPAnnotation vep : veps)
      csqs.add(vep.getConsequence());
    return getWorstConsequence(csqs.toArray(new String[0]));
  }

  public static VEPConsequence getWorstConsequence(VEPAnnotation vep) {
    return getWorstConsequence(vep.getConsequence());
  }

  public static VEPConsequence getWorstConsequence(Collection<String> csqs) {
    return getWorstConsequence(csqs.toArray(new String[0]));
  }

  public static VEPConsequence getWorstConsequence(String... csqs) {
    VEPConsequence worstCsq = VEPConsequence.EMPTY;

    for (String unsplits : csqs)
      for (String csq : unsplits.split("\\&")) {
        VEPConsequence vc = VEPConsequence.getConsequence(csq);

        if (vc != null){
          if (vc.getLevel() > worstCsq.getLevel())
            worstCsq = vc;
        } else
          Message.debug("NULL for "+csq);
      }
    return worstCsq;
  }

  public enum Impact {
    UNKNOWN(0),
    MODIFIER(1),
    LOW(2),
    MODERATE(3),
    HIGH(4);

    private final int level;

    Impact(int level) {
      this.level = level;
    }

    public int getLevel() {
      return level;
    }

    public String getName() {
      return this.name();
    }
  }
}
