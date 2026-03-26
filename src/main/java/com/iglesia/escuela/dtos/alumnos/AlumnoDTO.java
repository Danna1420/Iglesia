package com.iglesia.escuela.dtos.alumnos;

import java.time.LocalDate;

public class AlumnoDTO {

    private Integer id;

    private String nombre;

    private String apellido;

    private String telefono;

    private String materia;

    private String grado;

    private LocalDate fechaNacimiento;

    private int tallaCamisa;

    private int tallaPantalon;

    private int tallaZapato;

    private String nombreAcudiente;

    private String apellidoAcudiente;

    private String telefonoAcudiente;

    private String casaOracionAcudiente;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getTallaCamisa() {
        return tallaCamisa;
    }

    public void setTallaCamisa(int tallaCamisa) {
        this.tallaCamisa = tallaCamisa;
    }

    public int getTallaPantalon() {
        return tallaPantalon;
    }

    public void setTallaPantalon(int tallaPantalon) {
        this.tallaPantalon = tallaPantalon;
    }

    public int getTallaZapato() {
        return tallaZapato;
    }

    public void setTallaZapato(int tallaZapato) {
        this.tallaZapato = tallaZapato;
    }

    public String getNombreAcudiente() {
        return nombreAcudiente;
    }

    public void setNombreAcudiente(String nombreAcudiente) {
        this.nombreAcudiente = nombreAcudiente;
    }

    public String getApellidoAcudiente() {
        return apellidoAcudiente;
    }

    public void setApellidoAcudiente(String apellidoAcudiente) {
        this.apellidoAcudiente = apellidoAcudiente;
    }

    public String getTelefonoAcudiente() {
        return telefonoAcudiente;
    }

    public void setTelefonoAcudiente(String telefonoAcudiente) {
        this.telefonoAcudiente = telefonoAcudiente;
    }

    public String getCasaOracionAcudiente() {
        return casaOracionAcudiente;
    }

    public void setCasaOracionAcudiente(String casaOracionAcudiente) {
        this.casaOracionAcudiente = casaOracionAcudiente;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}
