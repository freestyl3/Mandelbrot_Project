package ru.tyaguschev.gui;

import ru.tyaguschev.fractals.Mandelbrot;
import ru.tyaguschev.gui.Palette.ColorPalette;
import ru.tyaguschev.math.complex.ComplexNumber;
import ru.tyaguschev.math.converter.Converter;
import ru.tyaguschev.math.coordinates.Coordinates;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class FractalPainter implements Painter {
    private final Mandelbrot mandelbrot = new Mandelbrot();
    private final Converter converter;
    private int degree;
    private double ratio;
    private ColorPalette palette;

    public FractalPainter(Coordinates cords, ColorPalette palette, int width, int height) {
        this.converter = new Converter(cords.xMin, cords.xMax, cords.yMin, cords.yMax, width, height);
        this.ratio = (double) width / (double) height;
        this.degree = 0;
        this.palette = palette;
    }

    public BufferedImage createImage() {
        try {
            mandelbrot.setMaxIter((int) (200 * Math.pow(2, this.degree)));
            ArrayList<Thread> threads = new ArrayList<>();

            int numThreads = 20;

            int width = converter.getWidth();
            int height = converter.getHeight();

            BufferedImage image = new BufferedImage(width + 1, height + 1, BufferedImage.TYPE_INT_RGB);
            final Object sync = new Object();

            int segmentWidth = (int) Math.ceil((double) width / numThreads);

            for (int t = 0; t < numThreads; t++) {
                int startX = t * segmentWidth;
                int endX = Math.min(width, (t + 1) * segmentWidth);

                Runnable task = () -> {
                    for (int i = startX; i < endX; i++) {
                        for (int j = 0; j <= height; j++) {
                            double x = converter.xScreenToCartesian(i);
                            double y = converter.yScreenToCartesian(j);

                            double multiplier = 1 - mandelbrot.isInSet(new ComplexNumber.Base(x, y));

                            var color = this.palette.getColor(multiplier);
                            synchronized (sync) {
                                image.setRGB(i, j, color.getRGB());
                            }
                        }
                    }
                };
                Thread thread = new Thread(task);
                threads.add(thread);
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            return image;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Converter getConverter() {
        return this.converter;
    }

    public Coordinates updateCoordinates(Coordinates cords) {
        var deltaX = Math.abs(cords.xMax - cords.xMin);
        var deltaY = Math.abs(cords.yMax - cords.yMin) * this.ratio;
        var delta = Math.abs(deltaX - deltaY) / 2;
        if (deltaX > deltaY) {
            cords.yMin += delta / this.ratio;
            cords.yMax -= delta / this.ratio;
        } else if (deltaX < deltaY) {
            cords.xMin -= delta;
            cords.xMax += delta;
        }
        converter.setXShape(cords.xMin, cords.xMax);
        converter.setYShape(cords.yMin, cords.yMax);
        this.degree = Math.min(6, -((int) (Math.log10(deltaX))));
        mandelbrot.setMaxIter((int) (200 * Math.pow(2, this.degree)));
        return cords;
    }

    public void saveAspectRatio(Coordinates coordinates, double ratio) {
        this.ratio = ratio;
        updateCoordinates(coordinates);
    }

    public void setPalette(ColorPalette palette) {
        this.palette = palette;
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
