package ar.edu.utn.diplomatura.productservice.dto;

import java.math.BigDecimal;

public record SaldoDTO(
        Long id,
        String numeroCuenta,
        BigDecimal saldo,
        String moneda
) {}
