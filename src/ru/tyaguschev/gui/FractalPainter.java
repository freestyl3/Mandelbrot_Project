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
    private final ArrayList<Thread> threads = new ArrayList<>();
    @Override
    public void paint(Graphics g) {
        try {
            for (Thread thread : threads) {
                synchronized (g){
                    thread.interrupt();
                }
            }
            threads.clear();

            int numThreads = 8;

            int width = converter.getWidth() + 1;
            int height = converter.getHeight() + 1;

            int segmentWidth = width / numThreads;
            for (int t = 0; t < numThreads; t++) {
                int startX = t * segmentWidth;
                int endX = (t == numThreads - 1) ? width : startX + segmentWidth;

                Runnable task = () -> {
                    for (int i = startX; i < endX; i++) {
                        for (int j = 0; j <= height; j++) {
                            var x = converter.xScreenToCartesian(i);
                            var y = converter.yScreenToCartesian(j);
                            double multiplier = 1 - mandelbrot.isInSet(new ComplexNumber.Base(x, y));
                            var color = new Color(
                                    (int) (255 * multiplier * (Math.abs(Math.cos(multiplier + Math.PI / 2)))),
                                    (int) (255 * multiplier * (Math.abs(Math.cos(multiplier + Math.PI / 3)))),
                                    (int) (255 * multiplier * (Math.abs(Math.cos(multiplier + Math.PI / 5))))
                            );
                            synchronized (g) {
                                g.setColor(color);
                                g.fillRect(i, j, 1, 1);
                            }
                        }
                    }
                };
                threads.add(new Thread(task));
                threads.getLast().start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Converter getConverter() {
        return this.converter;
    }

    public ArrayList<Double> updateCoordinates(double xMin, double xMax, double yMin, double yMax) {
        var deltaX = Math.abs(xMax - xMin);
        var deltaY = Math.abs(yMax - yMin) * this.ratio;
        var delta = Math.abs(deltaX - deltaY) / 2;
//        System.out.println(xMin + ", " + xMax + ", " + yMin + ", " + yMax);
        if (deltaX > deltaY) {
            yMin += delta;
            yMax -= delta;
        } else {
            xMin -= delta;
            xMax += delta;
        }
//        System.out.println((Math.max(xMin, xMax) - Math.min(xMax, xMin)) +  ", " + (Math.max(yMin, yMax) - Math.min(yMax, yMin)));
        converter.setXShape(xMin, xMax);
        converter.setYShape(yMin, yMax);
        this.degree = Math.min(6, -((int) (Math.log10(xMax - xMin))));
        mandelbrot.setMaxIter((int) (200 * Math.pow(2, this.degree)));
        return new ArrayList<>(List.of(xMin, xMax, yMin * ratio, yMax * ratio));
//        System.out.println(mandelbrot.getMaxIter());
    }

    public void saveAspectRatio(double xMin, double xMax, double yMin, double yMax, double ratio) {
        this.ratio = ratio;

//        var deltaX = Math.abs(xMax - xMin);
//        var deltaY = Math.abs(yMax - yMin) * ratio;
//        if (deltaX > deltaY) {
//            yMin /= ratio;
//            yMax /= ratio;
//        } else {
//            xMin *= ratio;
//            xMax *= ratio;
//        }
        converter.setXShape(xMin, xMax);
        converter.setYShape(yMin * ratio, yMax * ratio);
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
