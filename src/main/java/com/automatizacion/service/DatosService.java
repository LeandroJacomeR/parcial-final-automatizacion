package com.automatizacion.service;

import com.automatizacion.model.DatosEstacion;
import com.automatizacion.repository.DatosEstacionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Locale;

@Service
public class DatosService {

    @Autowired
    private DatosEstacionRepository repository;

    private static final int EXPECTED_FIELDS = 12;

    public DatosEstacion getDatosEstacion(int id) {
        return repository.findById(id).orElse(null);
    }

    public List<DatosEstacion> getDatosEstaciones(String orden) {
        return repository.findAll(Sort.by(orden).ascending());
    }

    public List<DatosEstacion> getDatosAgrupadosPorCampo(String campo, String valor) throws ParseException {
        switch (campo) {
            case "codigoEstacion":
                return repository.findByCodigoEstacion(valor);
            case "codigoSensor":
                return repository.findByDescripcionSensor(valor);
            case "zonaHidrografica":
                return repository.findByZonaHidrografica(valor);
            case "valorObservado":
                int valorObservado = Integer.parseInt(valor);
                return repository.findByValorObservado(valorObservado);
            // Añadir más casos para otros campos
            default:
                throw new IllegalArgumentException("Campo no soportado: " + campo);
        }
    }

    public List<DatosEstacion> findByFechaObservacion(Date fecha) {
        return repository.findByFechaObservacion(fecha);
    }

    public List<DatosEstacion> getAllWithNullFields() {
        return repository.findAllWithNullFields();
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public void guardarCSV(MultipartFile file) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            List<DatosEstacion> entidades = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.ENGLISH);

            // Leer la primera línea (encabezados) y descartarla
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] datos = line.split(";", -1);

                if (datos.length == EXPECTED_FIELDS) {
                    DatosEstacion entidad = new DatosEstacion();

                    entidad.setCodigoEstacion(datos[0]);
                    entidad.setCodigoSensor(datos[1]);

                    try {
                        Date fechaObservacion = dateFormat.parse(datos[2]);
                        entidad.setFechaObservacion(fechaObservacion);
                    } catch (ParseException e) {
                        System.err.println("Error al parsear la fecha: " + datos[2] + " - " + e.getMessage());
                        continue;  // Ignorar esta línea y pasar a la siguiente
                    }

                    try {
                        entidad.setValorObservado(Integer.parseInt(datos[3]));
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear valor observado: " + datos[3] + " - " + e.getMessage());
                        continue;  // Ignorar esta línea y pasar a la siguiente
                    }

                    entidad.setNombreEstacion(datos[4]);
                    entidad.setDepartamento(datos[5]);
                    entidad.setMunicipio(datos[6]);
                    entidad.setZonaHidrografica(datos[7]);
                    entidad.setLatitud(datos[8]);
                    entidad.setLongitud(datos[9]);
                    entidad.setDescripcionSensor(datos[10]);
                    entidad.setUnidadMedida(datos[11]);

                    entidades.add(entidad);
                } else {
                    System.err.println("Número de campos incorrecto en línea: " + line);
                }
            }

            repository.saveAll(entidades);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ByteArrayInputStream exportarDatosAExcel(String campo) throws IOException {
        String[] columnas = {"CodigoEstacion", "CodigoSensor", "FechaObservacion", "ValorObservado",
                "NombreEstacion", "Departamento", "Municipio", "ZonaHidrografica",
                "Latitud", "Longitud", "DescripcionSensor", "UnidadMedida"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("DatosEstacion");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLACK.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Crear fila de encabezado
            Row headerRow = sheet.createRow(0);

            for (int col = 0; col < columnas.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columnas[col]);
                cell.setCellStyle(headerCellStyle);
            }

            List<DatosEstacion> datosEstaciones = getDatosEstaciones(campo);

            int rowIdx = 1;
            for (DatosEstacion datos : datosEstaciones) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(datos.getCodigoEstacion());
                row.createCell(1).setCellValue(datos.getCodigoSensor());
                row.createCell(2).setCellValue(datos.getFechaObservacion().toString());
                row.createCell(3).setCellValue(datos.getValorObservado());
                row.createCell(4).setCellValue(datos.getNombreEstacion());
                row.createCell(5).setCellValue(datos.getDepartamento());
                row.createCell(6).setCellValue(datos.getMunicipio());
                row.createCell(7).setCellValue(datos.getZonaHidrografica());
                row.createCell(8).setCellValue(datos.getLatitud());
                row.createCell(9).setCellValue(datos.getLongitud());
                row.createCell(10).setCellValue(datos.getDescripcionSensor());
                row.createCell(11).setCellValue(datos.getUnidadMedida());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
