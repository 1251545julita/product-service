# Application — Product Service — Microservicio B

** Diplomatura en Desarrollo de Software FinTech: IA y Microservicios — UTN / Codeki**
** Student: Julita, Jesica R. **

---
## 1. Qué se necesita antes de empezar

- **JDK 21** instalado.
- **MySQL** instalado.
- **IntelliJ IDEA** (Community alcanza).

> Este microservicio funciona de forma independiente. **customer-service** (Microservicio A) lo consume para gestionar las cuentas de cada cliente, así que conviene levantarlo primero.

---

## 2. Cómo abrirlo en IntelliJ IDEA

1. Luego de clonar el repositorio https://github.com/1251545julita/product-service y abrir IntelliJ.
2. **File > Open** → elegir la carpeta `product-service`, la que contiene el `pom.xml`.
   No abrir el `pom.xml` directamente.
3. Cuando pregunte, aceptar **Trust Project**.
4. Verificar el JDK en `File > Project Structure > Project > SDK`: tiene que apuntar a un **JDK 21**. Si no aparece ninguno, elegir `Add SDK > Download JDK` y descargar una distribución 21.
5. Si no ve el panel de Maven: `View > Tool Windows > Maven`, y tocar el botón de recargar.
6. En el archivo `application.yml` agregar la password de la Base de Datos MySql a ejecutar localmente, en datasource.password así queda en la configuración local.

**Si el `pom.xml` aparece en gris o no hay carpeta `External Libraries`:** clic derecho
sobre `pom.xml` → **Add as Maven Project**.

---

## 3. Cómo ejecutarlo

### Desde IntelliJ

Abrí `src/main/java/ar/edu/utn/diplomatura/productservice/ProductServiceApplication.java` y hacé clic en
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
java -jar target/product-service-1.0.0.jar
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
java -jar target/product-service-1.0.0.jar
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
java -jar target/product-service-1.0.0.jar
```

---

## 4. Cómo probar los endpoints

El `Main` contiene un main para ejecutar la API de Cuentas, el mismo carga los datos iniciales de cada
tipo de cuenta e imprime por consola el inicio de espera de solicitudes de la API iniciada.

En la consola vas a ver, en este orden:

```
======================================================================
  Microservicio B - Product-Service
  App:         http://localhost:8082/cuentas
               JDBC URL: jdbc:mysql://localhost:3306/product-service   user: root
======================================================================
```

Esa última línea es la que importa: quiere decir que el servidor está andando.

**Para probar que funciona**, abrí en el navegador:

http://localhost:8082/cuentas

Tenés que ver las 6 cuentas en formato JSON.

### Tipos de cuenta

| Tipo (`tipo`) | Campo propio | Qué significa |
|---------------|--------------|---------------|
| `Caja de Ahorro` | `porcentajeTasaInteresAnual` | Tasa de interés anual de la caja de ahorro |
| `Cuenta Corriente` | `limiteDescubierto` | Monto máximo que la cuenta puede quedar en descubierto |

Cada tipo acepta solamente su campo propio: enviar `limiteDescubierto` en una Caja de Ahorro (o la tasa en una Cuenta Corriente)
devuelve **400 Bad Request**. El número de cuenta lo genera el sistema, y la fecha de apertura y el estado (`habilitada`) también.
Cada cuenta se asocia a un cliente mediante `clienteId`.

### Endpoints disponibles

| Método | Ruta | Qué hace |
|--------|------|----------|
| `GET` | `/cuentas` | Lista todas las cuentas |
| `GET` | `/cuentas?numeroCuenta=N` | Filtra por número de cuenta |
| `GET` | `/cuentas/{id}` | Trae una cuenta específica |
| `GET` | `/cuentas/clientes/{id}` | Lista las cuentas de un cliente |
| `GET` | `/cuentas/clientes/{id}?numeroCuenta=N` | Filtra las cuentas de un cliente por número de cuenta |
| `GET` | `/cuentas/clientes/{id}/{cuentaId}` | Trae una cuenta específica de un cliente |
| `POST` | `/cuentas` | Crea una cuenta nueva (Caja de Ahorro o Cuenta Corriente) |
| `PUT` | `/cuentas/clientes/{id}/{cuentaId}` | Actualiza datos de la cuenta (ej. tasa anual o límite de descubierto) |
| `DELETE` | `/cuentas/{id}?modo=logico` | Elimina una cuenta de forma lógica |
| `POST` | `/cuentas/{id}/acreditaciones?saldo=N` | Suma un importe al saldo de la cuenta |
| `POST` | `/cuentas/{id}/retiros?saldo=N` | Resta un importe al saldo de la cuenta |

Cuando algo no se puede hacer (el id no existe, los datos enviados no corresponden al tipo de cuenta),
la respuesta llega con un código de error específico como **404 Not Found**, **400 Bad Request** o **409 Conflict**.


### Cómo se prueban: con `curl`, desde **Git Bash**

**Abrí Git Bash** (viene con Git; en Windows, botón derecho en cualquier carpeta →
*Git Bash Here*, o buscalo en el menú Inicio). Dejá la aplicación corriendo en IntelliJ y
copiá y pegá los comandos de abajo, uno por vez.

> **Tiene que ser Git Bash, no PowerShell ni CMD.** En PowerShell, `curl` es en realidad
> otro comando disfrazado y esta sintaxis no le funciona.

> **Desde el navegador solo se puede probar `GET`.** La barra de direcciones no sabe hacer
> `POST`, `PUT` ni `DELETE`. Por eso usamos `curl`.

Cada comando va en **una sola línea**: copialo entero y pegalo.

**Listar todas las cuentas** (devuelve 200)
```bash

