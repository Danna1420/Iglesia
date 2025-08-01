package com.iglesia.escuela.controladores.calificaciones;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.calificaciones.CalificacionDTO;
import com.iglesia.escuela.modelos.calificaciones.CalificacionesModelo;
import com.iglesia.escuela.vistas.calificaciones.CalificacionesVista;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalificacionesControlador extends BaseControlador {

    private final CalificacionesVista calificacionesVista;

    private final CalificacionesModelo calificacionesModelo;

    private AbstractTableModel modeloTabla;

    private final TableRowSorter<AbstractTableModel> sorterTabla;

    private final List<CalificacionDTO> calificacionDTOList;

    public CalificacionesControlador() {
        this.calificacionesVista = new CalificacionesVista();
        this.calificacionesModelo = new CalificacionesModelo();
        this.calificacionDTOList = new ArrayList<>();
        this.sorterTabla = new TableRowSorter<>();
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        setModeloCalificacionTable();
        sorterTabla.setModel(modeloTabla);
        calificacionesVista.getCalificacionesTable().setRowSorter(sorterTabla);
        filtrarTabla();
        calificacionesVista.getNotasButton().addActionListener(e -> mostrarEditorCalificaciones());
        calificacionesVista.getImprimirButton().addActionListener(e -> imprimirPdf());
    }

    private void setModeloCalificacionTable() {
        modeloTabla = new AbstractTableModel() {
            private final String[] columnas = {"Nombre", "Apellido", "Grado", "Materia", "Promedio"};

            @Override
            public int getRowCount() {
                return calificacionDTOList.size();
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
                CalificacionDTO calificacion = calificacionDTOList.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> calificacion.getAlumnoDTO().getNombre();
                    case 1 -> calificacion.getAlumnoDTO().getApellido();
                    case 2 -> calificacion.getAlumnoDTO().getGrado();
                    case 3 -> calificacion.getAlumnoDTO().getMateria();
                    case 4 -> {
                        long cantidadCalificaciones = calificacion.getCantidadCalificaciones();
                        if (cantidadCalificaciones == 0L) {
                            yield "0.00";
                        }

                        BigDecimal promedio = calificacion.getTotalCalificaciones()
                                .divide(new BigDecimal(Long.toString(calificacion.getCantidadCalificaciones())),
                                        2, RoundingMode.HALF_UP);

                        yield String.format("%.2f", promedio);
                    }
                    default -> null;
                };
            }
        };

        calificacionesVista.getCalificacionesTable().setModel(modeloTabla);
    }

    private void filtrarTabla() {
        calificacionesVista.getBuscarField().getDocument().addDocumentListener(new DocumentListener() {
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
                String filtro = calificacionesVista.getBuscarField().getText();
                if (filtro.isBlank()) {
                    return;
                }

                sorterTabla.setRowFilter(RowFilter.regexFilter("(?i)" + filtro));
            }
        });
    }

    private void mostrarEditorCalificaciones() {
        CalificacionDTO calificacionDTO = getCursoSeleccionado();
        if (calificacionDTO == null) {
            Alertas.error("No se ha seleccionado un alumno");
            return;
        }

        EditorCalificacionesControlador editorCalificacionesControlador = new EditorCalificacionesControlador(
                calificacionesModelo, calificacionDTO);
        boolean activado = editorCalificacionesControlador.activar();
        if (!activado) {
            return;
        }

        JDialog dialog = (JDialog) editorCalificacionesControlador.getVista();
        dialog.setVisible(true);

        if (editorCalificacionesControlador.isActualizarTabla()) {
            buscarCalificaciones();
        }
    }

    public CalificacionDTO getCursoSeleccionado() {
        int selectedRow = calificacionesVista.getCalificacionesTable().getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        int modelRow = calificacionesVista.getCalificacionesTable().convertRowIndexToModel(selectedRow);
        return calificacionDTOList.get(modelRow);
    }

    private void imprimirPdf() {
        Document documento = new Document();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        String fechaActual = formato.format(new Date());

        try {
            File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
            File archivo = new File(desktopDir, "CALIFICACIONES.pdf");

            if (archivo.exists()) {
                if (!Alertas.confirmacion("El archivo ya existe. ¿Deseas sobrescribirlo?")) {
                    return;
                }
            }

            PdfWriter.getInstance(documento, new FileOutputStream(archivo));

            documento.open();

            Paragraph titulo = new Paragraph("CALIFICACIONES\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK));
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph fecha = new Paragraph("Fecha: " + fechaActual + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 12));
            fecha.setAlignment(Element.ALIGN_RIGHT);
            documento.add(fecha);

            PdfPTable tabla = getPdfPTable();
            documento.add(tabla);

            documento.close();

            Alertas.informacion("Documento creado con éxito en el escritorio:\n" + archivo.getAbsolutePath());
        } catch (DocumentException | IOException e) {
            Alertas.error("Error al generar el PDF:\n" + e.getMessage());
        }
    }

    private PdfPTable getPdfPTable() {
        PdfPTable tablacalificacion = new PdfPTable(5);

        tablacalificacion.addCell("Nombre");
        tablacalificacion.addCell("Apellido");
        tablacalificacion.addCell("Grado");
        tablacalificacion.addCell("Materia");
        tablacalificacion.addCell("Promedio");

        List<CalificacionDTO> calificacionDTOList1 = calificacionDTOList.stream()
                .filter(calificacionDTO -> calificacionDTO.getTotalCalificaciones().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        for (CalificacionDTO calificacionDTO : calificacionDTOList1) {
            tablacalificacion.addCell(calificacionDTO.getAlumnoDTO().getNombre());
            tablacalificacion.addCell(calificacionDTO.getAlumnoDTO().getApellido());
            tablacalificacion.addCell(calificacionDTO.getAlumnoDTO().getGrado());
            tablacalificacion.addCell(calificacionDTO.getAlumnoDTO().getGrado());

            BigDecimal totalCalificaciones = calificacionDTO.getTotalCalificaciones();
            long numCalificaciones = calificacionDTO.getCantidadCalificaciones();
            BigDecimal promedio = totalCalificaciones.divide(new BigDecimal(Long.toString(numCalificaciones)), 2, RoundingMode.HALF_UP);
            tablacalificacion.addCell(String.format("%.2f", promedio));
        }
        return tablacalificacion;
    }

    @Override
    public boolean activar() {
        buscarCalificaciones();
        return true;
    }

    private void buscarCalificaciones() {
        if (!calificacionDTOList.isEmpty()) {
            calificacionDTOList.clear();
        }

        calificacionDTOList.addAll(calificacionesModelo.getListaCalificaciones());
        modeloTabla.fireTableDataChanged();
        calificacionesVista.getCantidadLabel().setText(Integer.toString(calificacionDTOList.size()));
    }

    @Override
    public boolean desactivar() {
        return true;
    }

    @Override
    public Component getVista() {
        return calificacionesVista;
    }
}
