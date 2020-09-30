package fr.inserm.u1078.tludwig.vcfprocessor.commandline;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-06
 */
public class PropertyArguments {

  public static final String TYPE = "Variant Property Filters";

  public static final Argument KEEP_ID = Argument.newArgument("--keep-id",
          TYPE,
          "id1,id2,....",
          new String[][]{
            {"rs123456", "variants with ID matching rs123456"},
            {"rs123456,COSM654321", "variants with ID matching rs123456 or COSM654321"}
          },
          new Description("Keep Variants with ID field matching a comma-separated list of ID (e.g. a dbSNP rsID).")
  );
  public static final Argument REMOVE_ID = Argument.newArgument("--remove-id",
          TYPE,
          "id1,id2,....",
          new String[][]{
            {"rs123456", "variants with ID matching rs123456"},
            {"rs123456,COSM654321", "variants with ID matching rs123456 or COSM654321"}
          },
          new Description("Remove Variants with ID field matching a comma-separated list of ID (e.g. a dbSNP rsID).")
  );
  public static final Argument KEEP_IDS = Argument.newArgument("--keep-ids",
          TYPE,
          "filename.txt",
          new String[][]{{"selected.id.txt", "variants with ID list in the file selected.id.txt"}},
          new Description("Keep Variants with ID matching the ones listed in the file (one per line).")
  );
  public static final Argument REMOVE_IDS = Argument.newArgument("--remove-ids",
          TYPE,
          "filename.txt",
          new String[][]{{"selected.id.txt", "variants with ID list in the file selected.id.txt"}},
          new Description("Remove Variants with ID matching the ones listed in the file (one per line).")
  );

  public static final Argument KEEP_INDELS = Argument.newArgument("--keep-indels",
          TYPE,
          "",
          new String[][]{},
          new Description("Keep variant sites where at least one alternate allele is an indel (AC->GT is considered as an SNV not an INDEL).")
  );
  public static final Argument REMOVE_INDELS = Argument.newArgument("--remove-indels",
          TYPE,
          "",
          new String[][]{},
          new Description("Remove variant sites where at least one alternate allele is an indel (AC->GT is considered as an SNV not an INDEL).")
  );

  public static final Argument KEEP_SNVS = Argument.newArgument("--keep-snvs",
          TYPE,
          "",
          new String[][]{},
          new Description("Keep variant sites where at least one alternate allele is an SNV (AC->GT is considered as an SNV not an INDEL).")
  );
  public static final Argument REMOVE_SNVS = Argument.newArgument("--remove-snvs",
          TYPE,
          "",
          new String[][]{},
          new Description("Remove variant sites where at least one alternate allele is an SNV (AC->GT is considered as an SNV not an INDEL).")
  );

  public static final String KEY_KEEP_FILTERED_ALL = "--keep-filtered-all";
  public static final String KEY_KEEP_FILTERED_ANY = "--keep-filtered-any";
  public static final String KEY_REMOVE_FILTERED_ALL = "--remove-filtered-all";
  public static final String KEY_REMOVE_FILTERED_ANY = "--remove-filtered-any";
  public static final String KEY_STRICT_KEEP_FILTERED_ALL = "--strict-keep-filtered-all";
  public static final String KEY_STRICT_KEEP_FILTERED_ANY = "--strict-keep-filtered-any";
  public static final String KEY_STRICT_REMOVE_FILTERED_ALL = "--strict-remove-filtered-all";
  public static final String KEY_STRICT_REMOVE_FILTERED_ANY = "--strict-remove-filtered-any";

