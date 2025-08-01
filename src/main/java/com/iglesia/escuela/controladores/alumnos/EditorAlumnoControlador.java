package com.iglesia.escuela.controladores.alumnos;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;
import com.iglesia.escuela.dtos.cursos.CursoDTO;
import com.iglesia.escuela.modelos.alumnos.AlumnosModelo;
import com.iglesia.escuela.modelos.cursos.CursosModelo;
import com.iglesia.escuela.vistas.alumnos.EditorAlumnoVista;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorAlumnoControlador extends BaseControlador {

    private final EditorAlumnoVista editorAlumnoVista;

    private final AlumnosModelo alumnosModelo;

    private final CursosModelo cursosModelo;

    private AlumnoDTO alumnoDTO;

    private boolean actualizarTabla;

    public EditorAlumnoControlador(AlumnosModelo alumnosModelo, CursosModelo cursosModelo, AlumnoDTO alumnoDTO) {
        this.editorAlumnoVista = new EditorAlumnoVista(null, true);
        this.alumnosModelo = alumnosModelo;
        this.cursosModelo = cursosModelo;
        this.alumnoDTO = alumnoDTO;
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        editorAlumnoVista.getGuardarButton().addActionListener(e -> guardarAlumno());
    }

    private void guardarAlumno() {
        if (!validarCampos()) {
            return;
        }

        if (!Alertas.confirmacion("¿Seguro quiere guardar al alumno?")) {
            return;
        }

        setAlumnoDTO();
        boolean guardado = alumnosModelo.guardarAlumno(alumnoDTO);
        if (!guardado) {
            Alertas.error("No se pudo guardar el alumno");
            return;
        }

        if (esNuevo()) {
            limpiarCampos();
            alumnoDTO = new AlumnoDTO();
        }

        actualizarTabla = true;
        Alertas.informacion("Alumno guardado correctamente");
    }

    private boolean validarCampos() {
        Map<String, JTextField> textFieldMap = new HashMap<>();
        textFieldMap.put("Nombre", editorAlumnoVista.getNombreField());
        textFieldMap.put("Apellido", editorAlumnoVista.getApellidoField());
        textFieldMap.put("Telefono", editorAlumnoVista.getTelefonoField());

        for (Map.Entry<String, JTextField> entry : textFieldMap.entrySet()) {
            if (entry.getValue().getText().isBlank()) {
                Alertas.error(entry.getKey() + " es obligatorio");
                return false;
            }
        }

        return true;
    }

    private void setAlumnoDTO() {
        alumnoDTO.setNombre(editorAlumnoVista.getNombreField().getText());
        alumnoDTO.setApellido(editorAlumnoVista.getApellidoField().getText());
        alumnoDTO.setTelefono(editorAlumnoVista.getTelefonoField().getText());
        alumnoDTO.setMateria((String) editorAlumnoVista.getMateriaCombo().getSelectedItem());
        alumnoDTO.setGrado((String) editorAlumnoVista.getGradoCombo().getSelectedItem());
    }

    private boolean esNuevo() {
        return alumnoDTO.getId() == null;
    }

    private void limpiarCampos() {
        editorAlumnoVista.getNombreField().setText("");
        editorAlumnoVista.getApellidoField().setText("");
        editorAlumnoVista.getTelefonoField().setText("");
        editorAlumnoVista.getGradoCombo().setSelectedIndex(0);
        editorAlumnoVista.getMateriaCombo().setSelectedIndex(0);
    }

    @Override
    public boolean activar() {
        setModeloCombos();
        setCampos();
        return true;
    }

    private void setModeloCombos() {
        List<String> cursoDTOList = cursosModelo.getListaCursos().stream()
                .map(CursoDTO::getNombre)
                .toList();
        editorAlumnoVista.getMateriaCombo().setModel(new DefaultComboBoxModel<>(cursoDTOList.toArray(new String[0])));
    }

    private void setCampos() {
        if (esNuevo()) {
            return;
        }

        editorAlumnoVista.getNombreField().setText(alumnoDTO.getNombre());
        editorAlumnoVista.getApellidoField().setText(alumnoDTO.getApellido());
        editorAlumnoVista.getTelefonoField().setText(alumnoDTO.getTelefono());
        editorAlumnoVista.getGradoCombo().setSelectedItem(alumnoDTO.getGrado());
        editorAlumnoVista.getMateriaCombo().setSelectedItem(alumnoDTO.getMateria());
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
        return editorAlumnoVista;
    }
}
