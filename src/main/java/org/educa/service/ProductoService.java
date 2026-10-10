package org.educa.service;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOImpl;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;

import java.io.File;
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
    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }
}
