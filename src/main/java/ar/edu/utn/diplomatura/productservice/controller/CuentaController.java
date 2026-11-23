package ar.edu.utn.diplomatura.productservice.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

//import ar.edu.utn.diplomatura.productservice.dto.ClienteDTO;
import ar.edu.utn.diplomatura.productservice.dto.*;
import ar.edu.utn.diplomatura.productservice.exception.*;
//import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.utn.diplomatura.productservice.entity.Cuenta;
import ar.edu.utn.diplomatura.productservice.service.CuentaService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Puerta de entrada HTTP. Recibe la peticion, llama al service y devuelve el
 * resultado. No lleva reglas de negocio.
 *
 * @RestController convierte a JSON lo que devuelven los metodos.
 * @RequestMapping("/cuentas") es el prefijo comun de todas las rutas.
 */
@RestController
@RequestMapping("/cuentas")
@Tag(name = "Cuentas", description = "Alta, baja, consulta y operaciones sobre las cuentas del banco.")
public class CuentaController {

    private final CuentaService cuentaService;
    //private final ClientesClient clientesClient;

    public CuentaController(CuentaService cuentaService) {
        //this.clientesClient = clientesClient;
        this.cuentaService = cuentaService;
    }

    /**
     * GET /cuentas -> todos
     * GET /cuentas?numeroCuenta=xxx -> filtra por número de cuenta
     *
     * @RequestParam es lo que va despues del "?" en la URL.
     */
    @Operation(summary = "Lista todos las cuentas",
            description = "Devuelve la lista completa. Con el parametro 'numeroCuenta' filtra por "
                    + "coincidencia parcial, sin distinguir mayusculas.")
    @ApiResponse(responseCode = "200", description = "Lista de cuentas (puede venir vacia)")
    @GetMapping
    public List<Cuenta> listar(@Parameter(description = "Lista por el número de cuenta a buscar", example = "123456789012")
                                                  @RequestParam(required = false) String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.isBlank()) {
            return cuentaService.listar();
        }
        return cuentaService.buscarPorNumeroCuenta(numeroCuenta);
    }

    /**
     * GET /cuentas/clientes/{clienteId}
     * Lista todas las cuentas de un cliente puntual.
     * GET /cuentas/clientes/{clienteId}?numeroCuenta=xxx
     * Filtra las cuentas por número.
     */
    @Operation(summary = "Lista las cuentas de un cliente",
            description = "Devuelve todas las cuentas asociadas al cliente. "
                    + "Con el parámetro 'numeroCuenta' filtra por coincidencia parcial, sin distinguir mayúsculas.")
    @ApiResponse(responseCode = "200", description = "Lista de cuentas (puede venir vacía)")
    @GetMapping("/clientes/{clienteId}")
    public List<CuentaDTO> listarTodos(@Parameter(description = "Identificador del cliente", example = "1")
                                       @PathVariable Long clienteId,
                                       @Parameter(description = "Número de cuenta a buscar", example = "123456789012")
                                       @RequestParam(required = false) String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.isBlank()) {
            return cuentaService.listarTodos(clienteId);
        }
        return cuentaService.buscarPorNumeroCuenta(clienteId, numeroCuenta);
    }


    /**
     * GET /cuentas/clientes/{clienteId}/{id}
     * Obtiene una cuenta específica.
     * @PathVariable toma el "1" de la URL.
     */
    @Operation(summary = "Obtener una cuenta por su id",
            description = "Devuelve los datos de la cuenta que coincide con el id indicado. "
                    + "Si no existe una cuenta con ese id, responde 404.")
    @ApiResponse(responseCode = "200", description = "Cuenta encontrada")
    @ApiResponse(responseCode = "404", description = "La cuenta no existe o no pertenece al cliente buscado.")
    @GetMapping("/clientes/{clienteId}/{id}")
    public ResponseEntity<Cuenta> buscarPorId(@Parameter(description = "Identificador de la cuenta", example = "1")
                                              @PathVariable Long id,
                                              @Parameter(description = "Identificador del cliente", example = "1")
                                              @PathVariable Long clienteId) {
        return ResponseEntity.ok(cuentaService.obtenerOFallar(id, clienteId));
    }

    /**
     * POST /cuentas
     *
     * @RequestBody convierte el JSON del cuerpo en un objeto Cuenta.
     */
    @Operation(summary = "Crear una cuenta",
            description = "Abre una Caja de Ahorro o una Cuenta Corriente para un usuario existente. "
                    + "El número de cuenta lo genera el sistema.")
    @ApiResponse(responseCode = "201", description = "Cuenta creada.")
    @ApiResponse(responseCode = "400", description = "Tipo de cuenta inválido o campos que no corresponden al tipo.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
//        try {
            CuentaResponse creada = cuentaService.crear(request);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}").buildAndExpand(creada.id()).toUri();
            return ResponseEntity.created(location).body(creada);
