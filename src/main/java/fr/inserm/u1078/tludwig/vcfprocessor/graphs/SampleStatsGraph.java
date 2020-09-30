package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.ColorTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-08
 * Checked for release on 2020-09-08
 * Unit Test defined on 2020-09-08
 */
public class SampleStatsGraph extends XYLineGraph {

  public static final int TYPE_VAR = 0;//"variants";
  public static final int TYPE_DEPTH = 1;//"depth";
  public static final int TYPE_TSTV = 2;//"tstv";
  public static final int TYPE_HETHOM = 3;//"hethom";
  public static final int TYPE_MISSING = 4;//"missing";
  public static final int[] TYPES = {TYPE_VAR, TYPE_DEPTH, TYPE_TSTV, TYPE_HETHOM, TYPE_MISSING};

  private static final String[] EXT = {"variants", "depth", "tstv", "hethom", "missing"};
  public static final String[] TITLE = {"Number of Variants", "Mean Depth", "TS/TV", "Het/HomAlt", "Missing"};
  private static final int[] INDEX = {7, 6, 11, 13, 4};

  private final String filename;
  private final int type;
  private final String title;
  private final ArrayList<String> allGroups;
  private final ArrayList<String> groups;
  private final HashMap<String, ArrayList<Double>> values;

  private double min = Double.MAX_VALUE;
  private double max = -min;

  public SampleStatsGraph(String filename, String title, int type) {
    this.filename = filename;
    this.title = title;
    this.type = type;
    allGroups = new ArrayList<>();
    groups = new ArrayList<>();
    values = new HashMap<>();
  }

  public String getExtensions() {
    return EXT[type];
  }

  @Override
  protected void loadData() throws GraphException {
    try {
      UniversalReader in = new UniversalReader(this.filename);
      in.readLine();
      String line;
      while ((line = in.readLine()) != null) {
        String[] f = line.split(T);
        String group = f[1];
        this.groups.add(group);
        if (!allGroups.contains(group)){
          allGroups.add(group);
          this.values.put(group, new ArrayList<>());
        }

        double value = Double.parseDouble(f[INDEX[type]]);

        if (value > max)
          max = value;
        if (value < min)
          min = value;
        
        
        this.values.get(group).add(value);
      }
      in.close();
    } catch (Exception e) {
      throw new GraphException("Could not load data from " + this.filename, e);
    }
    
    for(ArrayList<Double> vals : this.values.values())
      Collections.sort(vals);

    double spread = (max - min) * 0.1;
    if (spread == 0)
      spread = 1;
    min -= spread;
    max += spread;
  }

  @Override
  protected XYDataset createXYDataset() {
    XYSeriesCollection dataset = new XYSeriesCollection();
    int i = 0;
    for (String group : allGroups){
      XYSeries series = new XYSeries(capitalize(group));
      for(double val : this.values.get(group))
        series.add(i++, val);      
      
      dataset.addSeries(series);
    }
    return dataset;
  }

  private static String capitalize(String string) {
    return string.toUpperCase().charAt(0) + string.toLowerCase().substring(1);
  }
  

  @Override
  protected void customizeGraph() {
    JFreeChart chart = this.getChart();

    final XYPlot plot = chart.getXYPlot();
    plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
    plot.setBackgroundPaint(Color.white);
    plot.setRangeMinorGridlinesVisible(true);
    plot.setRangeGridlinesVisible(true);

    NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
    int size = 0;
    for(ArrayList<Double> vals : values.values())
      size += vals.size();
    
    xAxis.setRange(-10, size + 10);
    NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
    yAxis.setRange(min, max);

    final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

    Shape shape = new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0);
    for (int g = 0; g < allGroups.size(); g++) {
      renderer.setSeriesShapesVisible(g, true);
      renderer.setSeriesLinesVisible(g, false);
      renderer.setSeriesShape(g, shape);
      renderer.setSeriesPaint(g, ColorTools.getColor(this.allGroups.get(g)));
    }

    plot.setRenderer(renderer);
    LegendTitle legend = chart.getLegend();
    legend.setPosition(RectangleEdge.LEFT);
  }

  @Override
  protected String getMainTitle() {
    return title+" ("+TITLE[type]+")";
  }

  @Override
  protected String getXAxisLabel() {
    return "Individuals";
  }

  @Override
  protected String getYAxisLabel() {
    return TITLE[type];
  }
}
