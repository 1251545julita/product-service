package ar.edu.utn.diplomatura.productservice.dto;

import ar.edu.utn.diplomatura.productservice.entity.CajaAhorro;
import ar.edu.utn.diplomatura.productservice.entity.Cuenta;
import ar.edu.utn.diplomatura.productservice.entity.CuentaCorriente;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CuentaResponse(
        Long id,
        String numeroCuenta,
        String tipo,
        String moneda,
        BigDecimal saldo,
        BigDecimal saldoDisponible,
        BigDecimal limiteDescubierto,
        BigDecimal porcentajeTasaInteresAnual,
        Long clienteId,
        LocalDateTime fechaDeApertura,
        boolean habilitada) {

    public static CuentaResponse desdeEntidad(Cuenta cuenta) {
        BigDecimal limite = cuenta instanceof CuentaCorriente corriente ? corriente.getLimiteDescubierto() : null;
        BigDecimal tasa = cuenta instanceof CajaAhorro caja ? caja.getPorcentajeTasaInteresAnual() : null;

        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getMoneda(),
                cuenta.getSaldo(),
                cuenta.obtenerSaldoDisponible(),
                limite,
                tasa,
                cuenta.getClienteId(),
                cuenta.getFechaDeApertura(),
                cuenta.isHabilitada());
    }
}
