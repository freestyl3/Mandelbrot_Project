package ru.tyaguschev.presentation;

import kotlin.Pair;
import ru.tyaguschev.data.Repository;
import ru.tyaguschev.presentation.gui.Rect;

import java.awt.image.BufferedImage;
import java.util.Stack;

public class ViewModel extends BackStack.Abstract {
    private final Repository repository;

    private Double scale = 0.01;
    private Point currentCentre = new Point(0., 0.);


    public ViewModel(Repository repository) {
        this.repository = repository;
    }

    private Thread task;
    private ImageCallback callback = _ -> {};

    void init(
            Integer windowHeight,
            Integer windowWidth,
            ImageCallback callback
    ) {
        this.callback = callback;
        addToBackStack(scale, currentCentre);
        print(windowHeight, windowWidth);
    }

    void print(
            Integer windowHeight,
            Integer windowWidth
    ) {
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

    void move(
            Integer height,
            Integer width,
            Point newCentre
    ) {
        var centreX = width / 2;
        var centreY = height / 2;
        currentCentre = new Point(
                (newCentre.x() - centreX) * scale + currentCentre.x(),
                (newCentre.y() - centreY) * scale + currentCentre.y()
        );
        print(height, width);
    }

    void zoom(
            Integer windowHeight,
            Integer windowWidth,
            Rect rect
    ) {
        var height = rect.getHeigth();
        var width = rect.getWidth();
        var xCentre = -windowWidth / 2 + width / 2 + rect.getStartPoint().x;
        var yCentre = -windowHeight / 2 + height / 2 + rect.getStartPoint().y;

        currentCentre = new Point(currentCentre.x() + xCentre * scale, currentCentre.y() + yCentre * scale);
        scale = (height < width) ? scale * ((double) width / (double) windowWidth) :
                scale * ((double) height / (double) windowHeight);
        addToBackStack(scale, currentCentre);
        print(windowHeight, windowWidth);
    }

    @Override
    public void goBack(
            Integer windowHeight,
            Integer windowWidth
    ) {
        super.goBack(windowHeight, windowWidth);
        var lastState = stack.peek();
        this.scale = lastState.getFirst();
        this.currentCentre = lastState.getSecond();
        print(windowHeight, windowWidth);
    }
}

interface ImageCallback {
    void invoke(BufferedImage image);
}

interface BackStack {
    void goBack(
            Integer windowHeight,
            Integer windowWidth
    );

    abstract class Abstract implements BackStack {
        protected final Stack<Pair<Double, Point>> stack = new Stack<>();

        protected void addToBackStack(Double scale, Point centre) {
            stack.add(new Pair<>(scale, centre));
        }

        @Override
        public void goBack(
                Integer windowHeight,
                Integer windowWidth
        ) {
            stack.pop();
        }
    }

}
