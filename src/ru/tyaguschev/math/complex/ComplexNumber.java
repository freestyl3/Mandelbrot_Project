package ru.tyaguschev.math.complex;

public interface ComplexNumber {
    Double realValue();
    Double imaginaryValue();
    Double abs();
    Double abs2();
    ComplexNumber times(ComplexNumber other);
    ComplexNumber plus(ComplexNumber other);
    ComplexNumber minus(ComplexNumber complex);
    ComplexNumber divide(ComplexNumber complex);
    ComplexNumber pow(Integer number);


    class Base implements ComplexNumber {

        private final double real;
        private final double imaginary;

        public Base() {
            this.real = 0;
            this.imaginary = 0;
        }

        public Base(double real, double imaginary) {
            this.real = real;
            this.imaginary = imaginary;
        }

        @Override
        public ComplexNumber plus(ComplexNumber other) {
            return new Base(this.real + other.realValue(), this.imaginary + other.imaginaryValue());
        }

        @Override
        public ComplexNumber minus(ComplexNumber other) {
            return new Base(this.real - other.realValue(), this.imaginary - other.imaginaryValue());
        }

        @Override
        public ComplexNumber times(ComplexNumber other) {
            double newReal = this.real * other.realValue() - this.imaginary * other.imaginaryValue();
            double newImaginary = this.real * other.imaginaryValue() + other.realValue() * this.imaginary;
            return new Base(newReal, newImaginary);
        }

        @Override
        public ComplexNumber divide(ComplexNumber other) {
            double denominator = Math.pow(other.realValue(), 2) + Math.pow(other.imaginaryValue(), 2);
            double realNumenator = this.real * other.realValue() + this.imaginary * other.imaginaryValue();
            double imaginaryNumenator = this.imaginary * other.realValue() - this.real * other.imaginaryValue();
            return new Base(realNumenator / denominator, imaginaryNumenator / denominator);
        }

        @Override
        public Double abs() { return Math.sqrt(this.real * this.real + this.imaginary * this.imaginary); }

        @Override
        public Double abs2() { return this.real * this.real + this.imaginary * this.imaginary; }

        @Override
        public ComplexNumber pow(Integer number) {
            double real = this.real;
            double imaginary = this.imaginary;
            ComplexNumber newComplex = new Base(real, imaginary);
            for (int i = 1; i < number; i++) {
                newComplex = newComplex.times(new Base(real, imaginary));
            }
            return newComplex;
        }

        @Override
        public String toString() {
            var stringComplex = new StringBuilder();

            stringComplex.append(this.real);
            stringComplex.append(' ');
            if (this.imaginary >= 0)
                stringComplex.append("+ ");
            else
                stringComplex.append("- ");
            stringComplex.append(Math.abs(this.imaginary));
            stringComplex.append('i');

            return stringComplex.toString();
        }

        @Override
        public Double realValue() {
            return real;
        }

        @Override
        public Double imaginaryValue() {
            return imaginary;
        }
    }
}
