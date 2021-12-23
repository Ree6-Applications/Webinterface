package de.presti.ree6.webinterface;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebinterfaceApplicationTests {

    @Test
    void contextLoads() {
        Server.getInstance().logger.warn("Please do not use Test mode!");
    }

}
