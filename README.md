# Application — Product Service — Microservicio A

** Diplomatura en Desarrollo de Software FinTech: IA y Microservicios — UTN / Codeki**
** Student: Julita, Jesica R. **

---

## 1. Qué se necesita antes de empezar

- **JDK 21** instalado.
- **MySQL** instalado.
- **IntelliJ IDEA** (Community alcanza).
- **product-service (product-service)** corriendo en `http://localhost:8082`, porque este microservicio le delega la gestión de las cuentas.

---

## 2. Cómo abrirlo en IntelliJ IDEA

1. Luego de clonar el repositorio `https://github.com/1251545julita/product-service` abrir IntelliJ.
2. **File > Open** → elegir la carpeta `product-service`, la que contiene el `pom.xml`.
   No abrir el `pom.xml` directamente.
3. Cuando pregunte, aceptar **Trust Project**.
4. Verificar el JDK en `File > Project Structure > Project > SDK`: tiene que apuntar a un **JDK 21**. Si no aparece ninguno, elegir `Add SDK > Download JDK` y descargar una distribución 21.
5. Si no ve el panel de Maven: `View > Tool Windows > Maven`, y tocar el botón de recargar.
6. En el archivo `application.yml` agregar la password de la Base de Datos MySql a ejecutar localmente, en datasource.password así queda en la configuración local.
7. En el mismo `application.yml` verificar la URL de product-service (`http://localhost:8082`) que usa el cliente Feign.

**Si el `pom.xml` aparece en gris o no hay carpeta `External Libraries`:** clic derecho
sobre `pom.xml` → **Add as Maven Project**.

---

## 3. Cómo ejecutarlo

> **Orden:** primero levantar **product-service** (puerto 8082) y después **customer-service** (puerto 8081).

### Desde IntelliJ

Abrí `src/main/java/ar/edu/utn/diplomatura/customerservice/ProductServiceApplication.java` y hacé clic en
la flecha verde ▶ que está al lado de `public static void main`.
Se ejecuta la aplicación y queda a la escucha de peticiones y la salida aparece en la consola de abajo.

### Desde la terminal, con Maven desde IntelliJ

Compilar el proyecto:
```
mvn clean compile
```

Ejecutar los tests:
```
mvn test
```

Ejecutar la aplicación Spring Boot:
```
mvn spring-boot:run
```

Generar el paquete ejecutable:
```
mvn clean package
```

Ejecutar el JAR generado:
```
java -jar target/customer-service-1.0.0.jar
```


### Desde la terminal, utilizando Maven Wrapper

El Maven Wrapper permite ejecutar Maven sin necesidad de tenerlo instalado globalmente.
Utiliza la versión de Maven definida por el proyecto. En Linux, macOS o Git Bash:

```bash
# Compilar
./mvnw clean compile

# Ejecutar los tests
./mvnw test

# Levantar la aplicación
./mvnw spring-boot:run

# Generar el JAR ejecutable
./mvnw clean package

# Ejecutar el JAR
java -jar target/customer-service-1.0.0.jar
```

En PowerShell (Windows), esas líneas son:

```powershell
# Compilar
.\mvnw.cmd clean compile

# Ejecutar los tests
.\mvnw.cmd test

# Levantar la aplicación
.\mvnw.cmd spring-boot:run

# Generar el JAR ejecutable
.\mvnw.cmd clean package

# Ejecutar el JAR
java -jar target/customer-service-1.0.0.jar
```

---

## 4. Cómo probar los endpoints

Con la aplicación levantada, la API queda a la escucha en `http://localhost:8081/clientes`.
Las operaciones sobre cuentas (`/clientes/{id}/cuentas`) las resuelve **product-service** (`http://localhost:8082/cuentas`)
a través de un cliente Feign, por eso tiene que estar corriendo.

**Para probar que funciona**, abrí en el navegador:

http://localhost:8081/clientes

Tenés que ver la lista de clientes en formato JSON.

La documentación interactiva de la API (Swagger UI) queda disponible en:

http://localhost:8081/swagger-ui/index.html

### Tipos de cliente

El tipo de cliente se determina por el campo **`tipoDocumento`** del body, no hace falta enviar ningún otro campo de tipo:

