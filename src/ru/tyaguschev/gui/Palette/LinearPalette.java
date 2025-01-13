package ru.tyaguschev.gui.Palette;

import ru.tyaguschev.math.Pair.Pair;

import java.awt.*;

public class LinearPalette implements ColorPalette{
    Pair red, green, blue;
    public LinearPalette(Pair red, Pair green, Pair blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public Color getColor(double multiplier) {
        if (multiplier == 0) return Color.BLACK;
        return new Color(
                (int) (this.red.first * multiplier + red.second),
                (int) (this.green.first * multiplier + green.second),
                (int) (this.blue.first * multiplier + blue.second)
        );
    }
}