//        } catch (TipoCuentaInvalidosException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        } catch (UsuarioNoEncontradoException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
//        } catch (CamposInvalidosException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
    }

    /** PUT /cuentas/clientes/1/2
     *
     * @PathVariable envia por path param el id de la cuenta existente.
     * @PathVariable envia tambien el cliente supuesto de esa cuenta.
     * @RequestBody convierte el JSON del cuerpo en un objeto CuentaUpdateRequest para actualizar datos de uan cuenta existente.
     */
    @Operation(summary = "Actualiza una cuenta existente",
            description = "Actualiza el porcentajeTasaInteresAnual de una Caja de Ahorro o el limiteDescuerto de una Cuenta Corriente.")
    @ApiResponse(responseCode = "200", description = "Cuenta actualizada.")
    @ApiResponse(responseCode = "404", description = "Cuenta indicada no encontrada.")
    @ApiResponse(responseCode = "400", description = "Campos que no corresponden al tipo de cuenta indicado.")
    @PutMapping("/clientes/{clienteId}/{id}")
    public ResponseEntity<CuentaResponse> actualizar(@Parameter(description = "Identificador de la cuenta", example = "10")
                                                     @PathVariable Long id,
                                                     @Parameter(description = "Identificador del cliente", example = "1")
                                                     @PathVariable Long clienteId,
                                                     @Valid @RequestBody CuentaUpdateRequest datosNuevos) {
//        try {
                return ResponseEntity.ok(cuentaService.actualizar(id, clienteId, datosNuevos));
//        } catch (CuentaNoEncontradaException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
//        } catch (CamposInvalidosException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
    }


    /** DELETE /cuentas/clientes/1/1
     *
     * @PathVariable envia por path param el id de la cuenta existente.
     * @RequestParam envia por path param el modo de cómo eliminar esa cuenta existente. Puede ser lógico o físico.
     */
    @Operation(summary = "Borra logica o fisicamente una cuenta existente",
            description = "Deshabilita una cuenta existente en modo logico. En modo fisico, borra fisicamente una cuenta existente.")
    @ApiResponse(responseCode = "204", description = "Cuenta deshabilitada.")
    @ApiResponse(responseCode = "404", description = "Cuenta indicada no encontrada o ya se borró.")
    @ApiResponse(responseCode = "400", description = "El modo debe ser 'logico' o 'fisico' en los parametros.")
    @DeleteMapping("/clientes/{clienteId}/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @PathVariable Long clienteId, @RequestParam(defaultValue = "logico")String modo) {

        if (!modo.equals("fisico") && !modo.equals("logico")){
            throw new ModoEliminacionInvalidoException(modo); // 400
        }

        boolean seDioDeBaja = modo.equals("logico") ? cuentaService.desactivar(id, clienteId) : cuentaService.eliminar(id, clienteId);

        if (!seDioDeBaja){
            throw new CuentaNoEncontradaException(id, clienteId); // 404
        }
        return ResponseEntity.noContent().build(); // 204
    }

    /** POST /cuentas/clientes/6/1/acreditaciones?saldo=500
     *
     * @PathVariable envia por path param el id de la cuenta existente.
     * @PathVariable envia por path param el id del cliente de la cuenta.
     * @RequestParam envia por path param el saldo a acreditar en la cuenta.
     */
    @Operation(summary = "Ingresar dinero en una cuenta")
    @ApiResponse(responseCode = "200", description = "Acreditacion realizada; devuelve la cuenta con el saldo actualizado.")
    @ApiResponse(responseCode = "400", description = "Monto inválido.")
    @ApiResponse(responseCode = "404", description = "La cuenta no existe.")
    @PostMapping("/clientes/{clienteId}/{id}/acreditaciones")
    public ResponseEntity<?> acreditar(@PathVariable Long id, @PathVariable Long clienteId, @RequestParam BigDecimal saldo) {
      //  try {
            return ResponseEntity.ok(cuentaService.acreditarSaldo(id, clienteId, saldo));
//        } catch (SaldoInvalidoException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));            // 400
//        } catch (CuentaNoEncontradaException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage())); // 404
//        }
    }

    /** POST /cuentas/1/1/retiros?saldo=1000
     *
     * @PathVariable envia por path param el id de la cuenta existente.
     * @RequestParam envia por path param el saldo a retirar de la cuenta.
     */
    @Operation(summary = "Retirar dinero de una cuenta",
            description = "Una cuenta corriente puede quedar en descubierto hasta su límite.")
    @ApiResponse(responseCode = "200", description = "Retiro realizado; devuelve la cuenta con el saldo actualizado.")
    @ApiResponse(responseCode = "404", description = "La cuenta no existe.")
    @ApiResponse(responseCode = "409", description = "Saldo insuficiente.")
    @PostMapping("/clientes/{clienteId}/{id}/retiros")
    public ResponseEntity<?> retirar(@PathVariable Long id, @PathVariable Long clienteId, @RequestParam BigDecimal saldo) {
//        try {
            return ResponseEntity.ok(cuentaService.retirarSaldo(id, clienteId, saldo));
//        } catch (SaldoInvalidoException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        } catch (CuentaNoEncontradaException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
//        } catch (SaldoInsuficienteException e) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
//        }
    }

    /**
     * GET /cuentas/clientes/1/1/saldo
     * Consulta el saldo de una cuenta.
     */
    @Operation(summary = "Consulta el saldo de una cuenta de un cliente.",
            description = "Devuelve el identificador, número de cuenta, saldo disponible y moneda.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Saldo consultado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Cuenta inexistente o no pertenece al cliente")})
    @GetMapping("/clientes/{clienteId}/{id}/saldo")
    public ResponseEntity<SaldoDTO> consultarSaldo(@Parameter(description = "Identificador del cliente", example = "1")
                                                   @PathVariable Long clienteId,
                                                   @Parameter(description = "Identificador de la cuenta", example = "10")
                                                   @PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.consultarSaldo(clienteId, id));
    }

    /** URLs para el manejo de los clientes bancarios **/

    /** GET /cuentas/clientes: le pide la lista al servicio de clientes bancarios y la devuelve tal cual. */
