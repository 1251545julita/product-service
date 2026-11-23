package ar.edu.utn.diplomatura.productservice.exception;

public class ModoEliminacionInvalidoException extends RuntimeException {

    public ModoEliminacionInvalidoException(String modo) {
        super("Modo de eliminación inválido: " + modo + ". Los valores permitidos son 'logico' o 'fisico'.");
    }
}
