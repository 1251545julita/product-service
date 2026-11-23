package ar.edu.utn.diplomatura.productservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Lo que este servicio necesita saber de un cliente.
 *
 * Tiene la forma del JSON que devuelve el servicio de clientes, pero solo con los
 * campos que usamos aca: el cuit y el email que vienen en ese JSON se ignoran.
 * No es una entidad (no lleva @Entity): este servicio no tiene base de datos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteDTO(

        String id,
        String nombre,
        String tipoDocumento,
        String correoElectronico,
        boolean activo,
        String sucursal
) {
}
