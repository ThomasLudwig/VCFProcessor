package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.ColorTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.ArrayList;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-07
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class CompareGenotypesGraph  extends XYLineGraph {
    private final String filename;
    private final String csq;
    public static final double FLOOR = 0.9;    
    public static final int STEP =10;
    public static final int HET = 0;
    public static final int HOM = 1;
    
    //private int[][] countTotal;
    private String[] groups;
    private final ArrayList<int[][]> countsStep; // count[1 + (100/step)][2]   2 : 0/1   1/1
    private final int nbSpikes;
    
    public CompareGenotypesGraph(String filename, String csq){
        this.filename = filename;
        this.csq = csq;
        countsStep = new ArrayList<>();
        this.nbSpikes = 1 + (100/STEP);
    }

    @Override
    protected void loadData() throws GraphException {
        String line = "";
        try{
            UniversalReader in = new UniversalReader(this.filename);
            line = in.readLine();
            String[] f = line.split(T);
          int total = 0;
            int nbGroup = f.length/2;
          int[] totals = new int[nbGroup];
            groups = new String[nbGroup];
            for(int i = 0; i < nbGroup; i++){
                groups[i] = f[i*2];
                totals[i] = Integer.parseInt(f[(i*2+1)]);
                total += totals[i];
                //counts.add(new int[totals[i]+1][2]);
                this.countsStep.add(new int[this.nbSpikes][2]);
            }
            //counts.add(new int[total+1][2]);
            this.countsStep.add(new int[this.nbSpikes][2]);
            in.readLine();
            int read = 0;
            while((line = in.readLine()) != null){
                read++;
                if(read % 10000 == 0)
                    Message.progressInfo(read+" processed");
                f = line.split(T);
                if(contains(f[4], this.csq)){
                    int n = 5;
                    int nHet = Integer.parseInt(f[n]);
                    int nHom = Integer.parseInt(f[n+1]);
                    
                    countsStep.get(countsStep.size()-1)[x(nHet, total)][HET]++; //Total
                    countsStep.get(countsStep.size()-1)[x(nHom, total)][HOM]++; //Total
                    for(int i = 0; i < groups.length; i++){ //Groups
                        n += 2;
                        nHet = Integer.parseInt(f[n]);
                        nHom = Integer.parseInt(f[n+1]);
                        countsStep.get(i)[x(nHet, totals[i])][HET]++;
                        countsStep.get(i)[x(nHom, totals[i])][HOM]++;
                    }
                }
            }
            Message.info(read+" processed");
            in.close();
        } catch(IOException | NumberFormatException e){
            throw new GraphException("Could not load data from "+this.filename+" line :\n"+line, e);
        }
    }
    
    private static boolean contains(String stack, String needle){
        for(String s : stack.split(","))
            if(s.equals(needle))
                return true;
        return false;
    }
    
    public int x(int n, int tot){
      return (100*n)/(STEP*tot);
    }
    
    @Override
    protected XYDataset createXYDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries all0 = new XYSeries("All 0/1");
        XYSeries all1 = new XYSeries("All 1/1");
        int[][] count = countsStep.get(countsStep.size() - 1);
        for(int n = 0 ; n < this.nbSpikes; n++){
            int nHet = count[n][HET];
            int nHom = count[n][HOM];
            all0.add(n * STEP, nHet != 0 ? nHet : FLOOR);
            all1.add(n * STEP, nHom != 0 ? nHom : FLOOR);
        }
        dataset.addSeries(all0);
        dataset.addSeries(all1);
        
        for(int i = 0; i < groups.length; i++){
            XYSeries group0 = new XYSeries(groups[i]+" 0/1");
            XYSeries group1 = new XYSeries(groups[i]+" 1/1");
            count = countsStep.get(i);
            for(int n = 0 ; n < this.nbSpikes; n++){
                int nHet = count[n][HET];
                int nHom = count[n][HOM];
                group0.add(n * STEP, nHet != 0 ? nHet : FLOOR);
                group1.add(n * STEP, nHom != 0 ? nHom : FLOOR);
            }
            dataset.addSeries(group0);
            dataset.addSeries(group1);
        }
        
        return dataset;
    }

    @Override
    protected void customizeGraph() {
        JFreeChart chart = this.getChart();
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        
        plot.setDomainGridlinePaint(Color.gray);
        plot.setDomainMinorGridlinePaint(Color.lightGray);
        plot.setDomainMinorGridlinesVisible(true);
        
        plot.setRangeGridlinePaint(Color.gray);
        
        final XYLineAndShapeRenderer renderer = new SmoothXYLineAndShapeRenderer();

        Shape triangle = ShapeUtilities.createUpTriangle(3);
        Shape circle = new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0);
        
        renderer.setSeriesStroke(HET, new BasicStroke(3f));
        renderer.setSeriesShapesVisible(HET, true);
        renderer.setSeriesLinesVisible(HET, true);
        renderer.setSeriesPaint(HET, Color.BLACK);
        renderer.setSeriesShape(HET, triangle);
        
        renderer.setSeriesStroke(HOM, new BasicStroke(3f));
        renderer.setSeriesShapesVisible(HOM, true);
        renderer.setSeriesLinesVisible(HOM, true);
        renderer.setSeriesPaint(HOM, Color.BLACK);
        renderer.setSeriesShape(HOM, circle);
        for(int i = 0 ; i < this.groups.length; i++){
            int n = (i+1)*2;
            renderer.setSeriesStroke(n+HET, new BasicStroke(1f));
            renderer.setSeriesShapesVisible(n+HET, true);
            renderer.setSeriesLinesVisible(n+HET, true);
            renderer.setSeriesPaint(n+HET, ColorTools.getColor(this.groups[i]));
            renderer.setSeriesShape(n+HET, triangle);
            
            renderer.setSeriesStroke(n+HOM, new BasicStroke(1f));
            renderer.setSeriesShapesVisible(n+HOM, true);
            renderer.setSeriesLinesVisible(n+HOM, true);
            renderer.setSeriesPaint(n+HOM, ColorTools.getColor(this.groups[i]));
            renderer.setSeriesShape(n+HOM, circle);
        }
        
        plot.setRenderer(renderer);

        //X-AXIS
        final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setRange(-1, 101);
        xAxis.setMinorTickCount(5);
        
        final NumberAxis yAxis = new LogarithmicAxis(plot.getRangeAxis().getLabel());
        plot.setRangeAxis(yAxis);
        
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.LEFT);
    }
    
    @Override
    protected String getMainTitle() {
        return "Number of ["+csq+"] with genotypes 0/1 and 1/1";
    }

    @Override
    protected String getXAxisLabel() {
        return "Frequency of genotypes (in % of individuals)";
    }

    @Override
    protected String getYAxisLabel() {
        return "Number of ["+csq+"]";
    }

}
