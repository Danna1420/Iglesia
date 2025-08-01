package com.iglesia.escuela.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ImprimirExcel {

    public static Sheet crearHoja(Workbook book, String name) {
        Sheet sheet = book.createSheet(name);
        sheet.setDisplayGridlines(true);
        return sheet;
    }

    public static void crearEncabezado(Sheet sheet, String[] head) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < head.length; i++) {
            row.createCell(i).setCellValue(head[i]);
        }
    }

    public static void crearColumnaConDatos(Sheet sheet, List<String> stringList) {
        int numberRow = 1;
        for (String data : stringList) {
            Row row = sheet.createRow(numberRow);
            row.createCell(0).setCellValue(data);
            numberRow++;
        }
    }

    public static void crearFilasConDatos(Sheet sheet, List<Object[]> list, int countColumn) {
        int numberRow = 1;
        for (Object[] object : list) {
            Row row = sheet.createRow(numberRow);
            for (int i = 0; i <= countColumn; i++) {
                String value = object[i].toString();
                row.createCell(i).setCellValue(value);
            }
            numberRow++;
        }
    }

    public static void cerrarExcel(Workbook book, FileOutputStream fileOutputStream, File fileExcel) throws IOException {
        book.write(fileOutputStream);
        fileOutputStream.close();
        Desktop.getDesktop().open(fileExcel);
    }

    public static File buscarRuta(boolean typePath) throws IOException {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivo Excel", "xls", "xlsx");
        chooser.setFileFilter(filter);
        chooser.showOpenDialog(null);
        File file = chooser.getSelectedFile();

        if (file == null) {
            return null;
        }

        String pathFile = file.getAbsolutePath();
        if (typePath) {
            file = new File(pathFile);
            if (file.exists()) {
                file.delete();
            } else {
                pathFile += ".xlsx";
                file = new File(pathFile);
            }
            file.createNewFile();
        }
        return file;
    }

}
