package com.ferrecontrol.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ferrecontrol.model.Cliente;
import com.ferrecontrol.model.Producto;
import com.ferrecontrol.model.Proveedor;
import com.ferrecontrol.model.Usuario;
import com.ferrecontrol.repository.ClienteRepository;
import com.ferrecontrol.repository.ProveedorRepository;
import com.ferrecontrol.repository.UsuarioRepository;
import com.ferrecontrol.service.ProductoService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FerreControlController {

    private final ProductoService productoService;
    private final ClienteRepository clienteRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;

    public FerreControlController(
            ProductoService productoService,
            ClienteRepository clienteRepository,
            ProveedorRepository proveedorRepository,
            UsuarioRepository usuarioRepository) {
        this.productoService = productoService;
        this.clienteRepository = clienteRepository;
        this.proveedorRepository = proveedorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ==================== RESUMEN ====================

    @GetMapping("/resumen")
    public Map<String, Object> obtenerResumen() {
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalProductos", productoService.listarTodos().size());
        resumen.put("productosBajoStock", productoService.obtenerProductosConBajoStock().size());
        resumen.put("totalClientes", clienteRepository.count());
        resumen.put("totalProveedores", proveedorRepository.count());
        resumen.put("totalUsuarios", usuarioRepository.count());
        return resumen;
    }

    // ==================== PRODUCTOS ====================

    @GetMapping("/productos")
    public List<Producto> listarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria) {
        if (nombre != null && !nombre.isBlank()) {
            return productoService.buscarPorNombre(nombre);
        }
        if (categoria != null && !categoria.isBlank()) {
            return productoService.buscarPorCategoria(categoria);
        }
        return productoService.listarTodos();
    }

    @GetMapping("/productos/{id}")
    public Producto obtenerProducto(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado con id: " + id));
    }

    @GetMapping("/productos/codigo/{codigo}")
    public Producto obtenerProductoPorCodigo(@PathVariable String codigo) {
        return productoService.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado con código: " + codigo));
    }

    @GetMapping("/productos/bajo-stock")
    public List<Producto> listarProductosConBajoStock() {
        return productoService.obtenerProductosConBajoStock();
    }

    @PostMapping("/productos")
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        Producto guardado = productoService.guardar(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/productos/{id}")
    public Producto actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    @PatchMapping("/productos/{id}/stock")
    public Producto ajustarStockProducto(@PathVariable Long id, @RequestParam int cantidad) {
        return productoService.ajustarStock(id, cantidad);
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== CLIENTES ====================

    @GetMapping("/clientes")
    public List<Cliente> listarClientes(@RequestParam(required = false) String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            return clienteRepository.findByNombreContainingIgnoreCase(nombre);
        }
        return clienteRepository.findAll();
    }

    @GetMapping("/clientes/{id}")
    public Cliente obtenerCliente(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente no encontrado con id: " + id));
    }

    @PostMapping("/clientes")
    public ResponseEntity<Cliente> crearCliente(@RequestBody Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del cliente es obligatorio");
        }
        Cliente guardado = clienteRepository.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/clientes/{id}")
    public Cliente actualizarCliente(@PathVariable Long id, @RequestBody Cliente datos) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente no encontrado con id: " + id));

        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del cliente es obligatorio");
        }

        existente.setNombre(datos.getNombre());
        existente.setDocumento(datos.getDocumento());
        existente.setTelefono(datos.getTelefono());
        existente.setEmail(datos.getEmail());
        existente.setDireccion(datos.getDireccion());
        return clienteRepository.save(existente);
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PROVEEDORES ====================

    @GetMapping("/proveedores")
    public List<Proveedor> listarProveedores(@RequestParam(required = false) String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            return proveedorRepository.findByNombreContainingIgnoreCase(nombre);
        }
        return proveedorRepository.findAll();
    }

    @GetMapping("/proveedores/{id}")
    public Proveedor obtenerProveedor(@PathVariable Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Proveedor no encontrado con id: " + id));
    }

    @PostMapping("/proveedores")
    public ResponseEntity<Proveedor> crearProveedor(@RequestBody Proveedor proveedor) {
        if (proveedor.getNombre() == null || proveedor.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del proveedor es obligatorio");
        }
        Proveedor guardado = proveedorRepository.save(proveedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/proveedores/{id}")
    public Proveedor actualizarProveedor(@PathVariable Long id, @RequestBody Proveedor datos) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Proveedor no encontrado con id: " + id));

        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del proveedor es obligatorio");
        }

        existente.setNombre(datos.getNombre());
        existente.setIdentificacion(datos.getIdentificacion());
        existente.setTelefono(datos.getTelefono());
        existente.setEmail(datos.getEmail());
        existente.setDireccion(datos.getDireccion());
        return proveedorRepository.save(existente);
    }

    @DeleteMapping("/proveedores/{id}")
    public ResponseEntity<Void> eliminarProveedor(@PathVariable Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Proveedor no encontrado con id: " + id);
        }
        proveedorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== USUARIOS ====================

    @GetMapping("/usuarios")
    public List<Map<String, Object>> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::toUsuarioResponse)
                .toList();
    }

    @GetMapping("/usuarios/{id}")
    public Map<String, Object> obtenerUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con id: " + id));
        return toUsuarioResponse(usuario);
    }

    @PostMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> crearUsuario(@RequestBody Usuario usuario) {
        validarUsuario(usuario, true);

        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El nombre de usuario ya está registrado: " + usuario.getUsername());
        }

        if (usuario.getRol() == null || usuario.getRol().isBlank()) {
            usuario.setRol("VENDEDOR");
        }
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }

        Usuario guardado = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(toUsuarioResponse(guardado));
    }

    @PutMapping("/usuarios/{id}")
    public Map<String, Object> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario datos) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado con id: " + id));

        validarUsuario(datos, false);

        usuarioRepository.findByUsername(datos.getUsername())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT, "El nombre de usuario ya está en uso: " + datos.getUsername());
                });

        existente.setNombre(datos.getNombre());
        existente.setUsername(datos.getUsername());
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            existente.setPassword(datos.getPassword());
        }
        existente.setEmail(datos.getEmail());
        if (datos.getRol() != null && !datos.getRol().isBlank()) {
            existente.setRol(datos.getRol());
        }
        if (datos.getActivo() != null) {
            existente.setActivo(datos.getActivo());
        }

        return toUsuarioResponse(usuarioRepository.save(existente));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credenciales) {
        String username = credenciales.get("username");
        String password = credenciales.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe ingresar username y password");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .filter(u -> password.equals(u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales inválidas o usuario inactivo"));

        return toUsuarioResponse(usuario);
    }

    private void validarUsuario(Usuario usuario, boolean exigirPassword) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del usuario es obligatorio");
        }
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El username es obligatorio");
        }
        if (exigirPassword && (usuario.getPassword() == null || usuario.getPassword().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
    }

    private Map<String, Object> toUsuarioResponse(Usuario usuario) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", usuario.getId());
        response.put("nombre", usuario.getNombre());
        response.put("username", usuario.getUsername());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol());
        response.put("activo", usuario.getActivo());
        return response;
    }
}
