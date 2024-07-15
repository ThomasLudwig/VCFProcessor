package fr.inserm.u1078.tludwig.vcfprocessor.commandline;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;

public class BCFArguments {
  public static final String TYPE = "BCF Speedup Filters";

  public static final Argument CONSERVE_FORMAT = Argument.newArgument(
      "--conserve-format",
      TYPE,
      "DP,GQ....,AD",
      new String[][]{
          {"ALL", "*All* FORMAT fields"},
          {"DP", "DP field"},
          {"DP,GQ", "DP and GQ FORMAT fields"}
      },
      new Description("Intended to speed-up BCF parsing. Has no effects on VCF files").addLine("Conserve the values for the comma-separated list of FORMAT field. Replaces others with missing values (\".\")")
  );

  public static final Argument IGNORE_FORMAT = Argument.newArgument(
      "--ignore-format",
      TYPE,
      "DP,GQ....,AD",
      new String[][]{
          {"ALL", "*All* FORMAT fields"},
          {"DP", "DP field"},
          {"DP,GQ", "DP and GQ FORMAT fields"}
      },
      new Description("Intended to speed-up BCF parsing. Has no effects on VCF files").addLine("Replaces the values for the comma-separated list of FORMAT field with missing values (\".\")")
  );

  public static final Argument CONSERVE_INFO = Argument.newArgument(
      "--conserve-info",
      TYPE,
      "AC,AN,AF,...",
      new String[][]{
          {"ALL", "*All* INFO fields"},
          {"AC", "AC field"},
          {"AC,AN,AF", "AC, AN and AF FORMAT fields"}
      },
      new Description("Intended to speed-up BCF parsing. Has no effects on VCF files").addLine("Conserve the comma-separated list of INFO fields. Others are dropped")
  );

  public static final Argument IGNORE_INFO = Argument.newArgument(
      "--ignore-info",
      TYPE,
      "AC,AN,AF,...",
      new String[][]{
          {"ALL", "*All* INFO fields"},
          {"AC", "AC field"},
          {"AC,AN,AF", "AC, AN and AF FORMAT fields"}
      },
      new Description("Intended to speed-up BCF parsing. Has no effects on VCF files").addLine("Drops the comma-separated list of INFO fields.")
  );

}
