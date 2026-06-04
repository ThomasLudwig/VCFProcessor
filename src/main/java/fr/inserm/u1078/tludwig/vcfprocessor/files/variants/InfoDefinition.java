package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.GeneticsException;

/**
 * The defintion of the Info Field within the VCF Header
 */
public class InfoDefinition extends GeneralDefinition {
  private static final String HEADER = "##INFO=<";

  private final String source;
  private final String version;

  /**
   * @param id (required) the id of the info field
   * @param number (required) 0,1,2,3... A=1 per alt allele, R=1 per allele including ref, G=1per possible genotype, .=variable
   * @param type (required) String, Character, Integer, Float, Flag(Number=0)
   * @param description (required) the description of the field
   * @param source (optional)
   * @param version (optional)
   */
  public InfoDefinition(String id, GeneralDefinition.Type type, int number, String description, String source, String version) {
    super(id, type, number, description);
    this.source = source;
    this.version = version;
  }

  /**
   * Parses the line from the VCF header
   * @param line
   * @return
   * @throws GeneticsException
   */
  public static InfoDefinition parseLine(String line) throws GeneticsException {
    GeneralDefinition gd = GeneralDefinition.parseLine(line, HEADER);
    String id = gd.getId();
    int number = gd.getNumber();
    Type type = gd.getType();
    String description = gd.getDescription();
    String source = null;
    String version = null;

    try {
      //TODO, in fact split by "," is dangerous, has description could contain several ,
      for (String split : line.substring(HEADER.length(), line.length() - 1).split(",")) {
        String[] kv = split.split("=");
        switch (kv[0].toLowerCase()) {
          case "source":
            source = removeQuote(kv[1]);
            break;
          case "version":
            version = removeQuote(kv[1]);
            break;
        }
      }

      return VEPInfoDefinition.isCSQ(id)
          ? new VEPInfoDefinition(id, type, number, description, source, version)
          : new InfoDefinition(id, type, number, description, source, version);
    } catch(Exception e) {
      Message.error("Error while parsing line [" + line + "]: " + e.getMessage());
      throw new GeneticsException("Error while parsing line [" + line + "]: " + e.getMessage(), e);
    }
  }

  /**
   * Returns the number of expected values (-1 for unknown)
   * @param alts the number of alternate allele for this variant
   * @param samples the number of genotypes
   * @return the number of expected values
   */
  public int getNumberOfValues(int alts, int samples){
    return switch (getNumber()) {
      case VALUE_NUMBER_UNKNOWN -> -1;
      case VALUE_NUMBER_ALTS -> alts;
      case VALUE_NUMBER_ALLELES -> alts+1;
      case VALUE_NUMBER_GENOTYPES -> samples;
      default -> getNumber();
    };
  }

  @Override
  public String toString() {
    return "##INFO=<"
        + "ID="+getId()+","
        + "Number="+asTag(getNumber())+","
        + "Type="+getType()+","
        + "Description=\""+getDescription()+"\""
        + (source == null ? "": ",Source=\""+getSource()+"\"")
        + (version == null ? "": ",Version=\""+getVersion()+"\"") //parentheses for priority reasons
        + ">";
  }

  public String getSource() { return source; }

  public String getVersion() { return version; }
}
