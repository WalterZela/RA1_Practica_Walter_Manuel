package org.educa.service;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOImpl;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {
    private final ProductoDAO productoDAO = new ProductoDAOImpl();

    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {
        //TODO: Implementar
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

    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        //TODO: Implementar

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

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
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
    }
}