curl -i http://localhost:8082/cuentas

```

**Traer la cuenta con id 1** (devuelve 200)
```bash

curl -i http://localhost:8082/cuentas/1

```

**Traer un id que no existe** (devuelve 404)
```bash

curl -i http://localhost:8082/cuentas/999

```

**Filtrar por número de cuenta**
```bash

curl -i "http://localhost:8082/cuentas?numeroCuenta=123456789012"

```

**Listar las cuentas del cliente 1** (devuelve 200)
```bash

curl -i http://localhost:8082/cuentas/clientes/1

```

**Filtrar las cuentas del cliente 1 por número de cuenta** (devuelve 200)
```bash

curl -i "http://localhost:8082/cuentas/clientes/1?numeroCuenta=2028"

```

**Traer la cuenta 1 del cliente 1** (devuelve 200)
```bash

curl -i http://localhost:8082/cuentas/clientes/1/1

```

**Crear una Caja de Ahorro válida** (devuelve 201 Created)
```bash

curl -i -X POST http://localhost:8082/cuentas -H "Content-Type: application/json" -d '{"tipo": "Caja de Ahorro","moneda": "USD","clienteId": 1,"saldoInicial": 0.00,"porcentajeTasaInteresAnual": 16.00}'

```

**Intentar crear una Caja de Ahorro inválida (con límite al descubierto)** (devuelve 400 Bad Request)
```bash

curl -i -X POST http://localhost:8082/cuentas -H "Content-Type: application/json" -d '{"tipo": "Caja de Ahorro","moneda": "USD","clienteId": 1,"saldoInicial": 0.00,"limiteDescubierto": 1000000.00}'

```

**Crear una Cuenta Corriente válida** (devuelve 201 Created)
```bash

curl -i -X POST http://localhost:8082/cuentas -H "Content-Type: application/json" -d '{"tipo": "Cuenta Corriente","moneda": "USD","clienteId": 1,"saldoInicial": 10.00,"limiteDescubierto": 10000.00}'

```

**Actualizar porcentaje de tasa anual de la cuenta 1 del cliente 1** (devuelve 200)
```bash

curl -i -X PUT http://localhost:8082/cuentas/clientes/1/1 -H "Content-Type: application/json" -d '{"porcentajeTasaInteresAnual": 10.00}'

```

**Acreditar 15000 a la cuenta 1** (devuelve 200)
```bash

curl -i -X POST "http://localhost:8082/cuentas/1/acreditaciones?saldo=15000"

```

**Retirar 5000 de la cuenta 1** (devuelve 200)
```bash

curl -i -X POST "http://localhost:8082/cuentas/1/retiros?saldo=5000"

