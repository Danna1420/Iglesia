package com.iglesia.escuela.controladores.calificaciones;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;
import com.iglesia.escuela.dtos.calificaciones.CalificacionDTO;
import com.iglesia.escuela.dtos.calificaciones.NotaDTO;
import com.iglesia.escuela.modelos.calificaciones.CalificacionesModelo;
import com.iglesia.escuela.vistas.calificaciones.EditorCalificacionesVista;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorCalificacionesControlador extends BaseControlador {

    private final EditorCalificacionesVista editorCalificacionVista;

    private final CalificacionesModelo calificacionesModelo;

    private final CalificacionDTO calificacionDTO;

    private AbstractTableModel modeloTabla;

    private final List<NotaDTO> notaDTOList;

    private boolean actualizarTabla;

    public EditorCalificacionesControlador(CalificacionesModelo calificacionesModelo, CalificacionDTO calificacionDTO) {
        this.editorCalificacionVista = new EditorCalificacionesVista(null, true);
        this.calificacionesModelo = calificacionesModelo;
        this.calificacionDTO = calificacionDTO;
        this.notaDTOList = new ArrayList<>();
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        setModeloCalificacionTable();
        editorCalificacionVista.getAgregarButton().addActionListener(e -> mostrarEditorNotas(new NotaDTO()));
        editorCalificacionVista.getActualizarButton().addActionListener(e -> mostrarEditorNotas(getCursoSeleccionado()));
        editorCalificacionVista.getImprimirButton().addActionListener(e -> imprimirPdf());
    }

    private void setModeloCalificacionTable() {
        modeloTabla = new AbstractTableModel() {
            private final String[] columnas = {"Id", "Tarea", "Calificación"};

            @Override
            public int getRowCount() {
                return notaDTOList.size();
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
                NotaDTO notaDTO = notaDTOList.get(rowIndex);
                return switch (columnIndex) {
                    case 0 -> notaDTO.getId();
                    case 1 -> notaDTO.getTarea();
                    case 2 -> notaDTO.getCalificacion();
                    default -> null;
                };
            }
        };

        editorCalificacionVista.getCalificacionesTable().setModel(modeloTabla);
    }

    private void mostrarEditorNotas(NotaDTO notaDTO) {
        if (notaDTO == null) {
            Alertas.error("No se ha seleccionado una nota");
            return;
        }

        EditorNotaControlador editorNotaControlador = new EditorNotaControlador(
                calificacionesModelo, calificacionDTO, notaDTO);
        boolean activado = editorNotaControlador.activar();
        if (!activado) {
            return;
        }

        JDialog dialog = (JDialog) editorNotaControlador.getVista();
        dialog.setVisible(true);

        if (editorNotaControlador.isActualizarTabla()) {
            actualizarTabla = true;
            buscarNotas();
        }
    }

    public NotaDTO getCursoSeleccionado() {
        int selectedRow = editorCalificacionVista.getCalificacionesTable().getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        int modelRow = editorCalificacionVista.getCalificacionesTable().convertRowIndexToModel(selectedRow);
        return notaDTOList.get(modelRow);
    }

    private void imprimirPdf() {
        Document documento = new Document();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        String fechaActual = formato.format(new Date());

        try {
            File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
            File archivo = new File(desktopDir, calificacionDTO.getAlumnoDTO().toString() + ".pdf");

            if (archivo.exists()) {
                if (!Alertas.confirmacion("El archivo ya existe. ¿Deseas sobrescribirlo?")) {
                    return;
                }
            }

            PdfWriter.getInstance(documento, new FileOutputStream(archivo));

            documento.open();

            Paragraph titulo = new Paragraph("Informacion del alumno\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK));
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph fecha = new Paragraph("Fecha: " + fechaActual + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 12));
            fecha.setAlignment(Element.ALIGN_RIGHT);
            documento.add(fecha);

            PdfPTable tabla = getPdfPTable();
            documento.add(tabla);

            Paragraph notas = new Paragraph("Tareas Registradas\n\n", FontFactory.getFont(FontFactory.HELVETICA, 12));
            fecha.setAlignment(Element.ALIGN_CENTER);
            documento.add(notas);

            PdfPTable tablaNotas = getPdfPTableNotas();
            documento.add(tablaNotas);

            documento.close();

            Alertas.informacion("Documento creado con éxito en el escritorio:\n" + archivo.getAbsolutePath());
        } catch (DocumentException | IOException e) {
            Alertas.error("Error al generar el PDF:\n" + e.getMessage());
        }
    }

    private PdfPTable getPdfPTable() {
        PdfPTable tablacalificacion = new PdfPTable(4);

        tablacalificacion.addCell("Nombre");
        tablacalificacion.addCell("Apellido");
        tablacalificacion.addCell("Grado");
        tablacalificacion.addCell("Materia");

        tablacalificacion.addCell(calificacionDTO.getAlumnoDTO().getNombre());
        tablacalificacion.addCell(calificacionDTO.getAlumnoDTO().getApellido());
        tablacalificacion.addCell(calificacionDTO.getAlumnoDTO().getGrado());
        tablacalificacion.addCell(calificacionDTO.getAlumnoDTO().getMateria());
        return tablacalificacion;
    }

    private PdfPTable getPdfPTableNotas() {
        PdfPTable tablacalificacion = new PdfPTable(2);

        tablacalificacion.addCell("Tarea");
        tablacalificacion.addCell("Calificación");

        for (NotaDTO notaDTO : notaDTOList) {
            tablacalificacion.addCell(notaDTO.getTarea());
            tablacalificacion.addCell(notaDTO.getCalificacion().toPlainString());
        }
        return tablacalificacion;
    }

    @Override
    public boolean activar() {
        setCampos();
        buscarNotas();
        return true;
    }

    private void setCampos() {
        AlumnoDTO alumnoDTO = calificacionDTO.getAlumnoDTO();
        editorCalificacionVista.getNombreLabel().setText(alumnoDTO.getNombre());
        editorCalificacionVista.getApellidoLabel().setText(alumnoDTO.getApellido());
        editorCalificacionVista.getTelefonoLabel().setText(alumnoDTO.getTelefono());
        editorCalificacionVista.getGradoLabel().setText(alumnoDTO.getGrado());
    }

    private void buscarNotas() {
        if (!notaDTOList.isEmpty()) {
            notaDTOList.clear();
        }

        notaDTOList.addAll(calificacionesModelo.getListaNotasPorAlumno(calificacionDTO.getAlumnoDTO().getId()));
        modeloTabla.fireTableDataChanged();


        if (notaDTOList.isEmpty()) {
            editorCalificacionVista.getEstatusLabel().setText("Sin Notas");
            editorCalificacionVista.getCalificacionLabel().setText("0.00");
            return;
        }

        BigDecimal totalCalificaciones = notaDTOList.stream()
                .map(NotaDTO::getCalificacion)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        long numCalificaciones = notaDTOList.size();

        BigDecimal promedio = totalCalificaciones
                .divide(new BigDecimal(Long.toString(numCalificaciones)),
                        2, RoundingMode.HALF_UP);
        editorCalificacionVista.getCalificacionLabel().setText(String.format("%.2f", promedio));
        estatus(promedio, calificacionDTO.getAlumnoDTO().getGrado());
    }

    private void estatus(BigDecimal promedio, String curso) {
        Map<String, BigDecimal> promediosMinimos = new HashMap<>();
        promediosMinimos.put("1. Pasos", BigDecimal.valueOf(3.0));
        promediosMinimos.put("2. Fundamentos I", BigDecimal.valueOf(3.0));
        promediosMinimos.put("3. Fundamentos II", BigDecimal.valueOf(3.0));
        promediosMinimos.put("4. Liderazgo I", BigDecimal.valueOf(3.5));
        promediosMinimos.put("5. Liderazgo II", BigDecimal.valueOf(3.0));
        promediosMinimos.put("6. Liderazgo III", BigDecimal.valueOf(4.0));
        promediosMinimos.put("7. Liderazgo IV", BigDecimal.valueOf(4.3));
        promediosMinimos.put("8. Practicantes", BigDecimal.valueOf(4.5));

        BigDecimal notaMinima = promediosMinimos.getOrDefault(curso, BigDecimal.valueOf(3.5));
        if (promedio.compareTo(notaMinima) >= 0) {
            editorCalificacionVista.getEstatusLabel().setText("Aprobado");
        } else {
            editorCalificacionVista.getEstatusLabel().setText("Reprobado");
        }
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
        return editorCalificacionVista;
    }
}
