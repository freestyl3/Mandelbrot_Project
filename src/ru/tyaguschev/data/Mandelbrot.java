package ru.tyaguschev.data;

public class Mandelbrot {
    private int maxIter = 2000;

    public double isInSet(ComplexNumber complex) {
        ComplexNumber z = new ComplexNumber.Base();
        int i = 0;
        double r2 = 4.0;
        while (z.abs2() < r2 && i < maxIter) {
            z = z.times(z).plus(complex);
            i++;
        }
        return (double) i / maxIter;
    }

    public void setMaxIter(int iters) {
        if (iters >= 13000)
            iters = 13000;
        this.maxIter = iters;
    }

    public int getMaxIter() {
        return this.maxIter;
    }
}
