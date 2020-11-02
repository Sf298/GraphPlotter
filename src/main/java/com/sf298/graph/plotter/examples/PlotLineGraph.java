package com.sf298.graph.plotter.examples;

import com.sf298.graph.plotter.components.LineGraphComponenet;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;

public class PlotLineGraph {

    public static void main(String[] args) {
        List<Double> sinY = new ArrayList<>();
        List<Double> cosY = new ArrayList<>();
        for(double x=0; x<PI*2; x+=PI/100) {
            sinY.add(Math.sin(x));
            cosY.add(Math.cos(x));
        }

        LineGraphComponenet lgc = new LineGraphComponenet();
        lgc.setTitle("Sine and Cosine");
        lgc.addLine(sinY, Color.RED);
        lgc.addLine(cosY, Color.BLUE);

        lgc.plot();
    }

}
