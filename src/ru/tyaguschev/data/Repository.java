package ru.tyaguschev.data;

import ru.tyaguschev.presentation.ScreenConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static java.lang.Math.sqrt;

public interface Repository {
    BufferedImage createImage(ScreenConverter converter);

    class Base implements Repository {
        private final Mandelbrot mandelbrot = new Mandelbrot();
        private final ArrayList<Thread> threads = new ArrayList<>();

        @Override
        public BufferedImage createImage(ScreenConverter converter) {
            try {
                for (Thread thread : threads) {
                    synchronized (this) {
                        thread.interrupt();
                    }
                }
                mandelbrot.setMaxIter((int) (50 / sqrt(sqrt(converter.scale))));
                System.out.println(mandelbrot.getMaxIter());
                threads.clear();

                int numThreads = 20;

                int width = converter.width;
                int height = converter.height;

                BufferedImage image = new BufferedImage(width + 1, height + 1, BufferedImage.TYPE_INT_RGB);

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
                                synchronized (this) {
                                    image.setRGB(i, j, color.getRGB());
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
                return image;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