  public static final Argument KEEP_FILTERED_ALL = Argument.newArgument(KEY_KEEP_FILTERED_ALL,
          TYPE,
          "Tag1,Tag2,...",
          new String[][]{{"Tag1,Tag2", "Variants with Tag1 and/or Tag2 and no other will be kept"}},
          new Description("to pass, all variant's Tags must be listed").addLine("sites removed by " + Description.code(KEY_REMOVE_FILTERED_ALL) + " are kept").addTable(new String[][]{
    {"A,B", "keepall", "keepany", "removeall", "removeany", "keepstrictall", "keepstrictany", "removestrictall", "removestrictany"},
    {"A", "pass", "pass", "filtered", "filtered", "filtered", "filtered", "pass", "pass"},
    {"B", "pass", "pass", "filtered", "filtered", "filtered", "filtered", "pass", "pass"},
    {"C", "filtered", "	filtered", "pass", "pass", "filtered", "filtered", "pass", "pass"},
    {"A;B", "pass", "pass", "filtered", "filtered", "pass", "pass", "filtered", "filtered"},
    {"A;C", "filtered", "pass", "pass", "filtered", "filtered", "filtered", "pass", "pass"},
    {"B;C", "filtered", "pass", "pass", "filtered", "filtered", "filtered", "pass", "pass"},
    {"A;B;C", "filtered", "pass", "pass", "filtered", "filtered", "pass", "pass", "filtered"}
  }, true)
  );
  public static final Argument KEEP_FILTERED_ANY = Argument.newArgument(KEY_KEEP_FILTERED_ANY,
          TYPE,
          "Tag1,Tag2,...",
          new String[][]{{"Tag1,Tag2", "Variants with Tag1 and/or Tag2 (plus any other) will be kept"}},
          new Description("to pass, any variant's Tag must be listed (plus any other)").addLine("sites removed by " + Description.code(KEY_REMOVE_FILTERED_ANY) + " are kept")
  );
  public static final Argument REMOVE_FILTERED_ALL = Argument.newArgument(KEY_REMOVE_FILTERED_ALL,
          TYPE,
          "Tag1,Tag2,...",
          new String[][]{{"Tag1,Tag2", "Variants with Tag1 and/or Tag2 and no other will be removed"}},
          new Description("to pass, any of the variant's Tags must not be listed").addLine("sites kept by " + Description.code(KEY_KEEP_FILTERED_ALL) + " are removed")
  );
  public static final Argument REMOVE_FILTERED_ANY = Argument.newArgument(KEY_REMOVE_FILTERED_ANY,
          TYPE,
          "Tag1,Tag2,...",
          new String[][]{{"Tag1,Tag2", "Variants with Tag1 and/or Tag2 (and any other) will be removed"}},
          new Description("to pass, none of the variant's Tags must be listed").addLine("sites kept by " + Description.code(KEY_KEEP_FILTERED_ANY) + " are removed")
  );

  public static final Argument STRICT_KEEP_FILTERED_ALL = Argument.newArgument(KEY_STRICT_KEEP_FILTERED_ALL,
          TYPE,
          "Tag1,Tag2,...",
          new String[][]{{"Tag1,Tag2", "Variants with Tag1 and/or Tag2 and no other will be kept"}},
          new Description("to pass, the variant must have all listed Tags, and no other").addLine("sites removed by " + Description.code(KEY_STRICT_REMOVE_FILTERED_ALL) + " are kept")
  );
  public static final Argument STRICT_KEEP_FILTERED_ANY = Argument.newArgument(KEY_STRICT_KEEP_FILTERED_ANY,
          TYPE,
          "Tag1,Tag2,...",
          new String[][]{{"Tag1,Tag2", "Variants with Tag1 and Tag2 (plus any other) will be kept"}},
          new Description("to pass, the variant must have all listed Tags (plus any other)").addLine("sites removed by " + Description.code(KEY_STRICT_REMOVE_FILTERED_ANY) + " are kept")
  );
  public static final Argument STRICT_REMOVE_FILTERED_ALL = Argument.newArgument(KEY_STRICT_REMOVE_FILTERED_ALL,
          TYPE,
          "Tag1,Tag2,...",
          new String[][]{{"Tag1,Tag2", "Variants with Tag1 and Tag2, and not other will be removed"}},
          new Description("to pass, the variant must not have all the listed Tags, and no other").addLine("sites kept by " + Description.code(KEY_STRICT_KEEP_FILTERED_ALL) + " are removed")
  );
  public static final Argument STRICT_REMOVE_FILTERED_ANY = Argument.newArgument(KEY_STRICT_REMOVE_FILTERED_ANY,
          TYPE,
          "Tag1,Tag2,...",
          new String[][]{{"Tag1,Tag2", "Variants with Tag1 and Tag2 (plus any other tag) will be removed"}},
          new Description("to pass, the variant must not have all the listed Tags (plus any other)").addLine("sites kept by " + Description.code(KEY_STRICT_KEEP_FILTERED_ALL) + " are removed")
  );

  public static final Argument KEEP_ANY_INFO = Argument.newArgument("--keep-any-info",
          TYPE,
          "Info1,Info2,...",
          new String[][]{{"Info1,Info2", "Variant with Info1 and/or Info2 field in the INFO column are kept"}},
          new Description("Variant with any of the listed Info Fields are kept")
  );
  public static final Argument KEEP_ALL_INFO = Argument.newArgument("--keep-all-info",
          TYPE,
          "Info1,Info2,...",
          new String[][]{{"Info1,Info2", "Variant with Info1 and Info2 field in the INFO column are kept"}},
          new Description("Variant with all the listed Info Fields are kept")
  );
  public static final Argument REMOVE_ANY_INFO = Argument.newArgument("--remove-any-info",
          TYPE,
          "Info1,Info2,...",
          new String[][]{{"Info1,Info2", "Variant with Info1 and/or Info2 field in the INFO column are removed"}},
          new Description("Variant with any of the listed Info Fields are removed")
  );
  public static final Argument REMOVE_ALL_INFO = Argument.newArgument("--remove-all-info",
          TYPE,
          "Info1,Info2,...",
          new String[][]{{"Info1,Info2", "Variant with Info1 and Info2 field in the INFO column are removed"}},
          new Description("Variant with all the listed Info Fields are removed")
  );

