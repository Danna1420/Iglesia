package com.iglesia.escuela.controladores.alumnos;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;
import com.iglesia.escuela.modelos.alumnos.AlumnosModelo;
import com.iglesia.escuela.modelos.cursos.CursosModelo;
import com.iglesia.escuela.vistas.alumnos.AlumnosVista;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.List;

import static com.iglesia.escuela.controladores.alumnos.EditorAlumnoControlador.esMayorDeEdad;

public class AlumnosControlador extends BaseControlador {

    private final AlumnosVista alumnosVista;

    private final AlumnosModelo alumnosModelo;

    private final CursosModelo cursosModelo;

    private AbstractTableModel modeloTabla;

    private final TableRowSorter<AbstractTableModel> sorterTabla;

    private final List<AlumnoDTO> alumnoDTOList;

    private final Map<Integer, Integer> anchosOriginales;

    private boolean anchosGuardados = false;

    public AlumnosControlador() {
        this.alumnosVista = new AlumnosVista();
        this.alumnosModelo = new AlumnosModelo();
        this.cursosModelo = new CursosModelo();
        this.alumnoDTOList = new ArrayList<>();
        this.sorterTabla = new TableRowSorter<>();
        this.anchosOriginales = new HashMap<>();
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
        alumnosVista.getImprimirButton().addActionListener(e -> imprimirPdfAlumno());
    }

    private void setModeloAlumnoTable() {
        modeloTabla = new AbstractTableModel() {
            private final String[] columnas = {"Id", "Nombre", "Apellido", "Grado", "Telefono", "Materia", "Edad",
                    "Talla Camisa", "Talla Pantalón", "Talla Zapatos", "Nombre Acudiente", "Apellido Acudiente", "Telefono Acudiente",
                    "Casa de Oración"};

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
                    case 6 -> calcularEdad(alumno.getFechaNacimiento());
                    case 7 -> alumno.getTallaCamisa();
                    case 8 -> alumno.getTallaPantalon();
                    case 9 -> alumno.getTallaZapato();
                    case 10 -> alumno.getNombreAcudiente();
                    case 11 -> alumno.getApellidoAcudiente();
                    case 12 -> alumno.getTelefonoAcudiente();
                    case 13 -> alumno.getCasaOracionAcudiente();
                    default -> null;
                };
            }

            public static String calcularEdad(LocalDate fechaNacimiento) {
                if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) return "Fecha inválida";

                LocalDate hoy = LocalDate.now();
                Period periodo = Period.between(fechaNacimiento, hoy);

