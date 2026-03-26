package com.iglesia.escuela.controladores.principal;

import com.iglesia.escuela.controladores.alumnos.AlumnosControlador;
import com.iglesia.escuela.controladores.asistencias.AsistenciaControlador;
import com.iglesia.escuela.controladores.base.BaseControlador;
import com.iglesia.escuela.controladores.calificaciones.CalificacionesControlador;
import com.iglesia.escuela.controladores.cursos.CursosControlador;
import com.iglesia.escuela.controladores.iniciosesion.InicioSesionControlador;
import com.iglesia.escuela.controladores.usuarios.UsuariosControlador;
import com.iglesia.escuela.dtos.usuarios.UsuarioDTO;
import com.iglesia.escuela.vistas.principal.PrincipalVista;

import java.awt.*;

public class PrincipalControlador extends BaseControlador {

    private final PrincipalVista principalVista;

    public static UsuarioDTO usuarioSesionDTO;

    public PrincipalControlador() {
        this.principalVista = new PrincipalVista();
        iniciarEventos();
    }

    @Override
    protected void iniciarEventos() {
        principalVista.getAlumnosButton().addActionListener(e -> setContenidoPanel(new AlumnosControlador()));
        principalVista.getCursosButton().addActionListener(e -> setContenidoPanel(new CursosControlador()));
        principalVista.getCalificacionesButton().addActionListener(e -> setContenidoPanel(new CalificacionesControlador()));
        principalVista.getAsistenciasButton().addActionListener(e -> setContenidoPanel(new AsistenciaControlador()));

        if ("Administrador".equals(usuarioSesionDTO.getTipoNivel())) {
            principalVista.getUsuariosButton().addActionListener(e -> setContenidoPanel(new UsuariosControlador()));
        } else principalVista.getUsuariosButton().setVisible(false);

        principalVista.getSalirButton().addActionListener(e -> salir());
    }

    private void setContenidoPanel(BaseControlador controlador) {
        boolean activado = controlador.activar();
        if (!activado) {
            return;
        }

        principalVista.getContenidoPanel().removeAll();
        principalVista.getContenidoPanel().add(controlador.getVista(), BorderLayout.CENTER);
        principalVista.getContenidoPanel().revalidate();
        principalVista.getContenidoPanel().repaint();
    }

    private void salir() {
        usuarioSesionDTO = null;
        principalVista.dispose();
        InicioSesionControlador inicioSesionControlador = new InicioSesionControlador();
        inicioSesionControlador.getVista().setVisible(true);
    }

    @Override
    public boolean activar() {
        return true;
    }

    @Override
    public boolean desactivar() {
        return true;
    }

    @Override
    public Component getVista() {
        return principalVista;
    }
}
