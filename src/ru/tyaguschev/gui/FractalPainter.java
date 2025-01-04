package ru.tyaguschev.gui;

import ru.tyaguschev.fractals.Mandelbrot;
import ru.tyaguschev.math.complex.ComplexNumber;
import ru.tyaguschev.math.converter.Converter;

import java.awt.*;

public class FractalPainter implements Painter{
    private final Mandelbrot mandelbrot = new Mandelbrot();
    private final Converter converter;
    private int degree;

    public FractalPainter(double xMin, double xMax, double yMin, double yMax) {
        this.converter = new Converter(xMin, xMax, yMin, yMax, 0, 0);
        this.degree = 0;
    }

    @Override
    public void paint(Graphics g) {
        for (int i = 0; i < converter.getWidth(); i++) {
            for (int j = 0; j < converter.getHeight(); j++) {
                var x = converter.xScr2Crt(i);
                var y = converter.yScr2Crt(j);
//                var color = mandelbrot.isInSet(new ComplexNumber.Base(x, y)) ? Color.BLACK : Color.WHITE;
                double multiplier = 1 - mandelbrot.isInSet(new ComplexNumber.Base(x, y));
                var color = new Color(
                        (int) (255 * multiplier),
                        (int) (255 * multiplier),
                        (int) (255 * multiplier)
                );
                g.setColor(color);
                g.fillRect(i, j, 1, 1);
            }
        }
    }

    public Converter getConverter() {
        return this.converter;
    }

    public void updateCoordinates(double xMin, double xMax, double yMin, double yMax) {
        if (Math.abs(xMax - xMin) > Math.abs(yMax - yMin)) {
            var delta = (Math.abs(xMax - xMin) - Math.abs(yMin - yMax)) / 2;
            yMin += delta;
            yMax -= delta;
        } else {
            var delta = (Math.abs(yMin - yMax) - Math.abs(xMax - xMin)) / 2;
            xMin -= delta;
            xMax += delta;
        }
        converter.setXShape(xMin, xMax);
        converter.setYShape(yMin, yMax);
        this.degree = Math.min(6, -((int)(Math.log10(xMax - xMin))));
        mandelbrot.setMaxIter((int)(200 * Math.pow(2, this.degree)));
//        System.out.println(mandelbrot.getMaxIter());
    }

    @Override
    public int getWidth() {
        return converter.getWidth();
    }

    @Override
    public void setWidth(int width) {
        converter.setWidth(width);

    }


    @Override
    public int getHeight() {
        return converter.getHeight();
    }

    @Override
    public void setHeight(int height) {
        converter.setHeight(height);
    }

}
