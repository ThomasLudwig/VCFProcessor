package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-08-20
 */
public abstract class ScatterPlotGraph extends JFreeGraph {

  @Override
  protected final JFreeChart createChart() {
    boolean legend = ((XYSeriesCollection) this.getDataset()).getSeriesCount() > 1;
    return ChartFactory.createScatterPlot(
            this.getMainTitle(), // chart title
            this.getXAxisLabel(), // x axis label
            this.getYAxisLabel(), // y axis label
            (XYSeriesCollection) this.getDataset(), // data
            PlotOrientation.VERTICAL,
            legend, // include legend
            true, // tooltips
            false // urls
    );
  }

  @Override
  protected final Dataset createDataset() {
    return this.createXYDataset();
  }

  protected abstract XYSeriesCollection createXYDataset();
}