  public static final Argument MIN_ALLELES = Argument.newArgument("--min-alleles",
          TYPE,
          "number of alleles",
          new String[][]{{"3", "Keep variants with at least 3 alleles (1 reference and 2 alternates)"}},
          new Description("Keep variants with at least the number of specified alleles (reference plus alternates")
  );
  public static final Argument MAX_ALLELES = Argument.newArgument("--max-alleles",
          TYPE,
          "number of alleles",
          new String[][]{{"3", "Keep variants with at most 3 alleles (1 reference and 2 alternates)"}},
          new Description("Keep variants with at most the number of specified alleles (reference plus alternates")
  );
  /*public static final Argument REMOVE_MIN_ALLELES = Argument.newArgument("--remove-min-alleles",
          TYPE,
          "number of alleles",
          new String[][]{{"3", "Remove variants with at least 3 alleles (1 reference and 2 alternates)"}},
          new Description("Keep variants with at least the number of specified alleles (reference plus alternates")
  );
  public static final Argument REMOVE_MAX_ALLELES = Argument.newArgument("--remove-max-alleles",
          TYPE,
          "number of alleles",
          new String[][]{{"3", "Keep variants with at most 3 alleles (1 reference and 2 alternates)"}},
          new Description("Remove variants with at most the number of specified alleles (reference plus alternates")
  );*/

