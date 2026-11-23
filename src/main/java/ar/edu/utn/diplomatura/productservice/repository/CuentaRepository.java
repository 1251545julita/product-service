package ar.edu.utn.diplomatura.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.edu.utn.diplomatura.productservice.entity.Cuenta;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a la base de datos.
 *
 * Es una interfaz vacia a proposito: al extender JpaRepository, Spring genera
 * la implementacion solo y ya vienen hechos findAll(), findById(), save(),
 * deleteById(), existsById() y count().
 *
 * Los genericos son <Entidad, TipoDelId>.
 */
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    boolean existsByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findAllByClienteId(Long clienteId);

    Optional<Cuenta> findByIdAndClienteId(Long id, Long clienteId);

    List<Cuenta> findByClienteIdAndNumeroCuentaContainingIgnoreCase(Long clienteId, String numeroCuenta);

    // SELECT * FROM cuentas WHERE UPPER(numeroCuenta) LIKE UPPER('%?%')
    List<Cuenta> findByNumeroCuentaContainingIgnoreCase(String numeroCuenta);
}
