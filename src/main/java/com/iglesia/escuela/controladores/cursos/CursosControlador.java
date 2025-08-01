package com.iglesia.escuela.controladores.cursos;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.cursos.CursoDTO;
import com.iglesia.escuela.modelos.cursos.CursosModelo;
import com.iglesia.escuela.vistas.curso.CursosVista;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CursosControlador extends BaseControlador {

    private final CursosVista cursosVista;

    private final CursosModelo cursosModelo;

    private AbstractTableModel modeloTabla;

    private final TableRowSorter<AbstractTableModel> sorterTabla;

    private final List<CursoDTO> cursoDTOList;

    public CursosControlador() {
        this.cursosVista = new CursosVista();
        this.cursosModelo = new CursosModelo();
        this.cursoDTOList = new ArrayList<>();
        this.sorterTabla = new TableRowSorter<>();
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        setModeloCursoTable();
        sorterTabla.setModel(modeloTabla);
        cursosVista.getCursosTable().setRowSorter(sorterTabla);
        filtrarTabla();
        cursosVista.getAgregarButton().addActionListener(e -> mostrarEditor(new CursoDTO()));
        cursosVista.getActualizarButton().addActionListener(e -> mostrarEditor(getCursoSeleccionado()));
    }

    private void setModeloCursoTable() {
        modeloTabla = new AbstractTableModel() {
            private final String[] columnas = {"Id", "Nombre"};

            @Override
            public int getRowCount() {
                return cursoDTOList.size();
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
                CursoDTO curso = cursoDTOList.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> curso.getId();
                    case 1 -> curso.getNombre();
                    default -> null;
                };
            }
        };

        cursosVista.getCursosTable().setModel(modeloTabla);
    }

    private void filtrarTabla() {
        cursosVista.getBuscarField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }

            private void filtrar() {
                String filtro = cursosVista.getBuscarField().getText();
                if (filtro.isBlank()) {
                    return;
                }

                sorterTabla.setRowFilter(RowFilter.regexFilter("(?i)" + filtro));
            }
        });
    }

    private void mostrarEditor(CursoDTO cursoDTO) {
        if (cursoDTO == null) {
            Alertas.error("No se ha seleccionado un curso");
            return;
        }

        EditorCursoControlador editorCursoControlador = new EditorCursoControlador(cursosModelo, cursoDTO);
        boolean activado = editorCursoControlador.activar();
        if (!activado) {
            return;
        }

        JDialog dialog = (JDialog) editorCursoControlador.getVista();
        dialog.setVisible(true);

        if (editorCursoControlador.isActualizarTabla()) {
            buscarCursos();
        }
    }

    public CursoDTO getCursoSeleccionado() {
        int selectedRow = cursosVista.getCursosTable().getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        int modelRow = cursosVista.getCursosTable().convertRowIndexToModel(selectedRow);
        return cursoDTOList.get(modelRow);
    }

    @Override
    public boolean activar() {
        buscarCursos();
        return true;
    }

    private void buscarCursos() {
        if (!cursoDTOList.isEmpty()) {
            cursoDTOList.clear();
        }

        cursoDTOList.addAll(cursosModelo.getListaCursos());
        modeloTabla.fireTableDataChanged();
        cursosVista.getCantidadLabel().setText(Integer.toString(cursoDTOList.size()));
    }

    @Override
    public boolean desactivar() {
        return true;
    }

    @Override
    public Component getVista() {
        return cursosVista;
    }
}