  public static final Argument MIN_MEANDP = Argument.newArgument("--min-meanDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Keep variants with a mean DP accross samples at least equal to 10"}},
          new Description("Keep variants with a mean Depth of coverage accross samples at least equal to the given value")
  );
  public static final Argument MIN_MEDIANDP = Argument.newArgument("--min-medianDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Keep variants with a median DP accross samples at least equal to 10"}},
          new Description("Keep variants with a median Depth of coverage accross samples at least equal to the given value")
  );
  public static final Argument MAX_MEANDP = Argument.newArgument("--max-meanDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Keep variants with a mean DP accross samples at most equal to 10"}},
          new Description("Keep variants with a mean Depth of coverage accross samples at most equal to the given value")
  );
  public static final Argument MAX_MEDIANDP = Argument.newArgument("--max-medianDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Keep variants with a median DP accross samples at most equal to 10"}},
          new Description("Keep variants with a median Depth of coverage accross samples at most equal to the given value")
  );
  /*public static final Argument REMOVE_MIN_MEANDP = Argument.newArgument("--remove-min-meanDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Remove variants with a mean DP accross samples at least equal to 10"}},
          new Description("Remove variants with a mean Depth of coverage accross samples at least equal to the given value")
  );
  public static final Argument REMOVE_MIN_MEDIANDP = Argument.newArgument("--remove-min-medianDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Remove variants with a median DP accross samples at least equal to 10"}},
          new Description("Remove variants with a median Depth of coverage accross samples at least equal to the given value")
  );
  public static final Argument REMOVE_MAX_MEANDP = Argument.newArgument("--remove-max-meanDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Remove variants with a mean DP accross samples at most equal to 10"}},
          new Description("Remove variants with a mean Depth of coverage accross samples at most equal to the given value")
  );
  public static final Argument REMOVE_MAX_MEDIANDP = Argument.newArgument("--remove-max-medianDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Remove variants with a median DP accross samples at most equal to 10"}},
          new Description("Remove variants with a median Depth of coverage accross samples at most equal to the given value")
  );*/
  public static final Argument MIN_MEANGQ = Argument.newArgument("--min-meanGQ",
          TYPE,
          "genotype-quality (integer)",
          new String[][]{{"10", "Keep variants with a mean GQ accross samples at least equal to 10"}},
          new Description("Keep variants with a mean Genotype Quality accross samples at least equal to the given value")
  );
  public static final Argument MIN_MEDIANGQ = Argument.newArgument("--min-medianGQ",
          TYPE,
          "genotype-quality (integer)",
          new String[][]{{"10", "Keep variants with a median GQ accross samples at least equal to 10"}},
          new Description("Keep variants with a median Genotype Quality accross samples at least equal to the given value")
  );
  public static final Argument MAX_MEANGQ = Argument.newArgument("--max-meanGQ",
          TYPE,
          "genotype-quality (integer)",
          new String[][]{{"10", "Keep variants with a mean GQ accross samples at most equal to 10"}},
          new Description("Keep variants with a mean Genotype Quality accross samples at most equal to the given value")
  );
  public static final Argument MAX_MEDIANGQ = Argument.newArgument("--max-medianGQ",
          TYPE,
          "genotype-quality (integer)",
          new String[][]{{"10", "Keep variants with a median GQ accross samples at most equal to 10"}},
          new Description("Keep variants with a median Genotype Quality accross samples at most equal to the given value")
  );
  /*public static final Argument REMOVE_MIN_MEANGQ = Argument.newArgument("--remove-min-meanGQ",
          TYPE,
          "genotype-quality (integer)",
          new String[][]{{"10", "Remove variants with a mean GQ accross samples at least equal to 10"}},
          new Description("Remove variants with a mean Genotype Quality accross samples at least equal to the given value")
  );
  public static final Argument REMOVE_MIN_MEDIANGQ = Argument.newArgument("--remove-min-medianGQ",
          TYPE,
          "genotype-quality (integer)",
          new String[][]{{"10", "Remove variants with a median GQ accross samples at least equal to 10"}},
          new Description("Remove variants with a median Genotype Quality accross samples at least equal to the given value")
  );
  public static final Argument REMOVE_MAX_MEANGQ = Argument.newArgument("--remove-max-meanGQ",
          TYPE,
          "genotype-quality (integer)",
          new String[][]{{"10", "Remove variants with a mean GQ accross samples at most equal to 10"}},
          new Description("Remove variants with a mean Genotype Quality accross samples at most equal to the given value")
  );
  public static final Argument REMOVE_MAX_MEDIANGQ = Argument.newArgument("--remove-max-medianGQ",
          TYPE,
          "genotype-quality (integer)",
          new String[][]{{"10", "Remove variants with a median GQ accross samples at most equal to 10"}},
          new Description("Remove variants with a median Genotype Quality accross samples at most equal to the given value")
  );*/

  public static final Argument HWE = Argument.newArgument("--hwe",
          TYPE,
          "p-value (double)",
          new String[][]{{"0.0000001", "Keep variants with HWE p-value >= 0.0000001"}}, //TODO add formula to p-value
          new Description("Keep variants with Hardy-Weinberg Equilibrium p-value above the given threshold")
  );
  public static final Argument REMOVE_HWE = Argument.newArgument("--remove-hwe",
          TYPE,
          "p-value (double)",
          new String[][]{{"0.0000001", "Remove variants with HWE p-value >= 0.0000001"}},
          new Description("Remove variants with Hardy-Weinberg Equilibrium p-value above the given threshold")
  );

  public static final Argument MAX_MISSING = Argument.newArgument("--max-missing",
          TYPE,
          "ratio (double)",
          new String[][]{{"0.1", "Keep variant with less than 10% of missing genotypes"}},
          new Description("Keep variant with at most the given ratio of missing genotypes")
  );
  public static final Argument MIN_MISSING = Argument.newArgument("--min-missing",
          TYPE,
          "ratio (double)",
          new String[][]{{"0.1", "Keep variant with more than 10% of missing genotypes"}},
          new Description("Keep variant with at least the given ratio of missing genotypes")
  );
  public static final Argument MAX_MISSING_COUNT = Argument.newArgument("--max-missing-count",
          TYPE,
          "integer",
          new String[][]{{"17", "Keep variant with less than 17 missing genotypes"}},
          new Description("Keep variant with at most the given number of missing genotypes")
  );
  public static final Argument MIN_MISSING_COUNT = Argument.newArgument("--min-missing-count",
          TYPE,
          "integer",
          new String[][]{{"17", "Keep variant with more than 17 missing genotypes"}},
          new Description("Keep variant with at least the given number of missing genotypes")
  );

  public static final Argument PHASED = Argument.newArgument("--phased",
          TYPE,
          "",
          new String[][]{},
          new Description("Keep variant without unphased genotypes")
  );
  public static final Argument REMOVE_PHASED = Argument.newArgument("--remove-phased",
          TYPE,
          "",
          new String[][]{},
          new Description("Remove variant without unphased genotypes") //TODO useful ? find the dual operation
  );

  public static final Argument MINQ = Argument.newArgument("--minQ",
          TYPE,
          "quality (double)",
          new String[][]{{"500", "Keep variants with QUALITY >= 500"}},
          new Description("Keep variants with a quality (VCF column QUALITY) at least equal to the given value")
  );
  public static final Argument MAXQ = Argument.newArgument("--maxQ",
          TYPE,
          "quality (double)",
          new String[][]{{"500", "Keep variants with QUALITY <= 500"}},
          new Description("Keep variants with a quality (VCF column QUALITY) at most equal to the given value")
  );
}
