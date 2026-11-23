package ar.edu.utn.diplomatura.productservice.mapper;

import ar.edu.utn.diplomatura.productservice.dto.CuentaDTO;
import ar.edu.utn.diplomatura.productservice.entity.CajaAhorro;
import ar.edu.utn.diplomatura.productservice.entity.Cuenta;
import ar.edu.utn.diplomatura.productservice.entity.CuentaCorriente;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CuentaMapper {

    public CuentaDTO toCuentaDTO(Cuenta cuenta) {

        String tipoCuenta;
        BigDecimal limiteDescubierto = null;
        BigDecimal porcentajeTasaInteresAnual = null;

        if (cuenta instanceof CuentaCorriente unaCuentaCorriente) {
            tipoCuenta = "CUENTA_CORRIENTE";
            limiteDescubierto = unaCuentaCorriente.getLimiteDescubierto();
        } else if (cuenta instanceof CajaAhorro unaCajaDeAhorro) {
            tipoCuenta = "CAJA_AHORRO";
            porcentajeTasaInteresAnual = unaCajaDeAhorro.getPorcentajeTasaInteresAnual();
        } else {
            throw new IllegalArgumentException("Tipo de cuenta desconocido");
        }

        return new CuentaDTO(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                tipoCuenta,
                cuenta.getSaldo(),
                cuenta.getMoneda(),
                cuenta.getFechaDeApertura(),
                cuenta.isHabilitada(),
                cuenta.getClienteId(),
                limiteDescubierto,
                porcentajeTasaInteresAnual
        );
    }
}
