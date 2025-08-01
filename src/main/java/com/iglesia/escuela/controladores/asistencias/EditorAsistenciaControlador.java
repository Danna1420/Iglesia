package com.iglesia.escuela.controladores.asistencias;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.asistencias.AsistenciaDTO;
import com.iglesia.escuela.dtos.asistencias.ResumenAsistenciaDTO;
import com.iglesia.escuela.dtos.cursos.CursoDTO;
import com.iglesia.escuela.modelos.asistencias.AsistenciasModelo;
import com.iglesia.escuela.modelos.cursos.CursosModelo;
import com.iglesia.escuela.vistas.asistencias.EditorAsistenciaVista;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EditorAsistenciaControlador extends BaseControlador {

    private final EditorAsistenciaVista editorAsistenciaVista;

    private final AsistenciasModelo asistenciasModelo;

    private final CursosModelo cursosModelo;

    private final ResumenAsistenciaDTO resumenAsistenciaDTO;

    private AbstractTableModel modeloTabla;

    private final List<AsistenciaDTO> asistenciaDTOList;

    private boolean actualizarTabla;

    public EditorAsistenciaControlador(AsistenciasModelo asistenciasModelo, CursosModelo cursosModelo, ResumenAsistenciaDTO resumenAsistenciaDTO) {
        this.editorAsistenciaVista = new EditorAsistenciaVista(null, true);
        this.asistenciasModelo = asistenciasModelo;
        this.cursosModelo = cursosModelo;
        this.resumenAsistenciaDTO = resumenAsistenciaDTO;
        this.asistenciaDTOList = new ArrayList<>();
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        setModeloAsistenciaTable();
        editorAsistenciaVista.getGuardarButton().addActionListener(e -> guardarAsistencia());
        editorAsistenciaVista.getBuscarButton().addActionListener(e -> buscarAsistencias());
    }

    private void setModeloAsistenciaTable() {
        modeloTabla = new AbstractTableModel() {
            private final String[] columnas = {"Nombre", "Apellido", "Presente"};

            @Override
            public int getRowCount() {
                return asistenciaDTOList.size();
            }

            @Override
            public int getColumnCount() {
                return columnas.length;
            }

            @Override
            public String getColumnName(int column) {
                return columnas[column];
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                AsistenciaDTO asistencia = asistenciaDTOList.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> asistencia.getAlumnoDTO().getNombre();
                    case 1 -> asistencia.getAlumnoDTO().getApellido();
                    case 2 -> asistencia.isPresente();
                    default -> null;
                };
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 2; // Solo la columna "Presente" es editable
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                if (columnIndex == 2) {
                    asistenciaDTOList.get(rowIndex).setPresente((Boolean) aValue);
                    fireTableCellUpdated(rowIndex, columnIndex);
                }
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 1 -> String.class;
                    case 2 -> Boolean.class;
                    default -> Object.class;
                };
            }
        };

        editorAsistenciaVista.getAsistenciasTable().setModel(modeloTabla);
    }

    private void guardarAsistencia() {
        if (!Alertas.confirmacion("¿Seguro quiere guardar las asistencias?")) {
            return;
        }

        boolean guardado = asistenciasModelo.guardarAsistencias(asistenciaDTOList);
        if (!guardado) {
            Alertas.error("No se pudo guardar las asistencias");
            return;
        }

        actualizarTabla = true;
        Alertas.informacion("Asistencias guardadas correctamente");
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
        editorAsistenciaVista.getMateriaCombo().setModel(new DefaultComboBoxModel<>(cursoDTOList.toArray(new String[0])));
    }

    private void setCampos() {
        if (resumenAsistenciaDTO == null) {
            editorAsistenciaVista.getFechaChooser().setDate(new java.util.Date());
        } else {
            editorAsistenciaVista.getGradoCombo().setSelectedItem(resumenAsistenciaDTO.getGrado());
            editorAsistenciaVista.getMateriaCombo().setSelectedItem(resumenAsistenciaDTO.getMateria());
            editorAsistenciaVista.getFechaChooser().setDate(resumenAsistenciaDTO.getFecha());
            buscarAsistencias();
        }
    }

    private void buscarAsistencias() {
        if (!asistenciaDTOList.isEmpty()) {
            asistenciaDTOList.clear();
        }

        java.util.Date fechaUtil = editorAsistenciaVista.getFechaChooser().getDate();
        if (fechaUtil == null) {
            return;
        }

        java.sql.Date fecha = new java.sql.Date(fechaUtil.getTime());
        String materia = (String) editorAsistenciaVista.getMateriaCombo().getSelectedItem();
        String grado = (String) editorAsistenciaVista.getGradoCombo().getSelectedItem();
        asistenciaDTOList.addAll(asistenciasModelo.getListaAlumnos(fecha, materia, grado));
        modeloTabla.fireTableDataChanged();
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
        return editorAsistenciaVista;
    }
}
