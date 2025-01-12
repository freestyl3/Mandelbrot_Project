package ru.tyaguschev.data;

public class Mandelbrot {
    private int maxIterations = 2000;

    public double isInSet(ComplexNumber complex) {
        ComplexNumber z = new ComplexNumber.Base();
        int i = 0;
        double r2 = 4.0;
        while (z.abs2() < r2 && i < maxIterations) {
            z = z.times(z).plus(complex);
            i++;
        }
        return (double) i / maxIterations;
    }

    public void setMaxIterations(int iters) {
        if (iters >= 13000)
            iters = 13000;
        this.maxIterations = iters;
    }

    public int getMaxIterations() {
        return this.maxIterations;
    }
}
