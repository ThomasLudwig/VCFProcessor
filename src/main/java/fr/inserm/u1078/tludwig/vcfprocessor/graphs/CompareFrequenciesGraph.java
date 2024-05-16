package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import fr.inserm.u1078.tludwig.maok.Point;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-08-20
 */
public class CompareFrequenciesGraph extends ScatterPlotGraph {

  public static final Color BACKGROUND = Color.BLACK;
  public static final Color FOREGROUND = Color.WHITE;
  //public static Color BACKGROUND = Color.WHITE; public static Color FOREGROUND = Color.BLACK;

  private final Collection<Point> points;
  private final String datasetName;
  private final String xTitle;
  private final String yTitle;
  private final double minX;
  private final double minY;

  private final XYSeries series = new XYSeries("Frequencies");

  public CompareFrequenciesGraph(Collection<Point> points, String datasetName, String xTitle, String yTitle, double minX, double minY) {
    this.points = points;
    this.datasetName = datasetName;
    this.xTitle = xTitle;
    this.yTitle = yTitle;
    this.minX = minX;
    this.minY = minY;
  }

  @Override
  protected void loadData() throws GraphException {
    for (Point point : points)
      series.add(Math.max(minX, point.x), Math.max(minY, point.y));
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

    final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

    plot.setBackgroundPaint(BACKGROUND);
    for (int s = 0; s < 1; s++) {
      Color old = Color.GREEN;
      Color c = new Color(old.getRed(), old.getGreen(), old.getBlue(), 50);
      renderer.setSeriesPaint(s, c);
      renderer.setSeriesLinesVisible(s, false);
      renderer.setSeriesShapesVisible(s, true);
      renderer.setSeriesShape(s, new Ellipse2D.Double(-.5, -.5, 1, 1));
      renderer.setSeriesShapesFilled(s, false);
    }

    addLine(plot);

    plot.setRenderer(renderer);
  }

  void addLine(XYPlot plot) {
    double min = Math.min(minX, minY);
    XYLineAnnotation line = new XYLineAnnotation(min, min, 1, 1, new BasicStroke(1.5f), Color.RED);
    plot.addAnnotation(line);
  }

  public void setLinear() {
    JFreeChart chart = this.getChart();
    final XYPlot plot = chart.getXYPlot();
    final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
    NumberTickUnit ntu = new NumberTickUnit(.1);
    xAxis.setRange(-.01, 1.01);
    xAxis.setMinorTickCount(0);
    xAxis.setTickUnit(ntu);
    final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
    yAxis.setRange(-.01, 1.01);
    yAxis.setMinorTickCount(0);
    yAxis.setTickUnit(ntu);
  }

  public void setLog() {
    JFreeChart chart = this.getChart();
    final XYPlot plot = chart.getXYPlot();
    final NumberAxis xAxis = new LogarithmicAxis(plot.getDomainAxis().getLabel());
    xAxis.setAutoTickUnitSelection(true);
    xAxis.setRange(0.9 * minX, 1.1);
    plot.setDomainAxis(xAxis);
    final NumberAxis yAxis = new LogarithmicAxis(plot.getRangeAxis().getLabel());
    yAxis.setAutoTickUnitSelection(true);
    yAxis.setRange(0.9 * minY, 1.1);
    plot.setRangeAxis(yAxis);
  }

  @Override
  protected String getXAxisLabel() {
    return this.xTitle;
  }

  @Override
  protected String getYAxisLabel() {
    return this.yTitle;
  }

  @Override
  protected String getMainTitle() {
    return "Frequencies comparison (" + this.datasetName + ")";
  }

  @Override
  protected XYSeriesCollection createXYDataset() {
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(series);
    return dataset;
  }

  double getMinX() {
    return minX;
  }

  double getMinY() {
    return minY;
  }

  Collection<Point> getPoints() {
    return points;
  }

  public void exportPNG(String filename, int width, int height, boolean inLog) throws GraphException {
    this.init();
    if (inLog)
      this.setLog();
    else
      this.setLinear();
    this.writeInPNG(filename, width, height);
  }
}
