package com.iglesia.escuela.controladores.iniciosesion;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.controladores.principal.PrincipalControlador;
import com.iglesia.escuela.modelos.usuarios.UsuariosModelo;
import com.iglesia.escuela.vistas.iniciosesion.InicioSesionVista;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static com.iglesia.escuela.controladores.principal.PrincipalControlador.usuarioSesionDTO;

public class InicioSesionControlador extends BaseControlador {

    private final InicioSesionVista inicioSesionVista;

    private final UsuariosModelo usuariosModelo;

    public InicioSesionControlador() {
        this.inicioSesionVista = new InicioSesionVista();
        this.usuariosModelo = new UsuariosModelo();
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        inicioSesionVista.getIngresarButton().addActionListener(e -> ingresar());
    }

    private void ingresar() {
        if (!validarCampos()) {
            return;
        }

        usuarioSesionDTO = usuariosModelo.ingresar(inicioSesionVista.getUsuarioField().getText(),
                new String(inicioSesionVista.getContrasenaField().getPassword()));

        if (usuarioSesionDTO == null) {
            Alertas.error("Usuario o contraseña incorrectos");
            return;
        }

        inicioSesionVista.dispose();
        PrincipalControlador principalControlador = new PrincipalControlador();
        principalControlador.getVista().setVisible(true);
    }

    private boolean validarCampos() {
        Map<String, JTextField> textFieldMap = new HashMap<>();
        textFieldMap.put("Usuario", inicioSesionVista.getUsuarioField());
        textFieldMap.put("Contraseña", inicioSesionVista.getContrasenaField());

        for (Map.Entry<String, JTextField> entry : textFieldMap.entrySet()) {
            if (entry.getValue().getText().isBlank()) {
                Alertas.error(entry.getKey() + " es obligatorio");
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean activar() {
        return true;
    }

    @Override
    public boolean desactivar() {
        return true;
    }

    @Override
    public Component getVista() {
        return inicioSesionVista;
    }

}
