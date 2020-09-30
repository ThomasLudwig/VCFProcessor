package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author Thomas E. Ludwig (INSERM - U1078) 2019-08-20
 */
public abstract class XYLineGraph extends JFreeGraph {

  @Override
  protected final JFreeChart createChart() {
    return ChartFactory.createXYLineChart(
            this.getMainTitle(), // chart title
            this.getXAxisLabel(), // x axis label
            this.getYAxisLabel(), // y axis label
            (XYDataset) this.getDataset(), // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
    );
  }

  @Override
  protected final Dataset createDataset() {
    return this.createXYDataset();
  }

  protected abstract XYDataset createXYDataset();
}
