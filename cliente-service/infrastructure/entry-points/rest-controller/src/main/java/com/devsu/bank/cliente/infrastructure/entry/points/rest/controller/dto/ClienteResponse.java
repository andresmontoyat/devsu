package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto;

public record ClienteResponse(
    String clienteId,
    String nombre,
    String genero,
    int edad,
    String identificacion,
    String direccion,
    String telefono,
    boolean estado) {}
