package ru.tyaguschev.presentation;

/**
 * Класс Point представляет двумерную точку с координатами x и y в виде чисел с плавающей точкой.
 * Это неизменяемая структура данных, реализованная с использованием Java Record.
 *
 * @param x координата точки по оси X
 * @param y координата точки по оси Y
 */
public record Point(Double x, Double y) {}
