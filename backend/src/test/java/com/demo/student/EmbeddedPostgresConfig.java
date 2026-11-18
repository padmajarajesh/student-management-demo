package com.demo.student;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * Starts a real PostgreSQL server for the tests, so Flyway migrations and all
 * SQL run exactly as they would against a local database or Neon.
 */
@TestConfiguration(proxyBeanMethods = false)
public class EmbeddedPostgresConfig {

    @Bean(destroyMethod = "close")
    public EmbeddedPostgres embeddedPostgres() throws IOException {
        return EmbeddedPostgres.start();
    }

    @Bean
    @Primary
    public DataSource dataSource(EmbeddedPostgres postgres) {
        return postgres.getPostgresDatabase();
    }
}