//    @GetMapping("/clientes")
//    public List<ClienteDTO> listarClientes(@RequestParam(required = false) String apellido) {
//        if (apellido == null || apellido.isBlank()) {
//            return clientesClient.listar();
//        }
//        return clientesClient.buscarPorApellido(apellido);
//    }
//
//    /** GET /cuentas/clientes/1 */
//    @GetMapping("/clientes/{id}")
//    public ResponseEntity<ClienteDTO> obtenerCliente(@PathVariable Long id) {
//        try {
//            return ResponseEntity.ok(clientesClient.obtener(id));
//        } catch (FeignException.NotFound e) {
//            // El servicio de clientes contesto 404 y Feign lo convirtio en esta excepcion.
//            return ResponseEntity.notFound().build();
//        }
//    }

    /** POST /cuentas  con { "clienteId": 1, "importe": 15000, "concepto": "Sueldo octubre" } */
//    @PostMapping
//    public ResponseEntity<PagoResponse> pagar(@RequestBody PagoRequest pedido) {
//        try {
//            PagoResponse respuesta = pagosService.registrar(pedido);
//            if (respuesta.estado().equals("RECHAZADO")) {
//                return ResponseEntity.unprocessableEntity().body(respuesta); // 422
//            }
//            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta); // 201
//        } catch (FeignException.NotFound e) {
//            return ResponseEntity.notFound().build(); // 404
//        }
//    }
}
