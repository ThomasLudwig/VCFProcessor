package fr.inserm.u1078.tludwig.vcfprocessor.functions.other;

import fr.inserm.u1078.tludwig.maok.LineBuilder;
import fr.inserm.u1078.tludwig.maok.NumberSeries;
import fr.inserm.u1078.tludwig.maok.SortedList;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.vcfprocessor.documentation.Description;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.Function;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.StringParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.functions.parameters.TSVFileParameter;
import fr.inserm.u1078.tludwig.vcfprocessor.testing.TestingScript;

import java.util.ArrayList;

/**
 * Gets the coverage statistics for an input file
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on             2020-09-30
 * Checked for release on 2020-09-30
 * Unit Test defined on   2020-09-30
 */
public class CoverageStats extends Function {
    private final TSVFileParameter tsv = new TSVFileParameter(OPT_TSV, "cov.tsv.gz", "File containing depth-of-coverage");
    private final StringParameter chr = new StringParameter(OPT_CHROM, "chr1", "Chromosome name");
    private static final int[] DEPTHS = new int[]{1,5,10,15,20,25,30,40,50,100};
    private static final String[] HEADER = {"chr","pos","mean","median","1","5","10","15","20","25","30","40","50","100"};


    @Override
    public String getSummary() {
        return "Gets the coverage statistics for an input file";
    }

    @Override
    public Description getDescription() {
        return new Description(this.getSummary())
                .addLine("The input file has one line per chromosome position [1-chrSize] and one column per sample. Each cell contains the depth of coverage for the given sample at the given position.")
                .addLine("The output format is :")
                .addColumns(HEADER);
    }

    @Override
    public String getOutputExtension() {
        return OUT_TSV;
    }

    @Override
    public void executeFunction() throws Exception {
        UniversalReader in = tsv.getReader();
        String line;
        int pos = 0;
        while((line = in.readLine()) != null){
            pos++;
            NumberSeries s = new NumberSeries("", SortedList.Strategy.SORT_AFTERWARDS);
            LineBuilder lb = new LineBuilder(this.chr.getStringValue());
            lb.addColumn(pos);
            String[] f = line.split("\\s+",-1);
            for(String g : f)
                s.add(new Integer(g));
            lb.addColumn(s.getMean());
            lb.addColumn(s.getMedian());
            int l = s.size();
            ArrayList<Double> v = s.getAllValues();
            int i = 0;
            for(int depth : DEPTHS){
                while (i < l) {
                    if (v.get(i) >= depth) {
                        break;
                    }
                    i++;
                }
                lb.addColumn(l - i);
            }
            println(lb.toString());
        }
        in.close();
    }

    @Override
    public TestingScript[] getScripts() {
        return TestingScript.getEmpty();
    }


}