```

**Eliminar la cuenta 12 lógicamente** (devuelve 204 No Content)
```bash

curl -i -X DELETE "http://localhost:8082/cuentas/12?modo=logico"

```

**Intentar borrar la ruta raíz de cuentas** (devuelve 405 Method Not Allowed)
```bash

curl -i -X DELETE http://localhost:8082/cuentas

```

> **Qué significa el `-i`:** muestra también los encabezados de la respuesta, incluido el
> código de estado (`HTTP/1.1 200`, `201`, `400`, etc.). Sin `-i` verías solo el JSON del cuerpo.

---

## 5. Estructura del proyecto

```
product-service/
├── pom.xml
├── README.md
├── src/main/resources/application.yml    # Configuración de Base de Datos MySql
├── src/test/*                            # Contiene tests de lógica de negocio de componentes y entidades
└── src/main/java/ar/edu/utn/diplomatura/productservice/
    ├── ProductServiceApplication.java   # Ejecuta la carga y la impresión de los datos de las cuentas
    ├── Constants.java           # Definición de constantes que se utilizan en el sistema.
    ├── componentes/             # Métodos de validación
    ├── controller/              # Controller de SpringBoot para gestionar API de Cuentas
    ├── dto/                     # dtos para request y response de solicitudes de API de Cuentas
    ├── exception/               # Cada definición de las excepciones del sistema
    ├── mapper/                  # Conversión entre entidades Cuenta y sus DTOs
    ├── repository/              # Contiene repositorio para Cuentas
    ├── service/                 # Service de SpringBoot para cuentas, que funciona con el Controller para cada endpoint.
    └── entity/
        └── Cuenta.java          # clase abstracta, atributos, constructores, getters/setters
        ├── CajaAhorro.java      # subclase, atributos, constructores, getters/setters, métodos especificos
        ├── CuentaCorriente.java # subclase, atributos, constructores, getters/setters, métodos especificos
        └── Operable.java        # interfaz con definición de métodos especificos

```

---

## 6. Arquitectura

```
customer-service (:8081 /clientes) ──Feign──> product-service (:8082 /cuentas)
dueño de los clientes                          dueño de las cuentas
```

- **product-service** crea y administra las cuentas (Caja de Ahorro y Cuenta Corriente), con su saldo, moneda y fecha de apertura.
- Cada cuenta guarda únicamente el `clienteId`: **no conoce los datos del cliente**. La validación de que el cliente exista la hace **customer-service** antes de delegar la operación.
- El cliente Feign de customer-service (`CuentasClient`) consume los endpoints `/cuentas` descriptos arriba.

---

## 7. Problemas frecuentes

**Los acentos se ven como `?` o `�` en la consola.** Pasa en `cmd` y en PowerShell, porque la consola de Windows usa un codepage viejo en lugar de UTF-8. Es un problema de la terminal, no del código. Corré `chcp 65001` antes de ejecutar, o usá directamente la consola de IntelliJ, que muestra bien los acentos.

**`error: invalid source release: 21`.** El JDK configurado es anterior a 21. Revisá el punto 2.

**El botón ▶ no aparece al lado del `main`.** IntelliJ todavía no terminó de indexar, o no reconoció la carpeta como proyecto Maven. Esperá a que termine la barra de progreso de abajo, o recargá el proyecto desde el panel de Maven.

**`Port 8082 was already in use`.** Hay otra aplicación usando ese puerto. Cerrala o cambiá `server.port` en `application.yml` (y la URL del cliente Feign en customer-service).

**`404 Not Found` con `"path": "/"` al crear una cuenta desde customer-service.** La llamada llegó a la raíz de este servicio. Revisá que el método `crear` de `CuentasClient` tenga `@PostMapping("/cuentas")`.

**`tipoCuenta` o `habilitada` llegan en `null` a través de customer-service, pero bien al llamar directo.** Los nombres de campos de `CuentaResponse` en customer-service no coinciden con el JSON que devuelve este servicio. Compará ambos.

**`Access denied for user 'root'`.** Falta la password de MySQL en `application.yml` (`datasource.password`). Revisá el punto 2.