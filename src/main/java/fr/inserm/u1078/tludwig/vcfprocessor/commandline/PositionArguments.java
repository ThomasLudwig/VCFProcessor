package fr.inserm.u1078.tludwig.vcfprocessor.commandline;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.filters.line.PositionFilter;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2020-03-06
 */
public class PositionArguments {

  public static final String TYPE = "Position Filters";

  public static final Argument KEEP_POS = Argument.newArgument(
          "--keep-pos",
          TYPE,
          "chr[:start[-end]],...",
          new String[][]{
            {"14", "All variants from chromosome 14"},
            {"chr12", "All variants from chromosome 12"},
            {"10:145678", "All variants at position 10:145678"},
            {"chr3:10000-20000", "All variants in region 10000-20000 on chromosome 3"},
            {"chr3:10000-20000,chr3:30000-40000,chr3:50000-60000", "All variants in regions 10000-20000, 30000-40000 or 50000-60000 on chromosome 3"}
          },
          new Description("Keep Variants starting on a comma-separated list of positions.")
  );
  public static final Argument KEEP_POSITIONS = Argument.newArgument(
          "--keep-positions",
          TYPE,
          "filename.txt",
          new String[][]{{"my_positions.txt", "All positions listed in my_positions.txt"}},
          new Description("Keep Variants starting on positions listed in a file.")
          .addLine("Lines in the position file can have the following format")
          .addItemize(PositionFilter.POSITION_FILE_FORMATS)
  );

  public static final Argument KEEP_BED = Argument.newArgument(
          "--keep-bed",
          TYPE,
          "filename.bed",
          new String[][]{{"my_regions.bed", "All regions defined in the bed file my_regions.bed"}},
          new Description("Keep Variants starting on regions defined by a bed file. See FileFormats")
  );
  public static final Argument KEEP_POS_OVERLAP = Argument.newArgument(
          "--keep-pos-overlap",
          TYPE,
          "chr[:start[-end]],...",
          new String[][]{
            {"14", "All variants from chromosome 14"},
            {"chr12", "All variants from chromosome 12"},
            {"10:145678", "All variants at position 10:145678"},
            {"chr3:10000-20000", "All variants in region 10000-20000 on chromosome 3"},
            {"chr3:10000-20000,chr3:30000-40000,chr3:50000-60000", "All variants in regions 10000-20000, 30000-40000 or 50000-60000 on chromosome 3"}
          },
          new Description("Keep Variants overlapping on a comma-separated list of positions.")
  );
  public static final Argument KEEP_POSITIONS_OVERLAP = Argument.newArgument(
          "--keep-positions-overlap",
          TYPE,
          "filename.txt",
          new String[][]{{"my_positions.txt", "All positions listed in my_positions.txt"}},
          new Description("Keep Variants overlapping positions listed in a file.")
          .addLine("Lines in the position file can have the following format")
          .addItemize(PositionFilter.POSITION_FILE_FORMATS)
  );

  public static final Argument REMOVE_POS = Argument.newArgument(
          "--remove-pos",
          TYPE,
          "chr[:start[-end]],...",
          new String[][]{
            {"14", "All variants from chromosome 14"},
            {"chr12", "All variants from chromosome 12"},
            {"10:145678", "All variants at position 10:145678"},
            {"chr3:10000-20000", "All variants in region 10000-20000 on chromosome 3"},
            {"chr3:10000-20000,chr3:30000-40000,chr3:50000-60000", "All variants in regions 10000-20000, 30000-40000 or 50000-60000 on chromosome 3"}
          },
          new Description("Remove Variants starting on a comma-separated list of positions.")
  );
  public static final Argument REMOVE_POSITIONS = Argument.newArgument(
          "--remove-positions",
          TYPE,
          "filename.txt",
          new String[][]{{"my_positions.txt", "All positions listed in my_positions.txt"}},
          new Description("Remove Variants starting on positions listed in a file.")
          .addLine("Lines in the position file can have the following format")
          .addItemize(PositionFilter.POSITION_FILE_FORMATS)
  );
  public static final Argument REMOVE_BED = Argument.newArgument(
          "--remove-bed",
          TYPE,
          "filename.bed",
          new String[][]{{"my_regions.bed", "All regions defined in the bed file my_regions.bed"}},
          new Description("Keep Variants starting on regions defined by a bed file. See FileFormats")
  );
  public static final Argument REMOVE_POS_OVERLAP = Argument.newArgument("--remove-pos-overlap",
          TYPE,
          "chr[:start[-end]],...",
          new String[][]{
            {"14", "All variants from chromosome 14"},
            {"chr12", "All variants from chromosome 12"},
            {"10:145678", "All variants at position 10:145678"},
            {"chr3:10000-20000", "All variants in region 10000-20000 on chromosome 3"},
            {"chr3:10000-20000,chr3:30000-40000,chr3:50000-60000", "All variants in regions 10000-20000, 30000-40000 or 50000-60000 on chromosome 3"}
          },
          new Description("Remove Variants overlapping a comma-separated list of positions.")
  );
  public static final Argument REMOVE_POSITIONS_OVERLAP = Argument.newArgument("--remove-positions-overlap",
          TYPE,
          "filename.txt",
          new String[][]{{"my_positions.txt", "All positions listed in my_positions.txt"}},
          new Description("Remove Variants overlapping positions listed in a file.")
          .addLine("Lines in the position file can have the following format")
          .addItemize(PositionFilter.POSITION_FILE_FORMATS)
  );
  
  public static final Argument THIN = Argument.newArgument("--thin",
          TYPE,
          "distance (integer no_units, kb or mb)",
          new String[][]{
            {"1000", "avoids having sites closer than 1kb from each other"},
            {"1kb", "avoids having sites closer than 1kb from each other"},
            {"5000000", "avoids having sites closer than 5mb from each other"},
            {"5mb", "avoids having sites closer than 5mb from each other"}
                  
          },
          new Description("Thin sites so that no two sites are within the specified distance from one another.")
  );
}
