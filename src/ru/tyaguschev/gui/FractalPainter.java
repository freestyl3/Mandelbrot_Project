package ru.tyaguschev.gui;

import ru.tyaguschev.fractals.Mandelbrot;
import ru.tyaguschev.math.complex.ComplexNumber;
import ru.tyaguschev.math.converter.Converter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FractalPainter implements Painter {
    private final Mandelbrot mandelbrot = new Mandelbrot();
    private final Converter converter;
    private int degree;
    private double ratio = 1.0;

    public FractalPainter(double xMin, double xMax, double yMin, double yMax) {
        this.converter = new Converter(xMin, xMax, yMin, yMax, 0, 0);
        this.degree = 0;
    }

    @Override
    public void paint(Graphics g) {
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i <= converter.getWidth(); i++) {
            int iCopy = i;
            Runnable task = () -> {
                for (int j = 0; j <= converter.getHeight(); j++) {
                    var x = converter.xScreenToCartesian(iCopy);
                    var y = converter.yScreenToCartesian(j);
//                var color = mandelbrot.isInSet(new ComplexNumber.Base(x, y)) ? Color.BLACK : Color.WHITE;
                    double multiplier = 1 - mandelbrot.isInSet(new ComplexNumber.Base(x, y));
                    var color = new Color(
                            (int) (255 * multiplier * (Math.abs(Math.cos(multiplier + Math.PI / 2)))),
                            (int) (255 * multiplier * (Math.abs(Math.cos(multiplier + Math.PI / 3)))),
                            (int) (255 * multiplier * (Math.abs(Math.cos(multiplier + Math.PI / 5))))
                    );
//                    var color = new Color(
//                            (int) (255 * multiplier * (1 - Math.abs(Math.cos(multiplier + Math.PI / 2)))),
//                            (int) (255 * multiplier * (1 - Math.abs(Math.cos(multiplier + Math.PI / 3)))),
//                            (int) (255 * multiplier * (1 - Math.abs(Math.cos(multiplier + Math.PI / 5))))
//                    );
//                    var color = new Color(
//                            (int) (205 * multiplier * Math.sin(Math.PI/2)),
//                            (int) (155 * multiplier * Math.sin(Math.PI/2)),
//                            (int) (197 * 4 * multiplier * (1 + multiplier * Math.cos(Math.PI)))
//                    );
//                    var color = new Color(
//                            Math.min(255, (int) (-1216.67 * multiplier * multiplier + 1271.67 * multiplier)),
//                            (int) (-292.22 * multiplier * multiplier + 329.22 * multiplier),
//                            (int) (87.78 * multiplier * multiplier + 1.22 * multiplier)
//                    );
                    synchronized (g){
                        g.setColor(color);
                        g.fillRect(iCopy, j, 1, 1);
                    }
                }
            };
            threads.add(
                    new Thread(task)
            );
            threads.getLast().start();
        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (
                InterruptedException e) {
            throw new RuntimeException(e);
        }

//        System.out.println(threads.toArray().length);
    }

    public Converter getConverter() {
        return this.converter;
    }

    public void updateCoordinates(double xMin, double xMax, double yMin, double yMax) {
//        var coordinates = saveAspectRatio(xMin, xMax, yMin, yMax);
//        System.out.println(ratio);
        var deltaX = Math.abs(xMax - xMin);
        var deltaY = Math.abs(yMax - yMin) * this.ratio;
        var delta = Math.abs(deltaX - deltaY) / 2;
        if (deltaX > deltaY) {
//            var delta = (Math.abs(xMax - xMin) - Math.abs(yMin - yMax)) / 2;
            yMin += delta;
            yMax -= delta;
//            yMin /= ratio;
//            yMax /= ratio;
        } else {
//            var delta = (Math.abs(yMin - yMax) - Math.abs(xMax - xMin)) / 2;
            xMin -= delta;
            xMax += delta;
//            xMin *= ratio;
//            xMax *= ratio;
        }
        converter.setXShape(xMin, xMax);
        converter.setYShape(yMin, yMax);
        this.degree = Math.min(6, -((int) (Math.log10(xMax - xMin))));
        mandelbrot.setMaxIter((int) (200 * Math.pow(2, this.degree)));
//        System.out.println(mandelbrot.getMaxIter());
    }

    public void updateRatio(double ratio) {
        this.ratio = ratio;
    }

    public void saveAspectRatio(double xMin, double xMax, double yMin, double yMax) {
        var deltaX = Math.abs(xMax - xMin);
        var deltaY = Math.abs(yMax - yMin) * ratio;
        if (deltaX > deltaY) {
            yMin /= ratio;
            yMax /= ratio;
        } else {
            xMin *= ratio;
            xMax *= ratio;
        }
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
