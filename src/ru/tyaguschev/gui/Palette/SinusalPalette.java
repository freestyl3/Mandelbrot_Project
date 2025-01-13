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
                (int) (this.red * multiplier * (Math.abs(Math.sin(multiplier + Math.PI / 2)))),
                (int) (this.green * multiplier * (Math.abs(Math.sin(multiplier + Math.PI / 3)))),
                (int) (this.blue * multiplier * (Math.abs(Math.sin(multiplier + Math.PI / 5))))
        );
    }
}
