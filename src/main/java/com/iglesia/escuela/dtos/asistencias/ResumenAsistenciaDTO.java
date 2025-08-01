package com.iglesia.escuela.dtos.asistencias;

public class ResumenAsistenciaDTO {

    private java.sql.Date fecha;

    private String materia;

    private String grado;

    private long cantidasAsistencias;

    private long cantidasInasistencias;

    public java.sql.Date getFecha() {
        return fecha;
    }

    public void setFecha(java.sql.Date fecha) {
        this.fecha = fecha;
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

    public long getCantidasAsistencias() {
        return cantidasAsistencias;
    }

    public void setCantidasAsistencias(long cantidasAsistencias) {
        this.cantidasAsistencias = cantidasAsistencias;
    }

    public long getCantidasInasistencias() {
        return cantidasInasistencias;
    }

    public void setCantidasInasistencias(long cantidasInasistencias) {
        this.cantidasInasistencias = cantidasInasistencias;
    }
}
