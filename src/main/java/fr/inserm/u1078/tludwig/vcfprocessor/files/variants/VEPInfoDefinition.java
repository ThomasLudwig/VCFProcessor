package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPFacade;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPVariantIDFacade;

import java.util.HashMap;
import java.util.Map;

public class VEPInfoDefinition extends InfoDefinition {
  private static final String CUT = "Format: ";

  public static final String CSQ = "CSQ";
  public static final String ANN = "ANN";

  private final Map<String, Integer> vepFieldIndices;

  public VEPInfoDefinition(String id, Type type, int number, String description, String source, String version) {
    super(id, type, number, description, source, version);
    this.vepFieldIndices = buildIndexMap();
  }

  public VEPInfoDefinition(String id, Type type, int number, String description) {
    super(id, type, number, description, null, null);
    this.vepFieldIndices = buildIndexMap();
  }

  public Map<String, Integer> buildIndexMap() {
    Map<String, Integer> vepFieldIndices = new HashMap<>();
    String[] f = this.getDescription().substring(CUT.length()).split("\\|", -1);
    for(int i = 0; i < f.length; i++)
      vepFieldIndices.put(f[i], i);

    Message.error(!buildIndexMap().containsKey(VEPVariantIDFacade.ALLELE_NUM), "VEP annotations must contain ["+VEPVariantIDFacade.ALLELE_NUM+"]");

    return vepFieldIndices;
  }

  public int getIndexFor(String vepField) {
    Integer i = vepFieldIndices.get(vepField);
    return i == null ? -1 : i;
  }

  public static boolean isCSQ(String s) { return CSQ.equals(s) || ANN.equals(s); }

  public int size() { return vepFieldIndices.size(); }

}
