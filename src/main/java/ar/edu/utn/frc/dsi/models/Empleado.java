package ar.edu.utn.frc.dsi.models;

import jakarta.persistence.*;

@Entity
@Table(name = "empleado")
public class Empleado {
    @Id
    private Long id;

    private String nombre;
    private String apellido;
    private String email;

    @ManyToOne
    @JoinColumn(name = "comision_medica")
    private ComisionMedica comisionMedica;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ComisionMedica getCm() {
        return comisionMedica;
        // Cambiar en el diagrama de secuencia el get nombre porque se devuelve el
        // objeto entero, porque si no no se puede hacer lo siguiente
    }

    public void setComisionMedica(ComisionMedica comisionMedica) {
        this.comisionMedica = comisionMedica;
    }

}
