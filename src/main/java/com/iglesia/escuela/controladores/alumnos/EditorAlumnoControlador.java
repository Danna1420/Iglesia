package com.iglesia.escuela.controladores.alumnos;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.componentes.AloneNumberTextField;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;
import com.iglesia.escuela.dtos.cursos.CursoDTO;
import com.iglesia.escuela.modelos.alumnos.AlumnosModelo;
import com.iglesia.escuela.modelos.cursos.CursosModelo;
import com.iglesia.escuela.vistas.alumnos.EditorAlumnoVista;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

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
        ((AbstractDocument) editorAlumnoVista
                .getTallaCamisaField().getDocument()).setDocumentFilter(new AloneNumberTextField(2));
        ((AbstractDocument) editorAlumnoVista.getTallaPantalonField().getDocument()).setDocumentFilter(new AloneNumberTextField(2));
        ((AbstractDocument) editorAlumnoVista.getTallaZapatosField().getDocument()).setDocumentFilter(new AloneNumberTextField(2));
        ((AbstractDocument) editorAlumnoVista.getTelefonoField().getDocument()).setDocumentFilter(new AloneNumberTextField(10));
        ((AbstractDocument) editorAlumnoVista.getTelefonoAcudienteField().getDocument()).setDocumentFilter(new AloneNumberTextField(10));

        editorAlumnoVista.getFechaNacimientoChooser().addPropertyChangeListener("date", evt -> visibilidadCamposMenorDeEdad());

        editorAlumnoVista.getGuardarButton().addActionListener(e -> guardarAlumno());
    }

    private void visibilidadCamposMenorDeEdad() {
        boolean esMenorDeEdad = !esMayorDeEdad(getFechaNacimiento());
        editorAlumnoVista.getAcudienteScrollPane().setVisible(esMenorDeEdad);
        editorAlumnoVista.getTallaCamisaLabel().setVisible(esMenorDeEdad);
        editorAlumnoVista.getTallaCamisaField().setVisible(esMenorDeEdad);
        editorAlumnoVista.getTallaPantalonLabel().setVisible(esMenorDeEdad);
        editorAlumnoVista.getTallaPantalonField().setVisible(esMenorDeEdad);
        editorAlumnoVista.getTallaZapatosLabel().setVisible(esMenorDeEdad);
        editorAlumnoVista.getTallaZapatosField().setVisible(esMenorDeEdad);
        editorAlumnoVista.getTelefonoLabel().setVisible(!esMenorDeEdad);
        editorAlumnoVista.getTelefonoField().setVisible(!esMenorDeEdad);
    }

    private void guardarAlumno() {
        if (!validarCampos()) return;

        if (!Alertas.confirmacion("¿Seguro quiere guardar al alumno?")) return;

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
        Map<String, JTextField> textFieldMap = new LinkedHashMap<>();
        textFieldMap.put("Nombre", editorAlumnoVista.getNombreField());
        textFieldMap.put("Apellido", editorAlumnoVista.getApellidoField());

        boolean esMayorDeEdad = esMayorDeEdad(getFechaNacimiento());
        if (esMayorDeEdad) textFieldMap.put("Teléfono", editorAlumnoVista.getTelefonoField());

        if (!esMayorDeEdad) {
            textFieldMap.put("Talla Camisa", editorAlumnoVista.getTallaCamisaField());
            textFieldMap.put("Talla Pantalón", editorAlumnoVista.getTallaPantalonField());
            textFieldMap.put("Talla Zapatos", editorAlumnoVista.getTallaZapatosField());

            textFieldMap.put("Nombre Acudiente", editorAlumnoVista.getNombreAcudienteField());
            textFieldMap.put("Apellido Acudiente", editorAlumnoVista.getApellidoAcudienteField());
            textFieldMap.put("Teléfono Acudiente", editorAlumnoVista.getTelefonoAcudienteField());
            textFieldMap.put("Casa Oración Acudiente", editorAlumnoVista.getCasaOracionField());
        }

        List<String> camposVacios = new ArrayList<>();
        for (Map.Entry<String, JTextField> entry : textFieldMap.entrySet()) {
            if (entry.getValue().getText().isBlank()) {
                camposVacios.add(entry.getKey());
            }
        }

        if (!camposVacios.isEmpty()) {
            String mensaje = "Los siguientes campos son obligatorios:\n\n- " + String.join("\n- ", camposVacios);
            Alertas.error(mensaje);
            return false;
        }

        return true;
    }

    private LocalDate getFechaNacimiento() {
        Date fechaNacimiento = editorAlumnoVista.getFechaNacimientoChooser().getDate();
        if (fechaNacimiento == null) return null;

        return fechaNacimiento.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }

    public static boolean esMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) return true;

        LocalDate hoy = LocalDate.now();
        int edad = Period.between(fechaNacimiento, hoy).getYears();

        return edad >= 18;
    }

    public static int calcularEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) return 0;

        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    private void setAlumnoDTO() {
        alumnoDTO.setNombre(editorAlumnoVista.getNombreField().getText());
        alumnoDTO.setApellido(editorAlumnoVista.getApellidoField().getText());
        alumnoDTO.setTelefono(editorAlumnoVista.getTelefonoField().getText());
        alumnoDTO.setMateria((String) editorAlumnoVista.getMateriaCombo().getSelectedItem());
        alumnoDTO.setGrado((String) editorAlumnoVista.getGradoCombo().getSelectedItem());
        alumnoDTO.setFechaNacimiento(getFechaNacimiento());
        alumnoDTO.setTallaCamisa(validarParseInt(editorAlumnoVista.getTallaCamisaField().getText()));
        alumnoDTO.setTallaPantalon(validarParseInt(editorAlumnoVista.getTallaPantalonField().getText()));
        alumnoDTO.setTallaZapato(validarParseInt(editorAlumnoVista.getTallaZapatosField().getText()));

        alumnoDTO.setNombreAcudiente(editorAlumnoVista.getNombreAcudienteField().getText());
        alumnoDTO.setApellidoAcudiente(editorAlumnoVista.getApellidoAcudienteField().getText());
        alumnoDTO.setTelefonoAcudiente(editorAlumnoVista.getTelefonoAcudienteField().getText());
        alumnoDTO.setCasaOracionAcudiente(editorAlumnoVista.getCasaOracionField().getText());
    }

    public static int validarParseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return 0;
        }
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
        editorAlumnoVista.getFechaNacimientoChooser().setDate(new Date());
        editorAlumnoVista.getTallaCamisaField().setText("");
        editorAlumnoVista.getTallaPantalonField().setText("");
        editorAlumnoVista.getTallaZapatosField().setText("");

        editorAlumnoVista.getNombreAcudienteField().setText("");
        editorAlumnoVista.getApellidoAcudienteField().setText("");
        editorAlumnoVista.getTelefonoAcudienteField().setText("");
        editorAlumnoVista.getCasaOracionField().setText("");
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
            editorAlumnoVista.getFechaNacimientoChooser().setDate(new java.util.Date());
            return;
        }

        editorAlumnoVista.getNombreField().setText(alumnoDTO.getNombre());
        editorAlumnoVista.getApellidoField().setText(alumnoDTO.getApellido());
        editorAlumnoVista.getTelefonoField().setText(alumnoDTO.getTelefono());
        editorAlumnoVista.getGradoCombo().setSelectedItem(alumnoDTO.getGrado());
        editorAlumnoVista.getMateriaCombo().setSelectedItem(alumnoDTO.getMateria());

        LocalDate fechaNacimiento = alumnoDTO.getFechaNacimiento();
        if (fechaNacimiento != null) {
            Date date = Date.from(fechaNacimiento.atStartOfDay(ZoneId.systemDefault()).toInstant());
            editorAlumnoVista.getFechaNacimientoChooser().setDate(date);
        } else visibilidadCamposMenorDeEdad();

        editorAlumnoVista.getTallaCamisaField().setText(String.valueOf(alumnoDTO.getTallaCamisa()));
        editorAlumnoVista.getTallaPantalonField().setText(String.valueOf(alumnoDTO.getTallaPantalon()));
        editorAlumnoVista.getTallaZapatosField().setText(String.valueOf(alumnoDTO.getTallaZapato()));
        editorAlumnoVista.getNombreAcudienteField().setText(alumnoDTO.getNombreAcudiente());
        editorAlumnoVista.getApellidoAcudienteField().setText(alumnoDTO.getApellidoAcudiente());
        editorAlumnoVista.getTelefonoAcudienteField().setText(alumnoDTO.getTelefonoAcudiente());
        editorAlumnoVista.getCasaOracionField().setText(alumnoDTO.getCasaOracionAcudiente());
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
