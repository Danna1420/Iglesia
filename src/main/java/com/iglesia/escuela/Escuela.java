package com.iglesia.escuela;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.iniciosesion.InicioSesionControlador;

public class Escuela {

    public static void main(String[] args) {
        try{
            FlatLaf.registerCustomDefaultsSource("tema");
            FlatLightLaf.setup();

            InicioSesionControlador inicioSesionControlador = new InicioSesionControlador();
            inicioSesionControlador.getVista().setVisible(true);
        } catch (Exception e) {
            Alertas.error("No se pudo iniciar el programa: " + e.getMessage());
        }
    }
}
