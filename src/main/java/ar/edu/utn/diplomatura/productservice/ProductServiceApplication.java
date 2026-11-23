package ar.edu.utn.diplomatura.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Punto de entrada. Ejecutar el main de esta clase levanta la aplicacion.
 *
 * @SpringBootApplication hace tres cosas:
 *   - marca esta clase como configuracion,
 *   - configura la aplicación sola según las librerias del pom.xml
 *     (por eso arranca Tomcat en el 8080 y se conecta a H2),
 *   - recorre este paquete y los de abajo buscando clases anotadas
 *     (@Component, @Service, @Repository, @RestController) para crearlas.
 *
 * Por eso toda clase que Spring deba gestionar tiene que estar dentro de
 * ar.edu.utn.diplomatura.cuentasusuarios o en un subpaquete.
 */
@SpringBootApplication
//@EnableFeignClients
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);

        System.out.println();
        System.out.println("===========================================================");
        System.out.println("  Microservicio A - Product Service");
        System.out.println("  App:         http://localhost:8082/cuentas");
        System.out.println("               JDBC URL: jdbc:mysql://localhost:3306/product_service   user: root");
        System.out.println("===========================================================");
        System.out.println();

    }
}