                return String.valueOf(periodo.getYears());
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
        });

        alumnosVista.getTipoAlumnoCombo().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                filtrar();
                SwingUtilities.invokeLater(this::aplicarFiltroColumnas);
            }
        });
    }

    private void filtrar() {
        String filtro = alumnosVista.getBuscarField().getText().toUpperCase();
        String tipoAlumno = (String) alumnosVista.getTipoAlumnoCombo().getSelectedItem();

        RowFilter<Object, Object> filtroTexto = RowFilter.regexFilter("(?i)" + filtro);

        RowFilter<Object, Object> filtroTipo = new RowFilter<>() {
            @Override
            public boolean include(Entry<?, ?> entry) {
                AlumnoDTO dto = alumnoDTOList.get((Integer) entry.getIdentifier());

                if ("Adulto".equals(tipoAlumno)) {
                    return esMayorDeEdad(dto.getFechaNacimiento());
                } else if ("Menor".equals(tipoAlumno)) {
                    return !esMayorDeEdad(dto.getFechaNacimiento());
                }
                return true; // "Todos"
            }
        };

        sorterTabla.setRowFilter(RowFilter.andFilter(Arrays.asList(filtroTexto, filtroTipo)));
    }

    private void aplicarFiltroColumnas() {
        JTable tabla = alumnosVista.getAlumnosTable();

        //Guardar anchos la primera vez
        guardarAnchosOriginales(tabla);

        String tipoAlumno = (String) alumnosVista.getTipoAlumnoCombo().getSelectedItem();
        boolean esAdulto = !"Menor".equals(tipoAlumno);

        restaurarColumnas(tabla);

        if (esAdulto) {
            // Adulto: ocultar 6-13
            for (int i = 6; i <= 13; i++) {
                ocultarColumna(tabla, i);
            }
        } else {
            // Menor: ocultar col 4
            ocultarColumna(tabla, 4);
        }
    }

    private void guardarAnchosOriginales(JTable tabla) {
        if (anchosGuardados) return;

        TableColumnModel columnModel = tabla.getColumnModel();

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn columna = columnModel.getColumn(i);
            anchosOriginales.put(i, columna.getWidth());
        }

        anchosGuardados = true;
    }

    private void ocultarColumna(JTable tabla, int index) {
        TableColumn columna = tabla.getColumnModel().getColumn(index);
        columna.setMinWidth(0);
        columna.setMaxWidth(0);
        columna.setWidth(0);
    }

    private void restaurarColumnas(JTable tabla) {
        TableColumnModel columnModel = tabla.getColumnModel();

        for (Map.Entry<Integer, Integer> entry : anchosOriginales.entrySet()) {
            int index = entry.getKey();
            int ancho = entry.getValue();

            TableColumn columna = columnModel.getColumn(index);
            columna.setMinWidth(15);
            columna.setMaxWidth(300);
            columna.setPreferredWidth(ancho);
            columna.setWidth(ancho);
        }
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

    private void imprimirPdfAlumno() {
        AlumnoDTO alumnoDTO = getAlumnoSeleccionado();
        if (alumnoDTO == null) {
            Alertas.error("No se ha seleccionado un alumno");
            return;
        }

        Document documento = new Document();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        String fechaActual = formato.format(new Date());

        try {
            File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
            File archivo = new File(desktopDir, "ALUMNO_" + alumnoDTO.getNombre() + ".pdf");

            if (archivo.exists()) {
                if (!Alertas.confirmacion("El archivo ya existe. ¿Deseas sobrescribirlo?")) {
                    return;
                }
            }

            PdfWriter.getInstance(documento, new FileOutputStream(archivo));
            documento.open();

            //TÍTULO
            Paragraph titulo = new Paragraph("INFORMACIÓN DEL ALUMNO\n\n",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            //FECHA
            Paragraph fecha = new Paragraph("Fecha: " + fechaActual + "\n\n",
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            fecha.setAlignment(Element.ALIGN_RIGHT);
            documento.add(fecha);

            //DATOS DEL ALUMNO
            documento.add(new Paragraph("DATOS DEL ALUMNO\n",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

            PdfPTable tablaAlumno = new PdfPTable(2);
            tablaAlumno.setWidthPercentage(100);

            agregarFila(tablaAlumno, "Nombre", alumnoDTO.getNombre());
            agregarFila(tablaAlumno, "Apellido", alumnoDTO.getApellido());
            agregarFila(tablaAlumno, "Teléfono", alumnoDTO.getTelefono());
            agregarFila(tablaAlumno, "Grado", alumnoDTO.getGrado());
            agregarFila(tablaAlumno, "Materia", alumnoDTO.getMateria());

            if (alumnoDTO.getFechaNacimiento() != null) {
                agregarFila(tablaAlumno, "Fecha Nacimiento", alumnoDTO.getFechaNacimiento().toString());
                agregarFila(tablaAlumno, "Edad", String.valueOf(EditorAlumnoControlador.calcularEdad(alumnoDTO.getFechaNacimiento())));
            }

            boolean esMenorDeEdad = !EditorAlumnoControlador.esMayorDeEdad(alumnoDTO.getFechaNacimiento());
            if (esMenorDeEdad) {
                agregarFila(tablaAlumno, "Talla Camisa", String.valueOf(alumnoDTO.getTallaCamisa()));
                agregarFila(tablaAlumno, "Talla Pantalón", String.valueOf(alumnoDTO.getTallaPantalon()));
                agregarFila(tablaAlumno, "Talla Zapatos", String.valueOf(alumnoDTO.getTallaZapato()));
            }

            documento.add(tablaAlumno);

            documento.add(new Paragraph("\n"));

            //ACUDIENTE
            if (esMenorDeEdad) {
                documento.add(new Paragraph("DATOS DEL ACUDIENTE\n",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

                PdfPTable tablaAcudiente = new PdfPTable(2);
                tablaAcudiente.setWidthPercentage(100);

                agregarFila(tablaAcudiente, "Nombre", alumnoDTO.getNombreAcudiente());
                agregarFila(tablaAcudiente, "Apellido", alumnoDTO.getApellidoAcudiente());
                agregarFila(tablaAcudiente, "Teléfono", alumnoDTO.getTelefonoAcudiente());
                agregarFila(tablaAcudiente, "Casa de Oración", alumnoDTO.getCasaOracionAcudiente());

                documento.add(tablaAcudiente);
            }

            documento.close();

            Alertas.informacion("PDF generado con éxito:\n" + archivo.getAbsolutePath());

        } catch (DocumentException | IOException e) {
            Alertas.error("Error al generar PDF:\n" + e.getMessage());
        }
    }

    private void agregarFila(PdfPTable tabla, String label, String valor) {
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontLabel));
        cellLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
        tabla.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(valor != null ? valor : "", fontValue));
        tabla.addCell(cellValue);
    }

    @Override
    public boolean activar() {
        buscarAlumnos();
        aplicarFiltroColumnas();
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
