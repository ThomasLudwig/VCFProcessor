package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcffilter;

import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFVariantPedFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BooleanParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Genotype;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Abstract class extended by function looking for Compound Heterozygous Variants
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-08-13
 * Checked for release on 2020-08-13
 * Unit Test defined on 2020-08-13
 */
public abstract class AbstractCompoundFunction extends ParallelVCFVariantPedFunction<AbstractCompoundFunction.Analysis> {

  public final BooleanParameter noHomo = new BooleanParameter(OPT_NO_HOMO, "Reject if a case is homozygous to alternate allele or if a control has none of the allele ?");
  public static final String FIELD = "COMPOUND";
  public static final String DEFINITION = "##INFO=<ID=" + FIELD + ",Number=.,Type=String,Description=\"Partner variants and affected genes, when the variant is involved in a Compound Heterozygous couple. Format : AlleleNumber1>PartnerVariant1(geneA|geneB|geneC)&PartnerVariant1(geneD|geneE|geneF),AlleleNumber2>PartnerVariant3(geneG|geneH|geneI)&PartnerVariant4(geneJ|geneK|geneL),... \">";

  private SortedList<Variant> results;
  private HashMap<String, HashMap<Variant, ArrayList<Integer>>> byGenes;
  /**
   * Variant is current variant, Integer is concerned allele, String is partner chr:pos:ref:alt, list<String> is list of genes
   * No need to use a SortedList, genes were previously sorted and are added in that order
   */
  private HashMap<Variant, HashMap<Integer, HashMap<Partner, SortedList<String>>>> annotations;
  private int kept = 0;

