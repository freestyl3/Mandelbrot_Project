package ru.tyaguschev.data;

import ru.tyaguschev.presentation.ScreenConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public interface Repository {
    BufferedImage createImage(ScreenConverter converter);

    class Base implements Repository {
        private final Mandelbrot mandelbrot = new Mandelbrot();
        private final Object sync = new Object();
        @Override
        public BufferedImage createImage(ScreenConverter converter) {
            try {
                mandelbrot.setMaxIterations((int) (100 / Math.sqrt(Math.sqrt(Math.sqrt(converter.scale)))));
                System.out.println(mandelbrot.getMaxIterations());
                ArrayList<Thread> threads = new ArrayList<>();

                int numThreads = 20;

                int width = converter.width;
                int height = converter.height;

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
                                int red = (int) Math.min(255, Math.max(0, 255 * multiplier * Math.abs(Math.cos(multiplier + Math.PI / 2))));
                                int green = (int) Math.min(255, Math.max(0, 255 * multiplier * Math.abs(Math.cos(multiplier + Math.PI / 3))));
                                int blue = (int) Math.min(255, Math.max(0, 255 * multiplier * Math.abs(Math.cos(multiplier + Math.PI / 5))));

                                Color color = new Color(red, green, blue);

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
    }
}
