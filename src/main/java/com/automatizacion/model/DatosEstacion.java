package com.automatizacion.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosEstacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String codigoEstacion;
    private String codigoSensor;
    private Date fechaObservacion;
    private int valorObservado;
    private String nombreEstacion;
    private String departamento;
    private String municipio;
    private String zonaHidrografica;
    private String latitud;
    private String longitud;
    private String descripcionSensor;
    private String unidadMedida;
}
