CREATE TABLE asistencias (
    id INT PRIMARY KEY AUTO_INCREMENT,
    alumno_id INT NOT NULL,
    fecha DATE NOT NULL,
    presente BOOLEAN NOT NULL,
    materia VARCHAR(253),
    grado VARCHAR(253),
    FOREIGN KEY (alumno_id) REFERENCES alumnos(id_alumno)
);
