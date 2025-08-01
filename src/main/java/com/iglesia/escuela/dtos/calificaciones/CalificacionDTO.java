package com.iglesia.escuela.dtos.calificaciones;

import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;

import java.math.BigDecimal;

public class CalificacionDTO {

    private AlumnoDTO alumnoDTO;

    private BigDecimal totalCalificaciones;

    private Long cantidadCalificaciones;

    public AlumnoDTO getAlumnoDTO() {
        return alumnoDTO;
    }

    public void setAlumnoDTO(AlumnoDTO alumnoDTO) {
        this.alumnoDTO = alumnoDTO;
    }

    public BigDecimal getTotalCalificaciones() {
        return totalCalificaciones;
    }

    public void setTotalCalificaciones(BigDecimal totalCalificaciones) {
        this.totalCalificaciones = totalCalificaciones;
    }

    public Long getCantidadCalificaciones() {
        return cantidadCalificaciones;
    }

    public void setCantidadCalificaciones(Long cantidadCalificaciones) {
        this.cantidadCalificaciones = cantidadCalificaciones;
    }
}
