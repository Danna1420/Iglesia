package com.iglesia.escuela.modelos.usuarios;

import com.iglesia.escuela.basedatos.DataSource;
import com.iglesia.escuela.dtos.usuarios.UsuarioDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UsuariosModelo {

    private final DataSource dataSource;

    public UsuariosModelo() {
        this.dataSource = DataSource.getInstancia();
    }

    public UsuarioDTO ingresar(String nombre, String contrasena) {
        String selectSql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";
        List<Map<String, Object>> usuariosMap = dataSource.consultar(selectSql, new Object[]{nombre, contrasena});

        if (usuariosMap.isEmpty()) return null;

        return mapUsuario(usuariosMap.getFirst());
    }

    private UsuarioDTO mapUsuario(Map<String, Object> usuario) {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId((Integer) usuario.get("id_usuario"));
        usuarioDTO.setNombre((String) usuario.get("username"));
        usuarioDTO.setTipoNivel((String) usuario.get("tipo_nivel"));
        return usuarioDTO;
    }

    public List<UsuarioDTO> getListaUsuarios() {
        String selectSql = "SELECT * FROM usuarios WHERE tipo_nivel != 'Administrador'";
        List<Map<String, Object>> usuariosMap = dataSource.consultar(selectSql, null);

        List<UsuarioDTO> usuarioDTOList = new ArrayList<>();
        for (Map<String, Object> usuario : usuariosMap) {
            usuarioDTOList.add(mapUsuario(usuario));
        }
        return usuarioDTOList;
    }

    public boolean guardarUsuario(UsuarioDTO usuarioDTO) {
        boolean esNuevo = usuarioDTO.getId() == null;

        String sql = esNuevo
                ? "INSERT INTO usuarios (username, password, tipo_nivel) VALUES (?, ?, ?)"
                : "UPDATE usuarios SET username = ?, password = ?, tipo_nivel = ? WHERE id_usuario = ?";

        List<Object> parametros = new ArrayList<>();
        parametros.add(usuarioDTO.getNombre());
        parametros.add(usuarioDTO.getContrasena());
        parametros.add("Usuario");

        if (!esNuevo) {
            parametros.add(usuarioDTO.getId());
        }

        return dataSource.ejecutarConsulta(sql, parametros.toArray());
    }

    public boolean eliminarUsuario(Long id) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        return dataSource.eliminarPorId(sql, id);
    }

}
