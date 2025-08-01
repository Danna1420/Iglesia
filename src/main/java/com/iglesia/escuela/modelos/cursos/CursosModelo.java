package com.iglesia.escuela.modelos.cursos;

import com.iglesia.escuela.basedatos.DataSource;
import com.iglesia.escuela.dtos.cursos.CursoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CursosModelo {

    private final DataSource dataSource;

    public CursosModelo() {
        this.dataSource = DataSource.getInstancia();
    }

    public List<CursoDTO> getListaCursos() {
        String selectSql = "SELECT * FROM curso";
        List<Map<String, Object>> cursosMap = dataSource.consultar(selectSql, null);

        List<CursoDTO> cursoDTOList = new ArrayList<>();
        for (Map<String, Object> curso : cursosMap) {
            CursoDTO cursoDTO = new CursoDTO();
            cursoDTO.setId((Integer) curso.get("id_curso"));
            cursoDTO.setNombre((String) curso.get("nombre_curso"));
            cursoDTOList.add(cursoDTO);
        }
        return cursoDTOList;
    }

    public boolean guardarCurso(CursoDTO cursoDTO) {
        boolean esNuevo = cursoDTO.getId() == null;

        String sql = esNuevo
                ? "INSERT INTO curso (nombre_curso) VALUES (?)"
                : "UPDATE curso SET nombre_curso = ? WHERE id_curso = ?";

        List<Object> parametros = new ArrayList<>();
        parametros.add(cursoDTO.getNombre());

        if (!esNuevo) {
            parametros.add(cursoDTO.getId());
        }

        return dataSource.ejecutarConsulta(sql, parametros.toArray());
    }

    public boolean eliminarCurso(Long id) {
        String sql = "DELETE FROM curso WHERE id_curso = ?";
        return dataSource.eliminarPorId(sql, id);
    }

}
