package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import fr.inserm.u1078.tludwig.maok.tools.ColorTools;
import java.awt.Paint;
import org.jfree.chart.renderer.PaintScale;

/**
 * Paint Scale that is using log values
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-16
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class LogPaintScale implements PaintScale {
  private final double max;

  public LogPaintScale(double max) {
    this.max = max;
  }
  

  @Override
  public double getLowerBound() {
    return 0;
  }

  @Override
  public double getUpperBound() {
    return max;
  }

  @Override
  public Paint getPaint(double d) {
    return ColorTools.getLogColor(d, max);
  }

}
