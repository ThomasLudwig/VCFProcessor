package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantFilterPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.format.PrepareGnomADFile;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.RatioParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Canonical;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RAVAQPrefilter extends ParallelVCFVariantFilterPedFunction {
  private final TSVFileParameter gnomadFiles = new TSVFileParameter(OPT_FILES, "gnomad.list", "A list of gnomAD file, created with "+ PrepareGnomADFile.class.getSimpleName()+" the 1st column is exome/genome, the 2nd is the chrom(as CHR17) and the 3rd is the filename");
  private final StringParameter gnomadSubpop = new StringParameter(OPT_POP, "NFE", "A gnomAD Population");
  private final RatioParameter frq = new RatioParameter(OPT_FRQ,"The frequency threshold F (rare<=F, common>=(1-F), gnomAD>=(1-2F)" );

  private double rare;
  private double common;
  private double gnomADF;
  private String pop;
  private Map<String, Integer> groupSizes;
  private final HashMap<Canonical, PrepareGnomADFile.GnomAD> gnomadExome = new HashMap<>();
  private final HashMap<Canonical, PrepareGnomADFile.GnomAD> gnomadGenome = new HashMap<>();

  @Override
  public String getSummary() {
    return "Prefilters variants according the GnomAD filters and frequencies.";
  }

  @Override
  public Description getDesc() {
    return new Description(getSummary())
        .addLine("The variants are filtered if")
        .addEnumerate(
            "A filter exists in GnomAD",
            "The variant is Frequent in a Set, Frequent in GnomAD and Rare in the other Set"
        );
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.DROP_IF_ONE_FAILS); }

  @Override
  public void begin() {
    super.begin();
    this.rare = frq.getFloatValue();
    this.common = 1 - rare;
    this.gnomADF = 1 - (2*rare);
    this.pop = gnomadSubpop.getStringValue();
    this.groupSizes = this.getPed().getGroupSizes();
    if(this.groupSizes.size() != 2)
      Message.die("Number of groups in the PED must be 2");
    //load gnomAD
    try { loadGnomAD(); }
    catch(IOException e){ Message.fatal("Unable to read gnomAD files", e, true); }
  }

  private void loadGnomAD() throws IOException {
    UniversalReader in = gnomadFiles.getReader();
    String line;
    while ((line = in.readLine()) != null) {
      final String[] f = line.split(T, -1);
      String type = f[0].toLowerCase();
      //String chr = f[1];
      String filename = f[2];
      switch (type) {
        case "exomes":
        case "exome" : loadGnomAD(filename, gnomadExome); break;
        case "genomes":
        case "genome" : loadGnomAD(filename, gnomadGenome); break;
        default: System.err.println("Unknown GnomAD type ["+type+"]");
      }
    }
    in.close();
  }

  private void loadGnomAD(String filename, HashMap<Canonical, PrepareGnomADFile.GnomAD> gnomad) throws IOException {
    UniversalReader in = new UniversalReader(filename);
    String line;
    int count = 0;
    while((line = in.readLine()) != null){
      count++;
      if(count%10000 == 0)
        Message.info("Loaded " + count + " GnomAD variants from " + filename);
      PrepareGnomADFile.GnomAD gnomAD = new PrepareGnomADFile.GnomAD(line);
      gnomad.put(gnomAD.getCanonical(), gnomAD);
    }
    Message.info("Loaded " + count + " GnomAD variants from " + filename);
    in.close();
  }

  @Override
  public String[] processInputVariantForFilter(Variant variant) {
    for (int a : variant.getNonStarAltAllelesAsArray())
      if(filter(variant, a))
        return NO_OUTPUT;
    return asOutput(variant);
  }

  private boolean filter(Variant variant, int a) {
    Canonical canonical = variant.getCanonical(a);
    PrepareGnomADFile.GnomAD exome = gnomadExome.get(canonical);
    PrepareGnomADFile.GnomAD genome = gnomadGenome.get(canonical);

    //No GnomAD data --> no filter
    if(exome == null && genome == null) return false;
    //GnomAD has filters --> filter
    if(hasFilter(exome, genome)) return true;
    //GnomAD variant is rare --> no filter
    if(getMax(exome, genome) < this.gnomADF) return false;

    //GnomAD variant is frenquent --> measure is Set1 is rare and Set2 is frequent (and vice versa)
    Map<String, Double> frqs = variant.getFrequencyByGroup(groupSizes, a);
    double[] af = new  double[frqs.size()];
    int i = 0;
    for(Map.Entry<String, Double> entry : frqs.entrySet())
      af[i++] = entry.getValue();

    //check af1 < rare && af2 > common
    if(af[0] < rare)
      return af[1] > common;
    //check af2 < rare && af1 > common
    if(af[1] < rare)
      return af[0] > common;
    return false;
  }

  private double getMax(PrepareGnomADFile.GnomAD exome, PrepareGnomADFile.GnomAD genome) {
    double max = 0;
    for(PrepareGnomADFile.GnomAD gnomad : new PrepareGnomADFile.GnomAD[]{exome, genome})
      if(gnomad != null)
        for(String s : new String[]{gnomad.getAF(), gnomad.getAF(pop)}){
          try{
            double d = Double.parseDouble(s);
            if(d > max)
              max = d;
          } catch (NumberFormatException ignore) {}
        }
    return max;
  }

  private static boolean hasFilter(PrepareGnomADFile.GnomAD exome, PrepareGnomADFile.GnomAD genome) {
    if(exome != null && hasFilter(exome.getFilter()))
      return true;
    if(genome != null && hasFilter(genome.getFilter()))
      return true;
    return false;
  }

  private static boolean hasFilter(String filter) {
    if(filter == null) return false;
    if(filter.isEmpty()) return false;
    if("PASS".equals(filter)) return false;
    return true;
  }

  @Override
  public TestingScript[] getScripts() {
    return new TestingScript[0];
  }
}
