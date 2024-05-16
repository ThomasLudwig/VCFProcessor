package fr.inserm.u1078.tludwig.vcfprocessor.commandline;

import static fr.inserm.u1078.tludwig.vcfprocessor.commandline.SampleArguments.KEY_PED;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-06
 */
public class FrequencyArguments {

  public static final String TYPE = "Variant Frequency Filters";
  
  public static final String FORMAT_GROUP_AC = "group1:ac1,group2:ac2,...,groupN:acN";
  public static final String FORMAT_GROUP_AF = "group1:af1,group2:af2,...,groupN:afN";

  public static final Argument MIN_MAF = Argument.newArgument("--min-maf",
          TYPE,
          "frequency (double)",
          new String[][]{{"0.05", "Keep only variants with AF of the rarest reference/alternate allele above 5%"}},
          new Description("Keep only variants with AF of the rarest reference/alternate allele above the threshold")
                  .addWarning("Diploid variants expected")
  );
  public static final Argument MAX_MAF = Argument.newArgument("--max-maf",
          TYPE,
          "frequency (double)",
          new String[][]{{"0.05", "Keep only variants with AF of the rarest reference/alternate allele below 5%"}},
          new Description("Keep only variants with AF of the rarest reference/alternate allele below the threshold")
                  .addWarning("Diploid variants expected")
  );
  public static final Argument MIN_GROUP_MAF = Argument.newArgument("--min-group-maf",
          TYPE,
          "frequency ("+FORMAT_GROUP_AF+")",
          new String[][]{{"GRP1:0.05,GRP2:0.07", "Keep only variants with AF of the rarest reference/alternate allele above 5% in GRP1 and 7% in GRP2"}},
          new Description("Keep only variants with AF of the rarest reference/alternate allele above the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
                  .addWarning("Diploid variants expected")
  );
  public static final Argument MAX_GROUP_MAF = Argument.newArgument("--max-group-maf",
          TYPE,
          "frequency ("+FORMAT_GROUP_AF+")",
          new String[][]{{"GRP1:0.05,GRP2:0.07", "Keep only variants with AF of the rarest reference/alternate allele below 5% in GRP1 and 7% in GRP2"}},
          new Description("Keep only variants with AF of the rarest reference/alternate allele below the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
                  .addWarning("Diploid variants expected")
  );

  public static final Argument MIN_REF_AF = Argument.newArgument("--min-ref-af",
          TYPE,
          "frequency (double)",
          new String[][]{{"0.05", "Keep only variants with AF of the reference allele above 5%"}},
          new Description("Keep only variants with AF of the reference allele above the threshold")
                  .addWarning("Diploid variants expected")
  );
  public static final Argument MAX_REF_AF = Argument.newArgument("--max-ref-af",
          TYPE,
          "frequency (double)",
          new String[][]{{"0.05", "Keep only variants with AF of the reference allele below 5%"}},
          new Description("Keep only variants with AF of the reference allele below the threshold")
                  .addWarning("Diploid variants expected")
  );
  public static final Argument MIN_GROUP_REF_AF = Argument.newArgument("--min-group-ref-af",
          TYPE,
          "frequency ("+FORMAT_GROUP_AF+")",
          new String[][]{{"GRP1:0.05,GRP2:0.07", "Keep only variants with AF of the reference allele above 5% in GRP1 and 7% in GRP2"}},
          new Description("Keep only variants with AF of the reference allele above the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
                  .addWarning("Diploid variants expected")
  );
  public static final Argument MAX_GROUP_REF_AF = Argument.newArgument("--max-group-ref-af",
          TYPE,
          "frequency ("+FORMAT_GROUP_AF+")",
          new String[][]{{"GRP1:0.05,GRP2:0.07", "Keep only variants with AF of the reference allele below 5% in GRP1 and 7% in GRP2"}},
          new Description("Keep only variants with AF of the reference allele below the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
                  .addWarning("Diploid variants expected")
  );
 
  
  public static final Argument MIN_ALL_NON_REF_AF = Argument.newArgument("--min-all-non-ref-af",
          TYPE,
          "frequency (double)",
          new String[][]{{"0.05", "Keep only variants with AF of all the alternate allele above 5%"}},
          new Description("Keep only variants with AF of all the alternate allele above the threshold")
                  .addWarning("Diploid variants expected")
  );
  public static final Argument MAX_ALL_NON_REF_AF = Argument.newArgument("--max-all-non-ref-af",
          TYPE,
          "frequency (double)",
          new String[][]{{"0.05", "Keep only variants with AF of all the alternate allele below 5%"}},
          new Description("Keep only variants with AF of all the alternate allele below the threshold")
                  .addWarning("Diploid variants expected")
  );
  public static final Argument MIN_GROUP_ALL_NON_REF_AF = Argument.newArgument("--min-group-all-non-ref-af",
          TYPE,
          "frequency ("+FORMAT_GROUP_AF+")",
          new String[][]{{"GRP1:0.05,GRP2:0.07", "Keep only variants with AF of all the alternate allele above 5% in GRP1 and 7% in GRP2"}},
          new Description("Keep only variants with AF of all the alternate allele above the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
                  .addWarning("Diploid variants expected")
  );
  public static final Argument MAX_GROUP_ALL_NON_REF_AF = Argument.newArgument("--max-group-all-non-ref-af",
          TYPE,
          "frequency ("+FORMAT_GROUP_AF+")",
          new String[][]{{"GRP1:0.05,GRP2:0.07", "Keep only variants with AF of all the alternate allele below 5% in GRP1 and 7% in GRP2"}},
          new Description("Keep only variants with AF of all the alternate allele below the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
                  .addWarning("Diploid variants expected")
  );

  
  
  public static final Argument MIN_ANY_NON_REF_AF = Argument.newArgument("--min-any-non-ref-af",
          TYPE,
          "frequency (double)",
          new String[][]{{"0.05", "Keep only variants with AF of any alternate allele above 5%"}},
          new Description("Keep only variants with AF of any alternate allele above the threshold")
  );
  public static final Argument MAX_ANY_NON_REF_ANY = Argument.newArgument("--max-any-non-ref-af",
          TYPE,
          "frequency (double)",
          new String[][]{{"0.05", "Keep only variants with AF of any alternate allele below 5%"}},
          new Description("Keep only variants with AF of any alternate allele below the threshold")
  );
  public static final Argument MIN_GROUP_ANY_NON_REF_AF = Argument.newArgument("--min-group-any-non-ref-af",
          TYPE,
          "frequency ("+FORMAT_GROUP_AF+")",
          new String[][]{{"GRP1:0.05,GRP2:0.07", "Keep only variants with AF of any alternate allele above 5% in GRP1 and 7% in GRP2"}},
          new Description("Keep only variants with AF of any alternate allele above the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
  );
  public static final Argument MAX_GROUP_ANY_NON_REF_AF = Argument.newArgument("--max-group-any-non-ref-af",
          TYPE,
          "frequency ("+FORMAT_GROUP_AF+")",
          new String[][]{{"GRP1:0.05,GRP2:0.07", "Keep only variants with AF of any alternate allele below 5% in GRP1 and 7% in GRP2"}},
          new Description("Keep only variants with AF of any alternate allele below the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
  );
  
  public static final Argument MIN_REF_AC = Argument.newArgument("--min-ref-ac",
          TYPE,
          "allele-count (integer)",
          new String[][]{{"12", "Keep only variants with AC of the reference allele above 12"}},
          new Description("Keep only variants with AC of the reference allele above the threshold")
  );
  public static final Argument MAX_REF_AC = Argument.newArgument("--max-ref-ac",
          TYPE,
          "allele-count (integer)",
          new String[][]{{"12", "Keep only variants with AC of the reference allele below 12"}},
          new Description("Keep only variants with AC of the reference allele below the threshold")
  );
  public static final Argument MIN_GROUP_REF_AC = Argument.newArgument("--min-group-ref-ac",
          TYPE,
          "allele-count ("+FORMAT_GROUP_AC+")",
          new String[][]{{"GRP1:8,GRP2:6", "Keep only variants with AC of the reference allele above 8 in GRP1 and 6 in GRP2"}},
          new Description("Keep only variants with AC of the reference allele above the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
  );
  public static final Argument MAX_GROUP_REF_AC = Argument.newArgument("--max-group-ref-ac",
          TYPE,
          "allele-count ("+FORMAT_GROUP_AC+")",
          new String[][]{{"GRP1:8,GRP2:6", "Keep only variants with AC of the reference allele below 8 in GRP1 and 6 in GRP2"}},
          new Description("Keep only variants with AC of the reference allele below the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
  );

  
  
  public static final Argument MIN_ALL_NON_REF_AC = Argument.newArgument("--min-all-non-ref-ac",
          TYPE,
          "allele-count (integer)",
          new String[][]{{"12", "Keep only variants with AC of all the alternate allele above 12"}},
          new Description("Keep only variants with AC of all the alternate allele above the threshold")
  );
  public static final Argument MAX_ALL_NON_REF_AC = Argument.newArgument("--max-all-non-ref-ac",
          TYPE,
          "allele-count (integer)",
          new String[][]{{"12", "Keep only variants with AC of all the alternate allele below 12"}},
          new Description("Keep only variants with AC of all the alternate allele below the threshold")
  );
  public static final Argument MIN_GROUP_ALL_NON_REF_AC = Argument.newArgument("--min-group-all-non-ref-ac",
          TYPE,
          "allele-count ("+FORMAT_GROUP_AC+")",
          new String[][]{{"GRP1:8,GRP2:6", "Keep only variants with AC of all the alternate allele above 8 in GRP1 and 6 in GRP2"}},
          new Description("Keep only variants with AC of all the alternate allele above the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
  );
  public static final Argument MAX_GROUP_ALL_NON_REF_AC = Argument.newArgument("--max-group-all-non-ref-ac",
          TYPE,
          "allele-count ("+FORMAT_GROUP_AC+")",
          new String[][]{{"GRP1:8,GRP2:6", "Keep only variants with AC of all the alternate allele below 8 in GRP1 and 6 in GRP2"}},
          new Description("Keep only variants with AC of all the alternate allele below the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
  );

  
  public static final Argument MIN_MAC = Argument.newArgument("--min-mac",
          TYPE,
          "allele-count (integer)",
          new String[][]{{"12", "Keep only variants with AC of the rarest reference/alternate allele above 12"}},
          new Description("Keep only variants with AC of the rarest alternate reference/allele above the threshold")
  );
  public static final Argument MAX_MAC = Argument.newArgument("--max-mac",
          TYPE,
          "allele-count (integer)",
          new String[][]{{"12", "Keep only variants with AC of the rarest reference/alternate allele below 12"}},
          new Description("Keep only variants with AC of the rarest reference/alternate allele below the threshold")
  );
  public static final Argument MIN_GROUP_MAC = Argument.newArgument("--min-group-mac",
          TYPE,
          "allele-count ("+FORMAT_GROUP_AC+")",
          new String[][]{{"GRP1:8,GRP2:6", "Keep only variants with AC of the rarest reference/alternate allele above 8 in GRP1 and 6 in GRP2"}},
          new Description("Keep only variants with AC of the rarest reference/alternate allele above the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
  );
  public static final Argument MAX_GROUP_MAC = Argument.newArgument("--max-group-mac",
          TYPE,
          "allele-count ("+FORMAT_GROUP_AC+")",
          new String[][]{{"GRP1:8,GRP2:6", "Keep only variants with AC of the rarest reference/alternate allele below 8 in GRP1 and 6 in GRP2"}},
          new Description("Keep only variants with AC of the rarest reference/alternate allele below the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
  );
  
  public static final Argument MIN_ANY_NON_REF_AC = Argument.newArgument("--min-any-non-ref-ac",
          TYPE,
          "allele-count (integer)",
          new String[][]{{"12", "Keep only variants with AC of any alternate allele above 12"}},
          new Description("Keep only variants with AC of any alternate allele above the threshold")
  );
  public static final Argument MAX_ANY_NON_REF_AC = Argument.newArgument("--max-any-non-ref-ac",
          TYPE,
          "allele-count (integer)",
          new String[][]{{"12", "Keep only variants with AC of any alternate allele below 12"}},
          new Description("Keep only variants with AC of any alternate allele below the threshold")
  );
  public static final Argument MIN_GROUP_ANY_NON_REF_AC = Argument.newArgument("--min-group-any-non-ref-ac", //TODO doc not up-to-date !!!
          TYPE,
          "allele-count ("+FORMAT_GROUP_AC+")",
          new String[][]{{"GRP1:8,GRP2:6", "Keep only variants with AC of any alternate allele above 8 in GRP1 and 6 in GRP2"}},
          new Description("Keep only variants with AC of any alternate allele above the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
  );
  public static final Argument MAX_GROUP_ANY_NON_REF_AC = Argument.newArgument("--max-group-any-non-ref-ac",
          TYPE,
          "allele-count ("+FORMAT_GROUP_AC+")",
          new String[][]{{"GRP1:8,GRP2:6", "Keep only variants with AC of any alternate allele below 8 in GRP1 and 6 in GRP2"}},
          new Description("Keep only variants with AC of any alternate allele below the threshold in every group")
                  .addWarning("has to be combined to " + Description.code(KEY_PED))
  );
}