| `tipoDocumento` | Tipo de cliente | Campos propios |
|-----------------|-----------------|----------------|
| `dni` | Persona Física | `apellido`, `fechaDeNacimiento`, `profesion`, `ingresosDeclarados`, `estadoCivil`, `nacionalidad`, `situacionLaboral` |
| `cuit` | Persona Jurídica | `razonSocial`, `rubro`, `representanteLegal`, `condicionIva`, `fechaConstitucion`, `facturacionAnual` |

Los campos comunes a todos los clientes son: `nombre`, `tipoDocumento`, `numeroDocumento`, `domicilio`, `telefono`,
`correoElectronico` y `sucursal`. La fecha de alta y el estado (`activo`) los completa el sistema.
Las fechas se envían en formato `yyyy-MM-dd` (por ejemplo `1990-05-10`).

### Endpoints disponibles

| Método | Ruta | Qué hace |
|--------|------|----------|
| `GET` | `/clientes` | Lista todos los clientes |
| `GET` | `/clientes?numeroDocumento=N` | Filtra por número de documento (coincidencia parcial) |
| `GET` | `/clientes/{id}` | Trae un cliente específico |
| `POST` | `/clientes` | Crea un cliente nuevo (Persona Física o Jurídica) |
| `PUT` | `/clientes/{id}` | Actualiza los datos de un cliente (el tipo no se puede cambiar) |
| `DELETE` | `/clientes/{id}?modo=logico` | Da de baja un cliente de forma lógica (por defecto) |
| `DELETE` | `/clientes/{id}?modo=fisico` | Elimina un cliente físicamente de la Base de Datos |
| `GET` | `/clientes/{id}/cuentas` | Lista las cuentas de un cliente |
| `GET` | `/clientes/{id}/cuentas?numeroCuenta=N` | Filtra las cuentas del cliente por número de cuenta |
| `GET` | `/clientes/{id}/cuentas/{cuentaId}` | Trae una cuenta específica de un cliente |
| `POST` | `/clientes/cuentas` | Crea una cuenta (Caja de Ahorro o Cuenta Corriente) para un cliente existente |
| `PUT` | `/clientes/{id}/cuentas/{cuentaId}` | Actualiza datos de la cuenta (ej. límite de descubierto) |

Cuando algo no se puede hacer (el id no existe, el DNI o CUIT es inválido, el email ya está registrado, los datos enviados no corresponden),
la respuesta llega con un código de error específico:

| Código | Cuándo |
|--------|--------|
| **400 Bad Request** | Documento inválido, `tipoDocumento` ausente o desconocido, modo de baja inválido, cambio de tipo de cliente, campos que no corresponden al tipo de cuenta |
| **404 Not Found** | Cliente o cuenta no encontrados |
| **409 Conflict** | El email ya está registrado |
| **503 Service Unavailable** | product-service no está disponible |


### Cómo se prueban: con `curl`, desde **Git Bash**

**Abrí Git Bash** (viene con Git; en Windows, botón derecho en cualquier carpeta →
*Git Bash Here*, o buscalo en el menú Inicio). Dejá las dos aplicaciones corriendo en IntelliJ y
copiá y pegá los comandos de abajo, uno por vez.

> **Tiene que ser Git Bash, no PowerShell ni CMD.** En PowerShell, `curl` es en realidad
> otro comando disfrazado y esta sintaxis no le funciona.

> **Desde el navegador solo se puede probar `GET`.** La barra de direcciones no sabe hacer
> `POST`, `PUT` ni `DELETE`. Por eso usamos `curl`.

Cada comando va en **una sola línea**: copialo entero y pegalo.

### Sección Clientes

**Listar todos los clientes** (devuelve 200)
```bash

curl -i http://localhost:8081/clientes

```

**Traer el cliente con id 1** (devuelve 200)
```bash

curl -i http://localhost:8081/clientes/1

```

**Traer un id que no existe** (devuelve 404)
```bash

curl -i http://localhost:8081/clientes/999

```

**Filtrar por número de documento**
```bash

curl -i "http://localhost:8081/clientes?numeroDocumento=30123456"

```

**Crear una Persona Física válida** (devuelve 201 Created)
```bash

curl -i -X POST http://localhost:8081/clientes -H "Content-Type: application/json" -d '{"nombre": "Irene","apellido": "Rodriguez","tipoDocumento": "dni","numeroDocumento": "30123456","domicilio": "Av. Corrientes 987, CABA","telefono": "11-8589-5555","correoElectronico": "irene@gmail.com","sucursal": "Sucursal 110 - Centro Corporativo","fechaDeNacimiento": "1990-05-10","profesion": "Contadora","ingresosDeclarados": 1500000.50,"estadoCivil": "Soltera","nacionalidad": "Argentina","situacionLaboral": "Relación de dependencia"}'

```