  public static final Description WARNING = new Description("It might be difficult to read results, since several combination of valid variants might exist. "
          + "So an extra INFO field " + FIELD + " is added detailing the variants relation.")
          .addLine("This field reads as")
          .addLine(Description.code(FIELD + "=A1>P1(gA|gB|gC)&P2(gD|gE|gF),A2>P3(gG|gH|gI)&P4(gJ|gK|gL),..."))
          .addLine("Where:").addEnumerate(Description.code("Ax") + " is the number of the allele involved",
          Description.code("Px") + " is the partner allele in form chr:pos:ref:alt",
          Description.code("gX") + " is the symbol of the gene common to this allele and it partner");

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.onlyVEP(VCFPolicies.MultiAllelicPolicy.KEEP_IF_ONE_SATISFY); }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getExtraHeaders() {
    return new String[]{DEFINITION};
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    this.results = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_INSERT_SORT);
    this.byGenes = new HashMap<>();
    this.annotations = new HashMap<>();
  }

  public abstract boolean isValidCandidate(Genotype[] genos, int a);

  public abstract boolean areCompound(Genotype[] g1, int a, Genotype[] g2, int b);

  @Override
  public final String[] processInputVariant(Variant variant) {
    Genotype[] genos = variant.getGenotypes();
    HashMap<Integer, String[]> genesByAllele = new HashMap<>();
    for (int a = 1; a < variant.getAlleles().length; a++)
      if (isValidCandidate(genos, a)) // Is the allele is a valid candidate, keep it
        genesByAllele.put(a, variant.getGeneList(a));

    if (!genesByAllele.isEmpty())
      this.pushAnalysis(new Analysis(variant, genesByAllele));
    return NO_OUTPUT;
  }

  @SuppressWarnings("unused")
  @Override
  public final void processAnalysis(Analysis analysis) {
    Variant variant = analysis.getVariant();
    HashMap<Integer, String[]> genesByAllele = analysis.genesByAllele;
    kept++;

    for (int allele : genesByAllele.keySet())
      //Add variant for each gene
      for (String gene : genesByAllele.get(allele))
        if (!gene.isEmpty()) {
          HashMap<Variant, ArrayList<Integer>> map = this.byGenes.computeIfAbsent(gene, k -> new HashMap<>());
          ArrayList<Integer> alleles = map.computeIfAbsent(variant, k -> new ArrayList<>());
          alleles.add(allele);
        }
  }

  public static class Analysis {
    private final Variant variant;
    private final HashMap<Integer, String[]> genesByAllele;

    public Analysis(Variant variant, HashMap<Integer, String[]> genesByAllele) {
      this.variant = variant;
      this.genesByAllele = genesByAllele;
    }

    public Variant getVariant() {
      return variant;
    }

    public HashMap<Integer, String[]> getGenesByAllele() {
      return genesByAllele;
    }
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getFooters(){
    ArrayList<String> out = new ArrayList<>();
    //Here there is a list of variant per gene, such has all variants are OK for the cases
    //Need to remove variants not valid for the controls...
    //take each pair of variants, if a pair is valid, add both variants (and annotate variants)
    Message.info("After looking at cases genotypes, there are " + kept + " variants left, from " + this.byGenes.keySet().size() + " genes.");
    double total = this.byGenes.keySet().size();
    int w = 0;
    int p = -1;
    for (String gene : this.byGenes.keySet()) {
      w++;
      int np = (int) (100 * w / total);
      if (np > p) {
        p = np;
        Message.progressInfo("Processed genes " + p + "% : " + w);
      }
      HashMap<Variant, ArrayList<Integer>> alleleMap = this.byGenes.get(gene);
      ArrayList<Variant> variants = new ArrayList<>(alleleMap.keySet());

      for (int i = 0; i < variants.size() - 1; i++) {
        Variant v1 = variants.get(i);
        Genotype[] g1 = v1.getGenotypes();
        for (int a : alleleMap.get(v1))
          for (int j = i + 1; j < alleleMap.size(); j++) {
            Variant v2 = variants.get(j);
            Genotype[] g2 = v2.getGenotypes();
            for (int b : alleleMap.get(v2))
              if (areCompound(g1, a, g2, b)) {
                addAnnotation(v1, a, v2, b, gene);
              }
          }
      }
    }
    Message.info("Processed genes 100% : " + w);

    //Then export the results List
    Message.info("Writing results (" + results.size() + " variants left after looking at controls genotypes)");
    total = results.size();
    p = -1;
    w = 0;
    for (Variant variant : results) {
      w++;
      int np = (int) (100 * w / total);
      if (np > p) {
        p = np;
        Message.progressInfo("Output written " + p + "% (" + w + " variants)");
      }

      variant.addInfo(getAnnotation(variant));
      out.add(asOutput(variant)[0]);
    }
    Message.info("Output written 100% (" + w + " variants)");
    return out.toArray(new String[0]);
  }

  //To store Annotation there are three levels
  //First a map of the variants refers to a map of allele --mostly one allele per variant
  //Second the map of alleles refers to a map of partner --mostly one partner per allele
  //where partner is chr:pos:ref:alt
  //Third the map of partner points to a list of genes 
  private void addAnnotation(Variant v1, int a, Variant v2, int b, String gene) {
    doAddAnnotation(v1, a, v2, b, gene);
    doAddAnnotation(v2, b, v1, a, gene);
  }

  private void doAddAnnotation(Variant variant, int num, Variant partnerV, int allele, String gene) {
    HashMap<Integer, HashMap<Partner, SortedList<String>>> alleleMap = annotations.get(variant);
    if (alleleMap == null) {
      alleleMap = new HashMap<>();
      annotations.put(variant, alleleMap);
      results.add(variant);
    }

    HashMap<Partner, SortedList<String>> partnerMap = alleleMap.computeIfAbsent(num, ignoredK -> new HashMap<>());

    Partner partner = new Partner(partnerV, allele);
    SortedList<String> geneList = partnerMap.get(partner);
    if (geneList == null) {
      geneList = new SortedList<>(new ArrayList<>(), SortedList.Strategy.ADD_INSERT_SORT);
      partnerMap.put(partner, geneList);
    }

    geneList.add(gene);

  }

  //Here order, first by allele, then by partner, then order genes
  private String getAnnotation(Variant v) {
    ArrayList<String> byAlleles = new ArrayList<>();
    SortedList<Integer> sortedAlleles = new SortedList<>(annotations.get(v).keySet(), SortedList.Strategy.ADD_INSERT_SORT);
    for (int a : sortedAlleles) {
      HashMap<Partner, SortedList<String>> partnerMap = annotations.get(v).get(a);
      ArrayList<String> byPartner = new ArrayList<>();
      for (Partner partner : new SortedList<>(partnerMap.keySet(), SortedList.Strategy.ADD_INSERT_SORT))
        byPartner.add(partner + "(" + String.join("|", partnerMap.get(partner)) + ")");

      byAlleles.add(a + ">" + String.join("&", byPartner));
    }

    return FIELD + "=" + String.join(",", byAlleles);
  }
  
  private static class Partner implements Comparable<Partner> {
    private final String chr;
    private final int pos;
    private final String ret;
    private final String alt;
    private final int allele;

    Partner(Variant v, int allele) {
      this.chr = v.getChrom();
      this.pos = v.getPos();
      this.ret = v.getRef();
      this.alt = v.getAllele(allele);
      this.allele = allele;
    }

    @Override
    public int compareTo(Partner o) {
      int comp = Variant.compare(this.chr, this.pos, o.chr, o.pos);
      return comp == 0 ? this.allele - o.allele : comp;
    }

    @Override
    public String toString() {
      return chr + ":" + pos + ":" + ret + ":" + alt;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.chr, this.pos, this.ret, this.alt, this.allele);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final Partner other = (Partner) obj;
      if (this.pos != other.pos)
        return false;
      if (this.allele != other.allele)
        return false;
      if (!Objects.equals(this.chr, other.chr))
        return false;
      if (!Objects.equals(this.ret, other.ret))
        return false;
      if (!Objects.equals(this.alt, other.alt))
        return false;
      return true;
    }
  }
}
