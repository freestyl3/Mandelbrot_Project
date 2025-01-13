package ru.tyaguschev.gui.Palette;

import java.awt.*;

public class SinusalPalette implements ColorPalette{
    private final int red, green, blue;

    public SinusalPalette(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public Color getColor(double multiplier) {
        return new Color(
                (int) (255 * multiplier * (Math.abs(Math.sin(multiplier + Math.PI / this.red)))),
                (int) (255 * multiplier * (Math.abs(Math.sin(multiplier + Math.PI / this.green)))),
                (int) (255 * multiplier * (Math.abs(Math.sin(multiplier + Math.PI / this.blue))))
        );
    }
}
