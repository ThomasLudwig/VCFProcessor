package fr.inserm.u1078.tludwig.vcfprocessor.graphs;

import fr.inserm.u1078.tludwig.maok.Point;
import fr.inserm.u1078.tludwig.maok.SVG;
import fr.inserm.u1078.tludwig.maok.UniversalReader;
import fr.inserm.u1078.tludwig.maok.tools.ColorTools;
import fr.inserm.u1078.tludwig.maok.tools.Message;

import java.awt.Color;
import java.io.IOException;

/**
 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX CLASS DESCRIPTION XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 *
 * @author Thomas E. Ludwig (INSERM - U1078)
 * Started on 2020-09-07
 * Checked for release on xxxx-xx-xx
 * Unit Test defined on xxxx-xx-xx
 */
public class F2Graph extends Graph {
private int[][] f2;
    private String[] groups;
    private final String filename;
    private SVG svg;
    private double size;
    private final String legend;
    private static final int L = 2;

    public F2Graph(String filename, String legend) {
        this.filename = filename;
        this.legend = legend;
    }

    @Override
    protected void loadData() throws GraphException {
        try(UniversalReader in = new UniversalReader(this.filename)){
            String line = in.readLine();
            String[] f = line.split("\\s+");
            this.groups = new String[f.length - 2];
            System.arraycopy(f, 1, this.groups, 0, this.groups.length);
            this.f2 = new int[this.groups.length][f.length-1];
            
            for(int i = 0 ; i < this.groups.length; i++){
                line = in.readLine();
                f = line.split("\\s+");
                for(int j = 0 ; j < f.length-1; j++)
                    this.f2[i][j] = Integer.parseInt(f[j+1]);
            }
        } catch(IOException e){
            throw new GraphException("Unable to load Data from "+this.filename, e);
        }

    }

    @Override
    protected String getMainTitle() {
        return "F2 Variants";
    }
    
    private String getLegend(){
        return this.legend;
    }
    
    private void draw(){
        final double x = 0;
        final double y = 0;
        final double tmpWidth = size;
        final double m = 0.025 * tmpWidth;
        final double lw = 0.27 * tmpWidth;
        final double lh = 0.46 * tmpWidth;
        final double var =  0.17 * tmpWidth;

        final double width = m + lw + m + lh + m + var + m; //correcting round up/down

        final double o = 0 + m;
        final double a = o + lw + m;
        final double b = a + lh + m;
        final double lg = lh;
        final double height = b + lg;

        svg.rectangle(new Point(x, y), width-L, height-L, L, Color.white, Color.black);

        drawTopLeftTitle(x + o, y + o, lw, lw);
        drawLeftLegend(x + o, y + a, lw, lh);
        drawTopLegend(x + a, y + o, lh, lw);
        drawTopVariantLegend(x + b, y + o, var, lw);
        drawMainF2(x + a, y + a, lh, lh);
        drawVariants(x + b, y + a, var, lh);
        drawGraphLegend(x, y + b);//, width, lg);
    }
    
    private void drawTopLeftTitle(double x, double y, double width, double height) {
        //out.println(SVG.rectangle(x, y, width, height, L, Color.pink, Color.black));
        int nbLetter = "fxx".length() * 2;
        double size = (width * 5) / (nbLetter * 3);
        double th = (size * 3) / 5;
        if (th > height) {
            size = (size * height) / th;
        }
        th = (size * 3) / 5;
        svg.text(new Point(x + (width / 2), y + (height + th) / 2), size, true, false, "middle", Color.BLACK, "f2");
    }

    private void drawGraphLegend(double x, double y) {
        double width = size;
        double height = size-y;
        svg.rectangle(new Point(x, y), width-L, height-L, L, Color.white, Color.black);
        int nbLetter = 50;
        
        double s = (width * 5) / (nbLetter * 3);
        double th = (s * 3) / 5;
        if (th > height) {
            s = (s * height) / th;
        }
        th = (s * 3) / 5;
        String l = this.getLegend();
        if (l.length() > nbLetter) {
            l = l.substring(0, nbLetter - 3) + "...";
        }
        System.out.println(l);
        svg.text(new Point(x + (width / 2), y + (height + th) / 2), s, true, false, "middle", Color.BLACK, l);
    }

    public void drawLeftLegend(double x, double y, double width, double height) {
        //out.println(SVG.rectangle(x, y, width, height, L, Color.pink, Color.black));
        double line = height / groups.length;
        for (int i = 0; i < groups.length; i++) {
            drawLeftLegendLine(i, x, y + (i * line), width, line);
        }
    }

    private void drawLeftLegendLine(int i, double x, double y, double width, double height) {
        double offset = 0.15 * height;
        //double newY = y + offset;
        double newHeight = height - 2 * offset;
        String group = this.groups[i];
        int nbLetter = getMaxLetter(this.groups);
        double size = (width * 5) / (nbLetter * 3);
        double th = (size * 3) / 5;
        if (th > newHeight) {
            size = (size * newHeight) / th;
        }
        //double space = (newHeight - th) / 2;
        svg.text(new Point(x + width - 1, y + height - 1), size, true, false, "end", ColorTools.getColor(group), group);
    }

