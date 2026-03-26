package com.iglesia.escuela.componentes;

import javax.swing.*;
import java.awt.*;

/**
 * PanelFlowCustom es un JPanel personalizado que implementa un diseño de flujo con soporte para Scrollable,
 * permitiendo organizar los componentes en líneas dinámicamente y adaptarse al ancho del contenedor.
 */
public class PanelFlowCustom extends JPanel implements Scrollable {

    /**
     * Espacio horizontal entre componentes
     *
     */
    private final int hGap;

    /**
     * Espacio vertical entre componentes
     *
     */
    private final int vGap;

    /**
     * Constructor que inicializa los espacios horizontales y verticales predeterminados entre componentes.
     */
    public PanelFlowCustom(int hGap, int vGap) {
        this.hGap = hGap;
        this.vGap = vGap;
        setOpaque(false);
        setBorder(null);
    }

    /**
     * Constructor que inicializa los espacios horizontales y verticales predeterminados entre componentes.
     */
    public PanelFlowCustom() {
        this(5, 5);
    }

    /**
     * Calcula el diseño del flujo del panel, distribuyendo los componentes en filas
     * y ajustándolos al ancho disponible del contenedor.
     *
     * @param children Si es true, establece las posiciones y tamaños de los componentes.
     * @return Las dimensiones calculadas para el diseño.
     */
    private Dimension calculateFlowLayout(boolean children) {
        Dimension dim = new Dimension(0, hGap);
        int maxWidth = (getParent() instanceof JViewport viewport)
                ? viewport.getExtentSize().width
                : (getParent() != null ? getParent().getWidth() : getWidth());

        synchronized (getTreeLock()) {
            int maxRowWidth = 0;
            int maxRowHeight = 0;
            int x = 0;

            for (Component m : getComponents()) {
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();

                    // Verificar si el componente cabe en la fila actual
                    if (x == 0 || (x + hGap + d.width + hGap) <= maxWidth) {
                        x += hGap;
                        if (children) {
                            m.setBounds(getPosition(x, maxWidth - d.width), dim.height, d.width, d.height);
                        }
                        x += d.width;
                        maxRowHeight = Math.max(maxRowHeight, d.height);
                    } else {
                        // Crear una nueva fila
                        dim.height += maxRowHeight + vGap;
                        if (children) {
                            m.setBounds(getPosition(hGap, maxWidth - d.width), dim.height, d.width, d.height);
                        }
                        maxRowWidth = Math.max(maxRowWidth, x);
                        x = hGap + d.width;
                        maxRowHeight = d.height;
                    }
                }
            }

            // Ajustar la última fila
            dim.height += maxRowHeight + vGap;
            dim.width = Math.max(maxRowWidth, x);
        }
        return dim;
    }

    /**
     * Calcula la posición horizontal de un componente, considerando la orientación del panel.
     *
     * @param x     Posición actual en el eje X.
     * @param width Ancho disponible del contenedor.
     * @return La posición ajustada según la orientación.
     */
    private int getPosition(int x, int width) {
        return (getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT)
                ? width - x
                : x + 5; // Ajuste adicional para alineación
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doLayout() {
        calculateFlowLayout(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return calculateFlowLayout(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getMinimumSize() {
        return calculateFlowLayout(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getMaximumSize() {
        return calculateFlowLayout(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return calculateFlowLayout(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport viewport
                && viewport.getHeight() > getPreferredSize().height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getParent() instanceof JViewport viewport
                && viewport.getWidth() > getPreferredSize().width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (getComponentCount() == 0)
                ? (orientation == SwingConstants.HORIZONTAL ? hGap : vGap)
                : (orientation == SwingConstants.HORIZONTAL
                ? getComponent(0).getWidth() + hGap
                : getComponent(0).getHeight() + vGap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (getComponentCount() == 0) {
            return orientation == SwingConstants.HORIZONTAL ? hGap : vGap;
        }

        if (orientation == SwingConstants.HORIZONTAL) {
            int hUnit = getComponent(0).getWidth() + hGap;
            return (visibleRect.width / hUnit) * hUnit;
        } else {
            int vUnit = getComponent(0).getHeight() + vGap;
            return (visibleRect.height / vUnit) * vUnit;
        }
    }

}