**Crear una Persona Jurídica válida** (devuelve 201 Created)
```bash

curl -i -X POST http://localhost:8081/clientes -H "Content-Type: application/json" -d '{"nombre": "Tecno SA","razonSocial": "Tecno Sociedad Anónima","tipoDocumento": "cuit","numeroDocumento": "30712345678","domicilio": "Av. Libertador 1500, CABA","telefono": "11-4000-1234","correoElectronico": "contacto@tecno.com","sucursal": "Sucursal 110 - Centro Corporativo","rubro": "Tecnología","representanteLegal": "Ana Torres","condicionIva": "Responsable Inscripto","fechaConstitucion": "2015-03-20","facturacionAnual": 250000000.00}'

```

**Intentar crear un cliente con email repetido** (devuelve 409 Conflict)
```bash

curl -i -X POST http://localhost:8081/clientes -H "Content-Type: application/json" -d '{"nombre": "Irene","apellido": "Rodriguez","tipoDocumento": "dni","numeroDocumento": "30123457","domicilio": "Av. Corrientes 987, CABA","telefono": "11-8589-5555","correoElectronico": "irene@gmail.com","sucursal": "Sucursal 110 - Centro Corporativo"}'

```

**Intentar crear un cliente sin `tipoDocumento`** (devuelve 400 Bad Request)
```bash

curl -i -X POST http://localhost:8081/clientes -H "Content-Type: application/json" -d '{"nombre": "Irene","numeroDocumento": "30123456","correoElectronico": "otra@gmail.com"}'

```

**Actualizar el cliente 1** (devuelve 200). El body debe incluir `tipoDocumento`, igual al del cliente guardado.
```bash

curl -i -X PUT http://localhost:8081/clientes/1 -H "Content-Type: application/json" -d '{"nombre": "Irene María","apellido": "Rodriguez","tipoDocumento": "dni","numeroDocumento": "30123456","domicilio": "Av. Corrientes 987, CABA","telefono": "11-8589-5555","correoElectronico": "irene@gmail.com","sucursal": "Sucursal 110 - Centro Corporativo","activo": true}'

```

**Eliminar el cliente 7 lógicamente** (devuelve 204 No Content)
```bash

curl -i -X DELETE "http://localhost:8081/clientes/7?modo=logico"

```

**Eliminar el cliente 8 físicamente** (devuelve 204 No Content)
```bash

curl -i -X DELETE "http://localhost:8081/clientes/8?modo=fisico"

```

**Intentar eliminar con un modo inválido** (devuelve 400 Bad Request)
```bash

curl -i -X DELETE "http://localhost:8081/clientes/7?modo=otro"

```

---
### Sección Cuentas de un cliente
---

**Listar las cuentas del cliente 1** (devuelve 200)
```bash

curl -i http://localhost:8081/clientes/1/cuentas

```

**Filtrar las cuentas del cliente 1 por número de cuenta** (devuelve 200)
```bash

curl -i "http://localhost:8081/clientes/1/cuentas?numeroCuenta=2028"

```

**Traer la cuenta 11 del cliente 1** (devuelve 200)
```bash

curl -i http://localhost:8081/clientes/1/cuentas/11

```

**Traer una cuenta que no existe de un cliente que existe** (devuelve 404)
```bash

curl -i http://localhost:8081/clientes/1/cuentas/999

```

**Listar las cuentas de un cliente que no existe** (devuelve 404)
```bash

curl -i http://localhost:8081/clientes/999/cuentas

```

**Crear una Caja de Ahorro para el cliente 1** (devuelve 201 Created)
```bash

curl -i -X POST http://localhost:8081/clientes/cuentas -H "Content-Type: application/json" -d '{"tipo": "Caja de Ahorro","moneda": "USD","clienteId": 1,"saldoInicial": 0.00,"porcentajeTasaInteresAnual": 16.00}'

```

**Crear una Cuenta Corriente para el cliente 1** (devuelve 201 Created)
```bash

curl -i -X POST http://localhost:8081/clientes/cuentas -H "Content-Type: application/json" -d '{"tipo": "Cuenta Corriente","moneda": "USD","clienteId": 1,"saldoInicial": 0.00,"limiteDescubierto": 100000.00}'

```

