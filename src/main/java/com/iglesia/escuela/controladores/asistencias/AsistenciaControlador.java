package com.iglesia.escuela.controladores.asistencias;

import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.asistencias.ResumenAsistenciaDTO;
import com.iglesia.escuela.modelos.asistencias.AsistenciasModelo;
import com.iglesia.escuela.modelos.cursos.CursosModelo;
import com.iglesia.escuela.vistas.asistencias.AsistenciasVista;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaControlador extends BaseControlador {

    private final AsistenciasVista asistenciasVista;

    private final AsistenciasModelo asistenciasModelo;

    private final CursosModelo cursosModelo;

    private AbstractTableModel modeloTabla;

    private final TableRowSorter<AbstractTableModel> sorterTabla;

    private final List<ResumenAsistenciaDTO> asistenciaDTOList;

    public AsistenciaControlador() {
        this.asistenciasVista = new AsistenciasVista();
        this.asistenciasModelo = new AsistenciasModelo();
        this.cursosModelo = new CursosModelo();
        this.asistenciaDTOList = new ArrayList<>();
        this.sorterTabla = new TableRowSorter<>();
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        setModeloAsistenciaTable();
        sorterTabla.setModel(modeloTabla);
        asistenciasVista.getAsistenciasTable().setRowSorter(sorterTabla);
        asistenciasVista.getMarcarAsistenciasButton().addActionListener(e -> mostrarEditor());
        asistenciasVista.getFechaChooser().addPropertyChangeListener("date", evt -> buscarAsistencias());
    }

    private void setModeloAsistenciaTable() {
        modeloTabla = new AbstractTableModel() {
            private final String[] columnas = {"Materia", "Grado", "Asistencias", "Inasistencias"};

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
                ResumenAsistenciaDTO asistencia = asistenciaDTOList.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> asistencia.getMateria();
                    case 1 -> asistencia.getGrado();
                    case 2 -> asistencia.getCantidasAsistencias();
                    case 3 -> asistencia.getCantidasInasistencias();
                    default -> null;
                };
            }
        };

        asistenciasVista.getAsistenciasTable().setModel(modeloTabla);
    }

    private void mostrarEditor() {
        EditorAsistenciaControlador editorAsistenciaControlador = new EditorAsistenciaControlador(
                asistenciasModelo, cursosModelo, getAlumnoSeleccionado());
        boolean activado = editorAsistenciaControlador.activar();
        if (!activado) {
            return;
        }

        JDialog dialog = (JDialog) editorAsistenciaControlador.getVista();
        dialog.setVisible(true);

        if (editorAsistenciaControlador.isActualizarTabla()) {
            buscarAsistencias();
        }
    }

    public ResumenAsistenciaDTO getAlumnoSeleccionado() {
        int selectedRow = asistenciasVista.getAsistenciasTable().getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        int modelRow = asistenciasVista.getAsistenciasTable().convertRowIndexToModel(selectedRow);
        return asistenciaDTOList.get(modelRow);
    }

    @Override
    public boolean activar() {
        asistenciasVista.getFechaChooser().setDate(new java.util.Date());
        return true;
    }

    private void buscarAsistencias() {
        if (!asistenciaDTOList.isEmpty()) {
            asistenciaDTOList.clear();
        }

        java.util.Date fechaUtil = asistenciasVista.getFechaChooser().getDate();
        if (fechaUtil == null) {
            return;
        }

        java.sql.Date fecha = new java.sql.Date(fechaUtil.getTime());
        asistenciaDTOList.addAll(asistenciasModelo.getListaResumenAsistenciasPorFecha(fecha));
        modeloTabla.fireTableDataChanged();
    }

    @Override
    public boolean desactivar() {
        return true;
    }

    @Override
    public Component getVista() {
        return asistenciasVista;
    }
}
