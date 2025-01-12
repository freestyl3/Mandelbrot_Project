package ru.tyaguschev.presentation.gui;

import ru.tyaguschev.data.Mandelbrot;
import ru.tyaguschev.data.ComplexNumber;
import ru.tyaguschev.presentation.Converter;

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
        if (deltaX > deltaY) {
            yMin += delta / this.ratio;
            yMax -= delta / this.ratio;
        } else if (deltaX < deltaY) {
            xMin -= delta;
            xMax += delta;
        }
        converter.setXShape(xMin, xMax);
        converter.setYShape(yMin, yMax);
        this.degree = Math.min(6, -((int) (Math.log10(xMax - xMin))));
        mandelbrot.setMaxIter((int) (200 * Math.pow(2, this.degree)));
        return new ArrayList<>(List.of(xMin, xMax, yMax, yMin));
    }

    public void saveAspectRatio(double xMin, double xMax, double yMin, double yMax, double ratio) {
        this.ratio = ratio;
        updateCoordinates(xMin, xMax, yMin, yMax);
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
