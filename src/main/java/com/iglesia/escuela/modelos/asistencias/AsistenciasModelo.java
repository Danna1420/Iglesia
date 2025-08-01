package com.iglesia.escuela.modelos.asistencias;

import com.iglesia.escuela.basedatos.DataSource;
import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;
import com.iglesia.escuela.dtos.asistencias.AsistenciaDTO;
import com.iglesia.escuela.dtos.asistencias.ResumenAsistenciaDTO;
import com.iglesia.escuela.modelos.alumnos.AlumnosModelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AsistenciasModelo {

    private final DataSource dataSource;

    private final AlumnosModelo alumnosModelo;

    public AsistenciasModelo() {
        this.dataSource = DataSource.getInstancia();
        this.alumnosModelo = new AlumnosModelo();
    }

    public List<ResumenAsistenciaDTO> getListaResumenAsistenciasPorFecha(java.sql.Date fecha) {
        String selectSql = """
                SELECT
                    fecha,
                    materia,
                    grado,
                    COUNT(CASE WHEN presente = true THEN 1 END) AS asistencias,
                    COUNT(CASE WHEN presente = false THEN 1 END) AS inasistencias
                FROM asistencias
                WHERE fecha = ?
                GROUP BY materia, grado;
                """;
        List<Map<String, Object>> asistenciasMap = dataSource.consultar(selectSql, new Object[]{fecha});

        List<ResumenAsistenciaDTO> asistenciaDTOList = new ArrayList<>();
        for (Map<String, Object> asistencia : asistenciasMap) {
            ResumenAsistenciaDTO asistenciaDTO = new ResumenAsistenciaDTO();
            asistenciaDTO.setFecha((java.sql.Date) asistencia.get("fecha"));
            asistenciaDTO.setMateria((String) asistencia.get("materia"));
            asistenciaDTO.setGrado((String) asistencia.get("grado"));
            asistenciaDTO.setCantidasAsistencias((Long) asistencia.get("asistencias"));
            asistenciaDTO.setCantidasInasistencias((Long) asistencia.get("inasistencias"));
            asistenciaDTOList.add(asistenciaDTO);
        }
        return asistenciaDTOList;
    }

    public List<AsistenciaDTO> getListaAlumnos(java.sql.Date fecha, String materia, String grado) {
        String selectSql = """
                SELECT
                    asi.id,
                    alu.id_alumno,
                    alu.nombre,
                    alu.apellido,
                    asi.materia,
                    asi.grado,
                    asi.presente,
                    asi.fecha
                FROM asistencias AS asi
                INNER JOIN alumnos AS alu ON asi.alumno_id = alu.id_alumno
                WHERE asi.fecha = ? AND asi.materia = ? AND asi.grado = ?;
                """;
        List<Map<String, Object>> asistenciasMap = dataSource.consultar(selectSql, new Object[]{fecha, materia, grado});
        if (!asistenciasMap.isEmpty()) {
            return getListaAsistenciaExistente(asistenciasMap);
        }

        List<AlumnoDTO> alumnoDTOList = alumnosModelo.getListaAlumnosPorMateriaYGrado(materia, grado);
        if (!alumnoDTOList.isEmpty()) {
            return getListaAsistenciaNueva(fecha, materia, grado, alumnoDTOList);
        }

        return Collections.emptyList();
    }

    private List<AsistenciaDTO> getListaAsistenciaExistente(List<Map<String, Object>> asistenciasMap) {
        List<AsistenciaDTO> asistenciaDTOList = new ArrayList<>();
        for (Map<String, Object> asistencia : asistenciasMap) {
            AsistenciaDTO asistenciaDTO = new AsistenciaDTO();
            asistenciaDTO.setId((Integer) asistencia.get("id"));
            asistenciaDTO.setFecha((java.sql.Date) asistencia.get("fecha"));
            asistenciaDTO.setMateria((String) asistencia.get("materia"));
            asistenciaDTO.setGrado((String) asistencia.get("grado"));
            asistenciaDTO.setPresente((Boolean) asistencia.get("presente"));

            AlumnoDTO alumnoDTO = new AlumnoDTO();
            alumnoDTO.setId((Integer) asistencia.get("id_alumno"));
            alumnoDTO.setNombre((String) asistencia.get("nombre"));
            alumnoDTO.setApellido((String) asistencia.get("apellido"));
            asistenciaDTO.setAlumnoDTO(alumnoDTO);

            asistenciaDTOList.add(asistenciaDTO);
        }

        return asistenciaDTOList;
    }

    private List<AsistenciaDTO> getListaAsistenciaNueva(java.sql.Date fecha, String materia, String grado, List<AlumnoDTO> alumnoDTOList) {
        List<AsistenciaDTO> asistenciaDTOList = new ArrayList<>();
        for (AlumnoDTO alumnoDTO : alumnoDTOList) {
            AsistenciaDTO asistenciaDTO = new AsistenciaDTO();
            asistenciaDTO.setFecha(fecha);
            asistenciaDTO.setAlumnoDTO(alumnoDTO);
            asistenciaDTO.setMateria(materia);
            asistenciaDTO.setGrado(grado);
            asistenciaDTOList.add(asistenciaDTO);
        }
        return asistenciaDTOList;
    }

    public boolean guardarAsistencias(List<AsistenciaDTO> asistencias) {
        List<Object[]> insertar = new ArrayList<>();
        List<Object[]> actualizar = new ArrayList<>();

        for (AsistenciaDTO asistenciaDTO : asistencias) {
            List<Object> parametros = new ArrayList<>();
            parametros.add(asistenciaDTO.getAlumnoDTO().getId());
            parametros.add(asistenciaDTO.getFecha());
            parametros.add(asistenciaDTO.isPresente());
            parametros.add(asistenciaDTO.getMateria());
            parametros.add(asistenciaDTO.getGrado());

            if (asistenciaDTO.getId() == 0) {
                insertar.add(parametros.toArray());
            } else {
                parametros.add(asistenciaDTO.getId());
                actualizar.add(parametros.toArray());
            }
        }

        boolean insertOk = true;
        boolean updateOk = true;

        if (!insertar.isEmpty()) {
            insertOk = dataSource.ejecutarConsultaBatch(getSqlAsistencia(true), insertar);
        }

        if (!actualizar.isEmpty()) {
            updateOk = dataSource.ejecutarConsultaBatch(getSqlAsistencia(false), actualizar);
        }

        return insertOk && updateOk;
    }

    private String getSqlAsistencia(boolean esNuevo) {
        return esNuevo
                ? "INSERT INTO asistencias (alumno_id, fecha, presente, materia, grado) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE asistencias SET alumno_id = ?, fecha = ?, presente = ?, materia = ?, grado = ? WHERE id = ?";
    }

}
