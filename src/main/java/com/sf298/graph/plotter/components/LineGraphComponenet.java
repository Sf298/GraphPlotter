
package com.sf298.graph.plotter.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import static java.util.Objects.isNull;

/**
 * Class to make plotting line graphs a breeze.
 * @author sf298
 */
public class LineGraphComponenet extends JComponent {

	private String title;
	private ArrayList<List<Double>> x = new ArrayList<>();
	private ArrayList<List<Double>> y = new ArrayList<>();
	private ArrayList<Color> c = new ArrayList<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Add a line to the graph.
	 * @param y the values that make up the line.
	 * @param c the colour of the line.
	 */
	public void addLine(List<Double> y, Color c) {
		addLine(null, y, c);
	}

	/**
	 * Add a line to the graph.
	 * @param x the values that label the y values.
	 * @param y the values that make up the y axis of the line.
	 * @param c the colour of the line.
	 */
	public void addLine(List<Double> x, List<Double> y, Color c) {
		if(isNull(x)) {
			x = new ArrayList<>(y.size());
			for(int i=0; i<y.size(); i++) {
				x.add((double) i);
			}
		}
		c = isNull(c) ? Color.BLACK : c;
		this.x.add(x);
		this.y.add(y);
		this.c.add(c);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		// setup background
		int w = getWidth();
		int h = getHeight();
		g2.setColor(Color.LIGHT_GRAY);
		g2.fillRect(0, 0, w, h);

		// calculate margin sizes for the labels
		Rectangle textSize = getStringBounds(g2, "1000.00", 0, 0);
		int marginN = 30;
		int marginS = (textSize.height+5)*x.size()+5;
		int marginE = 30;
		int marginW = (textSize.width+5)*y.size()+5;

		// setup plot area
		w = w - marginE - marginW;
		h = h - marginN - marginS;

		// draw axis
		g2.setColor(Color.BLACK);
		g2.drawLine(marginW, getHeight()-marginS, getWidth()-marginE, getHeight()-marginS);
		g2.drawLine(marginW, getHeight()-marginS, marginW, marginN);

		// setup grid line spacing
		int minGapW = 50;
		int minGapH = 25;
		int[][] lrWs = getLRs(w/minGapW, w);
		int[][] lrHs = getLRs(h/minGapH, h);
		
		// draw gridlines
		g2.setColor(Color.GRAY);
		for(int j=0; j<lrWs.length; j++) {
			if(j==0)
				g2.drawLine(lrWs[0][0], getHeight()-marginS, lrWs[0][0], marginN);
			g2.drawLine(lrWs[j][1]+marginW, getHeight()-marginS, lrWs[j][1]+marginW, marginN);
		}
		for(int j=0; j<lrHs.length; j++) {
			if(j==0)
				g2.drawLine(marginW, lrHs[0][0], getWidth()-marginE, lrHs[0][0]);
			g2.drawLine(marginW, lrHs[j][1], getWidth()-marginE, lrHs[j][1]);
		}

		for(int i=0; i<x.size(); i++) {
			// draw lines
			double maxX = Double.MIN_VALUE, minX=Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE, minY=Double.MAX_VALUE;
			for(int j=0; j<x.get(i).size(); j++) {
				if(x.get(i).get(j) > maxX) maxX = x.get(i).get(j);
				if(x.get(i).get(j) < minX) minX = x.get(i).get(j);
				if(y.get(i).get(j) > maxY) maxY = y.get(i).get(j);
				if(y.get(i).get(j) < minY) minY = y.get(i).get(j);
			}
			
			g2.setColor(c.get(i));
			for(int j=1; j<x.get(i).size(); j++) {
				int x1 = (int) remapToScale(x.get(i).get(j-1), maxX, minX, w, 0)+marginW;
				int x2 = (int) remapToScale(x.get(i).get(j), maxX, minX, w, 0)+marginW;
				int y1 = (int) remapToScale(y.get(i).get(j-1), maxY, minY, h, 0)+marginS;
				int y2 = (int) remapToScale(y.get(i).get(j), maxY, minY, h, 0)+marginS;
				g2.drawLine(x1, getHeight()-y1, x2, getHeight()-y2);
			}

			// draw axis labels
			for(int j=0; j<lrWs.length; j++) {
				if(j==0) {
					String str = String.format("%.2f", x.get(i).get(0));
					Rectangle bounds = getStringBounds(g2, str, 0, 0);
					g2.drawString(str, marginW-bounds.width/2, getHeight()-marginS+(i+1)*(bounds.height+5));
				}
				double notchVal = x.get(i).get(remapToScale(lrWs[j][1], w, 0, x.get(i).size()-1, 0));
				String str = String.format("%.2f", notchVal);
				Rectangle bounds = getStringBounds(g2, str, 0, 0);
				g2.drawString(str, lrWs[j][1]+marginW-bounds.width/2, getHeight()-marginS+(i+1)*(bounds.height+5));
			}
			for(int j=0; j<lrHs.length; j++) {
				if(j==0) {
					double notchVal = remapToScale(lrHs[0][0], h, 0, maxY, minY);
					String str = String.format("%.2f", notchVal);
					g2.drawString(str, i*(textSize.width+5)+5, getHeight()-(marginS+lrHs[j][0])+textSize.height/2);
				}
				double notchVal = remapToScale(lrHs[j][1], h, 0, maxY, minY);
				String str = String.format("%.2f", notchVal);
				g2.drawString(str, i*(textSize.width+5)+5, getHeight()-(marginS+lrHs[j][1])+textSize.height/2);
			}
		}
	}

