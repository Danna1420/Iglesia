package com.iglesia.escuela.controladores.calificaciones;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.calificaciones.CalificacionDTO;
import com.iglesia.escuela.dtos.calificaciones.NotaDTO;
import com.iglesia.escuela.modelos.calificaciones.CalificacionesModelo;
import com.iglesia.escuela.vistas.calificaciones.EditorNotaVista;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class EditorNotaControlador extends BaseControlador {

    private final EditorNotaVista editorNotaVista;

    private final CalificacionesModelo calificacionesModelo;

    private final CalificacionDTO calificacionDTO;

    private NotaDTO notaDTO;

    private boolean actualizarTabla;

    public EditorNotaControlador(CalificacionesModelo calificacionesModelo, CalificacionDTO calificacionDTO, NotaDTO notaDTO) {
        this.editorNotaVista = new EditorNotaVista(null, true);
        this.calificacionesModelo = calificacionesModelo;
        this.calificacionDTO = calificacionDTO;
        this.notaDTO = notaDTO;
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        editorNotaVista.getGuardarButton().addActionListener(e -> guardarNota());
    }

    private void guardarNota() {
        if (!validarCampos()) {
            return;
        }

        if (!Alertas.confirmacion("¿Seguro quiere guardar al nota?")) {
            return;
        }

        setNotaDTO();
        boolean guardado = calificacionesModelo.guardarNota(notaDTO);
        if (!guardado) {
            Alertas.error("No se pudo guardar el nota");
            return;
        }

        if (esNuevo()) {
            limpiarCampos();
            notaDTO = new NotaDTO();
        }

        actualizarTabla = true;
        Alertas.informacion("Nota guardado correctamente");
    }

    private boolean validarCampos() {
        Map<String, JTextField> textFieldMap = new HashMap<>();
        textFieldMap.put("Tarea", editorNotaVista.getTareaField());
        textFieldMap.put("Calificación", editorNotaVista.getCalificacionField());

        for (Map.Entry<String, JTextField> entry : textFieldMap.entrySet()) {
            if (entry.getValue().getText().isBlank()) {
                Alertas.error(entry.getKey() + " es obligatorio");
                return false;
            }
        }

        return true;
    }

    private void setNotaDTO() {
        notaDTO.setTarea(editorNotaVista.getTareaField().getText());
        notaDTO.setAlumnoId(calificacionDTO.getAlumnoDTO().getId());
        notaDTO.setMateria(calificacionDTO.getAlumnoDTO().getMateria());
        notaDTO.setCalificacion(BigDecimal.valueOf(Double.parseDouble(editorNotaVista.getCalificacionField().getText())));
    }

    private boolean esNuevo() {
        return notaDTO.getId() == 0;
    }

    private void limpiarCampos() {
        editorNotaVista.getTareaField().setText("");
        editorNotaVista.getCalificacionField().setText("");
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

        editorNotaVista.getTareaField().setText(notaDTO.getTarea());
        editorNotaVista.getCalificacionField().setText(notaDTO.getCalificacion().toPlainString());
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
        return editorNotaVista;
    }
}
