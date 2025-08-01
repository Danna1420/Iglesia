package com.iglesia.escuela.controladores.base;

import java.awt.*;

public abstract class BaseControlador {

    protected abstract void iniciarEventos();

    public abstract boolean activar();

    public abstract boolean desactivar();

    public abstract Component getVista();

}
