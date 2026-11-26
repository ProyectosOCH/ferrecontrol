package com.ferrecontrol.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ferrecontrol.model.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);
}
