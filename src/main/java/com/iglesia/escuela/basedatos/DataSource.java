package com.iglesia.escuela.basedatos;

import java.sql.*;
import java.util.*;

public class DataSource {

    private static DataSource instancia;

    private DataSource() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("No se encontró el driver JDBC:");
            e.printStackTrace();
        }
    }

    public static synchronized DataSource getInstancia() {
        if (instancia == null) {
            instancia = new DataSource();
        }
        return instancia;
    }

    private Connection crearConexion() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/escuela";
        String usuario = "root";
        String contrasena = "2303";
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    // INSERT o UPDATE
    public boolean ejecutarConsulta(String sql, Object[] parametros) {
        try (Connection conn = crearConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }

            int filas = stmt.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error al ejecutar consulta (INSERT/UPDATE):");
            e.printStackTrace();
            return false;
        }
    }

    public boolean ejecutarConsultaBatch(String sql, List<Object[]> parametrosBatch) {
        try (Connection conn = crearConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Object[] parametros : parametrosBatch) {
                for (int i = 0; i < parametros.length; i++) {
                    stmt.setObject(i + 1, parametros[i]);
                }
                stmt.addBatch();
            }

            int[] resultados = stmt.executeBatch();

            // Retornar true si al menos una fila fue afectada
            for (int filas : resultados) {
                if (filas > 0) return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Error al ejecutar consulta batch (INSERT/UPDATE):");
            e.printStackTrace();
            return false;
        }
    }

    // DELETE por ID
    public boolean eliminarPorId(String sql, Object id) {
        try (Connection conn = crearConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            int filas = stmt.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar por ID:");
            e.printStackTrace();
            return false;
        }
    }

    // SELECT que devuelve lista de mapas (clave = nombreColumna, valor = dato)
    public List<Map<String, Object>> consultar(String sql, Object[] parametros) {
        List<Map<String, Object>> resultados = new ArrayList<>();

        try (Connection conn = crearConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (parametros != null) {
                for (int i = 0; i < parametros.length; i++) {
                    stmt.setObject(i + 1, parametros[i]);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnas = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    for (int i = 1; i <= columnas; i++) {
                        String columna = meta.getColumnLabel(i);
                        Object valor = rs.getObject(i);
                        fila.put(columna, valor);
                    }
                    resultados.add(fila);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al ejecutar SELECT:");
            e.printStackTrace();
        }

        return resultados;
    }
}
