package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

public class ProductoDAOImpl implements ProductoDAO {
    /**
     * Lee y convierte el contenido de un archivo XML en un objeto Productos.
     *
     * @param file archivo XML que se va a leer
     * @return objeto Productos obtenido del archivo XML
     * @throws JAXBException si ocurre un error durante la lectura o conversión
     */
    @Override
    public Productos readFile(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Productos.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        return (Productos) unmarshaller.unmarshal(file);
    }
}

