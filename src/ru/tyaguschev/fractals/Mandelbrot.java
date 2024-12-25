package ru.tyaguschev.fractals;

import ru.tyaguschev.math.complex.ComplexNumber;

public class Mandelbrot {
    private final double r2 = 4.0;
    private final int maxIter = 500;

    public boolean isInSet(ComplexNumber complex) {
        ComplexNumber z = new ComplexNumber.Base();
        int i = 0;
        while (z.abs2() < r2 && i < maxIter) {
            z = z.times(z).plus(complex);
            i++;
        }
        return i == maxIter;
    }
}
