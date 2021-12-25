package de.presti.ree6.webinterface;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Base class used by SpringBoot to boot up the Application.
 */
@SpringBootApplication
public class WebinterfaceApplication {

    /**
     * Main method called to start the Application.
     * @param args {@link String[]} used as List of the Arguments given at the start of the Application.
     */
    public static void main(String[] args) {
        // Runt the Application.
        SpringApplication.run(WebinterfaceApplication.class, args);

        // Initialize the Server class
        new Server();
    }

}
