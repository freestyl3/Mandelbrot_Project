/**
 * Пакет содержит интерфейс и реализацию репозитория для создания изображений с использованием фракталов Мандельброта.
 */
package ru.tyaguschev.data;

import ru.tyaguschev.presentation.ScreenConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Интерфейс Repository предоставляет метод для создания изображения.
 */
public interface Repository {
    /**
     * Создает изображение с использованием заданного конвертера экрана.
     *
     * @param converter объект ScreenConverter, используемый для преобразования координат экрана в координаты декартовой системы.
     * @return сгенерированное изображение в формате BufferedImage.
     */
    BufferedImage createImage(ScreenConverter converter);

    /**
     * Базовая реализация интерфейса Repository, использующая фрактал Мандельброта для создания изображений.
     */
    class Base implements Repository {
        private final Mandelbrot mandelbrot = new Mandelbrot();
        private final Object sync = new Object();

        /**
         * Создает изображение с использованием фрактала Мандельброта.
         *
         * @param converter объект ScreenConverter для преобразования экранных координат в декартовые.
         *                  Также используется масштаб (scale) для настройки количества итераций.
         * @return сгенерированное изображение фрактала Мандельброта.
         */
        @Override
        public BufferedImage createImage(ScreenConverter converter) {
            mandelbrot.setMaxIterations((int) (100 / Math.sqrt(Math.sqrt(Math.sqrt(converter.scale)))));
            System.out.println(mandelbrot.getMaxIterations());

            int numThreads = 20;
            int width = converter.width;
            int height = converter.height;

            BufferedImage image = new BufferedImage(width + 1, height + 1, BufferedImage.TYPE_INT_RGB);

            ExecutorService executor = Executors.newFixedThreadPool(numThreads);

            int segmentWidth = (int) Math.ceil((double) width / numThreads);

            for (int t = 0; t < numThreads; t++) {
                int startX = t * segmentWidth;
                int endX = Math.min(width, (t + 1) * segmentWidth);

                executor.submit(() -> {
                    for (int i = startX; i < endX; i++) {
                        for (int j = 0; j <= height; j++) {
                            double x = converter.xScreenToCartesian(i);
                            double y = converter.yScreenToCartesian(j);

                            double multiplier = 1 - mandelbrot.isInSet(new ComplexNumber.Base(x, y));
                            int red = (int) Math.min(255, Math.max(0, 255 * multiplier * Math.abs(Math.cos(multiplier + Math.PI / 2))));
                            int green = (int) Math.min(255, Math.max(0, 255 * multiplier * Math.abs(Math.cos(multiplier + Math.PI / 3))));
                            int blue = (int) Math.min(255, Math.max(0, 255 * multiplier * Math.abs(Math.cos(multiplier + Math.PI / 5))));

                            Color color = new Color(red, green, blue);

                            synchronized (image) {
                                image.setRGB(i, j, color.getRGB());
                            }
                        }
                    }
                });
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }

            return image;
        }
    }
}
