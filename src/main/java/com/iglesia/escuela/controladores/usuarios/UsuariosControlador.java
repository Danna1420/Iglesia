package com.iglesia.escuela.controladores.usuarios;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.usuarios.UsuarioDTO;
import com.iglesia.escuela.modelos.usuarios.UsuariosModelo;
import com.iglesia.escuela.vistas.usuarios.UsuariosVista;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UsuariosControlador extends BaseControlador {

    private final UsuariosVista usuariosVista;

    private final UsuariosModelo usuariosModelo;

    private AbstractTableModel modeloTabla;

    private final TableRowSorter<AbstractTableModel> sorterTabla;

    private final List<UsuarioDTO> usuarioDTOList;

    public UsuariosControlador() {
        this.usuariosVista = new UsuariosVista();
        this.usuariosModelo = new UsuariosModelo();
        this.usuarioDTOList = new ArrayList<>();
        this.sorterTabla = new TableRowSorter<>();
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        setModeloUsuarioTable();
        sorterTabla.setModel(modeloTabla);
        usuariosVista.getUsuariosTable().setRowSorter(sorterTabla);
        filtrarTabla();
        usuariosVista.getAgregarButton().addActionListener(e -> mostrarEditor(new UsuarioDTO()));
        usuariosVista.getActualizarButton().addActionListener(e -> mostrarEditor(getUsuarioSeleccionado()));
    }

    private void setModeloUsuarioTable() {
        modeloTabla = new AbstractTableModel() {
            private final String[] columnas = {"Id", "Nombre"};

            @Override
            public int getRowCount() {
                return usuarioDTOList.size();
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
                UsuarioDTO usuario = usuarioDTOList.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> usuario.getId();
                    case 1 -> usuario.getNombre();
                    default -> null;
                };
            }
        };

        usuariosVista.getUsuariosTable().setModel(modeloTabla);
    }

    private void filtrarTabla() {
        usuariosVista.getBuscarField().getDocument().addDocumentListener(new DocumentListener() {
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
                String filtro = usuariosVista.getBuscarField().getText();
                if (filtro.isBlank()) {
                    return;
                }

                sorterTabla.setRowFilter(RowFilter.regexFilter("(?i)" + filtro));
            }
        });
    }

    private void mostrarEditor(UsuarioDTO usuarioDTO) {
        if (usuarioDTO == null) {
            Alertas.error("No se ha seleccionado un usuario");
            return;
        }

        EditorUsuarioControlador editorUsuarioControlador = new EditorUsuarioControlador(usuariosModelo, usuarioDTO);
        boolean activado = editorUsuarioControlador.activar();
        if (!activado) {
            return;
        }

        JDialog dialog = (JDialog) editorUsuarioControlador.getVista();
        dialog.setVisible(true);

        if (editorUsuarioControlador.isActualizarTabla()) {
            buscarUsuarios();
        }
    }

    public UsuarioDTO getUsuarioSeleccionado() {
        int selectedRow = usuariosVista.getUsuariosTable().getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        int modelRow = usuariosVista.getUsuariosTable().convertRowIndexToModel(selectedRow);
        return usuarioDTOList.get(modelRow);
    }

    @Override
    public boolean activar() {
        buscarUsuarios();
        return true;
    }

    private void buscarUsuarios() {
        if (!usuarioDTOList.isEmpty()) {
            usuarioDTOList.clear();
        }

        usuarioDTOList.addAll(usuariosModelo.getListaUsuarios());
        modeloTabla.fireTableDataChanged();
        usuariosVista.getCantidadLabel().setText(Integer.toString(usuarioDTOList.size()));
    }

    @Override
    public boolean desactivar() {
        return true;
    }

    @Override
    public Component getVista() {
        return usuariosVista;
    }
}
