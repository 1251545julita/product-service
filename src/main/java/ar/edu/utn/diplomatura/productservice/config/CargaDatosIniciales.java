package ar.edu.utn.diplomatura.productservice.config;

import java.math.BigDecimal;
import java.util.List;

import ar.edu.utn.diplomatura.productservice.entity.CajaAhorro;
import ar.edu.utn.diplomatura.productservice.entity.CuentaCorriente;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ar.edu.utn.diplomatura.productservice.entity.Cuenta;
import ar.edu.utn.diplomatura.productservice.repository.CuentaRepository;

import static ar.edu.utn.diplomatura.productservice.Constants.*;

/**
 * Carga cuentas de ejemplo al arrancar.
 *
 * Hace falta porque la base H2 vive en memoria: al apagar la aplicacion se
 * pierde todo, asi que cada arranque empieza con estos cinco.
 */
@Component
@Order(2)
public class CargaDatosIniciales implements CommandLineRunner {

    private final CuentaRepository cuentaRepository;

    public CargaDatosIniciales(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public void run(String... args) {
        if (cuentaRepository.count() > 0) {
            return;
        }

        cuentaRepository.saveAll(List.of(
                new CuentaCorriente("000000000042", BigDecimal.valueOf(152300.50), TIPO_MONEDA_PESOS, 1L,BigDecimal.valueOf(200000.00)),
                new CuentaCorriente("000000078521", BigDecimal.valueOf(8400.00), TIPO_MONEDA_PESOS, 2L,BigDecimal.valueOf(50000.00)),
                new CajaAhorro("000006543210", BigDecimal.valueOf(0.00), TIPO_MONEDA_PESOS, 3L, BigDecimal.valueOf(15.00)),
                new CajaAhorro("045678912345", BigDecimal.valueOf(1250000.00), TIPO_MONEDA_PESOS, 4L, BigDecimal.valueOf(19)),
                new CajaAhorro("789456123078", BigDecimal.valueOf(76500.75), TIPO_MONEDA_PESOS, 5L, BigDecimal.valueOf(15))
        ));

        Cuenta cajaAhorroInactiva = new CajaAhorro("123456789012",  BigDecimal.valueOf(500.00), TIPO_MONEDA_DOLARES, 6L,  BigDecimal.valueOf(19.00));
        cajaAhorroInactiva.setHabilitada(false);
        cuentaRepository.save(cajaAhorroInactiva);

        System.out.println("=== " + cuentaRepository.count() + " cuentas cargadas en MySql ===");
    }
}
