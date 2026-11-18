package com.demo.student;

import org.springframework.boot.SpringApplication;

/**
 * Runs the full backend against a temporary embedded PostgreSQL server, so you
 * can try the app without installing PostgreSQL:
 *
 * <pre>./mvnw spring-boot:test-run</pre>
 *
 * Data is discarded when the app stops. Use a real database for normal work.
 */
public class TestStudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.from(StudentManagementApplication::main)
                .with(EmbeddedPostgresConfig.class)
                .run(args);
    }
}