    private static int getMaxLetter(String[] strings) {
        int max = 0;
        for (String string : strings) {
            if (max < string.length()) {
                max = string.length();
            }
        }
        return max;
    }
    
    private void drawTopLegend(double x, double y, double width, double height) {
        //out.add(SVG.rectangle(x, y, width, height, L, Color.green, Color.black));
        double col = width / this.groups.length;
        for (int i = 0; i < this.groups.length; i++) {
            drawTopLegendColumn(i, x + (i * col), y, col, height);
        }
    }

    private void drawTopLegendColumn(int i, double x, double y, double width, double height) {
        double offset = 0.15 * width;
        double newX = x + offset;
        double newWidth = width - 2 * offset;
        String group = this.groups[i];//.substring(0,3);
        int nbLetter = getMaxLetter(this.groups);
        double size = (height * 5) / (nbLetter * 3);
        double tw = (size * 3) / 5;
        if (tw > newWidth) {
            size = (size * newWidth) / tw;
        }
        double space = (newWidth - tw) / 2;
        svg.text(new Point(newX + (newWidth - space) - 1, y + height - 1), size, true, true, "start", ColorTools.getColor(group), group);

    }

    private void drawTopVariantLegend(double x, double y, double width, double height) {
        //out.println(SVG.rectangle(x, y, width, height, L, Color.red, Color.black));
        double offset = 0.15 * width;
        double newX = x + offset;
        double newWidth = width - 2 * offset;
        //String text = "f₂ variants";
        String text = "variants";
        int nbLetter = text.length();
        double size = (height * 5) / (nbLetter * 3);
        double tw = (size * 3) / 5;
        if (tw > newWidth) {
            size = (size * newWidth) / tw;
        }
        //int space = (newWidth-tw)/2;
        svg.text(new Point(newX + offset + 1, y + height - 1), size, false, true, "start", Color.black, text);
    }

    private void drawMainF2(double x, double y, double width, double height) {
        double line = height / this.groups.length;
        double col = width / this.groups.length;
        for (int l = 0; l < this.groups.length; l++) { //lines
            for (int c = 0; c < this.groups.length; c++) { //columns
                drawF2(l, c, x + (c * col), y + (l * line), col, line);
            }
        }
    }

    private void drawF2(int g, int h, double x, double y, double width, double height) {
        //out.println(SVG.rectangle(x, y, width, height, 1, Color.blue, Color.black));
        double ratio = 0.95;
        int value = this.f2[g][h];
        int max = getMaxTable(this.f2);
        Color color = ColorTools.getColor(this.groups[h]);
        double newHeight = height * value * ratio / max;
        Message.verbose("value="+value+" max="+max+" height="+height+" draw="+height+"*"+value+"*"+ratio+"/"+max+"="+newHeight);
        double margin = height - newHeight;
        if (newHeight == 0) {
            svg.line(new Point(x, y + margin), new Point(x + width, y + margin), 1, Color.black);
        } else {
            svg.horizontalBar(new Point(x, y + margin), width, newHeight, 1, color, Color.black);
        }
    }
    
    private static int getMaxTable(int[][] f2){
        int max = 0;
        for(int[] l :f2)
            for(int i = 0 ; i < l.length -1 ; i++)
                if(l[i] > max)
                    max = l[i];
        return max;
    }

    private void drawVariants(double x, double y, double width, double height) {
        //out.println(SVG.rectangle(x, y, width, height, L, Color.green, Color.black));
        double line = height / this.groups.length;
        for (int i = 0; i < this.groups.length; i++) {
            drawVariant(i, x, y + (i * line), width, line);
        }
    }

    private void drawVariant(int i, double x, double y, double width, double height) {
        //out.println(SVG.rectangle(x, y, width, height, 1, Color.green, Color.black));
        double offset = 0.05 * height;
        int value = this.f2[i][this.groups.length];
        int max = getMaxTotal(f2);
        Color color = ColorTools.getColor(this.groups[i]);
        double newX = x + offset;
        double newY = y + offset;
        double newHeight = height - 2 * offset;
        double newWidth = width - 2 * offset;
        newWidth = (newWidth * value) / max;
        //System.out.println("vertical bar x:"+newX+" y:"+newY+" w:"+newWidth+" h:"+newHeight);
        svg.verticalBar(new Point(newX, newY), newWidth, newHeight, 1, color, Color.black);
    }
    
    private int getMaxTotal(int[][] f2){
        int max = 0;
        for (int[] row : f2)
            if (row[this.groups.length] > max)
              max = row[this.groups.length];
        return max;
    }


    @Override
    public void exportGraphAsPNG(String filename, int width, int height) throws GraphException {
        this.loadData();
        this.size = Math.max(width,height);
        svg = new SVG(width, height, false);
        this.draw();
        svg.exportAsPNG(filename);
    }

}
