package de.presti.ree6.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Base class used by SpringBoot to boot up the Application.
 */
@EnableJpaRepositories("de.presti.ree6.*")
@EntityScan("de.presti.ree6.*")
@SpringBootApplication
public class WebinterfaceApplication {

    /**
     * Main method called to start the Application.
     * @param args {@link String[]} used as List of the Arguments given at the start of the Application.
     */
    public static void main(String[] args) {

        // Initialize the Server class
        new Server(args);

        // Initialize SpringApplication.
        SpringApplication application = new SpringApplication(WebinterfaceApplication.class);

        // Run the Application.
        application.run(args);
    }

}
