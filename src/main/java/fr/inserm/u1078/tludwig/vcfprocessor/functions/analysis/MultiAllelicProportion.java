package fr.inserm.u1078.tludwig.vcfprocessor.functions.analysis;

import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VariantRecord;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.ParallelVCFFunction;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.VCFPolicies;
import fr.inserm.u1078.tludwig.vcfprocessor.genetics.Variant;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.ArrayList;

/**
 * Slides a 1kb window over the genome and outputs a list of regions ordered by the proportion of multi-allelic variations (desc.)
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2017-08-10
 * Checked for release on 2020-05-12
 * Unit Test defined on   2020-07-08
 */
public class MultiAllelicProportion extends ParallelVCFFunction<Integer[]> {
  private static final String[] HEADER = {"Chr","pos_n","pos_n+Window_size","nb_multialleleic variants"};
  ArrayList<Integer[]>[] keyValues;

  @Override
  public String getSummary() {
    return "Slides a 1kb window over the genome and outputs a list of regions ordered by the proportion of multi-allelic variations (desc.)";
  }

  @SuppressWarnings("unused")
  @Override
  public Description getDesc() {
    return new Description(this.getSummary())
            .addLine("Output format is :")
            .addColumns(HEADER);
  }

  @SuppressWarnings("unused")
  @Override
  public VCFPolicies getVCFPolicies() { return VCFPolicies.nothing(VCFPolicies.MultiAllelicPolicy.NA); }

  @Override
  public String getOutputExtension() {
    return OUT_TSV;
  }

  @SuppressWarnings("unused")
  @Override
  public void begin() {
    keyValues = new ArrayList[22];
    for (int i = 0; i < 22; i++) 
      keyValues[i] = new ArrayList<>();
      
  }

  @Override
  public String[] processInputRecord(VariantRecord record) {
    int chr = Variant.chromToNumber(record.getChrom());
    if (chr <= 22) {
      int value = record.getAlts().length + 1;
      if (value >= 4) {
        this.pushAnalysis(new Integer[]{chr - 1, record.getPos(), value});
      }
    }
    return NO_OUTPUT;
  }
  
  @SuppressWarnings("unused")
  @Override
  public void processAnalysis(Integer[] ckv) {
      keyValues[ckv[0]].add(ckv);
  }

  @SuppressWarnings("unused")
  @Override
  public String[] getHeaders() {
    return new String[]{String.join(T, HEADER)};
  }
  
  @SuppressWarnings("unused")
  @Override
  public String[] getFooters() {  
    Message.info("VCF file parsed, begin sliding windows");
    ArrayList<String> out = new ArrayList<>();
    
    for (int chr = 0; chr < 22; chr++) {
      Message.info("Chromosome " + (chr + 1));
      ArrayList<Integer[]> ckvs = keyValues[chr];
      int[] chrom = new int[ckvs.get(ckvs.size() - 1)[1]];

      Message.info("Building Window");
      for (Integer[] ckv : ckvs)
        chrom[ckv[1] - 1] = 1;

      Message.info("Sliding Window");
      
      int sum = 0;
      for(int i = 0; i < 1000; i++)
        sum += chrom[i];
      if(sum > 0)
        out.add((chr + 1) + T + 1 + T + 1000 + T + sum);//TODO at the moment when several window overlap, only the first one is kept, maybe change END-index
      
      for(int i = 1; i+999 < chrom.length; i++){
        if(chrom[i-1] != chrom[i+999]){
          sum += chrom[i+999] - chrom[i-1];
          if(sum > 0)
            out.add((chr + 1) + T + (i + 1) + T + (i + 1000) + T + sum);
        }
      }
    }
    return out.toArray(new String[0]);
  }
  
  @Override
  public TestingScript[] getScripts() {
    return TestingScript.getSimpleVCFAnalysisScript();
  }
}
