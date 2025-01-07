package ru.tyaguschev.fractals;

import ru.tyaguschev.math.complex.ComplexNumber;

public class Mandelbrot {
    private final double r2 = 4.0;
    private int maxIter = 5000;

    public double isInSet(ComplexNumber complex) {
        ComplexNumber z = new ComplexNumber.Base();
        int i = 0;
        while (z.abs2() < r2 && i < maxIter) {
            z = z.times(z).plus(complex);
            i++;
        }
        return (double) i / maxIter;
    }

    public void setMaxIter(int iters) {
        this.maxIter = iters;
    }

    public int getMaxIter() {
        return this.maxIter;
    }
}
