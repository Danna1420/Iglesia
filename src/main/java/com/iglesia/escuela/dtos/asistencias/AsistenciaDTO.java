package com.iglesia.escuela.dtos.asistencias;

import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;

public class AsistenciaDTO {

    private int id;

    private AlumnoDTO alumnoDTO;

    private java.sql.Date fecha;

    private boolean presente;

    private String materia;

    private String grado;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AlumnoDTO getAlumnoDTO() {
        return alumnoDTO;
    }

    public void setAlumnoDTO(AlumnoDTO alumnoDTO) {
        this.alumnoDTO = alumnoDTO;
    }

    public java.sql.Date getFecha() {
        return fecha;
    }

    public void setFecha(java.sql.Date fecha) {
        this.fecha = fecha;
    }

    public boolean isPresente() {
        return presente;
    }

    public void setPresente(boolean presente) {
        this.presente = presente;
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
}
