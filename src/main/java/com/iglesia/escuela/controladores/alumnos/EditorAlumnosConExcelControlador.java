package com.iglesia.escuela.controladores.alumnos;

import com.iglesia.escuela.alertas.Alertas;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;
import com.iglesia.escuela.dtos.cursos.CursoDTO;
import com.iglesia.escuela.excel.ImprimirExcel;
import com.iglesia.escuela.modelos.alumnos.AlumnosModelo;
import com.iglesia.escuela.modelos.cursos.CursosModelo;
import com.iglesia.escuela.vistas.alumnos.EditorAlumnosConExcelVista;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.iglesia.escuela.excel.ImprimirExcel.cerrarExcel;
import static com.iglesia.escuela.excel.ImprimirExcel.crearFilasConDatos;
import static com.iglesia.escuela.excel.ImprimirExcel.crearEncabezado;
import static com.iglesia.escuela.excel.ImprimirExcel.crearHoja;
import static com.iglesia.escuela.excel.ImprimirExcel.buscarRuta;

public class EditorAlumnosConExcelControlador extends BaseControlador {

    private final EditorAlumnosConExcelVista editorAlumnoVista;

    private final AlumnosModelo alumnosModelo;

    private final CursosModelo cursosModelo;

    private final List<AlumnoDTO> alumnoDTOList;

    private boolean actualizarTabla;

    public EditorAlumnosConExcelControlador(AlumnosModelo alumnosModelo, CursosModelo cursosModelo, List<AlumnoDTO> alumnoDTOList) {
        this.editorAlumnoVista = new EditorAlumnosConExcelVista(null, true);
        this.alumnosModelo = alumnosModelo;
        this.cursosModelo = cursosModelo;
        this.alumnoDTOList = alumnoDTOList;
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        editorAlumnoVista.getFormatoAgregarButton().addActionListener(e -> formatoAgregar());
        editorAlumnoVista.getFormatoActualizarButton().addActionListener(e -> formatoActualizar());
        editorAlumnoVista.getImportarButton().addActionListener(e -> importarExcel());
    }

    private void formatoAgregar() {
        String[] columnas = {"NOMBRE", "APELLIDO", "TELEFONO", "MATERIA", "GRADO"};
        generarFormatoExcel(false, columnas, null);
    }

    private void formatoActualizar() {
        String[] columnas = {"ID", "NOMBRE", "APELLIDO", "TELEFONO", "MATERIA", "GRADO"};
        generarFormatoExcel(true, columnas, getAlumnos());
    }

    private void generarFormatoExcel(boolean incluirDatos, String[] columnas, List<Object[]> datos) {
        File excel;
        try {
            excel = buscarRuta(true);
        } catch (IOException e) {
            return;
        }

        if (excel == null) {
            return;
        }

        SwingWorker<Boolean, Void> swingWorker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (XSSFWorkbook book = new XSSFWorkbook();
                     FileOutputStream fileOutputStream = new FileOutputStream(excel)) {

                    Sheet hojaAlumnos = crearHoja(book, "Alumnos");
                    crearEncabezado(hojaAlumnos, columnas);

                    if (incluirDatos && datos != null) {
                        crearFilasConDatos(hojaAlumnos, datos, columnas.length - 1);
                    }

                    List<String> cursoDTOList = cursosModelo.getListaCursos().stream()
                            .map(CursoDTO::getNombre)
                            .toList();
                    Sheet hojaMaterias = crearHoja(book, "Materias");
                    ImprimirExcel.crearColumnaConDatos(hojaMaterias, cursoDTOList);

                    List<String> gradosList = List.of("1. Pasos", "2. Fundamentos I", "3. Fundamentos II", "4. Liderazgo I",
                            "5. Liderazgo II", "6. Liderazgo III", "7. Liderazgo IV", "8. Practicantes");
                    Sheet hojaGrados = crearHoja(book, "Grados");
                    ImprimirExcel.crearColumnaConDatos(hojaGrados, gradosList);

                    cerrarExcel(book, fileOutputStream, excel);
                }
                return true;
            }

