package fr.inserm.u1078.tludwig.vcfprocessor.functions;

public class VCFPolicies {
  private final boolean needVEP;
  private final String[] customRequirements;
  private final MultiAllelicPolicy multiAllelicPolicies;
  public VCFPolicies(MultiAllelicPolicy multiAllelicPolicies, boolean needVEP, String... customRequirements) {
    this.multiAllelicPolicies = multiAllelicPolicies;
    this.needVEP = needVEP;
    this.customRequirements = customRequirements ==  null ? new String[0] : customRequirements;
  }

  public static VCFPolicies nothing(MultiAllelicPolicy multiAllelicPolicies) { return new VCFPolicies(multiAllelicPolicies, false); }
  public static VCFPolicies onlyVEP(MultiAllelicPolicy multiAllelicPolicies) { return new VCFPolicies(multiAllelicPolicies, true); }

  public boolean isNeedVEP() { return needVEP; }
  public String[] getCustomRequirements() { return customRequirements; }
  public MultiAllelicPolicy getMultiAllelicPolicies() { return multiAllelicPolicies; }

  public enum MultiAllelicPolicy {
    NA("na"),
    IGNORE_STAR_ALLELE_AS_LINE("Each alternate allele is processed independently, While '*' allele ignored."), //TODO ignore star in each function
    ALLELE_AS_LINE("Each alternate allele is processed independently."),
    FORBIDDEN("An error will be thrown, as this function expects only monoallelic variants. The affected variant line will be dropped."),
    DROP("The affected variant line will be silently dropped."),
    ANNOTATION_FOR_ALL("Annotation is added/updated for each alternate allele (comma-separated)."),
    KEEP_IF_ONE_SATISFY("If at least one alternate allele satisfy all the conditions, the whole variant line is kept."),
    DROP_IF_ONE_FAILS("If at least one alternate allele doesn't satisfy all the conditions, the whole variant line is dropped.");

    public static final String PREFIX_MULTIALLELIC = "In case of multiallelic variants : ";
    private final String description;

    MultiAllelicPolicy(String description) { this.description = description; }
    public String getDescription() { return PREFIX_MULTIALLELIC + description; }
  }
}
