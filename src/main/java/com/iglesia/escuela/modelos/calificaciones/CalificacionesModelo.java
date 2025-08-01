package com.iglesia.escuela.modelos.calificaciones;

import com.iglesia.escuela.basedatos.DataSource;
import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;
import com.iglesia.escuela.dtos.calificaciones.CalificacionDTO;
import com.iglesia.escuela.dtos.calificaciones.NotaDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalificacionesModelo {

    private final DataSource dataSource;

    public CalificacionesModelo() {
        this.dataSource = DataSource.getInstancia();
    }

    public List<CalificacionDTO> getListaCalificaciones() {
        String selectSql = """
                SELECT
                    alumnos.id_alumno,
                    alumnos.telefono,
                    alumnos.nombre,
                    alumnos.apellido,
                    alumnos.grado,
                    alumnos.id_curso_asignado AS curso,
                    COALESCE(SUM(notas.calificacion), 0) AS totalCalificaion,
                    COUNT(notas.calificacion) AS numCalificaciones
                 FROM alumnos
                 LEFT JOIN notas ON alumnos.id_alumno=notas.id_alumno_nota
                 GROUP BY alumnos.id_alumno
                """;
        List<Map<String, Object>> calificacionesMap = dataSource.consultar(selectSql, null);

        List<CalificacionDTO> calificacionDTOList = new ArrayList<>();
        for (Map<String, Object> calificacion : calificacionesMap) {
            CalificacionDTO calificacionDTO = new CalificacionDTO();
            AlumnoDTO alumnoDTO = new AlumnoDTO();
            alumnoDTO.setId((Integer) calificacion.get("id_alumno"));
            alumnoDTO.setNombre((String) calificacion.get("nombre"));
            alumnoDTO.setApellido((String) calificacion.get("apellido"));
            alumnoDTO.setGrado((String) calificacion.get("grado"));
            alumnoDTO.setTelefono((String) calificacion.get("telefono"));
            alumnoDTO.setMateria((String) calificacion.get("curso"));

            calificacionDTO.setAlumnoDTO(alumnoDTO);
            calificacionDTO.setTotalCalificaciones((BigDecimal) calificacion.get("totalCalificaion"));
            calificacionDTO.setCantidadCalificaciones((Long) calificacion.get("numCalificaciones"));
            calificacionDTOList.add(calificacionDTO);
        }
        return calificacionDTOList;
    }

    public List<NotaDTO> getListaNotasPorAlumno(int alumnoId) {
        String selectSql = "SELECT * FROM notas WHERE id_alumno_nota=?";
        List<Map<String, Object>> notasMap = dataSource.consultar(selectSql, new Object[]{alumnoId});

        List<NotaDTO> notaDTOList = new ArrayList<>();
        for (Map<String, Object> nota : notasMap) {
            NotaDTO notaDTO = new NotaDTO();
            notaDTO.setId((Integer) nota.get("id_nota"));
            notaDTO.setAlumnoId((Integer) nota.get("id_alumno_nota"));
            notaDTO.setMateria((String) nota.get("id_curso_nota"));
            notaDTO.setTarea((String) nota.get("tarea"));
            notaDTO.setCalificacion((BigDecimal) nota.get("calificacion"));
            notaDTOList.add(notaDTO);
        }

        return notaDTOList;
    }

    public boolean guardarNota(NotaDTO notaDTO) {
        boolean esNuevo = notaDTO.getId() == 0;

        String sql = esNuevo
                ? "INSERT INTO notas (id_alumno_nota, id_curso_nota, tarea, calificacion) VALUES (?,?,?,?)"
                : "UPDATE notas SET id_alumno_nota = ?, id_curso_nota = ?, tarea = ?, calificacion = ? WHERE id_nota = ?";

        List<Object> parametros = new ArrayList<>();
        parametros.add(notaDTO.getAlumnoId());
        parametros.add(notaDTO.getMateria());
        parametros.add(notaDTO.getTarea());
        parametros.add(notaDTO.getCalificacion());

        if (!esNuevo) {
            parametros.add(notaDTO.getId());
        }

        return dataSource.ejecutarConsulta(sql, parametros.toArray());
    }
}