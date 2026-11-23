package ar.edu.utn.diplomatura.productservice.exception;

public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException(String email) {
        super("Ya existe un usuario registrado con el email " + email);
    }
}