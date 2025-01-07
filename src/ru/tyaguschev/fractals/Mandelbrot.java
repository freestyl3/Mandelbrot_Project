package ru.tyaguschev.fractals;

import ru.tyaguschev.math.complex.ComplexNumber;

public class Mandelbrot {
    private int maxIter = 200;

    public double isInSet(ComplexNumber complex) {
        ComplexNumber z = new ComplexNumber.Base();
        int i = 0;
        double r2 = 4.0;
        while (z.abs2() < r2 && i < maxIter) {
            z = z.times(z).plus(complex);
            i++;
        }
//        double coeff = (double) i / maxIter;
//        if (coeff > 0.5)
//            return 1;
//        else
//            return coeff;
        return (double) i / maxIter;
//        return z.abs();
    }

    public void setMaxIter(int iters) {
        this.maxIter = iters;
    }

    public int getMaxIter() {
        return this.maxIter;
    }
}
