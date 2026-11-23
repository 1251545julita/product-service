package ar.edu.utn.diplomatura.productservice.exception;

public class CamposInvalidosException extends RuntimeException{

    public CamposInvalidosException(String campo, String tipoCuenta){
        super("El campo " + campo + " sólo aplica a una " + tipoCuenta);
    }
}
