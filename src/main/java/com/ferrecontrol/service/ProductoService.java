package com.ferrecontrol.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ferrecontrol.model.Producto;
import com.ferrecontrol.model.Proveedor;
import com.ferrecontrol.repository.ProductoRepository;
import com.ferrecontrol.repository.ProveedorRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;

    public ProductoService(ProductoRepository productoRepository, ProveedorRepository proveedorRepository) {
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCase(categoria);
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosConBajoStock() {
        return productoRepository.findProductosConBajoStock();
    }

    @Transactional
    public Producto guardar(Producto producto) {
        validarDatosProducto(producto);

        if (producto.getCodigo() != null && !producto.getCodigo().isBlank()) {
            Optional<Producto> existente = productoRepository.findByCodigo(producto.getCodigo());
            if (existente.isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya existe un producto registrado con el código: " + producto.getCodigo());
            }
        }

        if (producto.getStockMinimo() == null) {
            producto.setStockMinimo(5);
        }

        producto.setProveedor(resolverProveedor(producto.getProveedor()));
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, Producto datosActualizados) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado con id: " + id));

        validarDatosProducto(datosActualizados);

        if (datosActualizados.getCodigo() != null && !datosActualizados.getCodigo().isBlank()) {
            productoRepository.findByCodigo(datosActualizados.getCodigo())
                    .filter(otro -> !otro.getId().equals(id))
                    .ifPresent(otro -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Ya existe otro producto con el código: " + datosActualizados.getCodigo());
                    });
        }

        existente.setCodigo(datosActualizados.getCodigo());
        existente.setNombre(datosActualizados.getNombre());
        existente.setCategoria(datosActualizados.getCategoria());
        existente.setDescripcion(datosActualizados.getDescripcion());
        existente.setPrecio(datosActualizados.getPrecio());
        existente.setStock(datosActualizados.getStock());
        existente.setStockMinimo(
                datosActualizados.getStockMinimo() != null ? datosActualizados.getStockMinimo() : 5);
        existente.setProveedor(resolverProveedor(datosActualizados.getProveedor()));

        return productoRepository.save(existente);
    }

    @Transactional
    public Producto ajustarStock(Long id, int cantidad) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado con id: " + id));

        int nuevoStock = producto.getStock() + cantidad;
        if (nuevoStock < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock insuficiente. Stock actual: " + producto.getStock());
        }

        producto.setStock(nuevoStock);
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
    }

    private void validarDatosProducto(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto es obligatorio");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor o igual a cero");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock debe ser mayor o igual a cero");
        }
        if (producto.getStockMinimo() != null && producto.getStockMinimo() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El stock mínimo debe ser mayor o igual a cero");
        }
    }

    private Proveedor resolverProveedor(Proveedor proveedor) {
        if (proveedor == null || proveedor.getId() == null) {
            return null;
        }
        return proveedorRepository.findById(proveedor.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Proveedor no encontrado con id: " + proveedor.getId()));
    }
}
