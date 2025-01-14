package ru.tyaguschev.data;

/**
 * Интерфейс ComplexNumber представляет комплексное число и определяет основные операции
 * над ним, такие как сложение, вычитание, умножение, деление и возведение в степень.
 */
public interface ComplexNumber {

    /**
     * Возвращает действительную часть комплексного числа.
     *
     * @return значение действительной части
     */
    Double realValue();

    /**
     * Возвращает мнимую часть комплексного числа.
     *
     * @return значение мнимой части
     */
    Double imaginaryValue();

    /**
     * Вычисляет модуль (абсолютное значение) комплексного числа.
     *
     * @return модуль числа
     */
    Double abs();

    /**
     * Вычисляет квадрат модуля комплексного числа.
     *
     * @return квадрат модуля числа
     */
    Double abs2();

    /**
     * Умножает текущее комплексное число на другое.
     *
     * @param other другое комплексное число
     * @return результат умножения
     */
    ComplexNumber times(ComplexNumber other);

    /**
     * Складывает текущее комплексное число с другим.
     *
     * @param other другое комплексное число
     * @return результат сложения
     */
    ComplexNumber plus(ComplexNumber other);

    /**
     * Вычитает из текущего комплексного числа другое.
     *
     * @param other другое комплексное число
     * @return результат вычитания
     */
    ComplexNumber minus(ComplexNumber other);

    /**
     * Делит текущее комплексное число на другое.
     *
     * @param other другое комплексное число
     * @return результат деления
     */
    ComplexNumber divide(ComplexNumber other);

    /**
     * Возводит текущее комплексное число в целую степень.
     *
     * @param number степень
     * @return результат возведения в степень
     */
    ComplexNumber pow(Integer number);

    /**
     * Класс Base представляет стандартную реализацию интерфейса ComplexNumber.
     */
    class Base implements ComplexNumber {

        private final double real; // Действительная часть числа
        private final double imaginary; // Мнимая часть числа

        /**
         * Создает комплексное число с нулевыми значениями реальной и мнимой частей.
         */
        public Base() {
            this.real = 0;
            this.imaginary = 0;
        }

        /**
         * Создает комплексное число с заданными значениями реальной и мнимой частей.
         *
         * @param real      действительная часть
         * @param imaginary мнимая часть
         */
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
            double realNumerator = this.real * other.realValue() + this.imaginary * other.imaginaryValue();
            double imaginaryNumerator = this.imaginary * other.realValue() - this.real * other.imaginaryValue();
            return new Base(realNumerator / denominator, imaginaryNumerator / denominator);
        }

        @Override
        public Double abs() {
            return Math.sqrt(this.real * this.real + this.imaginary * this.imaginary);
        }

        @Override
        public Double abs2() {
            return this.real * this.real + this.imaginary * this.imaginary;
        }

        @Override
        public ComplexNumber pow(Integer number) {
            double real = this.real;
            double imaginary = this.imaginary;
            ComplexNumber result = new Base(real, imaginary);
            for (int i = 1; i < number; i++) {
                result = result.times(new Base(real, imaginary));
            }
            return result;
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
