package fr.inserm.u1078.tludwig.vcfprocessor.commandline;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-06
 */
public class SampleArguments {

  public static final String TYPE = "Sample Filters";

  public static final Argument KEEP_SAMPLE = Argument.newArgument("--keep-sample",
          TYPE,
          "Sample1,Sample2,...",
          new String[][]{{"Charlie,Juliett,Mike,Romeo,Victor", "Only the samples Charlie, Juliett, Mike, Romeo and Victor are kept"}},
          new Description("Keep only the samples given in a comma-separated list")
  );
  public static final Argument REMOVE_SAMPLE = Argument.newArgument("--remove-sample",
          TYPE,
          "Sample1,Sample2,...",
          new String[][]{{"Charlie,Juliett,Mike,Romeo,Victor", "The samples Charlie, Juliett, Mike, Romeo and Victor are removed"}},
          new Description("Remove the samples given in a comma-separated list")
  );
  public static final Argument KEEP_SAMPLES = Argument.newArgument("--keep-samples",
          TYPE,
          "filename.txt",
          new String[][]{{"samples.list.txt", "Keep only the sample listed in samples.list.txt (one per line)"}},
          new Description("Keep only the sample listed in the given file (one per line)")
  );
  public static final Argument REMOVE_SAMPLES = Argument.newArgument("--remove-samples",
          TYPE,
          "filename.txt",
          new String[][]{{"samples.list.txt", "Remove the sample listed in samples.list.txt (one per line)"}},
          new Description("Remove the sample listed in the given file (one per line)")
  );

  public static final Argument MAX_SAMPLE = Argument.newArgument("--max-sample",
          TYPE,
          "number-of-samples (integer)",
          new String[][]{{"5", "Keep only 5 samples at random"}},
          new Description("Remove random samples to keep only the provided number of samples")
  );

  public static final String KEY_PED = "--ped";
  public static final Argument PED = Argument.newArgument(KEY_PED,
          TYPE,
          "filename.ped",
          new String[][]{{"cohort.ped", "Keep only the samples described in cohort.ped"}},
          new Description("Keep only the samples described in the given PED file (see file formats)") //TODO link
  );

  public static final Argument KEEP_FAMILY = Argument.newArgument("--keep-family",
          TYPE,
          "Fam1,Fam2,...",
          new String[][]{{"Fam1,Fam2", "Keep only the samples whose FamilyID are Fam1 or Fam2"}},
          new Description("Keep only the sample with given FamilyID").addWarning("has to be combined to "+Description.code(KEY_PED)+"")
  );
  public static final Argument REMOVE_FAMILY = Argument.newArgument("--remove-family",
          TYPE,
          "Fam1,Fam2,...",
          new String[][]{{"Fam1,Fam2", "Remove the samples whose FamilyID are Fam1 or Fam2"}},
          new Description("Remove the sample with given FamilyID").addWarning("has to be combined to "+Description.code(KEY_PED)+"")
  );
  public static final Argument KEEP_SEX = Argument.newArgument("--keep-sex",
          TYPE,
          "sex (integer)",
          new String[][]{{"1", "Keep only the samples whose sex are 1"}},
          new Description("Keep only the sample with given sex").addWarning("has to be combined to "+Description.code(KEY_PED)+"")
  );
  public static final Argument REMOVE_SEX = Argument.newArgument("--remove-sex",
          TYPE,
          "sex (integer)",
          new String[][]{{"1", "Remove the samples whose sex are 1"}},
          new Description("Remove the sample with given sex").addWarning("has to be combined to "+Description.code(KEY_PED)+"")
  );
  public static final Argument KEEP_PHENOTYPE = Argument.newArgument("--keep-phenotype",
          TYPE,
          "phenotype (integer)",
          new String[][]{{"1", "Keep only the samples whose phenotype are 1"}},
          new Description("Keep only the sample with given phenotype").addWarning("has to be combined to "+Description.code(KEY_PED)+"")
  );
  public static final Argument REMOVE_PHENOTYPE = Argument.newArgument("--remove-phenotype",
          TYPE,
          "phenotype (integer)",
          new String[][]{{"1", "Remove the samples whose phenotype are 1"}},
          new Description("Remove the sample with given phenotype").addWarning("has to be combined to "+Description.code(KEY_PED)+"")
  );
  public static final Argument KEEP_GROUP = Argument.newArgument("--keep-group",
          TYPE,
          "Group1,Group2,...",
          new String[][]{{"CohortA,CohortB", "Keep only the samples belonging to CohortA or CohortB"}},
          new Description("Keep only the sample belonging to given phenotype").addWarning("has to be combined to "+Description.code(KEY_PED)+"")
  );
  public static final Argument REMOVE_GROUP = Argument.newArgument("--remove-group",
          TYPE,
          "Group1,Group2,...",
          new String[][]{{"CohortA,CohortB", "Remove the samples belonging to CohortA or CohortB"}},
          new Description("Remove the sample belonging to given phenotype").addWarning("has to be combined to "+Description.code(KEY_PED)+"")
  );

}
