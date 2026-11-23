package ar.edu.utn.diplomatura.productservice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import ar.edu.utn.diplomatura.productservice.dto.*;
import ar.edu.utn.diplomatura.productservice.entity.CajaAhorro;
import ar.edu.utn.diplomatura.productservice.entity.CuentaCorriente;
import ar.edu.utn.diplomatura.productservice.entity.Usuario;
import ar.edu.utn.diplomatura.productservice.exception.*;
import ar.edu.utn.diplomatura.productservice.mapper.CuentaMapper;
import org.springframework.stereotype.Service;

import ar.edu.utn.diplomatura.productservice.entity.Cuenta;
import ar.edu.utn.diplomatura.productservice.repository.CuentaRepository;

import static ar.edu.utn.diplomatura.productservice.Constants.*;

/**
 * Reglas del negocio. Está entre el controlador y el repositorio.
 *
 * Cuando algo no se puede hacer (el cliente no existe, el importe es invalido)
 * estos metodos devuelven null.
 */
@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    //private final UsuarioService usuarioService;
    private final CuentaMapper cuentaMapper;

    /** Inyeccion por constructor: Spring busca esos dos beans en el contenedor y los pasa como argumentos. **/
    public CuentaService(CuentaRepository cuentaRepository,  CuentaMapper cuentaMapper) { //UsuarioService usuarioService) {
        this.cuentaRepository = cuentaRepository;
        this.cuentaMapper = cuentaMapper;
        //this.usuarioService = usuarioService;
    }

    public List<Cuenta> listar() {
        return cuentaRepository.findAll();
    }

    public List<CuentaDTO> listarTodos(Long clienteId) {
        return cuentaRepository.findAllByClienteId(clienteId)
                .stream()
                .map(cuentaMapper::toDTO)
                .toList();
    }

    public List<Cuenta> buscarPorNumeroCuenta(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuentaContainingIgnoreCase(numeroCuenta);
    }

    // Evitar que se busque si existe la cuenta, independientemente de quién sea el titular.
    public Cuenta obtenerOFallar(Long id, Long clienteId) {
        return cuentaRepository.findByIdAndClienteId(id, clienteId)
                .orElseThrow(() -> new CuentaNoEncontradaException(id, clienteId));
    }

    public List<CuentaDTO> buscarPorNumeroCuenta(Long clienteId, String numeroCuenta) {
        return cuentaRepository
                .findByClienteIdAndNumeroCuentaContainingIgnoreCase(clienteId, numeroCuenta)
                .stream()
                .map(cuentaMapper::toDTO)
                .toList();
    }

    /** Crea una cuenta. */
    public CuentaResponse crear(CuentaRequest request) {

        //Usuario unUsuario = usuarioService.obtenerOFallar(request.usuarioId());
        Long clienteId = request.clienteId();
        String tipoCuenta = request.tipo();
        BigDecimal limiteDescubierto = request.limiteDescubierto(); //Objects.requireNonNullElse(request.limiteDescubierto(), BigDecimal.ZERO);
        BigDecimal porcentajeTasaInteresAnual = request.porcentajeTasaInteresAnual(); //Objects.requireNonNullElse(request.porcentajeTasaInteresAnual(), BigDecimal.ZERO);

        validarCamposSegunTipoCuenta(tipoCuenta, limiteDescubierto, porcentajeTasaInteresAnual);

        String numeroCuenta = String.format("%012d", ThreadLocalRandom.current().nextLong(1_000_000_000_000L));
        BigDecimal saldoInicial = request.saldoInicial();
        String moneda = request.moneda() != null ? request.moneda() : TIPO_MONEDA_PESOS;

        Cuenta unaCuenta = switch (tipoCuenta) {
            case TIPO_CUENTA_CAJA_AHORRO ->
                    new CajaAhorro(numeroCuenta, saldoInicial, moneda, clienteId, porcentajeTasaInteresAnual);
            case TIPO_CUENTA_CUENTA_CORRIENTE ->
                    new CuentaCorriente(numeroCuenta, saldoInicial, moneda, clienteId, limiteDescubierto);
            default -> throw new TipoCuentaInvalidosException(tipoCuenta);
        };

        return CuentaResponse.desdeEntidad(cuentaRepository.save(unaCuenta));
    }

    private void validarCamposSegunTipoCuenta(String tipo, BigDecimal limiteDescubierto, BigDecimal porcentajeTasaInteresAnual) {
        if (TIPO_CUENTA_CAJA_AHORRO.equals(tipo) && limiteDescubierto != null) {
            throw new CamposInvalidosException(CUENTA_CORRIENTE_CAMPO_EXCLUSIVO, TIPO_CUENTA_CUENTA_CORRIENTE);
        }
        if (TIPO_CUENTA_CUENTA_CORRIENTE.equals(tipo) && porcentajeTasaInteresAnual != null) {
            throw new CamposInvalidosException(CAJA_AHORRO_CAMPO_EXCLUSIVO, TIPO_CUENTA_CAJA_AHORRO);
        }
    }

    /** Actualiza porcentajeTasaInteresAnual(Solo para CA) o limiteDescubierto(Solo para CC). Devuelve null si el id no existe. */
    public CuentaResponse actualizar(Long id, Long clienteId, CuentaUpdateRequest datosNuevos) {
        Cuenta cuentaExistente = obtenerOFallar(id, clienteId);

        if (cuentaExistente instanceof CuentaCorriente corriente) {
            if (datosNuevos.porcentajeTasaInteresAnual() != null) {
                throw new CamposInvalidosException(CAJA_AHORRO_CAMPO_EXCLUSIVO, TIPO_CUENTA_CAJA_AHORRO);
            }
            if (datosNuevos.limiteDescubierto() != null) {
                corriente.setLimiteDescubierto(datosNuevos.limiteDescubierto());
            }
        } else if (cuentaExistente instanceof CajaAhorro caja) {
            if (datosNuevos.limiteDescubierto() != null) {
                throw new CamposInvalidosException(CUENTA_CORRIENTE_CAMPO_EXCLUSIVO, TIPO_CUENTA_CUENTA_CORRIENTE);
            }
            if (datosNuevos.porcentajeTasaInteresAnual() != null) {
                caja.setPorcentajeTasaInteresAnual(datosNuevos.porcentajeTasaInteresAnual());
            }
        }

        return CuentaResponse.desdeEntidad(cuentaRepository.save(cuentaExistente));
    }

    /** Elimina una cuenta. Si el id no existe, no hace nada. */
    public boolean eliminar(Long id, Long clienteId) {
        Cuenta unaCuenta = cuentaRepository
                .findByIdAndClienteId(id, clienteId)
                .orElse(null);

        if (unaCuenta == null) {
            return false;
        }
        cuentaRepository.deleteById((id));
        return true;
    }

    public boolean desactivar(Long id, Long clienteId){
        Cuenta unaCuenta = cuentaRepository
                .findByIdAndClienteId(id, clienteId)
                .orElse(null);

        if (unaCuenta == null) {
            return false;
        }
        unaCuenta.setHabilitada(false);
        cuentaRepository.save(unaCuenta);
        return true;
    }

    /** Suma un monto al saldo de una cuenta existente de un cliente puntual.
     * Devuelve excepción si la cuenta no existe o si el monto no es positivo. */
    public CuentaDTO acreditarSaldo(Long id, Long clienteId, BigDecimal unSaldo) {
        Cuenta cuentaExistente = obtenerOFallar(id, clienteId);
        cuentaExistente.ingresarSaldo(unSaldo);
        return cuentaMapper.toDTO(
                cuentaRepository.save(cuentaExistente)
        );
    }

    /** Resta un monto al saldo de una cuenta existente de un cliente puntual.
     * Devuelve una excepcion si el saldo es invalido o insuficiente. */
    public CuentaDTO retirarSaldo(Long id, Long clienteId, BigDecimal unSaldo) {
        Cuenta cuentaExistente = obtenerOFallar(id, clienteId);
        cuentaExistente.retirarSaldo(unSaldo);
        return cuentaMapper.toDTO(
                cuentaRepository.save(cuentaExistente)
        );
    }

    public SaldoDTO consultarSaldo(Long id, Long clienteId) {
        Cuenta cuenta = obtenerOFallar(id, clienteId);
        return new SaldoDTO(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo(),
                cuenta.getMoneda()
        );
    }
}
