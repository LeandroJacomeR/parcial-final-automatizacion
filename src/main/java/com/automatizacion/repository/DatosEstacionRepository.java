package com.automatizacion.repository;

import com.automatizacion.model.DatosEstacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DatosEstacionRepository extends JpaRepository<DatosEstacion, Integer> {

    List<DatosEstacion> findByCodigoEstacion(String codigoEstacion);

    List<DatosEstacion> findByDescripcionSensor(String descripcionSensor);

    List<DatosEstacion> findByZonaHidrografica(String zonaHidrografica);

    @Query("SELECT d FROM DatosEstacion d WHERE DATE_FORMAT(d.fechaObservacion, '%Y-%m-%d') = DATE_FORMAT(:fecha, '%Y-%m-%d')")
    List<DatosEstacion> findByFechaObservacion(Date fecha);

    List<DatosEstacion> findByValorObservado(int valorObservado);

    @Query("SELECT d FROM DatosEstacion d WHERE " +
            "d.codigoEstacion IS NULL OR " +
            "d.codigoSensor IS NULL OR " +
            "d.fechaObservacion IS NULL OR " +
            "d.valorObservado IS NULL OR " +
            "d.nombreEstacion IS NULL OR " +
            "d.departamento IS NULL OR " +
            "d.municipio IS NULL OR " +
            "d.zonaHidrografica IS NULL OR " +
            "d.latitud IS NULL OR " +
            "d.longitud IS NULL OR " +
            "d.descripcionSensor IS NULL OR " +
            "d.unidadMedida IS NULL")
    List<DatosEstacion> findAllWithNullFields();
}
