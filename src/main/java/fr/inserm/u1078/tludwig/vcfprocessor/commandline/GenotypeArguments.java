package fr.inserm.u1078.tludwig.vcfprocessor.commandline;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.genotype.GenotypeFlagFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-06
 */
public class GenotypeArguments {

  public static final String TYPE = "Genotype Filters";

  public static final Argument REMOVE_FILTERED_GENO_ALL = Argument.newArgument("--remove-filtered-geno-all",
          TYPE,
          "",
          new String[][]{},
          new Description("Sets genotypes to missing when the "+GenotypeFlagFilter.FT+" field has any value besides \".\" or \"PASS\"")
  );
  public static final Argument KEEP_FILTERED_GENO = Argument.newArgument("--keep-filtered-geno",
          TYPE,
          "Tag1,Tag2,...",
          new String[][]{{"Tag1,Tag2", "Sets genotypes to missing when the "+GenotypeFlagFilter.FT+" field doesn't have the value Tag1 and/or Tag2"}},
          new Description("Sets genotypes to missing when the "+GenotypeFlagFilter.FT+" field doesn't have at least one of the given value")
  );
  public static final Argument REMOVE_FILTERED_GENO = Argument.newArgument("--remove-filtered-geno",
          TYPE,
          "Tag1,Tag2,...",
          new String[][]{{"Tag1,Tag2", "Sets genotypes to missing when the "+GenotypeFlagFilter.FT+" field has the value Tag1 and/or Tag2"}},
          new Description("Sets genotypes to missing when the "+GenotypeFlagFilter.FT+" field has at least one of the given value")
  );

  public static final Argument MINDP = Argument.newArgument("--minDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Sets genotypes to missing if DP <10 (or SUM(AD) if DP is missing)"}},//TODO check that
          new Description("Sets genotypes to missing if DP is below the given value (or SUM(AD) if DP is missing)")
  );
  public static final Argument MAXDP = Argument.newArgument("--maxDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Sets genotypes to missing if DP >10 (or SUM(AD) if DP is missing)"}},//TODO check that
          new Description("Sets genotypes to missing if DP is above the given value (or SUM(AD) if DP is missing)")
  );
  /*public static final Argument REMOVE_MINDP = Argument.newArgument("--remove-minDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Sets genotypes to missing if DP >=10 (or SUM(AD) if DP is missing)"}},//TODO check that
          new Description("Sets genotypes to missing if DP is not below the given value (or SUM(AD) if DP is missing)")//sinnymous of maxDP ?
  );
  public static final Argument REMOVE_MAXDP = Argument.newArgument("--remove-maxDP",
          TYPE,
          "depth-of-coverage (integer)",
          new String[][]{{"10", "Sets genotypes to missing if DP <=10 (or SUM(AD) if DP is missing)"}},//TODO check that
          new Description("Sets genotypes to missing if DP is not above the given value (or SUM(AD) if DP is missing)")
  );*/
  public static final Argument MINGQ = Argument.newArgument("--minGQ",
          TYPE,
          "Genotype Quality (integer)",
          new String[][]{{"30", "Sets genotypes to missing if GQ <30"}},
          new Description("Sets genotypes to missing if GQ is below the given value")
  );
  public static final Argument MAXGQ = Argument.newArgument("--maxGQ",
          TYPE,
          "Genotype Quality (integer)",
          new String[][]{{"30", "Sets genotypes to missing if GQ >30"}},
          new Description("Sets genotypes to missing if GQ is above the given value")
  );
  /*public static final Argument REMOVE_MINGQ = Argument.newArgument("--remove-minGQ",
          TYPE,
          "Genotype Quality (integer)",
          new String[][]{{"30", "Sets genotypes to missing if GQ >=30"}},
          new Description("Sets genotypes to missing if GQ is not below the given value")
  );
  public static final Argument REMOVE_MAXGQ = Argument.newArgument("--remove-maxGQ",
          TYPE,
          "Genotype Quality (integer)",
          new String[][]{{"30", "Sets genotypes to missing if GQ <=30"}},
          new Description("Sets genotypes to missing if GQ is not above the given value")
  );*/
}
