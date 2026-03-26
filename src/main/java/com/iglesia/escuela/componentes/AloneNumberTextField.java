package com.iglesia.escuela.componentes;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Clase personalizada que extiende {@link DocumentFilter} para permitir
 * únicamente la entrada de números en un campo de texto con una longitud máxima.
 */
public class AloneNumberTextField extends DocumentFilter {

    /**
     * Longitud máxima permitida para el texto.
     */
    private final int maxLength;

    /**
     * Constructor de la clase.
     *
     * @param maxLength la longitud máxima permitida para el texto.
     */
    public AloneNumberTextField(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) return;

        string = normalizeIntegerString(string);
        StringBuilder currentText = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
        currentText.insert(offset, string);

        if ((isNumeric(currentText.toString()) && isWithinLimit(currentText.length())) || string.isBlank()) {
            super.insertString(fb, offset, string, attr);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) return;

        text = normalizeIntegerString(text);
        StringBuilder currentText = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
        currentText.replace(offset, offset + length, text);

        if ((isNumeric(currentText.toString()) && isWithinLimit(currentText.length())) || text.isBlank()) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        super.remove(fb, offset, length);
    }

    /**
     * Verifica si el texto ingresado es numérico.
     *
     * @param text el texto a verificar.
     * @return {@code true} si el texto es numérico, {@code false} en caso contrario.
     */
    private boolean isNumeric(String text) {
        try {
            BigDecimal number = new BigDecimal(text);
            return number.stripTrailingZeros().scale() <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Verifica si el texto nuevo cumple con la longitud máxima permitida.
     *
     * @param newTextLength la longitud del texto nuevo a insertar.
     * @return {@code true} si la longitud resultante está dentro del límite, {@code false} en caso contrario.
     */
    private boolean isWithinLimit(int newTextLength) {
        return newTextLength <= maxLength;
    }

    /**
     * Convierte una cadena numérica a su forma entera si no tiene parte decimal significativa.
     *
     * @param text la cadena numérica a normalizar, por ejemplo "10.000000"
     * @return una cadena sin decimales si representa un número entero, de lo contrario retorna el mismo texto
     */
    public String normalizeIntegerString(String text) {
        try {
            BigDecimal number = new BigDecimal(text);
            if (number.stripTrailingZeros().scale() <= 0) {
                return number.setScale(0, RoundingMode.DOWN).toPlainString();
            }
        } catch (NumberFormatException e) {
            return "";
        }
        return text;
    }

}
