package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.BooleanParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

/**
 * Affect a genotype for each sample, for each position from the GenotypeProbability annotation
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2017-01-12
 * Checked for release on 2020-05-26
 * Unit Test defined on   2020-08-04
 */
public class SetGenotypeFromProbability extends ParallelVCFFunction {

  private final BooleanParameter overwrite = new BooleanParameter(OPT_OVER, "overwrite existing genotypes ?");
  
  private static final String[] GENOS = new String[]{"0/0", "0/1", "1/1"};

  @Override
  public String getSummary() {
    return "Affect a genotype for each sample, for each position from the GenotypeProbability annotation. If a genotype is already present, it can be kept or replaced";
  }

  @Override
  public Description getDesc() {
    return new Description("The highest probabilty (given by annotation GP=p1,p2,p3) determines the genotype that will be affect")
            .addItemize(new String[]{
              "highest=p1 "+Description.RIGHT_ARROW+" 0/0",
              "highest=p2 "+Description.RIGHT_ARROW+" 0/1",
              "highest=p3 "+Description.RIGHT_ARROW+" 1/1"
            })
            .addLine("If a genotype is already present, it can be kept or replaced");
            
  }

  @Override
  public boolean needVEP() {
    return false;
  }
  
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_FORBIDDEN;
  }

  @Override
  public String getCustomRequierment() {
    return "The input VCF file must contain Genotype Probability (GP=p1,p2,p3) for each genotype";
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  private String getGenotype(String sample, int p) {
    String[] f = sample.split(":");
    String geno = f[0];

    if (geno.equals("./.") || this.overwrite.getBooleanValue()) {
      String[] ps = f[p].split(",");
      double[] probas = new double[]{new Double(ps[0]), new Double(ps[1]), new Double(ps[2])};
      int max = 0;
      if (probas[1] > probas[0])
        max = 1;
      if (probas[2] > probas[max])
        max = 2;
      geno = GENOS[max];
      String ret = geno;
      for (int i = 1; i < f.length; i++)
        ret += ":" + f[i];
      return ret;
    }
    return sample;
  }

  @Override
  public String[] processInputLine(String line) {
    String[] f = line.split(T);
    if(f[VCF.IDX_ALT].split(",").length > 1){
      Message.warning("Can't process line why more than 1 alternate allele ["+Variant.shortString(" ", f)+"].");
      return NO_OUTPUT;
    }
      

    String[] format = f[8].split(":");
    int p = -1;
    for (int i = 0; i < format.length; i++)
      if (format[i].equalsIgnoreCase("GP"))
        p = i;

    if (p == -1) {
      Message.warning("Missing GP field in line  ["+Variant.shortString(" ", f)+"].");
      return NO_OUTPUT;
    }

    for (int i = VCF.IDX_SAMPLE; i < f.length; i++)
      f[i] += T + getGenotype(f[i], p);

    return new String[]{String.join(T, f)};
  }
  
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript sFalse = TestingScript.newFileTransform();
    sFalse.addAnonymousFilename("vcf", "vcf");
    sFalse.addNamingValue("overwrite", "false");
    
    TestingScript sTrue = TestingScript.newFileTransform();
    sTrue.addAnonymousFilename("vcf", "vcf");
    sTrue.addNamingValue("overwrite", "true");
    
    return new TestingScript[]{sFalse, sTrue};
  }
}
