package ar.edu.utn.diplomatura.productservice.exception;

public class UsuarioNoEncontradoException extends RuntimeException{

    public UsuarioNoEncontradoException(Long id){
        super("No existe el usuario id " + id);
    }
}
