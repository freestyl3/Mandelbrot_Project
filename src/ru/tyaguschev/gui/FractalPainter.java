package ru.tyaguschev.gui;

import ru.tyaguschev.fractals.Mandelbrot;
import ru.tyaguschev.math.complex.ComplexNumber;
import ru.tyaguschev.math.converter.Converter;

import java.awt.*;

public class FractalPainter implements Painter{
    private final Mandelbrot mandelbrot = new Mandelbrot();
    private final Converter converter;

    public FractalPainter(double xMin, double xMax, double yMin, double yMax) {
        this.converter = new Converter(xMin, xMax, yMin, yMax, 0, 0);
    }

    @Override
    public void paint(Graphics g) {
        for (int i = 0; i < converter.getWidth(); i++) {
            for (int j = 0; j < converter.getHeight(); j++) {
                var x = converter.xScr2Crt(i);
                var y = converter.yScr2Crt(j);
                var color = mandelbrot.isInSet(new ComplexNumber.Base(x, y)) ? Color.BLACK : Color.WHITE;
                g.setColor(color);
                g.fillRect(i, j, 1, 1);
            }
        }
    }

    public Converter getConverter() {
        return this.converter;
    }

    public void updateCoordinates(double xMin, double xMax, double yMin, double yMax) {
        converter.setXShape(xMin, xMax);
        converter.setYShape(yMin, yMax);
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
