package org.educa.service;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOImpl;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {
    private final ProductoDAO productoDAO = new ProductoDAOImpl();

    /**
     * Lee los productos del archivo XML y calcula sus datos.*
     *
     * @param fileXml ruta del archivo XML que se va a leer
     * @return lista de productos con sus cálculos realizados
     * @throws JAXBException si ocurre un error al leer el XML
     */

    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {

        File file = new File(fileXml);
        Productos productos = productoDAO.readFile(file);
        List<ProductoEntity> lista = new ArrayList<>();
        for (Producto producto : productos.getProducto()) {

            ProductoEntity productoEntity = new ProductoEntity();

            productoEntity.setProducto(producto);

            BigDecimal descuento = producto.getPrecio()
                    .multiply(producto.getDescuento())
                    .divide(BigDecimal.valueOf(100));

            BigDecimal precioFinal = producto.getPrecio().subtract(descuento);

            BigDecimal coste = producto.getCostes().getCostesEnvio()
                    .add(producto.getCostes().getCostesAlmacenaje());

            BigDecimal beneficio = precioFinal.subtract(coste);

            productoEntity.setPrecioFinal(precioFinal);
            productoEntity.setCost(coste);
            productoEntity.setProfit(beneficio);

            lista.add(productoEntity);
        }

        return lista;
    }

    /**
     * Genera un archivo de texto con el resumen de los productos.*
     *
     * @throws JAXBException si ocurre un error al leer el XML
     * @throws IOException   si ocurre un error al escribir el archivo
     */
    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {

        File file = new File(fileXml);

        List<ProductoEntity> productos = readFile(fileXml);

        BigDecimal totalProfit = BigDecimal.ZERO;

        for (ProductoEntity producto : productos) {
            totalProfit = totalProfit.add(producto.getProfit());
        }

        String nombreFichero = file.getName().replace(".xml", "");
        String nombre = nombreFichero.replace("inventario_", "");

        SummaryEntity summary = new SummaryEntity(
                nombre,
                productos.size(),
                totalProfit,
                file.getAbsolutePath(),
                file.getName(),
                file.length()
        );
        File directorio = new File(path);

        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        File ficheroResumen = new File(path + "result_" + nombre + ".txt");

        try (FileWriter fileWriter = new FileWriter(ficheroResumen);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            bufferedWriter.write(summary.toPrint());
        }
    }

    /**
     * Genera un archivo Excel con los datos de los productos.*
     *
     * @param fileXml ruta del archivo XML que contiene los productos
     * @throws JAXBException  si ocurre un error al leer el XML
     * @throws IOException    si ocurre un error al crear o escribir el Excel
     * @throws ParseException si ocurre un error al interpretar el nombre del archivo
     */
    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        List<ProductoEntity> productos = readFile(fileXml);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        Row cabecera = sheet.createRow(0);

        cabecera.createCell(0).setCellValue("Codigo");
        cabecera.createCell(1).setCellValue("Numero de Serie");
        cabecera.createCell(2).setCellValue("Precio");
        cabecera.createCell(3).setCellValue("Descuento");
        cabecera.createCell(4).setCellValue("Precio Final");
        cabecera.createCell(5).setCellValue("Costes Envio");
        cabecera.createCell(6).setCellValue("Costes Almacenaje");
        cabecera.createCell(7).setCellValue("Beneficio");

        int fila = 1;

        for (ProductoEntity producto : productos) {
            Row row = sheet.createRow(fila);

            row.createCell(0).setCellValue(producto.getProducto().getCodigo());
            row.createCell(1).setCellValue(producto.getProducto().getNumeroSerie());
            row.createCell(2).setCellValue(producto.getProducto().getPrecio().doubleValue());
            row.createCell(3).setCellValue(producto.getProducto().getDescuento().doubleValue());
            row.createCell(4).setCellValue(producto.getPrecioFinal().doubleValue());
            row.createCell(5).setCellValue(producto.getProducto().getCostes().getCostesEnvio().doubleValue());
            row.createCell(6).setCellValue(producto.getProducto().getCostes().getCostesAlmacenaje().doubleValue());
            row.createCell(7).setCellValue(producto.getProfit().doubleValue());

            fila++;
        }
        CellStyle estiloCabecera = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);

        estiloCabecera.setFont(font);
        estiloCabecera.setAlignment(HorizontalAlignment.CENTER);
        estiloCabecera.setBorderTop(BorderStyle.THIN);
        estiloCabecera.setBorderBottom(BorderStyle.THIN);
        estiloCabecera.setBorderLeft(BorderStyle.THIN);
        estiloCabecera.setBorderRight(BorderStyle.THIN);

        for (Cell cell : cabecera) {
            cell.setCellStyle(estiloCabecera);
        }

        CellStyle estiloFila1 = workbook.createCellStyle();
        estiloFila1.setAlignment(HorizontalAlignment.CENTER);
        estiloFila1.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloFila1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloFila1.setBorderTop(BorderStyle.THIN);
        estiloFila1.setBorderBottom(BorderStyle.THIN);
        estiloFila1.setBorderLeft(BorderStyle.THIN);
        estiloFila1.setBorderRight(BorderStyle.THIN);

        CellStyle estiloFila2 = workbook.createCellStyle();
        estiloFila2.setAlignment(HorizontalAlignment.CENTER);
        estiloFila2.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        estiloFila2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloFila2.setBorderTop(BorderStyle.THIN);
        estiloFila2.setBorderBottom(BorderStyle.THIN);
        estiloFila2.setBorderLeft(BorderStyle.THIN);
        estiloFila2.setBorderRight(BorderStyle.THIN);

        int numeroFila = 1;

        for (Row row : sheet) {
            if (numeroFila > 0) {
                CellStyle estilo;

                if (numeroFila % 2 == 0) {
                    estilo = estiloFila1;
                } else {
                    estilo = estiloFila2;
                }

                for (Cell cell : row) {
                    cell.setCellStyle(estilo);
                }
            }

            numeroFila++;
        }

        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }

        File ficheroExcel = new File(path + "export_junio2026.xlsx");

        try (FileOutputStream archivo = new FileOutputStream(ficheroExcel)) {
            workbook.write(archivo);
        }

        workbook.close();
    }
}