**Intentar crear una Caja de Ahorro inválida (con límite al descubierto)** (devuelve 400 Bad Request)
```bash

curl -i -X POST http://localhost:8081/clientes/cuentas -H "Content-Type: application/json" -d '{"tipo": "Caja de Ahorro","moneda": "USD","clienteId": 1,"saldoInicial": 0.00,"limiteDescubierto": 1000000.00}'

```

**Actualizar el límite de descubierto de una Cuenta Corriente** (devuelve 200)
```bash

curl -i -X PUT http://localhost:8081/clientes/1/cuentas/11 -H "Content-Type: application/json" -d '{"limiteDescubierto": 50000.00}'

```

> **Qué significa el `-i`:** muestra también los encabezados de la respuesta, incluido el
> código de estado (`HTTP/1.1 200`, `201`, `400`, etc.). Sin `-i` verías solo el JSON del cuerpo.

---

## 5. Estructura del proyecto

```
customer-service/
├── pom.xml
├── README.md
├── src/main/resources/application.yml    # Configuración de Base de Datos MySql y URL de product-service
├── src/test/*                            # Contiene tests de lógica de negocio de componentes y entidades
└── src/main/java/ar/edu/utn/diplomatura/customerservice/
    ├── CustomerServiceApplication.java   # Clase principal que levanta la aplicación Spring Boot
    ├── Constants.java           # Definición de constantes que se utilizan en el sistema (tipos de documento).
    ├── client/                  # CuentasClient: cliente Feign que consume la API de product-service
    ├── controller/              # Controller de SpringBoot para gestionar API de Clientes y de sus Cuentas
    ├── dto/                     # dtos para request y response de solicitudes de API de Cuentas
    ├── exception/               # Definición de las excepciones del sistema y GlobalExceptionHandler
    ├── repository/              # Contiene repositorio para Clientes
    ├── service/                 # Service de SpringBoot para clientes, que funciona con el Controller para cada endpoint.
    └── entity/
        └── Cliente.java         # clase abstracta, atributos comunes, constructores, getters/setters
        ├── PersonaFisica.java   # subclase (DNI), atributos, constructores, getters/setters, métodos especificos
        ├── PersonaJuridica.java # subclase (CUIT), atributos, constructores, getters/setters, métodos especificos
        └── Preferencial.java    # subclase, atributos, constructores, getters/setters, métodos especificos

```

---

## 6. Arquitectura

```
Postman / curl ──> customer-service (:8081 /clientes) ──Feign──> product-service (:8082 /cuentas)
                   dueño de los clientes                          dueño de las cuentas
```

- **customer-service** administra los clientes y valida que el cliente exista antes de operar sobre sus cuentas.
- **product-service** crea y administra las cuentas (Caja de Ahorro y Cuenta Corriente). Solo guarda el `clienteId` de cada cuenta.
- Los errores de product-service se traducen a respuestas HTTP propias en `GlobalExceptionHandler`.

---

## 7. Problemas frecuentes

**Los acentos se ven como `?` o `�` en la consola.** Pasa en `cmd` y en PowerShell, porque la consola de Windows usa un codepage viejo en lugar de UTF-8. Es un problema de la terminal, no del código. Corré `chcp 65001` antes de ejecutar, o usá directamente la consola de IntelliJ, que muestra bien los acentos.

**`error: invalid source release: 21`.** El JDK configurado es anterior a 21. Revisá el punto 2.

**El botón ▶ no aparece al lado del `main`.** IntelliJ todavía no terminó de indexar, o no reconoció la carpeta como proyecto Maven. Esperá a que termine la barra de progreso de abajo, o recargá el proyecto desde el panel de Maven.

**`400 Bad Request` con `missing type id property 'tipoDocumento'`.** El body de `POST` o `PUT` a `/clientes` no incluye `tipoDocumento`. Es el campo que define si el cliente es Persona Física (`dni`) o Jurídica (`cuit`).

**`503 Service Unavailable` o error de conexión al operar con cuentas.** product-service no está levantada o no responde en `http://localhost:8082`. Levantala antes de usar los endpoints de cuentas.

**`404` al crear una cuenta.** Revisá que la ruta del método `crear` en `CuentasClient` coincida con la de product-service (`POST /cuentas`).

**`Port 8081 was already in use`.** Hay otra aplicación usando ese puerto. Cerrala o cambiá `server.port` en `application.yml`.