package ar.edu.utn.diplomatura.productservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CuentaDTO (
    Long id,
    String numeroCuenta,
    String tipoCuenta,
    BigDecimal saldo,
    String moneda,
    LocalDateTime fechaDeApertura,
    boolean habilitada,
    Long clienteId,
    BigDecimal limiteDescubierto, // Cuenta corriente
    BigDecimal porcentajeTasaInteresAnual // Caja de ahorro
){
}
