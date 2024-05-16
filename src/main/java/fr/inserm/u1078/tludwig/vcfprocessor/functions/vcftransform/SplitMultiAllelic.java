package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.files.VCF.InfoFormatHeader;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.HashMap;

/**
 * Splits multiallelic variants into several lines of monoallelic variants
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2020-01-20
 * Checked for release on 2020-08-04
 * Unit Test defined on   2020-08-04
 */
public class SplitMultiAllelic extends ParallelVCFFunction {
  
  private HashMap<String, Integer> pls;

  @Override
  public String getSummary() {
    return "Splits multiallelic variants into several lines of monoallelic variants";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary());
  }

  @SuppressWarnings("unused")
  @Override
  public boolean needVEP() {
    return false;
  }
  
  @SuppressWarnings("unused")
  @Override
  public String getMultiallelicPolicy() {
    return MULTIALLELIC_NA;
  }

  @SuppressWarnings("unused")
  @Override
  public String getCustomRequirement() {
    return null;
  }

  @Override
  public String getOutputExtension() {
    return OUT_VCF;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    super.begin();
    pls = new HashMap<>();
    int n = 0;
    for(int i = 0; i <= 10; i++){
      for(int j = 0; j <= i; j++){
        pls.put(j+"/"+i, n);
        n++;
      }
    }
  }

  @SuppressWarnings("unused")
  @Override
  public boolean checkAndProcessAnalysis(Object analysis) {
    return false;
  }

  @Override
  public String[] processInputLine(String line) {
    String[] f = line.split(T);

    String als = f[VCF.IDX_ALT];
    if (als.contains(",")) {
      String ref = f[VCF.IDX_REF];
      String[] alts = als.split(",");
      String[] infos = f[VCF.IDX_INFO].split(";");
      String[][] out = new String[alts.length][f.length];
      
      String[] ret = new String[out.length];
      for (int a = 0; a < alts.length; a++) {
        System.arraycopy(f, 0, out[a], 0, f.length);
        //produce new ref/alt couples
        String[] newPosRefAlt = getNewPosRefAlt(f[VCF.IDX_POS], ref, alts[a]);
        out[a][VCF.IDX_POS] = newPosRefAlt[0];
        out[a][VCF.IDX_REF] = newPosRefAlt[1];
        out[a][VCF.IDX_ALT] = newPosRefAlt[2];
        String[] outInfos = new String[infos.length];
        for (int i = 0; i < outInfos.length; i++)
          outInfos[i] = convertInfo(infos[i], a);

        out[a][VCF.IDX_INFO] = String.join(";", outInfos);

        for (int i = VCF.IDX_SAMPLE; i < out[a].length; i++)
          out[a][i] = convertGenotype(out[a][i], f[VCF.IDX_FORMAT], a);

        ret[a] = String.join(T, out[a]);
      }
      
      return ret;
    }
    return new String[]{line};
  }

  private String convertInfo(String info, int allele) {
    //for each info, keep as is, or split if number=A or R
    String[] f = info.split("=");
    InfoFormatHeader header = this.getVCF().getInfoHeader(f[0]);
    if (header == null || header.getNumber() == InfoFormatHeader.NUMBER_NONE)
      return info;

    String[] v = f[1].split(",");
    switch (header.getNumber()) {
      case InfoFormatHeader.NUMBER_ALLELES:
        if (v.length <= allele + 1) {
          Message.warning("Could not split info [" + info + "] for allele [" + allele + "]");
          return info;
        }
        return f[0] + "=" + v[0] + "," + v[allele];
      case InfoFormatHeader.NUMBER_ALTS:
        if (v.length <= allele) {
          Message.warning("Could not split info [" + info + "] for allele [" + allele + "]");
          return info;
        }
        return f[0] + "=" + v[allele];
      default:
        return info;
    }
  }

  private String convertGenotype(String genotype, String format, int alt) {
    //TODO careful on splitting AD/ PL/ ....
    //for each genotype, 0->0, x->1, y->0
    int allele = alt+1;
    String[] g = genotype.split(":");
    String[] f = format.split(":");

    //TODO case of missing
    String sep = "/";
    String[] gt = g[0].split("\\/");
    if (gt.length == 1) {
      sep = "|";
      gt = g[0].split("\\|");
    }

    //process GT
    for (int i = 0; i < gt.length; i++){
      if(gt[i].equals(allele+""))
        gt[i] = "1";
      else if (!gt[i].equals("."))
        gt[i] = "0";      
    }

    g[0] = String.join(sep, gt);

    //process the rest
    for (int i = 1; i < f.length && i < g.length; i++)
      if (!g[i].equals(".")) {
        String[] v = g[i].split(",");
        InfoFormatHeader header = this.getVCF().getFormatHeader(f[i]);
        if (header == null)
          Message.warning("Undefined Genotype Format [" + f[i] + "]");
        else
          switch (header.getNumber()) {
            case InfoFormatHeader.NUMBER_ALLELES:
              if (v.length <= allele) {
                Message.warning("Could not split genotype annotation [" + genotype + "] with format [" + format + "] for allele [" + alt + "]");
                return genotype;
              } else
                g[i] = v[0] + "," + v[allele];
              break;
            case InfoFormatHeader.NUMBER_ALTS:
              if (v.length <= alt) {
                Message.warning("Could not split genotype annotation [" + genotype + "] with format [" + format + "] for allele [" + alt + "]");
                return genotype;
              } else
                g[i] = v[alt];
              break;
            case InfoFormatHeader.NUMBER_GENOTYPES:
              if (v.length <= allele) {
                Message.warning("Could not split genotype annotation [" + genotype + "] with format [" + format + "] for allele [" + alt + "]");
                return genotype;
              } else{
                g[i] = v[0]+","+v[pls.get("0/"+allele)]+","+v[pls.get(allele+"/"+allele)]; //TODO if PL is updated, should GQ be updated also ?, i don't think so, because 1) gq is already affected, 2) genotype will not really change              
              }
              break;
            default:
              break;
          }
      }
    return String.join(":", g);
  }

  public static String[] getNewPosRefAlt(String oldPos, String oldRef, String oldAlt) {
    //Only look at the right part and not the left
    //Simplifying the left part would result in a change of position
    //While this could be more relevant biologically, it could create all sorts of problem
    //Especially if the new variants is co-localized with another pre-existing variant
    //The result here is the same as the one from vcftools
    
    String ref = oldRef;
    String alt = oldAlt;
    int rl = ref.length();
    int al = alt.length();
    int suffix = 0;
    int max = Math.min(rl, al);
    for(int i = 1; i < max && ref.charAt(rl-i) == alt.charAt(al-i); i++)
      suffix++;
    
    if(suffix > 0){
      ref = ref.substring(0, rl - suffix);
      alt = alt.substring(0, al-suffix);
    }
    
    return new String[]{oldPos, ref, alt};
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFTransformScript();
  }
}
