package ru.tyaguschev.presentation;

public class ScreenConverter {
    public final Integer height;
    public final Integer width;

    private final Point centre;
    public final Double scale;

    public ScreenConverter(
            Point centre,
            Integer height,
            Integer width,
            Double scale
    ) {
        this.centre = centre;
        this.height = height;
        this.width = width;
        this.scale = scale;
    }

    public double xScreenToCartesian(int x) {
        return (-width/2.0 + x) * scale  + centre.x();
    }

    public double yScreenToCartesian(int y) {
        return (-height/2.0 + y) * scale +  centre.y();
    }
}
