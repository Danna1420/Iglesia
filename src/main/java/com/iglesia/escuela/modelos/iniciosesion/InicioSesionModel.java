package com.iglesia.escuela.modelos.iniciosesion;

import com.iglesia.escuela.basedatos.DataSource;

import java.util.List;
import java.util.Map;

public class InicioSesionModel {

    private final DataSource dataSource;

    public InicioSesionModel() {
        this.dataSource = DataSource.getInstancia();
    }

    public boolean ingresar(String usuario, String contrasena) {
        String selectSql = "SELECT * FROM usuarios WHERE tipo_nivel = 'Administrador' AND username = ? AND password = ?";
        List<Map<String, Object>> alumnosMap = dataSource.consultar(selectSql, new Object[]{usuario, contrasena});
        return !alumnosMap.isEmpty();
    }

}