            @Override
            protected void done() {
                try {
                    if (!get()) {
                        Alertas.error("No se pudo crear el formato de alumnos");
                        return;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Alertas.error("No se pudo crear el formato de alumnos");
                    System.err.println("Error al crear el formato de alumnos: " + e.getMessage());
                    return;
                }
                Alertas.informacion("Formato creado correctamente");
            }
        };
        swingWorker.execute();

    }

    private List<Object[]> getAlumnos() {
        List<Object[]> lista = new ArrayList<>();
        for (AlumnoDTO a : alumnoDTOList) {
            lista.add(new Object[]{
                    a.getId(),
                    a.getNombre(),
                    a.getApellido(),
                    a.getTelefono(),
                    a.getMateria(),
                    a.getGrado()
            });
        }
        return lista;
    }

    private void importarExcel() {
        File excel;
        try {
            excel = buscarRuta(false);
        } catch (IOException e) {
            return;
        }

        if (excel == null) {
            return;
        }

        if (!Alertas.confirmacion("¿Seguro quiere importar los alumnos?")) {
            return;
        }

        mostrarPanelImportando(true);

        boolean sonNuevos = editorAlumnoVista.getAgregarCheck().isSelected();
        guardarAlumnos(excel, sonNuevos);
    }

    private void guardarAlumnos(File excel, boolean sonNuevos) {
        SwingWorker<Boolean, Void> swingWorker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (XSSFWorkbook book = new XSSFWorkbook(new FileInputStream(excel))) {
                    Sheet sheet = book.getSheetAt(0);
                    Row headerRow = sheet.getRow(0);
                    if (headerRow == null) {
                        Alertas.error("El archivo no contiene encabezados.");
                        return false;
                    }

                    String[] columnasEsperadas = sonNuevos
                            ? new String[]{"NOMBRE", "APELLIDO", "TELEFONO", "MATERIA", "GRADO"}
                            : new String[]{"ID", "NOMBRE", "APELLIDO", "TELEFONO", "MATERIA", "GRADO"};

                    for (int i = 0; i < columnasEsperadas.length; i++) {
                        Cell cell = headerRow.getCell(i);
                        String valorCelda = (cell != null) ? getDatoCelda(cell).trim().toUpperCase() : "";
                        if (!valorCelda.equals(columnasEsperadas[i])) {
                            Alertas.error("Encabezado incorrecto en la columna " + (i + 1) +
                                    ": se esperaba \"" + columnasEsperadas[i] + "\", pero se encontró \"" + valorCelda + "\".");
                            return false;
                        }
                    }

                    List<AlumnoDTO> alumnoDTOList1 = new ArrayList<>();
                    int numRow = sheet.getLastRowNum();

                    for (int i = 1; i <= numRow; i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        AlumnoDTO alumnoDTO = new AlumnoDTO();
                        int colIndex = 0;

                        if (!sonNuevos) {
                            alumnoDTO.setId(Integer.parseInt(getDatoCelda(row.getCell(colIndex++))));
                        }

                        alumnoDTO.setNombre(getDatoCelda(row.getCell(colIndex++)));
                        alumnoDTO.setApellido(getDatoCelda(row.getCell(colIndex++)));
                        alumnoDTO.setTelefono(getDatoCelda(row.getCell(colIndex++)));
                        alumnoDTO.setMateria(getDatoCelda(row.getCell(colIndex++)));
                        alumnoDTO.setGrado(getDatoCelda(row.getCell(colIndex)));

                        alumnoDTOList1.add(alumnoDTO);
                    }

                    return alumnosModelo.guardarAlumnos(alumnoDTOList1, sonNuevos);
                }
            }

            @Override
            protected void done() {
                mostrarPanelImportando(false);
                try {
                    if (!get()) {
                        Alertas.error("No se pudo importar los alumnos");
                        return;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Alertas.error("No se pudo importar los alumnos");
                    System.err.println("Error al importar los alumnos: " + e.getMessage());
                    return;
                }

                actualizarTabla = true;
                Alertas.informacion("Alumnos importados correctamente");
            }
        };

        swingWorker.execute();
    }

    private String getDatoCelda(Cell celda) {
        if (celda == null) {
            return "";
        }

        String dato;
        if (celda.getCellType() == CellType.NUMERIC) {
            double numericValue = celda.getNumericCellValue();
            if (numericValue == (long) numericValue) {
                dato = Long.toString((long) numericValue);
            } else {
                dato = Double.toString(numericValue);
            }
        } else if (celda.getCellType() == CellType.STRING) {
            dato = celda.getStringCellValue();
        } else {
            return "";
        }

        return dato.trim().isEmpty() ? "" : dato;
    }

    @Override
    public boolean activar() {
        mostrarPanelImportando(false);
        return true;
    }

    private void mostrarPanelImportando(boolean visible) {
        editorAlumnoVista.getImportandoPanel().setVisible(visible);
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
