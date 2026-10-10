package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBException;

import java.io.File;

public interface ProductoDAO {
    /**
     * Lee los productos desde un archivo XML.
     *
     * @param file archivo XML que contiene los productos
     * @return productos leídos del archivo
     * @throws JAXBException si ocurre un error al leer el XML
     */
    Productos readFile(File file) throws JAXBException;
}
