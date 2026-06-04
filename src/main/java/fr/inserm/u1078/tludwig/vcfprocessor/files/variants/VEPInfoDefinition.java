package fr.inserm.u1078.tludwig.vcfprocessor.files.variants;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.GeneticsException;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPFacade;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.variants.annotations.VEPVariantIDFacade;

import java.util.HashMap;
import java.util.Map;

public class VEPInfoDefinition extends InfoDefinition {
  private static final String CUT = "Format: ";

  public static final String CSQ = "CSQ";
  public static final String ANN = "ANN";

  private final Map<String, Integer> vepFieldIndices;

  public VEPInfoDefinition(String id, Type type, int number, String description, String source, String version) throws GeneticsException {
    super(id, type, number, description, source, version);
    this.vepFieldIndices = buildIndexMap();
  }

  public VEPInfoDefinition(String id, Type type, int number, String description) throws GeneticsException {
    super(id, type, number, description, null, null);
    this.vepFieldIndices = buildIndexMap();
  }

  public Map<String, Integer> buildIndexMap() throws GeneticsException {
    try{
      Map<String, Integer> vepFieldIndices = new HashMap<>();
      String[] f = this.getDescription().substring(CUT.length()).split("\\|", -1);
      for(int i = 0; i < f.length; i++)
        vepFieldIndices.put(f[i], i);
      if(!vepFieldIndices.containsKey(VEPVariantIDFacade.ALLELE_NUM))
        throw new GeneticsException("VEP annotations must contain ["+VEPVariantIDFacade.ALLELE_NUM+"]");
      return vepFieldIndices;
    } catch(Exception e){
      throw new GeneticsException("Could not build the Map of VEP Annotations from ["+getDescription()+"] ["+e.getMessage()+"]", e);
    }
  }

  public int getIndexFor(String vepField) {
    Integer i = vepFieldIndices.get(vepField);
    return i == null ? -1 : i;
  }

  public static boolean isCSQ(String s) { return CSQ.equals(s) || ANN.equals(s); }

  public int size() { return vepFieldIndices.size(); }

}
