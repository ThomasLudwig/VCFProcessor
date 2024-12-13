package fr.inserm.u1078.tludwig.vcfprocessor.functions.vcftransform;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.DateTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import static fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description.LEFT_RIGHT_ARROW;
import fr.inserm.u1078.tludwig.vcfprocessor.files.Fasta;
import fr.inserm.u1078.tludwig.vcfprocessor.files.variants.VCF;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FastaFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.FileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;
import java.util.Date;

/**
 * Generates a VCF with Homozygous-to-Reference Genotypes for every given positions and each sample (Alternate is a transition A<->G, C<->T)
 * 
 * @author Thomas E. Ludwig (INSERM - U1078) 
 * Started on             2018-10-30
 * Checked for release on 2020-05-26
 * Unit Test defined on   2020-09-22
 */
public class GenerateHomoRefForPosition extends Function { //TODO Parallelize

  private final FastaFileParameter fasta = new FastaFileParameter();
  private final FileParameter positionFile = new FileParameter(OPT_POS, "positions.tsv", "List of positions in the results VCF file");
  private final FileParameter sampleFile = new FileParameter(OPT_SAMPLE, "samples.txt", "List of samples in the results VCF File");

  public static final String DEFAULT_FORMAT = "GT:DP:GQ:AD:PL";
  public static final String DEFAULT_GENOTYPE = "0/0:30:99:30,0:0,50,500";

  @Override
  public String getOutputExtension() {
    return Function.OUT_VCF;
  }

  @Override
  public String getSummary() {
    return "Generates a VCF with Homozygous-to-Reference Genotypes for every given positions and each sample (Alternate is a transition A"+LEFT_RIGHT_ARROW+"G, C"+LEFT_RIGHT_ARROW+"T)";
  }

  @Override
  public Description getDescription() {
    return new Description("Given a Reference Genome, a list of positions and a list of individual, generates a VCF file with Homozygous-to-Reference Genotypes for every given positions and each sample.")
            .addLine("The reference must be in .fasta format, with its associated .fai index in the same directory")
            .addLine("The position file must contain one position per line, in the format : chr\tpos")
            .addLine("The sample file must contain one sample per line")
            .addLine("Each given position is looked up")
            .addLine("The Ref of the VCF is taken from the given reference genome")
            .addLine("Alt = Transition(Ref) : A"+LEFT_RIGHT_ARROW+"G / C"+LEFT_RIGHT_ARROW+"T")
            .addLine("Format for each position is " + Description.code(DEFAULT_FORMAT))
            .addLine("Each genotypes is " + Description.code(DEFAULT_GENOTYPE))
            //.addWarning("this function only work in a Unix operating system !")
            ;    
  }

