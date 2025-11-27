package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Check if there is a discrepancy between the ID Column and the VEP annotation for RS ID;
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-08-17
 * Checked for release on 2020-08-17
 * Unit Test defined on 2020-08-17
 */
public class DbSNPMismatch extends ParallelVCFVariantFunction<DbSNPMismatch.MiniVar> {
  public static final String[] HEADERS = {"CHR","POS","ID","REF","ALT", "VEP_Annotation"};
  SortedList<MiniVar> outputs;

  @Override
  public String getSummary() {
    return "Check if there is a discrepancy between the ID Column and the VEP annotation for RS ID.";
  }
  
  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output for the lines with discrepancies have the following format :")
            .addColumns(HEADERS);
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.ANNOTATION_FOR_ALL); }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T, HEADERS)};
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    this.outputs = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_FROM_END);
  }

  @Override
  public String[] processInputVariant(Variant variant) {
    String id = variant.getId();
    String rs = variant.getInfo().getRSs();
    if(rs == null || rs.isEmpty())
      rs = ".";

    for(String current : rs.split(","))
      if(id.equalsIgnoreCase(current))
        return NO_OUTPUT;
      
    
    //id was not found in the list
    this.pushAnalysis(new MiniVar(variant.getChrom(), variant.getPos(), id, variant.getRef(), variant.getAlt(), rs));
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(DbSNPMismatch.MiniVar mini) {
    this.outputs.add(mini);
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {
    ArrayList<String> out = new ArrayList<>();
    for(MiniVar minivar : this.outputs)
      out.add(minivar.toString());
    return out.toArray(new String[0]);
  }
  
  public static class MiniVar implements Comparable<MiniVar>{
    private final String chr;
    private final int pos;
    private final String id;
    private final String ref;
    private final String alt;
    private final String vep;

    MiniVar(String chr, int pos, String id, String ref, String alt, String vep) {
      this.chr = chr;
      this.pos = pos;
      this.id = id;
      this.ref = ref;
      this.alt = alt;
      this.vep = vep;
    }

    @Override
    public int compareTo(MiniVar o) {
      int compare = Variant.compare(this.chr, this.pos, o.chr, o.pos);
      if(compare == 0)
        return (ref+" "+alt+" "+id+" "+vep).compareTo(o.ref+" "+o.alt+" "+o.id+" "+o.vep);
      return compare;
    }

    @Override
    public String toString() {
      return String.join(T, new String[]{chr, pos+"", id, ref, alt, vep});
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
