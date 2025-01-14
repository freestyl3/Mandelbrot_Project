package ru.tyaguschev.presentation;

/**
 * Класс ScreenConverter выполняет преобразование координат с экрана в декартовы.
 * Он использует заданный масштаб, размеры экрана и центральную точку для выполнения преобразований.
 */
public class ScreenConverter {
    /**
     * Высота экрана.
     */
    public final Integer height;

    /**
     * Ширина экрана.
     */
    public final Integer width;

    /**
     * Центральная точка в декартовой системе координат.
     */
    private final Point centre;

    /**
     * Масштаб, определяющий соотношение пикселей экрана к единицам декартовой системы.
     */
    public final Double scale;

    /**
     * Конструктор ScreenConverter.
     *
     * @param centre центральная точка в декартовой системе координат.
     * @param height высота экрана в пикселях.
     * @param width ширина экрана в пикселях.
     * @param scale масштаб, определяющий размер единицы координат в пикселях.
     */
    public ScreenConverter(
            Point centre,
            Integer height,
            Integer width,
            Double scale
    ) {
        this.centre = centre;
        this.height = height;
        this.width = width;
        this.scale = scale;
    }

    /**
     * Преобразует координату X с экрана в декартовую систему координат.
     *
     * @param x координата X в пикселях на экране.
     * @return координата X в декартовой системе.
     */
    public double xScreenToCartesian(int x) {
        return (-width / 2.0 + x) * scale + centre.x();
    }

    /**
     * Преобразует координату Y с экрана в декартовую систему координат.
     *
     * @param y координата Y в пикселях на экране.
     * @return координата Y в декартовой системе.
     */
    public double yScreenToCartesian(int y) {
        return (-height / 2.0 + y) * scale + centre.y();
    }
}
