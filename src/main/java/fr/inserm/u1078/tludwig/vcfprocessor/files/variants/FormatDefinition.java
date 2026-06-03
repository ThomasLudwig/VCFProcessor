package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.vcfprocessor.genetics.GeneticsException;

public class FormatDefinition extends GeneralDefinition {
  private static final String HEADER = "##FORMAT=<";

  public FormatDefinition(String id, Type type, int number, String description) {
    super(id, type, number, description);
  }

  /**
   * Parses the line from the VCF header
   * @param line
   * @return
   * @throws GeneticsException
   */
  public static FormatDefinition parseLine(String line) throws GeneticsException {
    GeneralDefinition gd = GeneralDefinition.parseLine(line, HEADER);
    return new FormatDefinition(gd.getId(), gd.getType(), gd.getNumber(), gd.getDescription());
  }
}
