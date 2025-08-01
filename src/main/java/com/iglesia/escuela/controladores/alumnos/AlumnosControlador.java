package com.iglesia.escuela.controladores.alumnos;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;
import com.iglesia.escuela.modelos.alumnos.AlumnosModelo;
import com.iglesia.escuela.modelos.cursos.CursosModelo;
import com.iglesia.escuela.vistas.alumnos.AlumnosVista;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AlumnosControlador extends BaseControlador {

    private final AlumnosVista alumnosVista;

    private final AlumnosModelo alumnosModelo;

    private final CursosModelo cursosModelo;

    private AbstractTableModel modeloTabla;

    private final TableRowSorter<AbstractTableModel> sorterTabla;

    private final List<AlumnoDTO> alumnoDTOList;

    public AlumnosControlador() {
        this.alumnosVista = new AlumnosVista();
        this.alumnosModelo = new AlumnosModelo();
        this.cursosModelo = new CursosModelo();
        this.alumnoDTOList = new ArrayList<>();
        this.sorterTabla = new TableRowSorter<>();
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        setModeloAlumnoTable();
        sorterTabla.setModel(modeloTabla);
        alumnosVista.getAlumnosTable().setRowSorter(sorterTabla);
        filtrarTabla();
        alumnosVista.getAgregarButton().addActionListener(e -> mostrarEditor(new AlumnoDTO()));
        alumnosVista.getActualizarButton().addActionListener(e -> mostrarEditor(getAlumnoSeleccionado()));
        alumnosVista.getExcelButton().addActionListener(e -> mostrarEditorConExcel());
    }

    private void setModeloAlumnoTable() {
        modeloTabla = new AbstractTableModel() {
            private final String[] columnas = {"Id", "Nombre", "Apellido", "Grado", "Telefono", "Materia"};

            @Override
            public int getRowCount() {
                return alumnoDTOList.size();
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
                AlumnoDTO alumno = alumnoDTOList.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> alumno.getId();
                    case 1 -> alumno.getNombre();
                    case 2 -> alumno.getApellido();
                    case 3 -> alumno.getGrado();
                    case 4 -> alumno.getTelefono();
                    case 5 -> alumno.getMateria();
                    default -> null;
                };
            }
        };

        alumnosVista.getAlumnosTable().setModel(modeloTabla);
    }

    private void filtrarTabla() {
        alumnosVista.getBuscarField().getDocument().addDocumentListener(new DocumentListener() {
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
                String filtro = alumnosVista.getBuscarField().getText();
                if (filtro.isBlank()) {
                    return;
                }

                sorterTabla.setRowFilter(RowFilter.regexFilter("(?i)" + filtro));
            }
        });
    }

    private void mostrarEditor(AlumnoDTO alumnoDTO) {
        if (alumnoDTO == null) {
            Alertas.error("No se ha seleccionado un alumno");
            return;
        }

        EditorAlumnoControlador editorAlumnoControlador = new EditorAlumnoControlador(alumnosModelo, cursosModelo, alumnoDTO);
        boolean activado = editorAlumnoControlador.activar();
        if (!activado) {
            return;
        }

        JDialog dialog = (JDialog) editorAlumnoControlador.getVista();
        dialog.setVisible(true);

        if (editorAlumnoControlador.isActualizarTabla()) {
            buscarAlumnos();
        }
    }

    private void mostrarEditorConExcel() {
        EditorAlumnosConExcelControlador editorAlumnoControlador = new EditorAlumnosConExcelControlador(
                alumnosModelo, cursosModelo, alumnoDTOList);
        boolean activado = editorAlumnoControlador.activar();
        if (!activado) {
            return;
        }

        JDialog dialog = (JDialog) editorAlumnoControlador.getVista();
        dialog.setVisible(true);

        if (editorAlumnoControlador.isActualizarTabla()) {
            buscarAlumnos();
        }
    }

    public AlumnoDTO getAlumnoSeleccionado() {
        int selectedRow = alumnosVista.getAlumnosTable().getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        int modelRow = alumnosVista.getAlumnosTable().convertRowIndexToModel(selectedRow);
        return alumnoDTOList.get(modelRow);
    }

    @Override
    public boolean activar() {
        buscarAlumnos();
        return true;
    }

    private void buscarAlumnos() {
        if (!alumnoDTOList.isEmpty()) {
            alumnoDTOList.clear();
        }

        alumnoDTOList.addAll(alumnosModelo.getListaAlumnos());
        modeloTabla.fireTableDataChanged();
        alumnosVista.getCantidadLabel().setText(Integer.toString(alumnoDTOList.size()));
    }

    @Override
    public boolean desactivar() {
        return true;
    }

    @Override
    public Component getVista() {
        return alumnosVista;
    }
}
