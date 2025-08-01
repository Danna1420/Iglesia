package com.iglesia.escuela.controladores.cursos;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.cursos.CursoDTO;
import com.iglesia.escuela.modelos.cursos.CursosModelo;
import com.iglesia.escuela.vistas.curso.EditorCursoVista;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class EditorCursoControlador extends BaseControlador {

    private final EditorCursoVista editorCursoVista;

    private final CursosModelo cursosModelo;

    private CursoDTO cursoDTO;

    private boolean actualizarTabla;

    public EditorCursoControlador(CursosModelo cursosModelo, CursoDTO cursoDTO) {
        this.editorCursoVista = new EditorCursoVista(null, true);
        this.cursosModelo = cursosModelo;
        this.cursoDTO = cursoDTO;
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        editorCursoVista.getGuardarButton().addActionListener(e -> guardarCurso());
    }

    private void guardarCurso() {
        if (!validarCampos()) {
            return;
        }

        if (!Alertas.confirmacion("¿Seguro quiere guardar al curso?")) {
            return;
        }

        setCursoDTO();
        boolean guardado = cursosModelo.guardarCurso(cursoDTO);
        if (!guardado) {
            Alertas.error("No se pudo guardar el curso");
            return;
        }

        if (esNuevo()) {
            limpiarCampos();
            cursoDTO = new CursoDTO();
        }

        actualizarTabla = true;
        Alertas.informacion("Curso guardado correctamente");
    }

    private boolean validarCampos() {
        Map<String, JTextField> textFieldMap = new HashMap<>();
        textFieldMap.put("Nombre", editorCursoVista.getNombreField());

        for (Map.Entry<String, JTextField> entry : textFieldMap.entrySet()) {
            if (entry.getValue().getText().isBlank()) {
                Alertas.error(entry.getKey() + " es obligatorio");
                return false;
            }
        }

        return true;
    }

    private void setCursoDTO() {
        cursoDTO.setNombre(editorCursoVista.getNombreField().getText());
    }

    private boolean esNuevo() {
        return cursoDTO.getId() == null;
    }

    private void limpiarCampos() {
        editorCursoVista.getNombreField().setText("");
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

        editorCursoVista.getNombreField().setText(cursoDTO.getNombre());
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
        return editorCursoVista;
    }
}
