package ar.edu.utn.diplomatura.productservice.exception;

public class CuentaNoEncontradaException extends RuntimeException{

    public CuentaNoEncontradaException(Long id, Long clienteId) {
        super("La cuenta " + id + " del cliente " + clienteId + " no existe o ha sido borrada.");
    }
}
