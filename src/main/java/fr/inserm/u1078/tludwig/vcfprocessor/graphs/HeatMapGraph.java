package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-16
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public abstract class HeatMapGraph extends JFreeGraph {

  @Override
  protected final JFreeChart createChart() {
    /*boolean legend = ((XYSeriesCollection) this.getDataset()).getSeriesCount() > 1;
    return ChartFactory.createScatterPlot(
            this.getMainTitle(), // chart title
            this.getXAxisLabel(), // x axis label
            this.getYAxisLabel(), // y axis label
            (XYZDataset) this.getDataset(), // data
            PlotOrientation.VERTICAL,
            legend, // include legend
            true, // tooltips
            false // urls
    );*/

    PaintScale ps = this.getPaintScale();
    
    //TODO add padding around plot to keep it square

    XYPlot plot = new XYPlot((XYZDataset) this.getDataset(), this.getXAxis(), this.getYAxis(), new XYBlockRenderer());
    ((XYBlockRenderer) plot.getRenderer()).setPaintScale(this.getPaintScale());

    JFreeChart chart = new JFreeChart(null, null, plot, false);
    chart.setTitle(this.getMainTitle());
    TextTitle t = chart.getTitle();
    PaintScaleLegend psl = getPaintScaleLegend(ps);

    RectangleInsets r = t.getMargin();
    t.setMargin(r.getTop()+10, r.getLeft(), r.getBottom()+10, r.getRight());
    
    chart.addSubtitle(psl);

    return chart;
  }

  protected abstract NumberAxis getXAxis();

  protected abstract NumberAxis getYAxis();

  protected abstract PaintScale getPaintScale();

  protected PaintScaleLegend getPaintScaleLegend(PaintScale paintScale){
    PaintScaleLegend psl = new PaintScaleLegend(paintScale, new NumberAxis());
    psl.setPosition(RectangleEdge.RIGHT);
    psl.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
    psl.setMargin(100, 5.0, 100, 0.0);
    return psl;
  }

  @Override
  protected final Dataset createDataset() {
    return this.createXYZDataset();
  }

  protected abstract XYZDataset createXYZDataset();
}
