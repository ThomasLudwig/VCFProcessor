package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import fr.inserm.u1078.tludwig.maok.UniversalReader;
import java.io.IOException;
import java.util.ArrayList;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * Graph Function for JFS Results
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-15
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
//TODO set range -.5 ; MAX+.5
//TODO use log color in the other function
public class JFSGraph extends HeatMapGraph {

  private final String filename;
  private final String title;
  private final String xLabel;
  private final String yLabel;
  private ArrayList<int[]> counts;
  private int maxValue = 0;
  private final String maxScale;

  public JFSGraph(String filename, String title, String xLabel, String yLabel, String maxScale) {
    this.filename = filename;
    this.title = title;
    this.xLabel = "Number of Alleles for " + xLabel;
    this.yLabel = "Number of Alleles for " + yLabel;
    this.maxScale = maxScale;
  }

  @Override
  protected String getXAxisLabel() {
    return this.xLabel;
  }

  @Override
  protected String getYAxisLabel() {
    return this.yLabel;
  }

  @Override
  protected void loadData() throws GraphException {
    try {
      this.counts = new ArrayList<>();
      UniversalReader in = new UniversalReader(this.filename);
      String line;

      while ((line = in.readLine()) != null) {
        String[] f = line.split("\\s+", -1);
        int[] values = new int[f.length];
        for (int x = 0; x < f.length; x++){
          int val = new Integer(f[x]);
          if(val > this.maxValue)
            this.maxValue = val;
          values[x] = val;
        }
        this.counts.add(values);
      }

      in.close();
    } catch (IOException e) {
      throw new GraphException(e);
    }
  }

  @Override
  protected String getMainTitle() {
    return this.title;
  }

  @Override
  protected void customizeGraph() {

  }

  @Override
  protected XYZDataset createXYZDataset() {
    DefaultXYZDataset ret = new DefaultXYZDataset();
    double[][] xyz = new double[3][this.counts.size() * this.counts.get(0).length];
    for (int y = 0; y < this.counts.size(); y++)
      for (int x = 0; x < this.counts.get(y).length; x++) {
        int i = y * this.counts.get(y).length + x;
        xyz[0][i] = x;
        xyz[1][i] = y;
        xyz[2][i] = this.counts.get(y)[x];
      }
    ret.addSeries("Number of Variants", xyz);
    return ret;
  }

  @Override
  protected NumberAxis getXAxis() {
    NumberAxis axis = new NumberAxis(this.getXAxisLabel());
    axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    axis.setRange(-0.5, this.counts.get(0).length - .05);
    return axis;
  }

  @Override
  protected NumberAxis getYAxis() {
    NumberAxis axis = new NumberAxis(this.getYAxisLabel());
    axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    axis.setRange(-0.5, this.counts.size() - .05);
    return axis;
  }

  @Override
  protected PaintScale getPaintScale() {
    int max = this.maxValue;
    try {
      max = Integer.parseInt(this.maxScale);
    } catch (NumberFormatException e) {
      //Ignore, if not parsable, it means local max
    }
    return new LogPaintScale(max);
  }
}