	/**
	 * Gets the bounds of the given string.
	 * @param g2 The {@link Graphics2D} used to draw the string.
	 * @param str The String to analyse.
	 * @param x The x coordinate where the text will be rendered (optional).
	 * @param y The y coordinate where the text will be rendered (optional).
	 * @return Returns the bounds of the given String.
	 */
	private Rectangle getStringBounds(Graphics2D g2, String str, float x, float y) {
        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = g2.getFont().createGlyphVector(frc, str);
        return gv.getPixelBounds(null, x, y);
    }
	
	/**
     * Calculates the boundaries of a given segment of an evenly divided space.
     * @param segment The index of the segment
     * @param numberOfSegments The total number of segments.
     * @param spaceSize The integer size of the total space.
     * @return An array of the left (inclusive) and right (exclusive) indexes respectively.
     */
    private static int[] getLR(int segment, int numberOfSegments, int spaceSize) {
        // calc ileft and iright
        int a = spaceSize/numberOfSegments; // cols per process
        int rem = spaceSize%numberOfSegments; // remaining cols
        int left = segment * a + Math.min(segment, rem); // is 0 based
        int right = left + a + ((segment<rem) ? 1 : 0); // right is exclusive
        return new int[] {left, right};
    }

    /**
     * Calculates the boundaries of the segments of an evenly divided space.
     * @param numberOfSegments The number of processors available.
     * @param spaceSize Number of elements to partition.
     * @return Returns an array with values in the range of the LR values
     */
    private static int[][] getLRs(int numberOfSegments, int spaceSize) {
		int[][] out = new int[numberOfSegments][];
		for(int i=0; i<out.length; i++) {
			out[i] = getLR(i, numberOfSegments, spaceSize);
		}
		return out;
    }

	private static double remapToScale(double oVal, double oMax, double oMin, double nMax, double nMin) {
		return ((oVal-oMin)/(oMax-oMin))*(nMax-nMin)+nMin;
	}
	private static int remapToScale(int oVal, int oMax, int oMin, int nMax, int nMin) {
		return ((oVal-oMin)*(nMax-nMin))/(oMax-oMin)+nMin;
	}
	
	public void plot() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		//lgc.setPreferredSize(new Dimension(400, 600));
		mainPanel.add(this);

		JFrame frame = new JFrame(isNull(title) ? "" : title);
		frame.setSize(1000, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(mainPanel);
		frame.setVisible(true);
	}
	public static void plot(List<Double> y) {
		plot(null, y);
	}
	public static void plot(List<Double> x, List<Double> y) {
		LineGraphComponenet lgc = new LineGraphComponenet();
		lgc.addLine(x, y, Color.BLACK);
		lgc.plot();
	}
	
}
