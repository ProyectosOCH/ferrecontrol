package com.ferrecontrol.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ferrecontrol.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
}