  @SuppressWarnings("unused")
  @Override
  public void executeFunction() throws Exception {
    println("##fileformat=VCFv4.1");
    println("##ALT=<ID=NON_REF,Description=\"Represents any possible alternative allele at this location\">");
    println("##FORMAT=<ID=AD,Number=.,Type=Integer,Description=\"Allelic depths for the ref and alt alleles in the order listed\">");
    println("##FORMAT=<ID=DP,Number=1,Type=Integer,Description=\"Approximate read depth (reads with MQ=255 or with bad mates are filtered)\">");
    println("##FORMAT=<ID=GQ,Number=1,Type=Integer,Description=\"Genotype Quality\">");
    println("##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">");
    println("##FORMAT=<ID=PL,Number=G,Type=Integer,Description=\"Normalized, Phred-scaled likelihoods for genotypes as defined in the VCF specification\">");

    println("##contig=<ID=1,length=249250621,assembly=b37>");//TODO get from ref get contig from index http://www.htslib.org/doc/faidx.html
    println("##contig=<ID=2,length=243199373,assembly=b37>");
    println("##contig=<ID=3,length=198022430,assembly=b37>");
    println("##contig=<ID=4,length=191154276,assembly=b37>");
    println("##contig=<ID=5,length=180915260,assembly=b37>");
    println("##contig=<ID=6,length=171115067,assembly=b37>");
    println("##contig=<ID=7,length=159138663,assembly=b37>");
    println("##contig=<ID=8,length=146364022,assembly=b37>");
    println("##contig=<ID=9,length=141213431,assembly=b37>");
    println("##contig=<ID=10,length=135534747,assembly=b37>");
    println("##contig=<ID=11,length=135006516,assembly=b37>");
    println("##contig=<ID=12,length=133851895,assembly=b37>");
    println("##contig=<ID=13,length=115169878,assembly=b37>");
    println("##contig=<ID=14,length=107349540,assembly=b37>");
    println("##contig=<ID=15,length=102531392,assembly=b37>");
    println("##contig=<ID=16,length=90354753,assembly=b37>");
    println("##contig=<ID=17,length=81195210,assembly=b37>");
    println("##contig=<ID=18,length=78077248,assembly=b37>");
    println("##contig=<ID=19,length=59128983,assembly=b37>");
    println("##contig=<ID=20,length=63025520,assembly=b37>");
    println("##contig=<ID=21,length=48129895,assembly=b37>");
    println("##contig=<ID=22,length=51304566,assembly=b37>");
    println("##contig=<ID=X,length=155270560,assembly=b37>");
    println("##contig=<ID=Y,length=59373566,assembly=b37>");
    println("##contig=<ID=MT,length=16569,assembly=b37>");

    println("##reference=file://" + this.fasta.getFullPath());
    println(VCF.getStamp());

    //Get Samples
    StringBuilder header = new StringBuilder(String.join(T, "#CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO", "FORMAT"));
    int nbSample = 0;
    String line;
    try (UniversalReader sIn = sampleFile.getReader()) {
      while ((line = sIn.readLine()) != null) {
        nbSample++;
        header.append(T).append(line);
      }
    }


    Fasta reference = fasta.getFasta();

    println(header.toString());
    try (UniversalReader in = positionFile.getReader()) {
      Date start = new Date();
      int read = 0;
      while ((line = in.readLine()) != null) {
        read++;
        if (read % 10000 == 0) {
          Date now = new Date();
          int seconds = DateTools.durationInSeconds(start, now);
          if (seconds == 0)
            seconds++;
          int speed = read / seconds;
          Message.progressInfo(read + " lines read from " + fasta.getFilename() + " in " + DateTools.durationAsString(start, now) + " (" + speed + " lines/s)");
        }
        String[] f = line.split("\\s+");
        String[] out = new String[9 + nbSample];
        out[VCF.IDX_CHROM] = f[VCF.IDX_CHROM];
        out[VCF.IDX_POS] = f[VCF.IDX_POS];
        out[VCF.IDX_ID] = ".";
        out[VCF.IDX_REF] = "" + reference.getCharacterFor(out[VCF.IDX_CHROM], new Long(out[VCF.IDX_POS]));
        out[VCF.IDX_ALT] = "" + transition(out[VCF.IDX_REF].charAt(0));
        out[VCF.IDX_QUAL] = "2000";
        out[VCF.IDX_FILTER] = "PASS";
        out[VCF.IDX_INFO] = ".";
        out[VCF.IDX_FORMAT] = DEFAULT_FORMAT;
        for (int i = 0; i < nbSample; i++)
          out[VCF.IDX_SAMPLE + i] = DEFAULT_GENOTYPE;
        println(String.join(T, out));
      }
      Date now = new Date();
      int seconds = DateTools.durationInSeconds(start, now);
      if (seconds == 0)
        seconds++;
      int speed = read / seconds;
      Message.info(read + " lines read from " + fasta.getFilename() + " in " + DateTools.durationAsString(start, now) + " (" + speed + " lines/s)");
    }
  }

  private static char transition(char c) {
    switch (c) {
      case 'A':
        return 'G';
      case 'G':
        return 'A';
      case 'C':
        return 'T';
      case 'T':
        return 'C';
      default:
        return '?';
    }
  }
  
  @Override
  public TestingScript[] getScripts() {
    TestingScript ts = TestingScript.newFileTransform();
    ts.addAnonymousFilename("ref", "ref");
    ts.addAnonymousFilename("pos", "pos");
    ts.addAnonymousFilename("sample", "sample");
    return new TestingScript[]{ts};
  }
}
