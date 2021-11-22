package de.presti.ree6.webinterface;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebinterfaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebinterfaceApplication.class, args);
        new Server();
    }

}
