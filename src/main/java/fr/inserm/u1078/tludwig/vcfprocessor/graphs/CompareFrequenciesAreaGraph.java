package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import fr.inserm.u1078.tludwig.maok.Point;
import fr.inserm.u1078.tludwig.maok.tools.ColorTools;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Collection;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPolygonAnnotation;
import org.jfree.chart.plot.XYPlot;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-08-21
 */
public class CompareFrequenciesAreaGraph extends CompareFrequenciesGraph {

  public static final int NB = 100;
  private double maxP;
  private final Area[][] areas;

  public CompareFrequenciesAreaGraph(Collection<Point> points, String datasetName, String xTitle, String yTitle, double minX, double minY) {
    super(points, datasetName, xTitle, yTitle, minX, minY);
    this.areas = createAreas(minX, minY);
  }

  private Area[][] createAreas(double x, double y) {
    Area[][] as = new Area[NB + 1][NB + 1];
    double logX = Math.log10(x) / NB;
    double logY = Math.log10(y) / NB;
    for (int i = 0; i <= NB; i++) {
      double x1 = Math.pow(10, (NB - (i - .5)) * logX);
      double x2 = Math.pow(10, (NB - (i + .5)) * logX);
      for (int j = 0; j <= NB; j++) {
        double y1 = Math.pow(10, (NB - (j - .5)) * logY);
        double y2 = Math.pow(10, (NB - (j + .5)) * logY);
        as[i][j] = new Area(x1, x2, y1, y2);
      }
    }

    return as;
  }

  private Area getArea(double x, double y) {
    int posX = (NB + 2) / 2;
    int posY = (NB + 2) / 2;
    Area a = null;
    while (posX > -1 && posX < NB + 2 && posY > -1 && posY < NB + 2) {
      a = areas[posX][posY];
      int cX = a.compareX(x);
      int cY = a.compareY(y);
      if (cX == 0 && cY == 0)
        return a;
      posX += cX;
      posY += cY;
    }
    return a;
  }

  @Override
  protected void customizeGraph() {
    super.customizeGraph();

    JFreeChart chart = this.getChart();
    final XYPlot plot = chart.getXYPlot();

    for (Area[] as : areas)
      for (Area area : as)
        plot.addAnnotation(area.getPolygon());

    addLine(plot);
  }

  @Override
  protected void loadData() throws GraphException {
    //super.loadData();
    for (Point p : getPoints())
      try {
        this.getArea(p.x, p.y).add();
      } catch (Exception e) {
        throw new GraphException("Not found x=" + p.x + " y=" + p.y, e);
      }

    maxP = 0;
    for (Area[] as : areas)
      for (Area a : as)
        if (a.getCount() > maxP)
          maxP = a.getCount();

  }

  private Color getColor(double count) {
    Color c = ColorTools.getMonochromeScale(Math.log10(count + 1), 1, Math.log10(maxP + 1), BACKGROUND, FOREGROUND);
    return c;
  }

  public class Area {

    private final double x1, x2, y1, y2;
    private int count;

    public Area(double x1, double x2, double y1, double y2) {
      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
      this.count = 0;
    }

    public int compareX(double x) {
      if (x < x1)
        return -1;
      if (x < x2)
        return 0;
      return 1;
    }

    public int compareY(double y) {
      if (y < y1)
        return -1;
      if (y < y2)
        return 0;
      return 1;
    }

    public void add() {
      this.count++;
    }

    public int getCount() {
      return count;
    }

    public XYPolygonAnnotation getPolygon() {
      Color color = getColor(getCount());
      double[] poly = new double[]{
        x1, y1,
        x1, y2,
        x2, y2,
        x2, y1};
      return new XYPolygonAnnotation(poly, new BasicStroke(1), color, color);
    }

    @Override
    public String toString() {
      return "x[" + x1 + " -> " + x2 + "], y[" + y1 + " -> " + y2 + "] count = " + count;
    }
  }
}
