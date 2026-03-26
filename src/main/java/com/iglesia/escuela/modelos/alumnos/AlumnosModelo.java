package com.iglesia.escuela.modelos.alumnos;

import com.iglesia.escuela.basedatos.DataSource;
import com.iglesia.escuela.dtos.alumnos.AlumnoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlumnosModelo {

    private final DataSource dataSource;

    public AlumnosModelo() {
        this.dataSource = DataSource.getInstancia();
    }

    public List<AlumnoDTO> getListaAlumnosPorMateriaYGrado(String materia, String grado) {
        String selectSql = """
                SELECT
                    id_alumno,
                    nombre,
                    apellido
                FROM alumnos
                WHERE id_curso_asignado = ? AND grado = ?;
                """;
        List<Map<String, Object>> alumnosMap = dataSource.consultar(selectSql, new Object[]{materia, grado});

        List<AlumnoDTO> alumnoDTOList = new ArrayList<>();
        for (Map<String, Object> alumno : alumnosMap) {
            AlumnoDTO alumnoDTO = new AlumnoDTO();
            alumnoDTO.setId((Integer) alumno.get("id_alumno"));
            alumnoDTO.setNombre((String) alumno.get("nombre"));
            alumnoDTO.setApellido((String) alumno.get("apellido"));
            alumnoDTOList.add(alumnoDTO);
        }
        return alumnoDTOList;
    }

    public List<AlumnoDTO> getListaAlumnos() {
        String selectSql = "SELECT * FROM alumnos";
        List<Map<String, Object>> alumnosMap = dataSource.consultar(selectSql, null);

        List<AlumnoDTO> alumnoDTOList = new ArrayList<>();
        for (Map<String, Object> alumno : alumnosMap) {
            AlumnoDTO alumnoDTO = new AlumnoDTO();
            alumnoDTO.setId((Integer) alumno.get("id_alumno"));
            alumnoDTO.setNombre((String) alumno.get("nombre"));
            alumnoDTO.setApellido((String) alumno.get("apellido"));
            alumnoDTO.setGrado((String) alumno.get("grado"));
            alumnoDTO.setTelefono((String) alumno.get("telefono"));
            alumnoDTO.setMateria((String) alumno.get("id_curso_asignado"));

            java.sql.Date fechaNacimiento = (java.sql.Date) alumno.get("fecha_nacimiento");
            if (fechaNacimiento != null) alumnoDTO.setFechaNacimiento(fechaNacimiento.toLocalDate());

            alumnoDTO.setTallaCamisa((Integer) alumno.get("talla_camisa"));
            alumnoDTO.setTallaPantalon((Integer) alumno.get("talla_pantalon"));
            alumnoDTO.setTallaZapato((Integer) alumno.get("talla_zapato"));

            alumnoDTO.setNombreAcudiente((String) alumno.get("nombre_acudiente"));
            alumnoDTO.setApellidoAcudiente((String) alumno.get("apellido_acudiente"));
            alumnoDTO.setTelefonoAcudiente((String) alumno.get("telefono_acudiente"));
            alumnoDTO.setCasaOracionAcudiente((String) alumno.get("casa_oracion_acudiente"));
            alumnoDTOList.add(alumnoDTO);
        }
        return alumnoDTOList;
    }

    public boolean guardarAlumno(AlumnoDTO alumnoDTO) {
        boolean esNuevo = alumnoDTO.getId() == null;
        return dataSource.ejecutarConsulta(getSql(esNuevo), getParametros(alumnoDTO, esNuevo));
    }

    private String getSql(boolean esNuevo) {
        return esNuevo
                ? """
                INSERT INTO alumnos (nombre, apellido, grado, telefono, id_curso_asignado, fecha_nacimiento,
                talla_camisa, talla_pantalon, talla_zapato, nombre_acudiente, apellido_acudiente, telefono_acudiente,
                casa_oracion_acudiente)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """
                : """
                UPDATE alumnos SET nombre = ?, apellido = ?, grado = ?, telefono = ?, id_curso_asignado = ?,
                fecha_nacimiento = ?, talla_camisa = ?, talla_pantalon = ?, talla_zapato = ?, nombre_acudiente = ?,
                apellido_acudiente = ?, telefono_acudiente = ?, casa_oracion_acudiente = ?
                WHERE id_alumno = ?
                """;
    }

    private Object[] getParametros(AlumnoDTO alumnoDTO, boolean esNuevo) {
        List<Object> parametros = new ArrayList<>();
        parametros.add(alumnoDTO.getNombre());
        parametros.add(alumnoDTO.getApellido());
        parametros.add(alumnoDTO.getGrado());
        parametros.add(alumnoDTO.getTelefono());
        parametros.add(alumnoDTO.getMateria());
        parametros.add(alumnoDTO.getFechaNacimiento());
        parametros.add(alumnoDTO.getTallaCamisa());
        parametros.add(alumnoDTO.getTallaPantalon());
        parametros.add(alumnoDTO.getTallaZapato());

        parametros.add(alumnoDTO.getNombreAcudiente());
        parametros.add(alumnoDTO.getApellidoAcudiente());
        parametros.add(alumnoDTO.getTelefonoAcudiente());
        parametros.add(alumnoDTO.getCasaOracionAcudiente());

        if (!esNuevo) {
            parametros.add(alumnoDTO.getId());
        }

        return parametros.toArray();
    }

    public boolean guardarAlumnos(List<AlumnoDTO> alumnoDTOList, boolean sonNuevos) {
        List<Object[]> parametrosList = new ArrayList<>();
        for (AlumnoDTO alumnoDTO : alumnoDTOList) {
            parametrosList.add(getParametros(alumnoDTO, sonNuevos));
        }

        return dataSource.ejecutarConsultaBatch(getSql(sonNuevos), parametrosList);
    }

    public boolean eliminarAlumno(Long id) {
        String sql = "DELETE FROM alumnos WHERE id_alumno = ?";
        return dataSource.eliminarPorId(sql, id);
    }

}
