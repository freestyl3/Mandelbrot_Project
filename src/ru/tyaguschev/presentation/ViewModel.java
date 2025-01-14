package ru.tyaguschev.presentation;

import kotlin.Pair;
import ru.tyaguschev.data.Repository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

/**
 * ViewModel отвечает за управление состоянием и взаимодействие с репозиторием для генерации изображений,
 * а также за обработку пользовательских действий, таких как перемещение, масштабирование и сохранение состояния.
 */
public class ViewModel extends BackStack.Abstract {
    private final Repository repository;
    private Double scale = 0.01;
    private Point currentCentre = new Point(0., 0.);
    private Thread task;
    private ImageCallback callback = _ -> {
    };

    /**
     * Конструктор ViewModel.
     *
     * @param repository репозиторий, предоставляющий метод для генерации изображений.
     */
    public ViewModel(Repository repository) {
        this.repository = repository;
    }

    /**
     * Инициализация ViewModel с заданными параметрами окна и обратным вызовом для обновления изображения.
     *
     * @param windowHeight высота окна.
     * @param windowWidth ширина окна.
     * @param callback обратный вызов для обновления изображения.
     */
    void init(Integer windowHeight, Integer windowWidth, ImageCallback callback) {
        this.callback = callback;
        addToBackStack(scale, currentCentre);
        print(windowHeight, windowWidth);
    }

    /**
     * Отображает изображение в заданных параметрах окна.
     *
     * @param windowHeight высота окна.
     * @param windowWidth ширина окна.
     */
    void print(Integer windowHeight, Integer windowWidth) {
        var converter = new ScreenConverter(
                currentCentre,
                windowHeight,
                windowWidth,
                scale
        );
        if (task != null)
            task.interrupt();
        task = new Thread(() -> {
            var image = repository.createImage(converter);
            callback.invoke(image);
        });
        task.start();
    }

    /**
     * Перемещает видимую область на новое место.
     *
     * @param windowHeight высота окна.
     * @param windowWidth ширина окна.
     * @param newCentre новая центральная точка области.
     */
    void move(Integer windowHeight, Integer windowWidth, Point newCentre) {
        var centreX = windowWidth / 2;
        var centreY = windowHeight / 2;
        currentCentre = new Point(
                (newCentre.x() - centreX) * scale + currentCentre.x(),
                (newCentre.y() - centreY) * scale + currentCentre.y()
        );
        print(windowHeight, windowWidth);
    }

    /**
     * Масштабирует видимую область на основе выделенной прямоугольной области.
     *
     * @param windowHeight высота окна.
     * @param windowWidth ширина окна.
     * @param rect выделенная прямоугольная область.
     */
    void zoom(Integer windowHeight, Integer windowWidth, Rect rect) {
        var height = rect.getHeight();
        var width = rect.getWidth();
        var xCentre = -windowWidth / 2 + width / 2 + rect.getStartPoint().x;
        var yCentre = -windowHeight / 2 + height / 2 + rect.getStartPoint().y;

        currentCentre = new Point(currentCentre.x() + xCentre * scale, currentCentre.y() + yCentre * scale);
        scale = (height < width) ? scale * ((double) width / (double) windowWidth) :
                scale * ((double) height / (double) windowHeight);
        addToBackStack(scale, currentCentre);
        print(windowHeight, windowWidth);
    }

    /**
     * Возвращается к предыдущему состоянию из стека состояний.
     *
     * @param windowHeight высота окна.
     * @param windowWidth ширина окна.
     */
    @Override
    public void goBack(Integer windowHeight, Integer windowWidth) {
        super.goBack(windowHeight, windowWidth);
        var lastState = stack.peek();
        this.scale = lastState.getFirst();
        this.currentCentre = lastState.getSecond();
        print(windowHeight, windowWidth);
    }

    /**
     * Сохраняет текущие настройки в файл.
     *
     * @param fileName имя файла для сохранения.
     */
    void saveSettings(String fileName) {
        try {
            File outputFile = new File(fileName);
            FileWriter fileWriter = new FileWriter(outputFile.getAbsolutePath());
            fileWriter.write(scale + "\n");
            fileWriter.write(currentCentre.x() + "\n");
            fileWriter.write(currentCentre.y() + "\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Загружает настройки из файла и применяет их.
     *
     * @param windowHeight высота окна.
     * @param windowWidth ширина окна.
     * @param fileName имя файла для загрузки.
     */
    void openFile(Integer windowHeight, Integer windowWidth, String fileName) {
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            var newCords = new ArrayList<Double>();
            while (scanner.hasNextLine()) {
                newCords.add(Double.parseDouble(scanner.nextLine()));
            }
            scale = newCords.get(0);
            var xCentre = newCords.get(1);
            var yCentre = newCords.get(2);
            currentCentre = new Point(xCentre, yCentre);
            addToBackStack(scale, currentCentre);

            print(windowHeight, windowWidth);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Сохраняет изображение в файл.
     *
     * @param fileName имя файла для сохранения.
     * @param image изображение для сохранения.
     */
    void saveImage(String fileName, BufferedImage image) {
        try {
            File outputFile = new File(fileName);
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

/**
 * Интерфейс для обратного вызова при обновлении изображения.
 */
interface ImageCallback {
    /**
     * Вызывается при обновлении изображения.
     *
     * @param image новое изображение.
     */
    void invoke(BufferedImage image);
}

/**
 * Интерфейс для управления стеком состояний.
 */
interface BackStack {
    /**
     * Возвращает предыдущее состояние.
     *
     * @param windowHeight высота окна.
     * @param windowWidth ширина окна.
     */
    void goBack(Integer windowHeight, Integer windowWidth);

    /**
     * Абстрактный класс, реализующий базовую логику работы со стеком состояний.
     */
    abstract class Abstract implements BackStack {
        protected final Stack<Pair<Double, Point>> stack = new Stack<>();

        /**
         * Добавляет новое состояние в стек.
         *
         * @param scale масштаб.
         * @param centre центр координат.
         */
        protected void addToBackStack(Double scale, Point centre) {
            stack.add(new Pair<>(scale, centre));
        }

        @Override
        public void goBack(Integer windowHeight, Integer windowWidth) {
            if (stack.size() > 1)
                stack.pop();
        }
    }
}
