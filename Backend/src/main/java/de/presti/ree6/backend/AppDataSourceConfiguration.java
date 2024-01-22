package de.presti.ree6.backend;

import com.zaxxer.hikari.HikariDataSource;
import de.presti.ree6.sql.SQLSession;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AppDataSourceConfiguration {

    @Bean
    @ConfigurationProperties("app.datasource")
    public HikariDataSource dataSource() {
        return SQLSession.getSqlConnector().getDataSource();
    }
}