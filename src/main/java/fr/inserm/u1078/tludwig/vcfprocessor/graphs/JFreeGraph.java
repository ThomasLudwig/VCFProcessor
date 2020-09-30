package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import fr.inserm.u1078.tludwig.maok.tools.Message;
import java.io.File;
import java.io.IOException;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.general.Dataset;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-08-20
 */
public abstract class JFreeGraph extends Graph {

  private JFreeChart chart;
  private Dataset dataset;
  private boolean initialized = false;

  protected abstract Dataset createDataset();

  protected abstract JFreeChart createChart();

  protected abstract void customizeGraph();

  protected abstract String getXAxisLabel();

  protected abstract String getYAxisLabel();

  protected final JFreeChart getChart() {
    return this.chart;
  }

  protected final Dataset getDataset() {
    return this.dataset;
  }

  public final void init() throws GraphException {
    if (!initialized) {
      initialized = true;
      Message.info("Loading Data");
      this.loadData();
      Message.info("Creating Dataset");
      this.dataset = this.createDataset();
      Message.info("Creating Chart");
      this.chart = this.createChart();
      Message.info("Customizing Graph");
      this.customizeGraph();
    }
  }

  public final void writeInPNG(String filename, int width, int height) {
    Message.info("Exporting Graph as png > " + filename);
    ChartRenderingInfo chartInfo = new ChartRenderingInfo();
    chart.createBufferedImage(width, height, chartInfo);
    PlotRenderingInfo plotInfo = chartInfo.getPlotInfo();
    double w = plotInfo.getPlotArea().getWidth();
    double h = plotInfo.getPlotArea().getHeight();

    if (w < h)
      width = (int) (height * w / h);
    else if (h < w)
      height = (int) (width * h / w);

    try {
      ChartUtilities.saveChartAsPNG(new File(filename), chart, width, height);
    } catch (IOException e) {
      System.err.println("Problem occurred creating chart.");
    }
  }

  @Override
  public final void exportGraphAsPNG(String filename, int width, int height) throws GraphException {
    this.init();
    this.writeInPNG(filename, width, height);
  }
}
