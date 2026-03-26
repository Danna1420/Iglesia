-- NUEVA BASE DE DATOS
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100),
    password VARCHAR(100),
    tipo_nivel VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS alumnos (
    id_alumno INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    grado VARCHAR(50),
    telefono VARCHAR(20),
    id_curso_asignado VARCHAR(150),

    fecha_nacimiento DATE,
    talla_camisa INT DEFAULT 0,
    talla_pantalon INT DEFAULT 0,
    talla_zapato INT DEFAULT 0,

    nombre_acudiente VARCHAR(100),
    apellido_acudiente VARCHAR(100),
    telefono_acudiente VARCHAR(20),
    casa_oracion_acudiente VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS asistencias (
    id INT PRIMARY KEY AUTO_INCREMENT,
    alumno_id INT NOT NULL,
    fecha DATE NOT NULL,
    presente BOOLEAN NOT NULL,
    materia VARCHAR(253),
    grado VARCHAR(253),
    FOREIGN KEY (alumno_id) REFERENCES alumnos(id_alumno)
);

CREATE TABLE IF NOT EXISTS notas (
    id_nota INT PRIMARY KEY AUTO_INCREMENT,
    id_alumno_nota INTEGER NOT NULL,
    id_curso_nota VARCHAR(150),
    tarea VARCHAR(150),
    calificacion NUMERIC(5,2),
    FOREIGN KEY (id_alumno_nota) REFERENCES alumnos(id_alumno)
);

CREATE TABLE IF NOT EXISTS curso (
    id_curso INT PRIMARY KEY AUTO_INCREMENT,
    nombre_curso VARCHAR(150)
);

INSERT INTO usuarios (id_usuario, username, password, tipo_nivel) VALUES ('1', 'admin', 'admin', 'Administrador');


-- MIGRACIONES PARA BASE DE DATOS EXISTENTE
ALTER TABLE alumnos ADD COLUMN fecha_nacimiento DATE;
ALTER TABLE alumnos ADD COLUMN talla_camisa INT DEFAULT 0;
ALTER TABLE alumnos ADD COLUMN talla_pantalon INT DEFAULT 0;
ALTER TABLE alumnos ADD COLUMN talla_zapato INT DEFAULT 0;

ALTER TABLE alumnos ADD COLUMN nombre_acudiente VARCHAR(100);
ALTER TABLE alumnos ADD COLUMN apellido_acudiente VARCHAR(100);
ALTER TABLE alumnos ADD COLUMN telefono_acudiente VARCHAR(20);
ALTER TABLE alumnos ADD COLUMN casa_oracion_acudiente VARCHAR(150);