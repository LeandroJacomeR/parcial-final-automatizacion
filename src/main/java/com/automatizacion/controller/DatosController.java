package com.automatizacion.controller;

import com.automatizacion.model.DatosEstacion;
import com.automatizacion.service.DatosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/datos")
@CrossOrigin("*")
public class DatosController {

    @Autowired
    private DatosService service;

    @GetMapping("/all/{orden}")
    public ResponseEntity<List<DatosEstacion>> getAll(@PathVariable String orden) {
        try {
            return new ResponseEntity(service.getDatosEstaciones(orden), HttpStatus.OK);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<DatosEstacion> getById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(service.getDatosEstacion(id));
        }catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/datos-estacion")
    public ResponseEntity<List<DatosEstacion>> getDatosAgrupados(@RequestParam String campo, @RequestParam String valor) {
        try {
            System.out.println("valores: " + campo + ", " + valor);
            return ResponseEntity.ok(service.getDatosAgrupadosPorCampo(campo, valor));
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Por favor, sube un archivo CSV");
        }

        service.guardarCSV(file);
        return ResponseEntity.status(HttpStatus.OK).body("Archivo CSV subido y datos guardados exitosamente");
    }

    @GetMapping("/null-fields")
    public ResponseEntity<List<DatosEstacion>> getNullFields() {
        try {
            return ResponseEntity.ok(service.getAllWithNullFields());
        }catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/buscar-por-fecha")
    public ResponseEntity<List<DatosEstacion>> buscarPorFecha(
            @RequestParam("fecha") @DateTimeFormat(pattern = "dd/MM/yyyy") Date fecha) {

        try {
            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //String fechaFormateada = dateFormat.format(fecha);

            // Llamar al servicio para buscar por fecha
            return ResponseEntity.ok(service.findByFechaObservacion(fecha));
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete() {
        try {
            List<DatosEstacion> lista = service.getAllWithNullFields();
            for (DatosEstacion datosEstacion : lista) {
                service.deleteById(datosEstacion.getId());
            }
            return ResponseEntity.status(HttpStatus.OK).build();
        }catch(Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/exportar")
    public ResponseEntity<InputStreamResource> exportarDatosAExcel(@RequestParam String campo) throws IOException {
        ByteArrayInputStream in = service.exportarDatosAExcel(campo);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=datos_estacion.xlsx");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }
}
