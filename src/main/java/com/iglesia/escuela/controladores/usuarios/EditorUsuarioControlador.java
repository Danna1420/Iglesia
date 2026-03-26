package com.iglesia.escuela.controladores.usuarios;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.usuarios.UsuarioDTO;
import com.iglesia.escuela.modelos.usuarios.UsuariosModelo;
import com.iglesia.escuela.vistas.usuarios.EditorUsuarioVista;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class EditorUsuarioControlador extends BaseControlador {

    private final EditorUsuarioVista editorUsuarioVista;

    private final UsuariosModelo usuariosModelo;

    private UsuarioDTO usuarioDTO;

    private boolean actualizarTabla;

    public EditorUsuarioControlador(UsuariosModelo usuariosModelo, UsuarioDTO usuarioDTO) {
        this.editorUsuarioVista = new EditorUsuarioVista(null, true);
        this.usuariosModelo = usuariosModelo;
        this.usuarioDTO = usuarioDTO;
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        editorUsuarioVista.getGuardarButton().addActionListener(e -> guardarUsuario());
    }

    private void guardarUsuario() {
        if (!validarCampos()) {
            return;
        }

        if (!Alertas.confirmacion("¿Seguro quiere guardar al usuario?")) {
            return;
        }

        setUsuarioDTO();
        boolean guardado = usuariosModelo.guardarUsuario(usuarioDTO);
        if (!guardado) {
            Alertas.error("No se pudo guardar el usuario");
            return;
        }

        if (esNuevo()) {
            limpiarCampos();
            usuarioDTO = new UsuarioDTO();
        }

        actualizarTabla = true;
        Alertas.informacion("Usuario guardado correctamente");
    }

    private boolean validarCampos() {
        Map<String, JTextField> textFieldMap = new HashMap<>();
        textFieldMap.put("Nombre", editorUsuarioVista.getNombreField());
        textFieldMap.put("Contraseña", editorUsuarioVista.getContrasenaField());

        for (Map.Entry<String, JTextField> entry : textFieldMap.entrySet()) {
            if (entry.getValue().getText().isBlank()) {
                Alertas.error(entry.getKey() + " es obligatorio");
                return false;
            }
        }

        return true;
    }

    private void setUsuarioDTO() {
        usuarioDTO.setNombre(editorUsuarioVista.getNombreField().getText());
        usuarioDTO.setContrasena(new String(editorUsuarioVista.getContrasenaField().getPassword()));
    }

    private boolean esNuevo() {
        return usuarioDTO.getId() == null;
    }

    private void limpiarCampos() {
        editorUsuarioVista.getNombreField().setText("");
        editorUsuarioVista.getContrasenaField().setText("");
    }

    @Override
    public boolean activar() {
        setCampos();
        return true;
    }

    private void setCampos() {
        if (esNuevo()) {
            return;
        }

        editorUsuarioVista.getNombreField().setText(usuarioDTO.getNombre());
    }

    public boolean isActualizarTabla() {
        return actualizarTabla;
    }

    @Override
    public boolean desactivar() {
        return true;
    }

    @Override
    public Component getVista() {
        return editorUsuarioVista;
    }
}
